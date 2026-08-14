#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""知识库问答评测：golden.jsonl 逐题调 /kb/ask，输出 hit@k / MRR / coverage。

用法见 kb/eval/README.md。前置：网关 + KnowledgeServer 已启动，wiki 已 Sync。

  python kb/tools/eval_ask.py                    # 检索式，汇总 hit@3/5/8
  python kb/tools/eval_ask.py --use-llm          # 生成式 + 关键词检查
  python kb/tools/eval_ask.py --only M03 --min-hit 0.8 --gate-at-k 3
  python kb/tools/eval_ask.py --baseline         # 基线报告 baseline-ngram-*.json
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
DEFAULT_INJECT_GOLDEN = KB_DIR / "eval" / "guardrails_inject.jsonl"
REPORT_DIR = KB_DIR / "eval" / "reports"
# 单次请求 top_k≥max(STANDARD_HIT_AT) 时，由 first_rank 派生多档 hit@k，无需重复调 API
STANDARD_HIT_AT = (1, 3, 5, 8)

VALID_STRATEGIES = frozenset({"ngram", "hybrid", "hybrid-rerank"})
VALID_GRAPH = frozenset({"on", "off"})
VALID_DIFFICULTIES = frozenset({"easy", "paraphrase", "dirty", "multi-hop", "negative"})

ID_PREFIX_BY_SPACE = {
    "moli-ops-manual": "M",
    "enterprise-kb": "E",
    "jp-fe-ap-exam": "J",
}

# 拒答短语集合（契约 §3.3；大小写/全半角归一后子串匹配）
REFUSAL_MARKERS = (
    "暂无相关",
    "暂无",
    "无相关内容",
    "没有找到",
    "未找到",
    "知识库暂无",
    "无法回答",
    "抱歉，没有",
    "not found",
    "no relevant",
)

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


def _normalize_refusal_text(text: str) -> str:
    """大小写/全半角归一，便于拒答短语匹配。"""
    s = (text or "").lower()
    s = s.replace("　", " ")
    return s


def has_refusal_marker(answer: str) -> bool:
    norm = _normalize_refusal_text(answer)
    return any(_normalize_refusal_text(m) in norm for m in REFUSAL_MARKERS)


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
        for field in ("id", "space", "question", "difficulty", "expect_answerable"):
            if field not in row:
                raise SystemExit(f"golden.jsonl 第 {ln} 行缺字段 {field}")

        difficulty = row["difficulty"]
        if difficulty not in VALID_DIFFICULTIES:
            raise SystemExit(
                f"golden.jsonl 第 {ln} 行 difficulty 非法: {difficulty!r}，"
                f"允许 {sorted(VALID_DIFFICULTIES)}"
            )

        expect_answerable = row["expect_answerable"]
        if not isinstance(expect_answerable, bool):
            raise SystemExit(f"golden.jsonl 第 {ln} 行 expect_answerable 必须为 boolean")

        expect_slugs = row.get("expect_slugs")
        if expect_slugs is None:
            expect_slugs = []
        if not isinstance(expect_slugs, list):
            raise SystemExit(f"golden.jsonl 第 {ln} 行 expect_slugs 必须为数组")

        expect_keywords = row.get("expect_keywords")
        if expect_keywords is not None and not isinstance(expect_keywords, list):
            raise SystemExit(f"golden.jsonl 第 {ln} 行 expect_keywords 必须为数组")

        expect_all = row.get("expect_all", False)
        if expect_all and difficulty != "multi-hop":
            raise SystemExit(f"golden.jsonl 第 {ln} 行 expect_all=true 仅允许 multi-hop")

        if difficulty == "negative":
            if expect_answerable is not False:
                raise SystemExit(f"golden.jsonl 第 {ln} 行 negative 题 expect_answerable 必须为 false")
            if expect_slugs:
                raise SystemExit(f"golden.jsonl 第 {ln} 行 negative 题 expect_slugs 必须为空")
            if expect_keywords:
                raise SystemExit(f"golden.jsonl 第 {ln} 行 negative 题禁止 expect_keywords")
        else:
            if expect_answerable is not True:
                raise SystemExit(
                    f"golden.jsonl 第 {ln} 行 difficulty={difficulty} 时 expect_answerable 必须为 true"
                )
            if not expect_slugs:
                raise SystemExit(f"golden.jsonl 第 {ln} 行 expect_answerable=true 时 expect_slugs 非空必填")

        expected_prefix = ID_PREFIX_BY_SPACE.get(row["space"])
        if expected_prefix and not str(row["id"]).startswith(expected_prefix):
            raise SystemExit(
                f"golden.jsonl 第 {ln} 行 id 前缀与 space 不一致: "
                f"id={row['id']!r} space={row['space']!r} 期望前缀 {expected_prefix}"
            )

        row["expect_slugs"] = expect_slugs
        entries.append(row)

    ids = [r["id"] for r in entries]
    dup = {i for i in ids if ids.count(i) > 1}
    if dup:
        raise SystemExit(f"golden.jsonl id 重复: {sorted(dup)}")
    return entries


def load_inject_golden(path: Path) -> list[dict]:
    entries = []
    for ln, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        row = json.loads(line)
        if "question" not in row and "text" in row:
            row["question"] = row["text"]
        if "question" not in row or "expect" not in row:
            raise SystemExit(f"guardrails_inject.jsonl 第 {ln} 行缺 question/text/expect")
        if row["expect"] not in ("BLOCK", "PASS", "FLAG"):
            raise SystemExit(f"guardrails_inject.jsonl 第 {ln} 行 expect 非法: {row['expect']!r}")
        entries.append(row)
    return entries


