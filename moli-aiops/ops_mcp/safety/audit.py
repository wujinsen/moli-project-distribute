"""执行审计。

每一次工具调用都落一条，无论成功、被拦、还是失败。被拦下的记录尤其要留——
事后复盘时「Agent 曾经想执行什么但被挡住了」比「执行了什么」更有价值。
"""

from __future__ import annotations

import json
import sqlite3
import threading
import time
import uuid
from typing import Any

from .. import config

_LOCK = threading.Lock()
_CONN: sqlite3.Connection | None = None

_SCHEMA = """
CREATE TABLE IF NOT EXISTS ops_audit (
    id            TEXT PRIMARY KEY,
    ts            TEXT NOT NULL,
    incident_id   TEXT,
    step_id       TEXT,
    tool          TEXT NOT NULL,
    host          TEXT,
    command       TEXT,
    risk          TEXT,
    outcome       TEXT NOT NULL,
    approver      TEXT,
    approval_jti  TEXT,
    dry_run       INTEGER DEFAULT 0,
    exit_code     INTEGER,
    duration_ms   INTEGER,
    error_code    TEXT,
    detail        TEXT
);
CREATE INDEX IF NOT EXISTS idx_ops_audit_incident ON ops_audit(incident_id);
CREATE INDEX IF NOT EXISTS idx_ops_audit_ts ON ops_audit(ts);
"""


def _conn() -> sqlite3.Connection:
    global _CONN
    if _CONN is None:
        config.AUDIT_DB.parent.mkdir(parents=True, exist_ok=True)
        _CONN = sqlite3.connect(config.AUDIT_DB, check_same_thread=False)
        _CONN.execute("PRAGMA journal_mode=WAL")
        _CONN.executescript(_SCHEMA)
        _CONN.commit()
    return _CONN


def record(
    *,
    tool: str,
    outcome: str,
    incident_id: str = "",
    step_id: str = "",
    host: str = "",
    command: str = "",
    risk: str = "",
    approver: str = "",
    approval_jti: str = "",
    dry_run: bool = False,
    exit_code: int | None = None,
    duration_ms: int | None = None,
    error_code: str = "",
    detail: dict[str, Any] | None = None,
) -> str:
    audit_id = uuid.uuid4().hex
    row = (
        audit_id,
        time.strftime("%Y-%m-%dT%H:%M:%S"),
        incident_id,
        step_id,
        tool,
        host,
        command,
        risk,
        outcome,
        approver,
        approval_jti,
        1 if dry_run else 0,
        exit_code,
        duration_ms,
        error_code,
        json.dumps(detail or {}, ensure_ascii=False, default=str),
    )
    with _LOCK:
        conn = _conn()
        conn.execute(
            "INSERT INTO ops_audit (id, ts, incident_id, step_id, tool, host, command, risk,"
            " outcome, approver, approval_jti, dry_run, exit_code, duration_ms, error_code, detail)"
            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            row,
        )
        conn.commit()
    return audit_id


def list_for_incident(incident_id: str, limit: int = 200) -> list[dict[str, Any]]:
    with _LOCK:
        conn = _conn()
        conn.row_factory = sqlite3.Row
        rows = conn.execute(
            "SELECT * FROM ops_audit WHERE incident_id = ? ORDER BY ts ASC LIMIT ?",
            (incident_id, limit),
        ).fetchall()
    return [dict(r) for r in rows]
