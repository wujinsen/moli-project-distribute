"""只读全链路证据：SkyWalking OAP GraphQL v2 + Loki。

BanyanDB 拒收 queryBasicTraces / queryTrace（v1），必须走 queryTraces。
Loki 侧 trace_id 不得当标签（高基数），只在日志正文用 |= 搜 32 位根 ID。
"""

from __future__ import annotations

import re
from datetime import datetime, timedelta, timezone
from typing import Any
from urllib.parse import urlparse

import httpx

from .. import config
from ..errors import (
    OPS_INVALID_INPUT,
    OPS_OBSERVABILITY_UNAVAILABLE,
    OpsToolError,
)
from ..schemas import LokiLogDump, LokiLogHit, TraceDump, TraceSpanDump, model_dump_compact

# SkyWalking UI 复制的是「根 ID.segment.xxx」；Loki / 检索只认前 32 位 hex
_TID_PREFIX = re.compile(r"^(?:TID:)?", re.IGNORECASE)
_HEX32 = re.compile(r"([0-9a-fA-F]{32})")
_CST = timezone(timedelta(hours=8))

QUERY_TRACES = """
query OpsQueryTraces($condition: TraceQueryCondition) {
  queryTraces(condition: $condition) {
    traces {
      spans {
        traceId
        segmentId
        spanId
        parentSpanId
        serviceCode
        serviceInstanceName
        startTime
        endTime
        endpointName
        type
        peer
        component
        isError
        layer
      }
    }
  }
}
"""

# 与 Grafana Moli Trace Logs 面板一致；禁止再加 level=INFO（MyBatis / Dubbo 是 DEBUG）
DEFAULT_SERVICE_SELECTOR = (
    '{service=~"moli-gateway|user-center-server|order-server|ai-server|knowledge-server"}'
)


def _full_sw_id(raw: str) -> str:
    """去掉 TID: 后保留 UI 完整串（根 ID.segment.…）。OAP 按完整串才能命中。"""
    text = (raw or "").strip()
    if text.upper().startswith("TID:"):
        text = text[4:]
    return text


def _loki_selector(raw: str) -> str:
    selector = (raw or DEFAULT_SERVICE_SELECTOR).strip()
    if not selector.startswith("{"):
        selector = "{" + selector + "}"
    return selector


def normalize_trace_id(raw: str) -> str:
    """抽出 32 位根 Trace ID。UI 完整串、TID: 前缀、正文里嵌着的 ID 都能吃。"""
    text = (raw or "").strip()
    if not text:
        raise OpsToolError(OPS_INVALID_INPUT, "trace_id 不能为空")
    stripped = _TID_PREFIX.sub("", text)
    head = stripped.split(".", 1)[0]
    if re.fullmatch(r"[0-9a-fA-F]{32}", head):
        return head.lower()
    match = _HEX32.search(text)
    if match:
        return match.group(1).lower()
    raise OpsToolError(
        OPS_INVALID_INPUT,
        "无法从输入解析 32 位 Trace ID（不要带 .segment 后缀去搜 Loki）",
        detail={"raw": text[:120]},
    )


def extract_trace_id(alert: dict[str, Any] | None) -> str:
    """从告警字段或正文里抠 trace_id；没有就返回空串，不抛。"""
    if not alert:
        return ""
    labels = alert.get("labels") if isinstance(alert.get("labels"), dict) else {}
    candidates = (
        alert.get("trace_id"),
        alert.get("traceId"),
        labels.get("trace_id"),
        labels.get("traceId"),
        labels.get("tid"),
    )
    for item in candidates:
        if item:
            try:
                return normalize_trace_id(str(item))
            except OpsToolError:
                continue
    blob = f"{alert.get('title') or ''} {alert.get('description') or ''}"
    try:
        return normalize_trace_id(blob) if blob.strip() else ""
    except OpsToolError:
        return ""


def _shanghai_now() -> datetime:
    return datetime.now(_CST)


def query_duration(lookback_hours: int = 24) -> dict[str, str]:
    """OAP 容器 TZ=Asia/Shanghai，Duration 形如 YYYY-MM-dd HHmm。"""
    hours = max(1, min(int(lookback_hours or 24), 168))
    end = _shanghai_now()
    start = end - timedelta(hours=hours)
    return {
        "start": start.strftime("%Y-%m-%d %H%M"),
        "end": end.strftime("%Y-%m-%d %H%M"),
        "step": "MINUTE",
    }


