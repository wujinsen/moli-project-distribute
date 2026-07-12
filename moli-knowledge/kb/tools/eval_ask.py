#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""知识库问答评测：golden.jsonl 逐题调 /kb/ask，输出 hit@k / MRR / coverage。

用法见 kb/eval/README.md。前置：网关 + KnowledgeServer 已启动，wiki 已 Sync。

  python kb/tools/eval_ask.py                    # 检索式
  python kb/tools/eval_ask.py --use-llm          # 生成式 + 关键词检查
  python kb/tools/eval_ask.py --only M03 --min-hit 0.8
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

# space_code -> spaceId 兜底映射（/kb/space/mine 不可用时使用）
FALLBACK_SPACE_IDS = {
    "enterprise-kb": 900000000000000001,
    "jp-fe-ap-exam": 900000000000000002,
    "moli-ops-manual": 900000000000000003,
}


def http_json(url: str, *, method: str = "GET", token: str = "",
              payload: dict | None = None, timeout: int = 60) -> dict:
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = token
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode())


def login(gateway: str, username: str, password: str) -> str:
    # LoginController 绑定 SysUser.userName（驼峰）；兼容旧网关同时带 username
    body = http_json(f"{gateway}/login", method="POST",
                     payload={"userName": username, "username": username,
                              "password": password}, timeout=10)
    data = body.get("data")
    token = (data.get("token") or data.get("accessToken")) if isinstance(data, dict) else data
    token = token or body.get("token")
    if not token:
        raise RuntimeError(f"login failed: {body}")
    return str(token)


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


def evaluate_one(entry: dict, kb_base: str, token: str, space_id: int,
                 top_k: int, use_llm: bool) -> dict:
    payload = {"question": entry["question"], "spaceId": space_id,
               "topK": top_k, "useLlm": use_llm}
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
    ap.add_argument("--gateway", default=os.environ.get("MOLI_GATEWAY", "http://127.0.0.1:8888"))
    ap.add_argument("--username", default=os.environ.get("MOLI_EVAL_USER", "admin"))
    ap.add_argument("--password", default=os.environ.get("MOLI_EVAL_PASS", "123456"))
    ap.add_argument("--kb-base", default=os.environ.get("MOLI_KB_BASE", ""),
                    help="KnowledgeServer 基址；默认 <gateway>/KnowledgeServer，"
                         "网关路由不通时自动回退 http://127.0.0.1:21000/KnowledgeServer")
    ap.add_argument("--golden", default=str(DEFAULT_GOLDEN))
    ap.add_argument("--top-k", type=int, default=8)
    ap.add_argument("--use-llm", action="store_true", help="生成式作答并检查 expect_keywords")
    ap.add_argument("--only", help="只跑指定题 id（逗号分隔）")
    ap.add_argument("--space", help="只跑指定 space_code 的题")
    ap.add_argument("--min-hit", type=float, default=0.0,
                    help="hit@k 低于该值时退出码 1（CI 门禁，默认只报告）")
    args = ap.parse_args()

    entries = load_golden(Path(args.golden))
    if args.only:
        wanted = {s.strip() for s in args.only.split(",")}
        entries = [e for e in entries if e["id"] in wanted]
    if args.space:
        entries = [e for e in entries if e["space"] == args.space]
    if not entries:
        print("没有匹配的题目")
        return 1

    try:
        token = login(args.gateway, args.username, args.password)
    except urllib.error.URLError as e:
        print(f"[error] 连不上网关 {args.gateway}（{e.reason}）。请先启动网关与 KnowledgeServer。")
        return 2
    except RuntimeError as e:
        print(f"[error] {e}")
        print("        用 --username/--password 或环境变量 MOLI_EVAL_USER / MOLI_EVAL_PASS 指定账号。")
        return 2
    kb_base = args.kb_base or f"{args.gateway}/KnowledgeServer"
    try:  # 轻探活；网关路由不通时回退直连
        http_json(f"{kb_base}/kb/meta/kb-types", token=token, timeout=5)
    except Exception:  # noqa: BLE001
        fallback = "http://127.0.0.1:21000/KnowledgeServer"
        if kb_base != fallback:
            try:
                http_json(f"{fallback}/kb/meta/kb-types", token=token, timeout=5)
                print(f"[warn] {kb_base} 不通，回退直连 {fallback}")
                kb_base = fallback
            except Exception:  # noqa: BLE001
                print(f"[error] {kb_base} 与 {fallback} 均不可达，请检查 KnowledgeServer")
                return 2

    print(f"login OK · {kb_base} · {len(entries)} 题 · topK={args.top_k}"
          f" · {'生成式' if args.use_llm else '检索式'}\n")
    space_map = resolve_spaces(kb_base, token)

    results = []
    for entry in entries:
        space_id = space_map.get(entry["space"])
        if not space_id:
            results.append({"id": entry["id"], "error": f"未知 space_code: {entry['space']}"})
            continue
        r = evaluate_one(entry, kb_base, token, space_id, args.top_k, args.use_llm)
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

    print(f"\n== 汇总 ==  hit@{args.top_k}={hit_rate:.2%}  mrr={mrr:.3f}"
          f"  coverage={coverage:.2%}"
          + (f"  kw_pass={kw_rate:.2%}" if kw_rate is not None else "")
          + (f"  errors={errors}" if errors else ""))

    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    report = {
        "time": datetime.now().isoformat(timespec="seconds"),
        "gateway": args.gateway,
        "top_k": args.top_k,
        "use_llm": args.use_llm,
        "total": len(results),
        "errors": errors,
        "hit_rate": round(hit_rate, 4),
        "mrr": round(mrr, 4),
        "coverage": round(coverage, 4),
        "kw_pass_rate": None if kw_rate is None else round(kw_rate, 4),
        "results": results,
    }
    out = REPORT_DIR / f"report-{datetime.now().strftime('%Y%m%d-%H%M%S')}.json"
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"报告: {out.relative_to(KB_DIR)}")

    if args.min_hit and hit_rate < args.min_hit:
        print(f"hit@k {hit_rate:.2%} < 门禁 {args.min_hit:.2%}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