def evaluate_inject_one(entry: dict, kb_base: str, token: str, space_id: int,
                        top_k: int) -> dict:
    payload = {"question": entry["question"], "spaceId": space_id, "topK": top_k, "useLlm": True}
    t0 = time.time()
    try:
        body = http_json(f"{kb_base}/kb/ask", method="POST", token=token, payload=payload, timeout=120)
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as e:
        return {"id": entry.get("id"), "expect": entry.get("expect"), "error": str(e)}
    elapsed_ms = int((time.time() - t0) * 1000)
    data = body.get("data") or {}
    guard = data.get("guard") or {}
    return {
        "id": entry.get("id"),
        "expect": entry.get("expect"),
        "blocked": guard.get("blocked") is True,
        "flagged": guard.get("flagged") is True,
        "mode": data.get("mode"),
        "elapsed_ms": elapsed_ms,
    }


def summarize_inject_results(results: list[dict]) -> dict:
    ok = [r for r in results if not r.get("error")]
    block_rows = [r for r in ok if r.get("expect") == "BLOCK"]
    pass_rows = [r for r in ok if r.get("expect") == "PASS"]
    block_hits = sum(1 for r in block_rows if r.get("blocked"))
    false_blocks = sum(1 for r in pass_rows if r.get("blocked"))
    return {
        "total": len(ok),
        "block_accuracy": round(block_hits / len(block_rows), 4) if block_rows else None,
        "false_block_rate": round(false_blocks / len(pass_rows), 4) if pass_rows else None,
        "block_hits": block_hits,
        "block_total": len(block_rows),
        "false_blocks": false_blocks,
        "pass_total": len(pass_rows),
    }


def build_guard_metrics(scored: list[dict]) -> dict:
    generative = [
        r for r in scored
        if r.get("mode") == "generative" and r.get("guard_coverage") is not None
    ]
    covs = [float(r["guard_coverage"]) for r in generative]
    low_rows = [r for r in generative if r.get("guard_grounding_low")]
    samples: list[dict] = []
    for r in generative:
        unsupported = r.get("guard_unsupported") or []
        for stmt in unsupported[:2]:
            samples.append({
                "id": r.get("id"),
                "question": r.get("question"),
                "unsupported": stmt,
                "guard_coverage": r.get("guard_coverage"),
            })
            if len(samples) >= 10:
                break
        if len(samples) >= 10:
            break
    cov_mean = round(sum(covs) / len(covs), 4) if covs else None
    return {
        "generative_with_guard": len(generative),
        "grounding_coverage_mean": cov_mean,
        "hallucination_proxy_mean": round(1.0 - cov_mean, 4) if cov_mean is not None else None,
        "grounding_low_rate": round(len(low_rows) / len(generative), 4) if generative else None,
        "hallucination_samples": samples,
    }


def build_guardrails_compare_block(summary_off: dict, summary_on: dict, gate_k: int,
                                   inject_off: dict | None, inject_on: dict | None) -> dict:
    k = str(gate_k)
    hit_off = summary_off["hit_at"].get(k, summary_off["hit_rate"])
    hit_on = summary_on["hit_at"].get(k, summary_on["hit_rate"])
    guard_off = summary_off.get("guard_metrics") or {}
    guard_on = summary_on.get("guard_metrics") or {}
    compare = {
        "hit_at": {"off": hit_off, "on": hit_on, "delta": round(hit_on - hit_off, 4)},
        "citation_coverage": {
            "off": summary_off.get("coverage"),
            "on": summary_on.get("coverage"),
            "delta": round((summary_on.get("coverage") or 0) - (summary_off.get("coverage") or 0), 4),
        },
        "refusal_accuracy": {
            "off": summary_off.get("refusal_accuracy"),
            "on": summary_on.get("refusal_accuracy"),
        },
        "grounding_coverage_mean": {
            "off": guard_off.get("grounding_coverage_mean"),
            "on": guard_on.get("grounding_coverage_mean"),
        },
        "hallucination_proxy_mean": {
            "off": guard_off.get("hallucination_proxy_mean"),
            "on": guard_on.get("hallucination_proxy_mean"),
            "delta": None,
        },
        "grounding_low_rate": {
            "off": guard_off.get("grounding_low_rate"),
            "on": guard_on.get("grounding_low_rate"),
        },
        "hallucination_samples_on": guard_on.get("hallucination_samples") or [],
    }
    hp_off = guard_off.get("hallucination_proxy_mean")
    hp_on = guard_on.get("hallucination_proxy_mean")
    if hp_off is not None and hp_on is not None:
        compare["hallucination_proxy_mean"]["delta"] = round(hp_on - hp_off, 4)
    if inject_off and inject_on:
        compare["inject_block_accuracy"] = {
            "off": inject_off.get("block_accuracy"),
            "on": inject_on.get("block_accuracy"),
        }
        compare["inject_false_block_rate"] = {
            "off": inject_off.get("false_block_rate"),
            "on": inject_on.get("false_block_rate"),
        }
    return compare


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


def compute_refused_correct(citations: list, answer: str, use_llm: bool) -> bool:
    """negative 题拒答判定（契约 §3.2）。"""
    cited = citations or []
    if not use_llm:
        return len(cited) == 0
    return has_refusal_marker(answer) and len(cited) == 0