def _graphql_url() -> str:
    raw = config.SW_OAP_GRAPHQL_URL
    parsed = urlparse(raw)
    if parsed.path.rstrip("/") == "/graphql" or raw.endswith("/graphql"):
        return raw
    return raw.rstrip("/") + "/graphql"


def _post_graphql(payload: dict[str, Any]) -> dict[str, Any]:
    url = _graphql_url()
    try:
        with httpx.Client(timeout=config.OBS_TIMEOUT_S) as client:
            response = client.post(
                url,
                json=payload,
                headers={"Content-Type": "application/json", "Accept": "application/json"},
            )
            response.raise_for_status()
            body = response.json()
    except httpx.HTTPStatusError as exc:
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"SkyWalking OAP HTTP {exc.response.status_code}",
            detail={"url": url},
        ) from exc
    except Exception as exc:  # noqa: BLE001
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"SkyWalking OAP 不可达：{exc}",
            detail={"url": url},
        ) from exc
    if not isinstance(body, dict):
        raise OpsToolError(OPS_OBSERVABILITY_UNAVAILABLE, "SkyWalking OAP 返回非 JSON 对象")
    errors = body.get("errors") or []
    if errors:
        message = errors[0].get("message") if isinstance(errors[0], dict) else str(errors[0])
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"SkyWalking GraphQL 错误：{message}",
            detail={"errors": errors[:3]},
        )
    return body


def _query_traces(duration: dict[str, str], *, trace_id: str | None, page_size: int = 20) -> list[dict[str, Any]]:
    condition: dict[str, Any] = {
        "traceState": "ALL",
        "queryOrder": "BY_START_TIME",
        "paging": {"pageNum": 1, "pageSize": page_size},
        "queryDuration": duration,
    }
    if trace_id:
        condition["traceId"] = trace_id
    body = _post_graphql({"query": QUERY_TRACES, "variables": {"condition": condition}})
    traces = ((body.get("data") or {}).get("queryTraces") or {}).get("traces") or []
    spans: list[dict[str, Any]] = []
    for item in traces:
        if isinstance(item, dict):
            spans.extend(item.get("spans") or [])
    return spans


def fetch_trace(trace_id: str, *, lookback_hours: int = 24) -> dict[str, Any]:
    root = normalize_trace_id(trace_id)
    full = _full_sw_id(trace_id)
    duration = query_duration(lookback_hours)
    # BanyanDB / OAP 10.4 的 queryTraces 要完整 UI ID；只传 32 位根段会空结果
    candidates = []
    if full and full != root:
        candidates.append(full)
    candidates.append(root)
    spans: list[dict[str, Any]] = []
    used_id = root
    for candidate in candidates:
        spans = _query_traces(duration, trace_id=candidate)
        if spans:
            used_id = candidate
            break
    if not spans:
        # 信封只有 32 位根 ID 时：扫最近窗口，按前缀回配完整串
        scanned = _query_traces(duration, trace_id=None, page_size=50)
        matched = [
            span
            for span in scanned
            if isinstance(span, dict) and str(span.get("traceId") or "").startswith(root)
        ]
        if matched:
            used_id = str(matched[0].get("traceId") or root)
            spans = _query_traces(duration, trace_id=used_id) or matched

    dumps: list[TraceSpanDump] = []
    services: list[str] = []
    seen_svc: set[str] = set()
    min_start: int | None = None
    max_end: int | None = None
    errors = 0
    for span in spans[:80]:
        if not isinstance(span, dict):
            continue
        start = int(span.get("startTime") or 0)
        end = int(span.get("endTime") or start)
        if start:
            min_start = start if min_start is None else min(min_start, start)
        if end:
            max_end = end if max_end is None else max(max_end, end)
        svc = str(span.get("serviceCode") or "")
        if svc and svc not in seen_svc:
            seen_svc.add(svc)
            services.append(svc)
        is_error = bool(span.get("isError"))
        if is_error:
            errors += 1
        dumps.append(
            TraceSpanDump(
                service=svc,
                endpoint=str(span.get("endpointName") or ""),
                type=str(span.get("type") or ""),
                peer=str(span.get("peer") or ""),
                is_error=is_error,
                duration_ms=max(0, end - start),
            )
        )

    duration_ms = 0
    if min_start is not None and max_end is not None:
        duration_ms = max(0, max_end - min_start)

    note = ""
    if not dumps:
        note = (
            "OAP 未返回 Span。确认 ID 是 32 位根段、时间窗覆盖该请求，"
            "且不要用 queryBasicTraces（BanyanDB 已拒收 v1）。"
        )
    return model_dump_compact(
        TraceDump(
            trace_id=root,
            queried_id=trace_id,
            oap_trace_id=used_id,
            span_count=len(dumps),
            error_spans=errors,
            services=services,
            duration_ms=duration_ms,
            spans=dumps,
            note=note,
        )
    )


