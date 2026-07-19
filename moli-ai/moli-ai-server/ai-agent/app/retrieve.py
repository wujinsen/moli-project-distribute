"""retrieve_schema 节点：白名单表 keyword 检索 + 可选 kb-retrieval。"""
from __future__ import annotations

import re
from typing import Any

import httpx

from .config import RETRIEVAL_BASE_URL, SCHEMA_TOP_K, load_allow_tables


def _score_table(question: str, table: dict[str, Any]) -> float:
    q = question.lower()
    score = 0.0
    name = table.get("table", "")
    comment = table.get("comment") or ""
    if name and name.lower() in q:
        score += 3.0
    if comment and any(tok in q for tok in re.findall(r"[\u4e00-\u9fff]+", comment)):
        score += 2.0
    for col in table.get("columns") or []:
        cname = col.get("name") or ""
        ccomment = col.get("comment") or ""
        if cname and cname.lower() in q:
            score += 1.0
        if ccomment and ccomment in question:
            score += 0.5
    # 领域关键词
    if "order" in name or "订单" in comment:
        if any(k in q for k in ("订单", "order", "下单", "成交")):
            score += 2.0
    if "activity" in name or "活动" in comment:
        if any(k in q for k in ("活动", "activity", "库存", "秒杀")):
            score += 2.0
    return score


def retrieve_schema(question: str) -> tuple[list[dict[str, Any]], str]:
    tables = load_allow_tables()
    ranked = sorted(tables, key=lambda t: _score_table(question, t), reverse=True)
    hits = [t for t in ranked if _score_table(question, t) > 0][:SCHEMA_TOP_K]
    if not hits:
        hits = ranked[: min(2, len(ranked))]

    # 可选：调 kb-retrieval /search 做语义增强（失败则忽略）
    try:
        with httpx.Client(timeout=3.0) as client:
            resp = client.post(
                f"{RETRIEVAL_BASE_URL.rstrip('/')}/search",
                json={"query": question, "spaceIds": [], "topN": 5},
            )
            if resp.status_code == 200:
                pass  # MVP：索引未建 BI schema 时不改 hits
    except Exception:
        pass

    lines: list[str] = []
    for t in hits:
        cols = ", ".join(f"{c['name']}({c.get('type', '')})" for c in (t.get("columns") or []))
        lines.append(f"- {t['table']}: {t.get('comment', '')} | columns: {cols}")
    digest = "\n".join(lines)
    return hits, digest
