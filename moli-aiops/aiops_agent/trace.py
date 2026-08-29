"""全链路 trace 落库。

每个节点一条记录，含耗时、实际使用的模型与厂商、token 数、是否触发了跨厂商兜底、
重试次数、调用了哪些工具、失败原因。与工具层审计共用一个 sqlite，
按 incident_id 就能把「Agent 想了什么」和「Agent 做了什么」拼成完整时间线。
"""

from __future__ import annotations

import json
import sqlite3
import threading
import time
from typing import Any

from . import config
from .models import Alert, NodeTrace

_LOCK = threading.Lock()
_CONN: sqlite3.Connection | None = None

_SCHEMA = """
CREATE TABLE IF NOT EXISTS aiops_run (
    run_id      TEXT PRIMARY KEY,
    incident_id TEXT NOT NULL,
    title       TEXT,
    target      TEXT,
    severity    TEXT,
    status      TEXT NOT NULL,
    alert       TEXT,
    root_cause  TEXT,
    report_md   TEXT,
    degraded    INTEGER DEFAULT 0,
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS aiops_node_trace (
    id                TEXT PRIMARY KEY,
    run_id            TEXT NOT NULL,
    seq               INTEGER NOT NULL,
    node              TEXT NOT NULL,
    iteration         INTEGER DEFAULT 0,
    started_at        TEXT,
    duration_ms       INTEGER DEFAULT 0,
    status            TEXT,
    model             TEXT,
    provider          TEXT,
    prompt_tokens     INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    fallback_used     INTEGER DEFAULT 0,
    attempts          INTEGER DEFAULT 1,
    tool_calls        TEXT,
    error             TEXT
);
CREATE INDEX IF NOT EXISTS idx_trace_run ON aiops_node_trace(run_id, seq);
"""


def _conn() -> sqlite3.Connection:
    global _CONN
    if _CONN is None:
        config.DB_PATH.parent.mkdir(parents=True, exist_ok=True)
        _CONN = sqlite3.connect(config.DB_PATH, check_same_thread=False)
        _CONN.row_factory = sqlite3.Row
        _CONN.execute("PRAGMA journal_mode=WAL")
        _CONN.executescript(_SCHEMA)
        _CONN.commit()
    return _CONN


def _now() -> str:
    return time.strftime("%Y-%m-%dT%H:%M:%S")


def start_run(run_id: str, incident_id: str, alert: Alert) -> None:
    with _LOCK:
        conn = _conn()
        conn.execute(
            "INSERT OR REPLACE INTO aiops_run"
            " (run_id, incident_id, title, target, severity, status, alert, created_at, updated_at)"
            " VALUES (?,?,?,?,?,?,?,?,?)",
            (
                run_id, incident_id, alert.title, alert.target, "",
                "running", alert.model_dump_json(), _now(), _now(),
            ),
        )
        conn.commit()


def update_run(run_id: str, **fields: Any) -> None:
    allowed = {"status", "severity", "root_cause", "report_md", "degraded", "target", "title"}
    updates = {k: v for k, v in fields.items() if k in allowed}
    if not updates:
        return
    assignments = ", ".join(f"{k} = ?" for k in updates)
    values = list(updates.values()) + [_now(), run_id]
    with _LOCK:
        conn = _conn()
        conn.execute(
            f"UPDATE aiops_run SET {assignments}, updated_at = ? WHERE run_id = ?", values
        )
        conn.commit()


def add_node_trace(run_id: str, trace: NodeTrace) -> None:
    with _LOCK:
        conn = _conn()
        seq_row = conn.execute(
            "SELECT COALESCE(MAX(seq), 0) + 1 AS next FROM aiops_node_trace WHERE run_id = ?",
            (run_id,),
        ).fetchone()
        seq = int(seq_row["next"]) if seq_row else 1
        conn.execute(
            "INSERT INTO aiops_node_trace (id, run_id, seq, node, iteration, started_at,"
            " duration_ms, status, model, provider, prompt_tokens, completion_tokens,"
            " fallback_used, attempts, tool_calls, error)"
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (
                f"{run_id}:{seq}", run_id, seq, trace.node, trace.iteration,
                trace.started_at or _now(), trace.duration_ms, trace.status,
                trace.model, trace.provider, trace.prompt_tokens, trace.completion_tokens,
                1 if trace.fallback_used else 0, trace.attempts,
                json.dumps(trace.tool_calls, ensure_ascii=False), trace.error,
            ),
        )
        conn.commit()


def get_run(run_id: str) -> dict[str, Any] | None:
    with _LOCK:
        row = _conn().execute("SELECT * FROM aiops_run WHERE run_id = ?", (run_id,)).fetchone()
    return dict(row) if row else None


def list_runs(limit: int = 50) -> list[dict[str, Any]]:
    with _LOCK:
        rows = _conn().execute(
            "SELECT run_id, incident_id, title, target, severity, status, degraded, created_at,"
            " updated_at FROM aiops_run ORDER BY created_at DESC LIMIT ?",
            (limit,),
        ).fetchall()
    return [dict(r) for r in rows]


def get_traces(run_id: str) -> list[dict[str, Any]]:
    with _LOCK:
        rows = _conn().execute(
            "SELECT * FROM aiops_node_trace WHERE run_id = ? ORDER BY seq ASC", (run_id,)
        ).fetchall()
    result = []
    for row in rows:
        item = dict(row)
        item["tool_calls"] = json.loads(item.get("tool_calls") or "[]")
        item["fallback_used"] = bool(item.get("fallback_used"))
        result.append(item)
    return result


def summarize(run_id: str) -> dict[str, Any]:
    """成本与耗时汇总。回答「这次诊断花了多久、烧了多少 token、切过厂商没有」。"""
    traces = get_traces(run_id)
    if not traces:
        return {"nodes": 0}
    by_node: dict[str, dict[str, Any]] = {}
    for t in traces:
        bucket = by_node.setdefault(t["node"], {"calls": 0, "duration_ms": 0, "tokens": 0})
        bucket["calls"] += 1
        bucket["duration_ms"] += t["duration_ms"] or 0
        bucket["tokens"] += (t["prompt_tokens"] or 0) + (t["completion_tokens"] or 0)
    return {
        "nodes": len(traces),
        "total_duration_ms": sum(t["duration_ms"] or 0 for t in traces),
        "prompt_tokens": sum(t["prompt_tokens"] or 0 for t in traces),
        "completion_tokens": sum(t["completion_tokens"] or 0 for t in traces),
        "fallback_calls": sum(1 for t in traces if t["fallback_used"]),
        "error_nodes": [t["node"] for t in traces if t["status"] == "error"],
        "by_node": by_node,
    }