def evaluate_one(entry: dict, kb_base: str, token: str, space_id: int,
                 top_k: int, use_llm: bool, llm_context_top_k: int | None,
                 retrieval_strategy: str | None = None,
                 graph_expand: bool | None = None,
                 *, agentic: bool = False) -> dict:
    payload: dict = {"question": entry["question"], "spaceId": space_id,
                     "topK": top_k, "useLlm": use_llm}
    if llm_context_top_k is not None:
        payload["llmContextTopK"] = llm_context_top_k
    if retrieval_strategy:
        payload["retrievalStrategy"] = retrieval_strategy
    if graph_expand is not None:
        payload["graphExpand"] = graph_expand
    if agentic:
        payload["agentic"] = True
    endpoint = "/kb/ask/agentic" if agentic else "/kb/ask"
    t0 = time.time()
    try:
        body = http_json(f"{kb_base}{endpoint}", method="POST",
                         token=token, payload=payload, timeout=180)
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as e:
        return {"id": entry["id"], "error": str(e),
                "difficulty": entry.get("difficulty"),
                "expect_answerable": entry.get("expect_answerable")}

    elapsed_ms = int((time.time() - t0) * 1000)

    data = body.get("data") or {}
    citations = data.get("citations") or []
    answer = data.get("answer") or ""
    cited = [norm_slug(c.get("slug", "")) for c in citations]
    expect = [norm_slug(s) for s in entry.get("expect_slugs") or []]
    expect_answerable = entry.get("expect_answerable", True)
    difficulty = entry.get("difficulty")
    expect_all = entry.get("expect_all", False)

    base = {
        "id": entry["id"],
        "space": entry["space"],
        "question": entry["question"],
        "difficulty": difficulty,
        "expect_answerable": expect_answerable,
        "cited": cited,
        "mode": data.get("mode"),
        "scope": data.get("scope"),
        "elapsed_ms": elapsed_ms,
        "agentic": agentic,
    }
    if agentic:
        base["agentic_rounds"] = data.get("rounds")
        base["agentic_coverage"] = data.get("coverage")
        base["agentic_degraded"] = data.get("degraded")

    guard = data.get("guard") or {}
    if guard:
        base["guard"] = guard
        base["guard_blocked"] = guard.get("blocked")
        base["guard_coverage"] = guard.get("coverage")
        base["guard_grounding_low"] = guard.get("groundingLow")
        base["guard_grounding_applied"] = guard.get("groundingApplied")
        base["guard_unsupported"] = guard.get("unsupportedStatements") or []
    elif data.get("coverage") is not None and agentic:
        base["guard_coverage"] = data.get("coverage")
        base["guard_unsupported"] = data.get("unsupportedStatements") or []

    if not expect_answerable:
        refused = compute_refused_correct(citations, answer, use_llm)
        return {**base, "refused_correct": refused, "hit": None, "first_rank": 0,
                "coverage": None, "kw_pass": None}

    first_rank = 0
    for rank, slug in enumerate(cited, 1):
        if slug in expect:
            first_rank = rank
            break
    hit = first_rank > 0
    covered = sum(1 for s in expect if s in cited)
    coverage = covered / len(expect) if expect else 0.0

    all_hit = None
    if expect_all and expect:
        cited_top = cited[:top_k]
        all_hit = all(s in cited_top for s in expect)

    kw_pass = None
    if use_llm and entry.get("expect_keywords"):
        answer_lower = answer.lower()
        missing = [k for k in entry["expect_keywords"] if k.lower() not in answer_lower]
        kw_pass = not missing

    return {
        **base,
        "hit": hit,
        "first_rank": first_rank,
        "coverage": round(coverage, 3),
        "expect": expect,
        "all_hit": all_hit,
        "kw_pass": kw_pass,
    }


def _group_by_difficulty(results: list[dict]) -> dict[str, list[dict]]:
    groups: dict[str, list[dict]] = {d: [] for d in VALID_DIFFICULTIES}
    for r in results:
        d = r.get("difficulty")
        if d in groups:
            groups[d].append(r)
    return groups


def build_by_difficulty(groups: dict[str, list[dict]], report_ks: list[int]) -> dict:
    out: dict = {}
    for diff in ("easy", "paraphrase", "dirty", "multi-hop", "negative"):
        rows = [r for r in groups.get(diff, []) if not r.get("error")]
        if not rows:
            continue
        if diff == "negative":
            refused = sum(1 for r in rows if r.get("refused_correct"))
            out[diff] = {
                "total": len(rows),
                "refusal_accuracy": round(refused / len(rows), 4) if rows else 0.0,
            }
            continue
        hit_at = compute_hit_at_rates(rows, report_ks)
        mrr = sum((1 / r["first_rank"]) for r in rows if r.get("first_rank")) / len(rows)
        coverage = sum(r.get("coverage") or 0 for r in rows) / len(rows)
        block: dict = {
            "total": len(rows),
            "hit_at": hit_at,
            "mrr": round(mrr, 4),
            "coverage": round(coverage, 4),
        }
        if diff == "multi-hop":
            all_hit_rows = [r for r in rows if r.get("all_hit") is not None]
            if all_hit_rows:
                block["all_hit_rate"] = round(
                    sum(1 for r in all_hit_rows if r.get("all_hit")) / len(all_hit_rows), 4
                )
        out[diff] = block
    return out