def _loki_query_range(
    query: str, *, start_ns: int, end_ns: int, limit: int
) -> dict[str, Any]:
    url = f"{config.LOKI_URL.rstrip('/')}/loki/api/v1/query_range"
    try:
        with httpx.Client(timeout=config.OBS_TIMEOUT_S) as client:
            response = client.get(
                url,
                params={
                    "query": query,
                    "start": str(start_ns),
                    "end": str(end_ns),
                    "limit": str(limit),
                    "direction": "forward",
                },
            )
            response.raise_for_status()
            body = response.json()
    except httpx.HTTPStatusError as exc:
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"Loki HTTP {exc.response.status_code}: {(exc.response.text or '')[:240]}",
            detail={"url": url, "query": query},
        ) from exc
    except Exception as exc:  # noqa: BLE001
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"Loki 不可达：{exc}",
            detail={"url": url},
        ) from exc
    if not isinstance(body, dict):
        raise OpsToolError(OPS_OBSERVABILITY_UNAVAILABLE, "Loki 返回非 JSON 对象")
    status = body.get("status")
    if status and status != "success":
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"Loki 查询失败：{body.get('error') or status}",
            detail={"status": status},
        )
    return body


def fetch_logs_by_trace(
    trace_id: str,
    *,
    lookback_hours: int = 24,
    limit: int | None = None,
    service_selector: str | None = None,
) -> dict[str, Any]:
    root = normalize_trace_id(trace_id)
    cap = max(1, min(int(limit or config.LOKI_LOG_LIMIT), 200))
    selector = _loki_selector(service_selector or config.LOKI_SERVICE_SELECTOR or DEFAULT_SERVICE_SELECTOR)
    query = f'{selector} |= "{root}"'
    hours = max(1, min(int(lookback_hours or 24), 168))
    end = _shanghai_now()
    start = end - timedelta(hours=hours)
    body = _loki_query_range(
        query,
        start_ns=int(start.timestamp() * 1_000_000_000),
        end_ns=int(end.timestamp() * 1_000_000_000),
        limit=cap,
    )
    streams = ((body.get("data") or {}).get("result") or [])
    hits: list[LokiLogHit] = []
    for stream in streams:
        if not isinstance(stream, dict):
            continue
        labels = stream.get("stream") or {}
        service = str(labels.get("service") or "")
        level = str(labels.get("level") or "")
        for pair in stream.get("values") or []:
            if not isinstance(pair, (list, tuple)) or len(pair) < 2:
                continue
            ns_raw, line = pair[0], str(pair[1])
            ts = ""
            try:
                ts = datetime.fromtimestamp(int(ns_raw) / 1_000_000_000, tz=_CST).strftime(
                    "%Y-%m-%d %H:%M:%S"
                )
            except (TypeError, ValueError, OSError):
                ts = str(ns_raw)
            hits.append(
                LokiLogHit(
                    service=service,
                    level=level,
                    ts=ts,
                    line=line[:800],
                )
            )
            if len(hits) >= cap:
                break
        if len(hits) >= cap:
            break

    note = ""
    if not hits:
        note = (
            "Loki 无命中。只搜 32 位根 ID，不要带 .segment；"
            "不要加 level=INFO；时间窗要对上 logback 北京时间。"
        )
    return model_dump_compact(
        LokiLogDump(
            trace_id=root,
            queried_id=trace_id,
            query=query,
            hit_count=len(hits),
            truncated=len(hits) >= cap,
            hits=hits,
            note=note,
        )
    )
