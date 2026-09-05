"""把 Alertmanager webhook 收成内部 Alert。

Alertmanager 不会登录 Shiro，所以走独立 Bearer，不复用 /diagnose 的会话。
同一 fingerprint 在去重窗口内只开一次诊断，避免 repeat_interval 把图打爆。
"""

from __future__ import annotations

import threading
import time
from typing import Any

from ops_mcp.cmdb.base import SERVICE_ALIASES

from .models import Alert

_LOCK = threading.Lock()
_SEEN: dict[str, float] = {}


def webhook_token_ok(header: str, secret: str) -> bool:
    if not secret:
        return False
    raw = (header or "").strip()
    if raw.lower().startswith("bearer "):
        raw = raw[7:].strip()
    return bool(raw) and raw == secret


def fingerprint(labels: dict[str, str]) -> str:
    return "|".join(
        [
            labels.get("alertname") or "",
            labels.get("service") or "",
            labels.get("instance") or "",
        ]
    )


def should_skip_duplicate(fp: str, *, now: float | None = None, ttl_s: int = 600) -> bool:
    if not fp or ttl_s <= 0:
        return False
    ts = now if now is not None else time.time()
    with _LOCK:
        expired = [k for k, seen in _SEEN.items() if ts - seen > ttl_s]
        for key in expired:
            del _SEEN[key]
        last = _SEEN.get(fp)
        if last is not None and ts - last <= ttl_s:
            return True
        _SEEN[fp] = ts
        return False


def reset_dedup() -> None:
    with _LOCK:
        _SEEN.clear()


def _as_str_map(raw: Any) -> dict[str, str]:
    if not isinstance(raw, dict):
        return {}
    return {str(k): str(v) for k, v in raw.items() if v is not None}


def resolve_target(service: str, inventory) -> str:
    """inventory 对得上就用那台主机；对不上留空，investigator 只走指标/KB/全链路。"""
    if not service or inventory is None:
        return ""
    try:
        hit = inventory.find_by_service(service)
    except Exception:  # noqa: BLE001
        hit = None
    if hit is not None:
        return hit.id
    aliases = SERVICE_ALIASES.get(service, ())
    for alias in aliases:
        try:
            hit = inventory.find_by_service(alias)
        except Exception:  # noqa: BLE001
            hit = None
        if hit is not None:
            return hit.id
    return ""


def alerts_from_payload(payload: dict[str, Any], *, inventory=None) -> list[Alert]:
    """只收 firing。resolved 不诊断。"""
    if (payload.get("status") or "").lower() == "resolved":
        return []
    items = payload.get("alerts") or []
    out: list[Alert] = []
    for item in items:
        if not isinstance(item, dict):
            continue
        if (item.get("status") or "firing").lower() != "firing":
            continue
        labels = _as_str_map(item.get("labels"))
        annotations = _as_str_map(item.get("annotations"))
        service = labels.get("service") or ""
        title = annotations.get("summary") or labels.get("alertname") or "Prometheus 告警"
        description = annotations.get("description") or ""
        if item.get("generatorURL"):
            description = (description + f"\ngeneratorURL={item['generatorURL']}").strip()
        out.append(
            Alert(
                title=title,
                description=description,
                service=service,
                target=resolve_target(service, inventory),
                source="webhook",
                fired_at=str(item.get("startsAt") or ""),
                trace_id=labels.get("trace_id") or labels.get("traceId") or "",
                labels=labels,
            )
        )
    return out