def build_agentic_compare_block(summary_single: dict, summary_agentic: dict, gate_k: int) -> dict:
    k = str(gate_k)
    dirty_mh = ("dirty", "multi-hop")
    def subset_hit(summary: dict) -> float:
        rows = []
        for diff in dirty_mh:
            block = (summary.get("by_difficulty") or {}).get(diff, {})
            if block and "hit_at" in block:
                rows.append(block["hit_at"].get(k, 0))
        return sum(rows) / len(rows) if rows else summary["hit_at"].get(k, summary["hit_rate"])

    def subset_cov(summary: dict) -> float:
        total = 0.0
        count = 0
        for diff in dirty_mh:
            block = (summary.get("by_difficulty") or {}).get(diff, {})
            if block and "coverage" in block:
                total += block["coverage"]
                count += 1
        return total / count if count else summary.get("coverage", 0)

    hit_s = subset_hit(summary_single)
    hit_a = subset_hit(summary_agentic)
    cov_s = subset_cov(summary_single)
    cov_a = subset_cov(summary_agentic)
    lat_s = summary_single.get("p95_ms") or 0
    lat_a = summary_agentic.get("p95_ms") or 0
    avg_s = summary_single.get("avg_ms") or 0
    avg_a = summary_agentic.get("avg_ms") or 0
    return {
        "subset": list(dirty_mh),
        "hit_at": {"single": hit_s, "agentic": hit_a, "delta": round(hit_a - hit_s, 4)},
        "coverage": {"single": cov_s, "agentic": cov_a, "delta": round(cov_a - cov_s, 4)},
        "p95_ms": {"single": lat_s, "agentic": lat_a,
                   "ratio": round(lat_a / lat_s, 3) if lat_s else None},
        "avg_ms": {"single": avg_s or None, "agentic": avg_a or None,
                   "ratio": round(avg_a / avg_s, 3) if avg_s and avg_a else None},
    }


def build_report_summary(scored: list[dict], report_ks: list[int], gate_k: int,
                         include_negative: bool) -> dict:
    answerable = [r for r in scored if r.get("expect_answerable", True)]
    negative = [r for r in scored if not r.get("expect_answerable", True)]
    answerable_total = len(answerable)
    negative_total = len(negative)

    hit_rate = (sum(1 for r in answerable if r["hit"]) / answerable_total) if answerable else 0.0
    mrr = ((sum((1 / r["first_rank"]) for r in answerable if r.get("first_rank"))
            / answerable_total) if answerable else 0.0)
    coverage = ((sum(r.get("coverage") or 0 for r in answerable) / answerable_total)
                if answerable else 0.0)
    kw_scored = [r for r in answerable if r.get("kw_pass") is not None]
    kw_rate = (sum(1 for r in kw_scored if r["kw_pass"]) / len(kw_scored)) if kw_scored else None

    refusal_accuracy = None
    if negative_total and include_negative:
        refused_ok = sum(1 for r in negative if r.get("refused_correct"))
        refusal_accuracy = refused_ok / negative_total

    latencies = [int(r.get("elapsed_ms") or 0) for r in scored]
    p95_ms = percentile_ms(latencies, 95)
    avg_ms = int(sum(latencies) / len(latencies)) if latencies else None
    hit_at = compute_hit_at_rates(answerable, report_ks)
    gate_hit = hit_at.get(str(gate_k), hit_rate)
    by_difficulty = build_by_difficulty(_group_by_difficulty(scored), report_ks)
    guard_metrics = build_guard_metrics(scored)

    return {
        "answerable_total": answerable_total,
        "negative_total": negative_total,
        "hit_rate": round(hit_rate, 4),
        "hit_at": hit_at,
        "gate_hit": gate_hit,
        "mrr": round(mrr, 4),
        "p95_ms": p95_ms,
        "avg_ms": avg_ms,
        "coverage": round(coverage, 4),
        "refusal_accuracy": None if refusal_accuracy is None else round(refusal_accuracy, 4),
        "by_difficulty": by_difficulty,
        "kw_pass_rate": None if kw_rate is None else round(kw_rate, 4),
        "guard_metrics": guard_metrics,
    }


def run_eval_batch(entries: list[dict], kb_base: str, token: str, space_map: dict,
                   top_k: int, use_llm: bool, llm_ctx: int | None,
                   strategy: str | None, graph_expand: bool | None,
                   *, agentic: bool = False) -> list[dict]:
    results = []
    for entry in entries:
        space_id = space_map.get(entry["space"])
        if not space_id:
            results.append({"id": entry["id"], "error": f"未知 space_code: {entry['space']}",
                            "difficulty": entry.get("difficulty"),
                            "expect_answerable": entry.get("expect_answerable")})
            continue
        results.append(evaluate_one(entry, kb_base, token, space_id, top_k, use_llm, llm_ctx,
                                    strategy, graph_expand, agentic=agentic))
    return results


def run_inject_eval(inject_path: Path, kb_base: str, token: str, space_map: dict,
                    top_k: int) -> tuple[list[dict], dict]:
    entries = load_inject_golden(inject_path)
    space_id = space_map.get("moli-ops-manual") or next(iter(space_map.values()), None)
    if not space_id:
        return [], {"total": 0, "block_accuracy": None, "false_block_rate": None}
    results = [evaluate_inject_one(e, kb_base, token, space_id, top_k) for e in entries]
    return results, summarize_inject_results(results)


def print_results(entries: list[dict], results: list[dict]) -> None:
    entry_by_id = {e["id"]: e for e in entries}
    for r in results:
        entry = entry_by_id.get(r["id"], {})
        if r.get("error"):
            print(f"  {r['id']}  ERROR  {r['error']}")
        elif not r.get("expect_answerable", True):
            mark = "REFUSE_OK" if r.get("refused_correct") else "REFUSE_FAIL"
            print(f"  {r['id']}  {mark}  cites={len(r.get('cited') or [])}  "
                  f"{r['elapsed_ms']}ms  {entry.get('question', '')}")
        else:
            mark = "PASS" if r["hit"] else "MISS"
            kw = "" if r["kw_pass"] is None else ("  kw=" + ("OK" if r["kw_pass"] else "FAIL"))
            print(f"  {r['id']}  {mark}  rank={r['first_rank'] or '-'}"
                  f"  cov={r['coverage']}{kw}  {r['elapsed_ms']}ms  {entry.get('question', '')}")


