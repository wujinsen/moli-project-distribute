#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""知识库问答评测：golden.jsonl 逐题调 /kb/ask，输出 hit@k / MRR / coverage。

用法见 kb/eval/README.md。前置：网关 + KnowledgeServer 已启动，wiki 已 Sync。

  python kb/tools/eval_ask.py                    # 检索式，汇总 hit@3/5/8
  python kb/tools/eval_ask.py --use-llm          # 生成式 + 关键词检查
  python kb/tools/eval_ask.py --only M03 --min-hit 0.8 --gate-at-k 3
"""
from __future__ import annotations

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path

KB_DIR = Path(__file__).resolve().parent.parent
DEFAULT_GOLDEN = KB_DIR / "eval" / "golden.jsonl"
REPORT_DIR = KB_DIR / "eval" / "reports"
# 单次请求 top_k≥max(STANDARD_HIT_AT) 时，由 first_rank 派生多档 hit@k，无需重复调 API
STANDARD_HIT_AT = (3, 5, 8)

# space_code -> spaceId 兜底映射（/kb/space/mine 不可用时使用）
FALLBACK_SPACE_IDS = {
    "enterprise-kb": 900000000000000001,
    "jp-fe-ap-exam": 900000000000000002,
    "moli-ops-manual": 900000000000000003,
}


DEFAULT_LOGIN_BASE = os.environ.get("MOLI_LOGIN_BASE", "http://127.0.0.1:8888")
DEFAULT_KB_BASES = [
    os.environ.get("MOLI_KB_BASE", "").strip(),
    "http://127.0.0.1:21000/KnowledgeServer",
    "http://127.0.0.1:8090",
]
PROBE_PATH = "/kb/ask/llm-config"


def http_json(url: str, *, method: str = "GET", token: str = "",
              payload: dict | None = None, timeout: int = 60) -> dict:
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = token
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode())


def http_probe(url: str, *, token: str = "", timeout: int = 5) -> tuple[bool, str]:
    """探活：HTTP 200/401/403 视为可达；404/5xx 视为不可达并返回原因。"""
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = token
    req = urllib.request.Request(url, method="GET", headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return True, f"HTTP {resp.status}"
    except urllib.error.HTTPError as e:
        if e.code in (401, 403):
            return True, f"HTTP {e.code}"
        body = e.read().decode(errors="replace")[:200]
        return False, f"HTTP {e.code}: {body}"
    except urllib.error.URLError as e:
        return False, f"连接失败: {e.reason}"
    except Exception as e:  # noqa: BLE001
        return False, str(e)


def login(login_base: str, username: str, password: str) -> str:
    """登录拿 token。默认走 user-center 直连 8888/login。"""
    bases = []
    for b in (login_base, DEFAULT_LOGIN_BASE, "http://127.0.0.1:21000/UserCenter"):
        b = (b or "").rstrip("/")
        if b and b not in bases:
            bases.append(b)
    payload = {"userName": username, "username": username, "password": password}
    last_err = ""
    for base in bases:
        url = f"{base}/login"
        try:
            body = http_json(url, method="POST", payload=payload, timeout=10)
        except urllib.error.URLError as e:
            last_err = f"{url} 连接失败: {e.reason}"
            continue
        except urllib.error.HTTPError as e:
            last_err = f"{url} HTTP {e.code}"
            continue
        data = body.get("data")
        token = (data.get("token") or data.get("accessToken")) if isinstance(data, dict) else data
        token = token or body.get("token")
        if token:
            print(f"login OK · {url}")
            return str(token)
        last_err = f"{url} 登录失败: {body}"
    raise RuntimeError(last_err or "login failed")


def resolve_kb_base(kb_base_arg: str) -> tuple[str, list[str]]:
    """按优先级选 KnowledgeServer 基址。"""
    tried: list[str] = []
    candidates: list[str] = []
    if kb_base_arg:
        candidates.append(kb_base_arg.rstrip("/"))
    for b in DEFAULT_KB_BASES:
        b = (b or "").rstrip("/")
        if b and b not in candidates:
            candidates.append(b)
    errors: list[str] = []
    for base in candidates:
        tried.append(base)
        ok, detail = http_probe(f"{base}{PROBE_PATH}", timeout=5)
        if ok:
            if detail != "HTTP 200":
                print(f"[warn] {base} 探活 {detail}，视为可达")
            return base, tried
        errors.append(f"{base} -> {detail}")
    msg = "KnowledgeServer 均不可达。已尝试：\n  " + "\n  ".join(errors)
    msg += "\n提示：8888 是 user-center，不是网关；KnowledgeServer 走 21000/KnowledgeServer 或直连 8090。"
    raise RuntimeError(msg)


def auth_smoke_test(kb_base: str, token: str) -> None:
    """登录后轻量冒烟，提前暴露带 token 的 500 类运行时错误。"""
    payload = {"question": "ping", "spaceId": FALLBACK_SPACE_IDS["moli-ops-manual"],
               "topK": 1, "useLlm": False}
    try:
        http_json(f"{kb_base}/kb/ask", method="POST", token=token, payload=payload, timeout=30)
    except urllib.error.HTTPError as e:
        body = e.read().decode(errors="replace")[:300]
        raise RuntimeError(
            f"KnowledgeServer 已探活，但带登录态调用 /kb/ask 失败: HTTP {e.code}\n"
            f"  {body}\n"
            "常见原因：user-center / Dubbo / Redis Session 未就绪，或 knowledge-server 需重启。\n"
            "建议：先确认 user-center-server(8888) 正常，再重启 moli-knowledge-server(8090)。"
        ) from e


def resolve_spaces(kb_base: str, token: str) -> dict:
    """space_code -> spaceId。优先 /kb/space/mine，失败用兜底映射。"""
    try:
        body = http_json(f"{kb_base}/kb/space/mine", token=token, timeout=15)
        rows = body.get("data") or []
        mapping = {r["spaceCode"]: int(r["id"]) for r in rows if r.get("spaceCode")}
        if mapping:
            return mapping
    except Exception as e:  # noqa: BLE001
        print(f"[warn] /kb/space/mine 不可用（{e}），使用内置 space 映射")
    return dict(FALLBACK_SPACE_IDS)


def load_golden(path: Path) -> list[dict]:
    entries = []
    for ln, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        try:
            row = json.loads(line)
        except json.JSONDecodeError as e:
            raise SystemExit(f"golden.jsonl 第 {ln} 行 JSON 解析失败: {e}")
        for field in ("id", "space", "question", "expect_slugs"):
            if not row.get(field):
                raise SystemExit(f"golden.jsonl 第 {ln} 行缺字段 {field}")
        entries.append(row)
    ids = [r["id"] for r in entries]
    dup = {i for i in ids if ids.count(i) > 1}
    if dup:
        raise SystemExit(f"golden.jsonl id 重复: {sorted(dup)}")
    return entries


def norm_slug(s: str) -> str:
    return (s or "").strip().lstrip("/").lower()


def hit_at_k(first_rank: int, k: int) -> bool:
    """first_rank 为 1-based；0 表示未命中。"""
    return 0 < first_rank <= k


def compute_hit_at_rates(scored: list[dict], ks: list[int]) -> dict[str, float]:
    if not scored:
        return {str(k): 0.0 for k in ks}
    n = len(scored)
    out: dict[str, float] = {}
    for k in ks:
        hits = sum(1 for r in scored if hit_at_k(r.get("first_rank") or 0, k))
        out[str(k)] = round(hits / n, 4)
    return out


def evaluate_one(entry: dict, kb_base: str, token: str, space_id: int,
                 top_k: int, use_llm: bool, llm_context_top_k: int | None) -> dict:
    payload: dict = {"question": entry["question"], "spaceId": space_id,
                     "topK": top_k, "useLlm": use_llm}
    if llm_context_top_k is not None:
        payload["llmContextTopK"] = llm_context_top_k
    t0 = time.time()
    try:
        body = http_json(f"{kb_base}/kb/ask", method="POST",
                         token=token, payload=payload, timeout=120)
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as e:
        return {"id": entry["id"], "error": str(e)}
    elapsed_ms = int((time.time() - t0) * 1000)

    data = body.get("data") or {}
    citations = data.get("citations") or []
    cited = [norm_slug(c.get("slug", "")) for c in citations]
    expect = [norm_slug(s) for s in entry["expect_slugs"]]

    first_rank = 0  # 1-based；0=未命中
    for rank, slug in enumerate(cited, 1):
        if slug in expect:
            first_rank = rank
            break
    hit = first_rank > 0
    covered = sum(1 for s in expect if s in cited)
    coverage = covered / len(expect)

    kw_pass = None
    if use_llm and entry.get("expect_keywords"):
        answer = (data.get("answer") or "").lower()
        missing = [k for k in entry["expect_keywords"] if k.lower() not in answer]
        kw_pass = not missing

    return {
        "id": entry["id"],
        "space": entry["space"],
        "question": entry["question"],
        "hit": hit,
        "first_rank": first_rank,
        "coverage": round(coverage, 3),
        "cited": cited,
        "expect": expect,
        "mode": data.get("mode"),
        "scope": data.get("scope"),
        "kw_pass": kw_pass,
        "elapsed_ms": elapsed_ms,
    }


def main() -> int:
    ap = argparse.ArgumentParser(description="KB /kb/ask 评测（golden set 回归）")
    ap.add_argument("--login-base", default=os.environ.get("MOLI_LOGIN_BASE", DEFAULT_LOGIN_BASE),
                    help="登录基址（默认 user-center 直连 http://127.0.0.1:8888）")
    ap.add_argument("--gateway", default=os.environ.get("MOLI_GATEWAY", ""),
                    help="已废弃，等同 --login-base；保留兼容")
    ap.add_argument("--username", default=os.environ.get("MOLI_EVAL_USER", "admin"))
    ap.add_argument("--password", default=os.environ.get("MOLI_EVAL_PASS", "123456"))
    ap.add_argument("--kb-base", default=os.environ.get("MOLI_KB_BASE", ""),
                    help="KnowledgeServer 基址；默认自动尝试 21000/KnowledgeServer → 8090 直连")
    ap.add_argument("--golden", default=str(DEFAULT_GOLDEN))
    ap.add_argument("--top-k", type=int, default=8,
                    help="citations 召回上限（默认 8；需 ≥ gate-at-k 才能派生 hit@k）")
    ap.add_argument("--llm-context-top-k", type=int, default=None,
                    help="LLM 上下文页数上限；省略则走后端 kb.ask.llm-context-top-k（默认 3）")
    ap.add_argument("--use-llm", action="store_true", help="生成式作答并检查 expect_keywords")
    ap.add_argument("--only", help="只跑指定题 id（逗号分隔）")
    ap.add_argument("--space", help="只跑指定 space_code 的题")
    ap.add_argument("--min-hit", type=float, default=0.0,
                    help="hit@k 低于该值时退出码 1（CI 门禁，默认只报告）")
    ap.add_argument("--gate-at-k", type=int, default=0,
                    help="门禁使用的 k（0=等同 --top-k；常用 3 表示 hit@3 门禁）")
    args = ap.parse_args()

    gate_k = args.gate_at_k if args.gate_at_k > 0 else args.top_k
    if gate_k > args.top_k:
        print(f"[error] --gate-at-k {gate_k} 不能大于 --top-k {args.top_k}")
        return 1

    entries = load_golden(Path(args.golden))
    if args.only:
        wanted = {s.strip() for s in args.only.split(",")}
        entries = [e for e in entries if e["id"] in wanted]
    if args.space:
        entries = [e for e in entries if e["space"] == args.space]
    if not entries:
        print("没有匹配的题目")
        return 1

    login_base = args.login_base or args.gateway or DEFAULT_LOGIN_BASE
    try:
        token = login(login_base, args.username, args.password)
    except urllib.error.URLError as e:
        print(f"[error] 连不上登录服务 {login_base}（{e.reason}）。请先启动 user-center-server(8888)。")
        return 2
    except RuntimeError as e:
        print(f"[error] {e}")
        print("        用 --username/--password 或环境变量 MOLI_EVAL_USER / MOLI_EVAL_PASS 指定账号。")
        return 2
    try:
        kb_base, _ = resolve_kb_base(args.kb_base)
    except RuntimeError as e:
        print(f"[error] {e}")
        return 2
    try:
        auth_smoke_test(kb_base, token)
    except RuntimeError as e:
        print(f"[error] {e}")
        return 2

    llm_ctx = args.llm_context_top_k
    ctx_note = f" llmCtx={llm_ctx}" if llm_ctx is not None else ""
    print(f"kb_base · {kb_base} · {len(entries)} 题 · topK={args.top_k}{ctx_note}"
          f" · {'生成式' if args.use_llm else '检索式'}\n")
    space_map = resolve_spaces(kb_base, token)

    results = []
    for entry in entries:
        space_id = space_map.get(entry["space"])
        if not space_id:
            results.append({"id": entry["id"], "error": f"未知 space_code: {entry['space']}"})
            continue
        r = evaluate_one(entry, kb_base, token, space_id, args.top_k, args.use_llm, llm_ctx)
        results.append(r)
        if r.get("error"):
            print(f"  {r['id']}  ERROR  {r['error']}")
        else:
            mark = "PASS" if r["hit"] else "MISS"
            kw = "" if r["kw_pass"] is None else ("  kw=" + ("OK" if r["kw_pass"] else "FAIL"))
            print(f"  {r['id']}  {mark}  rank={r['first_rank'] or '-'}"
                  f"  cov={r['coverage']}{kw}  {r['elapsed_ms']}ms  {entry['question']}")

    scored = [r for r in results if not r.get("error")]
    errors = len(results) - len(scored)
    if not scored:
        print("\n全部请求失败，请检查服务是否启动")
        return 1
    hit_rate = sum(1 for r in scored if r["hit"]) / len(scored)
    mrr = sum((1 / r["first_rank"]) for r in scored if r["first_rank"]) / len(scored)
    coverage = sum(r["coverage"] for r in scored) / len(scored)
    kw_scored = [r for r in scored if r["kw_pass"] is not None]
    kw_rate = (sum(1 for r in kw_scored if r["kw_pass"]) / len(kw_scored)) if kw_scored else None

    report_ks = sorted({k for k in STANDARD_HIT_AT if k <= args.top_k} | {args.top_k, gate_k})
    hit_at = compute_hit_at_rates(scored, report_ks)
    gate_hit = hit_at.get(str(gate_k), hit_rate)

    hit_parts = "  ".join(f"hit@{k}={hit_at[str(k)]:.2%}" for k in report_ks)
    print(f"\n== 汇总 ==  {hit_parts}  mrr={mrr:.3f}"
          f"  coverage={coverage:.2%}"
          + (f"  kw_pass={kw_rate:.2%}" if kw_rate is not None else "")
          + (f"  errors={errors}" if errors else ""))

    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    report = {
        "time": datetime.now().isoformat(timespec="seconds"),
        "login_base": login_base,
        "kb_base": kb_base,
        "top_k": args.top_k,
        "llm_context_top_k": llm_ctx,
        "gate_at_k": gate_k,
        "use_llm": args.use_llm,
        "total": len(results),
        "errors": errors,
        "hit_rate": round(hit_rate, 4),
        "hit_at": hit_at,
        "mrr": round(mrr, 4),
        "coverage": round(coverage, 4),
        "kw_pass_rate": None if kw_rate is None else round(kw_rate, 4),
        "results": results,
    }
    out = REPORT_DIR / f"report-{datetime.now().strftime('%Y%m%d-%H%M%S')}.json"
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"报告: {out.relative_to(KB_DIR)}")

    if args.min_hit and gate_hit < args.min_hit:
        print(f"hit@{gate_k} {gate_hit:.2%} < 门禁 {args.min_hit:.2%}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
