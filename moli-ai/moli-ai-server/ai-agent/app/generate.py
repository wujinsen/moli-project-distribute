"""generate_sql + self_lint 节点（规则 MVP + 可选 LLM）。"""
from __future__ import annotations

import json
import re
from typing import Any, Optional

import httpx

from .config import LLM_API_KEY, LLM_BASE_URL, LLM_MODEL, MAX_ROWS
from .models import GenerateRequest, GenerateResponse


WRITE_PATTERNS = re.compile(
    r"\b(update|delete|insert|drop|truncate|alter|create|grant|revoke)\b|"
    r"(删除|修改|更新|写入|插入|清空|改成|改为|设为)",
    re.I,
)


def self_lint(sql: Optional[str]) -> Optional[str]:
    if not sql or not sql.strip():
        return "empty sql"
    s = sql.strip().lower()
    if ";" in s.rstrip(";"):
        return "multiple statements"
    if not s.startswith("select"):
        return "non-select"
    if re.search(r"\bselect\s+\*", s):
        return "star select"
    return None


def _rule_sql(question: str, tables: list[dict[str, Any]]) -> tuple[Optional[str], list[str], Optional[str]]:
    q = question.lower()
    if WRITE_PATTERNS.search(question):
        return None, [], "问题涉及写操作，只能生成只读 SELECT"

    table_names = {t["table"] for t in tables}
    used: list[str] = []

    join_tables = {"seckill_order", "seckill_activity"}.issubset(table_names)
    wants_join = join_tables and any(
        k in q for k in ("联", "join", "每个活动", "各活动", "活动订单", "联表")
    )
    if wants_join:
        used.extend(["seckill_activity", "seckill_order"])
        return (
            "SELECT a.id, a.name, COUNT(o.id) AS order_count "
            "FROM seckill_activity a "
            "LEFT JOIN seckill_order o ON o.activity_id = a.id "
            "GROUP BY a.id, a.name "
            f"LIMIT {MAX_ROWS}",
            used,
            None,
        )

    is_count_question = any(k in q for k in ("多少", "count", "数量", "总数", "统计"))
    is_top_n_question = (
        "seckill_order" in table_names
        and "订单" in q
        and not is_count_question
        and (
            re.search(r"前\s*\d+", q)
            or ("id" in q and any(k in q for k in ("状态", "status", "条")))
            or any(k in q for k in ("状态", "status"))
        )
    )
    if is_top_n_question:
        used.append("seckill_order")
        limit = 10 if re.search(r"前\s*10|10\s*条", q) else MAX_ROWS
        return (
            f"SELECT id, status FROM seckill_order LIMIT {limit}",
            used,
            None,
        )

    if "seckill_order" in table_names and (
        "create_time" in q or ("最近" in q and "订单" in q)
    ):
        used.append("seckill_order")
        return (
            f"SELECT id, activity_id, user_id, status, create_time FROM seckill_order "
            f"ORDER BY create_time DESC LIMIT {MAX_ROWS}",
            used,
            None,
        )

    if "seckill_activity" in table_names and any(k in q for k in ("start_time", "end_time")):
        used.append("seckill_activity")
        return (
            f"SELECT id, name, start_time, end_time FROM seckill_activity LIMIT {MAX_ROWS}",
            used,
            None,
        )

    # 订单计数
    if "seckill_order" in table_names and any(k in q for k in ("订单", "order", "多少", "count", "数量", "统计")):
        used.append("seckill_order")
        if "活动" in q or "activity" in q:
            return (
                f"SELECT activity_id, COUNT(*) AS order_count FROM seckill_order "
                f"GROUP BY activity_id LIMIT {MAX_ROWS}",
                used,
                None,
            )
        return (
            f"SELECT COUNT(*) AS order_count FROM seckill_order LIMIT {MAX_ROWS}",
            used,
            None,
        )

    # 活动列表 / 库存
    if "seckill_activity" in table_names and any(
        k in q for k in ("活动", "activity", "库存", "stock", "秒杀", "列表", "list")
    ):
        used.append("seckill_activity")
        return (
            f"SELECT id, name, stock, sold, status, start_time, end_time FROM seckill_activity "
            f"LIMIT {MAX_ROWS}",
            used,
            None,
        )

    # 联表兜底（未命中上面关键词时）
    if join_tables and any(k in q for k in ("联", "join")):
        used.extend(["seckill_activity", "seckill_order"])
        return (
            "SELECT a.id, a.name, COUNT(o.id) AS order_count "
            "FROM seckill_activity a "
            "LEFT JOIN seckill_order o ON o.activity_id = a.id "
            "GROUP BY a.id, a.name "
            f"LIMIT {MAX_ROWS}",
            used,
            None,
        )

    if table_names:
        t0 = tables[0]["table"]
        cols = [c["name"] for c in tables[0].get("columns") or []][:6]
        if not cols:
            cols = ["id"]
        used.append(t0)
        return (
            f"SELECT {', '.join(cols)} FROM {t0} LIMIT {MAX_ROWS}",
            used,
            None,
        )

    return None, [], "无法从问题推断安全查询"


def _llm_sql(req: GenerateRequest, schema_digest: str) -> tuple[Optional[str], list[str], Optional[str]]:
    if not LLM_API_KEY or not LLM_BASE_URL:
        return None, [], None
    system = (
        "你是只读数据分析师，只能生成单条 MySQL SELECT。"
        "禁止 DML/DDL/多语句/SELECT *。必须 LIMIT。"
        f"输出 JSON：{{\"sql\":\"...\",\"tables\":[\"...\"],\"refusal\":null}}"
    )
    user = f"问题：{req.question}\nschema：\n{schema_digest}"
    if req.retry > 0 and req.priorSql:
        user += f"\n上次 SQL：{req.priorSql}\n反馈：{req.priorError}"
    try:
        with httpx.Client(timeout=60.0) as client:
            resp = client.post(
                f"{LLM_BASE_URL.rstrip('/')}/chat/completions",
                headers={"Authorization": f"Bearer {LLM_API_KEY}"},
                json={
                    "model": LLM_MODEL,
                    "messages": [
                        {"role": "system", "content": system},
                        {"role": "user", "content": user},
                    ],
                    "temperature": 0.1,
                },
            )
            resp.raise_for_status()
            content = resp.json()["choices"][0]["message"]["content"]
            data = json.loads(content)
            return data.get("sql"), data.get("tables") or [], data.get("refusal")
    except Exception:
        return None, [], None


def generate_sql(req: GenerateRequest, tables: list[dict[str, Any]], schema_digest: str) -> GenerateResponse:
    refusal: Optional[str] = None
    draft: Optional[str] = None
    used: list[str] = []

    llm_sql, llm_tables, llm_refusal = _llm_sql(req, schema_digest)
    if llm_refusal:
        return GenerateResponse(refusal=llm_refusal, schemaDigest=schema_digest)
    if llm_sql:
        draft, used = llm_sql, llm_tables
    else:
        draft, used, refusal = _rule_sql(req.question, tables)

    if refusal:
        return GenerateResponse(refusal=refusal, schemaDigest=schema_digest)

    lint = self_lint(draft)
    if lint:
        return GenerateResponse(draftSql=None, usedTables=used, schemaDigest=schema_digest, refusal=lint)

    return GenerateResponse(draftSql=draft, usedTables=used, schemaDigest=schema_digest)