def report_prefix(strategy: str | None, baseline: bool, graph_expand: bool | None) -> str:
    if strategy and graph_expand is True:
        return f"ai5-compare-{strategy}+graph"
    if strategy:
        return f"ai2-compare-{strategy}"
    if baseline:
        return "baseline-ngram"
    return "report"


def write_report(path: Path, body: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(body, ensure_ascii=False, indent=2), encoding="utf-8")


def build_compare_block(summary_a: dict, summary_b: dict, label_a: str, label_b: str,
                        gate_k: int) -> dict:
    k = str(gate_k)
    hit_a = summary_a["hit_at"].get(k, summary_a["hit_rate"])
    hit_b = summary_b["hit_at"].get(k, summary_b["hit_rate"])
    mh_a = (summary_a.get("by_difficulty") or {}).get("multi-hop", {}).get("hit_at", {})
    mh_b = (summary_b.get("by_difficulty") or {}).get("multi-hop", {}).get("hit_at", {})
    mh_hit_a = mh_a.get(k, mh_a.get("3", 0))
    mh_hit_b = mh_b.get(k, mh_b.get("3", 0))
    return {
        "labels": [label_a, label_b],
        "full_hit_at": {label_a: hit_a, label_b: hit_b, "delta": round(hit_b - hit_a, 4)},
        "multi_hop_hit_at": {
            label_a: mh_hit_a,
            label_b: mh_hit_b,
            "delta": round(mh_hit_b - mh_hit_a, 4),
        },
        "mrr": {label_a: summary_a["mrr"], label_b: summary_b["mrr"],
                "delta": round(summary_b["mrr"] - summary_a["mrr"], 4)},
        "p95_ms": {label_a: summary_a["p95_ms"], label_b: summary_b["p95_ms"]},
    }


def percentile_ms(values: list[int], pct: float) -> int | None:
    if not values:
        return None
    ordered = sorted(values)
    idx = max(0, min(len(ordered) - 1, int(round((pct / 100.0) * (len(ordered) - 1)))))
    return ordered[idx]


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
    ap.add_argument("--difficulty", help="只跑指定难度（逗号分隔 easy,paraphrase,...）")
    ap.add_argument("--include-negative", dest="include_negative", action="store_true", default=True,
                    help="纳入 negative 题（默认）")
    ap.add_argument("--no-negative", dest="include_negative", action="store_false",
                    help="排除 negative 题")
    ap.add_argument("--baseline", action="store_true",
                    help="基线报告命名 baseline-ngram-YYYYMMDD-HHMMSS.json（AI-1 对照组）")
    ap.add_argument(
        "--strategy",
        choices=sorted(VALID_STRATEGIES),
        default=None,
        help="召回策略：ngram | hybrid | hybrid-rerank（透传 AskRequest.retrievalStrategy）",
    )
    ap.add_argument(
        "--graph",
        choices=sorted(VALID_GRAPH),
        default=None,
        help="GraphRAG 开关：on=AskRequest.graphExpand true；off=显式 false；省略=后端配置默认",
    )
    ap.add_argument(
        "--compare-graph",
        action="store_true",
        help="同一 strategy 连跑 hybrid 与 hybrid+graph，产出 ai5-graph-compare 报告（需 --strategy hybrid|hybrid-rerank）",
    )
    ap.add_argument(
        "--agentic",
        action="store_true",
        help="调用 POST /kb/ask/agentic（run 标签 agentic）",
    )
    ap.add_argument(
        "--compare-agentic",
        action="store_true",
        help="dirty+multi-hop 子集对比单轮 /kb/ask vs Agentic（需 --use-llm；产出 ai7-agentic-compare 报告）",
    )
    ap.add_argument(
        "--guardrails-baseline",
        action="store_true",
        help="Guardrails 关（kb.guardrails.enabled=false）基线报告 ai9-guardrails-off-*.json（需 --use-llm）",
    )
    ap.add_argument(
        "--compare-guardrails",
        action="store_true",
        help="对比 Guardrails 关/开：需 --guardrails-off-report + 服务端 enabled=true（需 --use-llm）",
    )
    ap.add_argument(
        "--guardrails-off-report",
        help="--compare-guardrails 用的 off 基线报告路径（来自 --guardrails-baseline）",
    )
    ap.add_argument(
        "--inject-golden",
        default=str(DEFAULT_INJECT_GOLDEN),
        help="注入检测金样 JSONL（默认 kb/eval/guardrails_inject.jsonl）",
    )
    ap.add_argument("--min-hit", type=float, default=0.0,
                    help="hit@k 低于该值时退出码 1（CI 门禁，默认只报告）")
    ap.add_argument("--gate-at-k", type=int, default=0,
                    help="门禁使用的 k（0=等同 --top-k；常用 3 表示 hit@3 门禁）")
    ap.add_argument("--gate-from-baselines", action="store_true",
                    help="按 kb/eval/baselines.json §1.2 判定门禁（需 --strategy）")
    ap.add_argument("--emit-db", action="store_true",
                    help="评测完成后写入 kb_eval_run（缺库参数时只告警）")
    args = ap.parse_args()

    if args.strategy and args.baseline:
        print("[warn] --strategy 与 --baseline 同用；报告前缀 ai2-compare-{strategy}")

    if args.gate_from_baselines and not args.strategy:
        print("[error] --gate-from-baselines 需要 --strategy")
        return 1

    if args.compare_graph:
        if not args.strategy or args.strategy == "ngram":
            print("[error] --compare-graph 需要 --strategy hybrid 或 hybrid-rerank")
            return 1
        if args.graph:
            print("[warn] --compare-graph 将忽略 --graph，自动跑 off/on 两轮")

    if args.compare_agentic:
        if not args.use_llm:
            print("[error] --compare-agentic 需要 --use-llm（Agentic 依赖 LLM 编排）")
            return 1
        if args.agentic:
            print("[warn] --compare-agentic 将忽略 --agentic，自动跑 single vs agentic 两轮")

    if args.guardrails_baseline and not args.use_llm:
        print("[error] --guardrails-baseline 需要 --use-llm")
        return 1

    if args.compare_guardrails:
        if not args.use_llm:
            print("[error] --compare-guardrails 需要 --use-llm")
            return 1
        if not args.guardrails_off_report:
            print("[error] --compare-guardrails 需要 --guardrails-off-report")
            print("  先执行：kb.guardrails.enabled=false → python kb/tools/eval_ask.py --use-llm --guardrails-baseline")
            return 1
        if args.guardrails_baseline:
            print("[warn] --compare-guardrails 将忽略 --guardrails-baseline")

    graph_expand: bool | None = None
    if args.graph == "on":
        graph_expand = True
    elif args.graph == "off":
        graph_expand = False

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
    if args.difficulty:
        wanted_diff = {s.strip() for s in args.difficulty.split(",")}
        unknown = wanted_diff - VALID_DIFFICULTIES
        if unknown:
            print(f"[error] 未知 difficulty: {sorted(unknown)}")
            return 1
        entries = [e for e in entries if e["difficulty"] in wanted_diff]
    compare_agentic_entries = None
    if args.compare_agentic:
        compare_agentic_entries = [
            e for e in entries if e.get("difficulty") in ("dirty", "multi-hop")
        ]
        if not compare_agentic_entries:
            print("[error] dirty+multi-hop 子集为空")
            return 1
    if not args.include_negative:
        entries = [e for e in entries if e.get("expect_answerable", True)]
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
    strat_note = f" strategy={args.strategy}" if args.strategy else ""
    graph_note = ""
    if args.compare_graph:
        graph_note = " compare-graph=off+on"
    elif graph_expand is not None:
        graph_note = f" graph={'on' if graph_expand else 'off'}"
    print(f"kb_base · {kb_base} · {len(entries)} 题 · topK={args.top_k}{ctx_note}{strat_note}"
          f"{graph_note} · {'生成式' if args.use_llm else '检索式'}\n")
    space_map = resolve_spaces(kb_base, token)

    report_ks = sorted({k for k in STANDARD_HIT_AT if k <= args.top_k} | {args.top_k, gate_k})

    if args.compare_agentic:
        subset = compare_agentic_entries
        print(f"== 轮次 1/2 · single · POST /kb/ask ==")
        results_single = run_eval_batch(subset, kb_base, token, space_map, args.top_k,
                                        True, llm_ctx, args.strategy, graph_expand, agentic=False)
        print_results(subset, results_single)
        scored_single = [r for r in results_single if not r.get("error")]
        summary_single = build_report_summary(scored_single, report_ks, gate_k, False)

        print(f"\n== 轮次 2/2 · agentic · POST /kb/ask/agentic ==")
        results_agentic = run_eval_batch(subset, kb_base, token, space_map, args.top_k,
                                         True, llm_ctx, args.strategy, graph_expand, agentic=True)
        print_results(subset, results_agentic)
        scored_agentic = [r for r in results_agentic if not r.get("error")]
        if not scored_agentic:
            print("\n[error] Agentic 轮全部失败（常见：knowledge-server 未重启或未部署 Phase B）")
            return 1
        summary_agentic = build_report_summary(scored_agentic, report_ks, gate_k, False)

        compare = build_agentic_compare_block(summary_single, summary_agentic, gate_k)
        print(f"\n== Agentic 对比 dirty+multi-hop @ hit@{gate_k} ==")
        print(f"  hit@k  single={compare['hit_at']['single']:.2%}  agentic={compare['hit_at']['agentic']:.2%}  "
              f"Δ={compare['hit_at']['delta']:+.2%}")
        print(f"  coverage  single={compare['coverage']['single']:.2%}  "
              f"agentic={compare['coverage']['agentic']:.2%}  Δ={compare['coverage']['delta']:+.2%}")
        if compare["avg_ms"]["ratio"] is not None:
            print(f"  avg_ms  single={compare['avg_ms']['single']}  agentic={compare['avg_ms']['agentic']}  "
                  f"ratio={compare['avg_ms']['ratio']:.2f}x")

        ts = datetime.now().strftime("%Y%m%d-%H%M%S")
        compare_report = {
            "time": datetime.now().isoformat(timespec="seconds"),
            "login_base": login_base,
            "kb_base": kb_base,
            "run": "agentic",
            "retrieval_strategy": args.strategy,
            "gate_at_k": gate_k,
            "compare": compare,
            "runs": {
                "single": {**summary_single, "results": results_single},
                "agentic": {**summary_agentic, "results": results_agentic},
            },
        }
        out = REPORT_DIR / f"ai7-agentic-compare-{ts}.json"
        write_report(out, compare_report)
        print(f"对比报告: {out.relative_to(KB_DIR)}")
        return 0

    inject_path = Path(args.inject_golden)

    if args.compare_guardrails:
        off_path = Path(args.guardrails_off_report)
        if not off_path.is_file():
            print(f"[error] 找不到 off 报告: {off_path}")
            return 1
        off_body = json.loads(off_path.read_text(encoding="utf-8"))
        summary_off = off_body.get("summary") or off_body
        inject_off = off_body.get("inject_summary")

        print("== Guardrails ON · golden 全集 ==")
        print("  （请确认 kb.guardrails.enabled=true 且已重启 KnowledgeServer）")
        results_on = run_eval_batch(entries, kb_base, token, space_map, args.top_k,
                                    True, llm_ctx, args.strategy, graph_expand)
        print_results(entries, results_on)
        scored_on = [r for r in results_on if not r.get("error")]
        if not scored_on:
            print("\n[error] Guardrails ON 轮全部失败")
            return 1
        summary_on = build_report_summary(scored_on, report_ks, gate_k, args.include_negative)

        print(f"\n== Guardrails ON · 注入金样 {inject_path.name} ==")
        inject_results_on, inject_on = run_inject_eval(
            inject_path, kb_base, token, space_map, args.top_k)
        if inject_on.get("block_accuracy") is not None:
            print(f"  block_accuracy={inject_on['block_accuracy']:.1%} "
                  f"({inject_on['block_hits']}/{inject_on['block_total']})  "
                  f"false_block={inject_on.get('false_block_rate', 0):.1%}")

        compare = build_guardrails_compare_block(
            summary_off, summary_on, gate_k, inject_off, inject_on)
        print(f"\n== Guardrails 对比 @ hit@{gate_k} ==")
        print(f"  hit@k  off={compare['hit_at']['off']:.2%}  on={compare['hit_at']['on']:.2%}  "
              f"Δ={compare['hit_at']['delta']:+.2%}")
        print(f"  citation_coverage  off={compare['citation_coverage']['off']:.2%}  "
              f"on={compare['citation_coverage']['on']:.2%}  "
              f"Δ={compare['citation_coverage']['delta']:+.2%}")
        if compare["refusal_accuracy"]["off"] is not None:
            print(f"  refusal_accuracy  off={compare['refusal_accuracy']['off']:.1%}  "
                  f"on={compare['refusal_accuracy']['on']:.1%}")
        if compare["grounding_coverage_mean"]["on"] is not None:
            print(f"  grounding_coverage_mean  on={compare['grounding_coverage_mean']['on']:.2%}")
        if compare["hallucination_proxy_mean"]["on"] is not None:
            print(f"  hallucination_proxy(1-cov)  on={compare['hallucination_proxy_mean']['on']:.2%}")
        samples = compare.get("hallucination_samples_on") or []
        if samples:
            print("  幻觉样例（unsupported 摘录）:")
            for s in samples[:5]:
                print(f"    - {s.get('id')}: {str(s.get('unsupported', ''))[:80]}")

        ts = datetime.now().strftime("%Y%m%d-%H%M%S")
        compare_report = {
            "time": datetime.now().isoformat(timespec="seconds"),
            "login_base": login_base,
            "kb_base": kb_base,
            "run": "guardrails-compare",
            "gate_at_k": gate_k,
            "off_report": str(off_path),
            "compare": compare,
            "runs": {
                "off": summary_off,
                "on": {**summary_on, "results": results_on,
                       "inject_summary": inject_on, "inject_results": inject_results_on},
            },
        }
        out = REPORT_DIR / f"ai9-guardrails-compare-{ts}.json"
        write_report(out, compare_report)
        print(f"对比报告: {out.relative_to(KB_DIR)}")
        if compare["hit_at"]["delta"] < -0.05:
            print(f"[warn] hit@{gate_k} 降幅 {compare['hit_at']['delta']:.2%} 超过容差 -5pp")
        return 0

    if args.compare_graph:
        label_base = args.strategy
        label_graph = f"{args.strategy}+graph"
        print(f"== 轮次 1/2 · {label_base} · graphExpand=false ==")
        results_base = run_eval_batch(entries, kb_base, token, space_map, args.top_k,
                                      args.use_llm, llm_ctx, args.strategy, False)
        print_results(entries, results_base)
        scored_base = [r for r in results_base if not r.get("error")]
        summary_base = build_report_summary(scored_base, report_ks, gate_k, args.include_negative)

        print(f"\n== 轮次 2/2 · {label_graph} · graphExpand=true ==")
        results_graph = run_eval_batch(entries, kb_base, token, space_map, args.top_k,
                                       args.use_llm, llm_ctx, args.strategy, True)
        print_results(entries, results_graph)
        scored_graph = [r for r in results_graph if not r.get("error")]
        summary_graph = build_report_summary(scored_graph, report_ks, gate_k, args.include_negative)

        compare = build_compare_block(summary_base, summary_graph, label_base, label_graph, gate_k)
        print(f"\n== Graph 对比 @ hit@{gate_k} ==")
        print(f"  全集  {label_base}={compare['full_hit_at'][label_base]:.2%}  "
              f"{label_graph}={compare['full_hit_at'][label_graph]:.2%}  "
              f"Δ={compare['full_hit_at']['delta']:+.2%}")
        print(f"  multi-hop  {label_base}={compare['multi_hop_hit_at'][label_base]:.2%}  "
              f"{label_graph}={compare['multi_hop_hit_at'][label_graph]:.2%}  "
              f"Δ={compare['multi_hop_hit_at']['delta']:+.2%}")

        ts = datetime.now().strftime("%Y%m%d-%H%M%S")
        compare_report = {
            "time": datetime.now().isoformat(timespec="seconds"),
            "login_base": login_base,
            "kb_base": kb_base,
            "retrieval_strategy": args.strategy,
            "gate_at_k": gate_k,
            "compare": compare,
            "runs": {
                label_base: {"graph_expand": False, **summary_base, "results": results_base},
                label_graph: {"graph_expand": True, **summary_graph, "results": results_graph},
            },
        }
        out = REPORT_DIR / f"ai5-graph-compare-{args.strategy}-{ts}.json"
        write_report(out, compare_report)
        print(f"对比报告: {out.relative_to(KB_DIR)}")
        return 0

    results = run_eval_batch(entries, kb_base, token, space_map, args.top_k, args.use_llm, llm_ctx,
                           args.strategy, graph_expand, agentic=args.agentic)
    print_results(entries, results)

    scored = [r for r in results if not r.get("error")]
    errors = len(results) - len(scored)
    if not scored:
        print("\n全部请求失败，请检查服务是否启动")
        return 1

    summary = build_report_summary(scored, report_ks, gate_k, args.include_negative)
    hit_at = summary["hit_at"]
    gate_hit = summary["gate_hit"]
    mrr = summary["mrr"]
    coverage = summary["coverage"]
    p95_ms = summary["p95_ms"]
    kw_rate = summary["kw_pass_rate"]
    by_difficulty = summary["by_difficulty"]
    answerable_total = summary["answerable_total"]
    negative_total = summary["negative_total"]
    refusal_accuracy = summary["refusal_accuracy"]

    hit_parts = "  ".join(f"hit@{k}={hit_at[str(k)]:.2%}" for k in report_ks)
    print(f"\n== 汇总 ==  {hit_parts}  mrr={mrr:.3f}"
          f"  coverage={coverage:.2%}"
          + (f"  p95={p95_ms}ms" if p95_ms is not None else "")
          + (f"  kw_pass={kw_rate:.2%}" if kw_rate is not None else "")
          + (f"  errors={errors}" if errors else "")
          + f"  answerable={answerable_total}  negative={negative_total}")

    layer_parts = []
    for diff in ("easy", "paraphrase", "dirty", "multi-hop"):
        block = by_difficulty.get(diff)
        if block and "hit_at" in block:
            h3 = block["hit_at"].get("3", block["hit_at"].get(str(gate_k), 0))
            layer_parts.append(f"{diff} hit@3={h3:.0%}")
    if layer_parts:
        print("== 分层 ==  " + "  ".join(layer_parts))

    if refusal_accuracy is not None:
        refused_ok = sum(1 for r in scored if not r.get("expect_answerable", True)
                         and r.get("refused_correct"))
        print(f"== 拒答 ==  refusal_accuracy={refusal_accuracy:.1%} ({refused_ok}/{negative_total})")

    ts = datetime.now().strftime("%Y%m%d-%H%M%S")
    prefix = "agentic" if args.agentic else report_prefix(args.strategy, args.baseline, graph_expand)
    report = {
        "time": datetime.now().isoformat(timespec="seconds"),
        "login_base": login_base,
        "kb_base": kb_base,
        "retrieval_strategy": args.strategy,
        "graph_expand": graph_expand,
        "top_k": args.top_k,
        "llm_context_top_k": llm_ctx,
        "gate_at_k": gate_k,
        "use_llm": args.use_llm,
        "agentic": args.agentic,
        "run": "agentic" if args.agentic else None,
        "total": len(results),
        "answerable_total": answerable_total,
        "negative_total": negative_total,
        "errors": errors,
        "hit_rate": summary["hit_rate"],
        "hit_at": hit_at,
        "mrr": mrr,
        "p95_ms": p95_ms,
        "coverage": coverage,
        "refusal_accuracy": refusal_accuracy,
        "by_difficulty": by_difficulty,
        "kw_pass_rate": kw_rate,
        "results": results,
    }
    out = REPORT_DIR / f"{prefix}-{ts}.json"
    write_report(out, report)
    print(f"报告: {out.relative_to(KB_DIR)}")

    if args.guardrails_baseline:
        print(f"\n== Guardrails OFF 基线 · 注入金样 {inject_path.name} ==")
        inject_results, inject_summary = run_inject_eval(
            inject_path, kb_base, token, space_map, args.top_k)
        if inject_summary.get("block_accuracy") is not None:
            print(f"  block_accuracy={inject_summary['block_accuracy']:.1%} "
                  f"(期望接近 0，enabled=false 时不拦截)")
        baseline_out = REPORT_DIR / f"ai9-guardrails-off-{ts}.json"
        baseline_body = {
            "time": report["time"],
            "login_base": login_base,
            "kb_base": kb_base,
            "run": "guardrails-off",
            "gate_at_k": gate_k,
            "summary": summary,
            "inject_summary": inject_summary,
            "inject_results": inject_results,
            "results": results,
        }
        write_report(baseline_out, baseline_body)
        print(f"Guardrails OFF 基线: {baseline_out.relative_to(KB_DIR)}")
        print("下一步：启用 kb.guardrails.enabled=true 重启后执行")
        print(f"  python kb/tools/eval_ask.py --use-llm --compare-guardrails "
              f"--guardrails-off-report {baseline_out.relative_to(KB_DIR)}")

    gate_pass: bool | None = None
    gate_failed = False
    if args.gate_from_baselines and args.strategy:
        from eval_gate import evaluate_gate, load_baselines

        bl = load_baselines()
        passed, failures, gate_pass = evaluate_gate(report, args.strategy, bl)
        if not passed:
            gate_failed = True
            for msg in failures:
                print(f"[gate] {msg}")
    elif args.min_hit and gate_hit < args.min_hit:
        print(f"hit@{gate_k} {gate_hit:.2%} < 门禁 {args.min_hit:.2%}")
        gate_failed = True

    if args.emit_db:
        from eval_gate import emit_eval_run

        emit_eval_run(
            report,
            out,
            strategy=args.strategy,
            gate_pass=gate_pass,
        )

    if gate_failed:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
