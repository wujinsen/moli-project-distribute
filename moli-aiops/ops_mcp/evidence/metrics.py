"""只读查 Prometheus。给诊断补「告警前后指标窗口」，不替代 Grafana。"""

from __future__ import annotations

from typing import Any
from urllib.parse import urljoin

import httpx

from .. import config
from ..errors import OPS_INVALID_INPUT, OPS_OBSERVABILITY_UNAVAILABLE, OpsToolError

PRESETS: dict[str, str] = {
    "up": 'up{{service="{service}"}}',
    "http_5xx_ratio": (
        "sum(rate(http_server_requests_seconds_count"
        '{{service="{service}",status=~"5.."}}[5m]))'
        " / clamp_min(sum(rate(http_server_requests_seconds_count"
        '{{service="{service}"}}[5m])), 0.001)'
    ),
    "heap_ratio": (
        'sum(jvm_memory_used_bytes{{service="{service}",area="heap"}})'
        ' / clamp_min(sum(jvm_memory_max_bytes{{service="{service}",area="heap"}}), 1)'
    ),
    "cpu": 'process_cpu_usage{{service="{service}"}}',
}


def _query_expr(preset: str, service: str, query: str) -> str:
    if query.strip():
        if len(query) > 800:
            raise OpsToolError(OPS_INVALID_INPUT, "PromQL 过长")
        return query.strip()
    name = (preset or "up").strip()
    if name not in PRESETS:
        raise OpsToolError(
            OPS_INVALID_INPUT,
            f"未知 preset={name}",
            detail={"known": sorted(PRESETS)},
        )
    svc = (service or "").strip()
    if not svc:
        raise OpsToolError(OPS_INVALID_INPUT, "preset 查询必须带 service")
    return PRESETS[name].format(service=svc)


def instant_query(
    *,
    service: str = "",
    preset: str = "up",
    query: str = "",
) -> dict[str, Any]:
    expr = _query_expr(preset, service, query)
    url = urljoin(config.PROMETHEUS_URL.rstrip("/") + "/", "api/v1/query")
    try:
        with httpx.Client(timeout=config.OBS_TIMEOUT_S) as client:
            response = client.get(url, params={"query": expr})
            response.raise_for_status()
            body = response.json()
    except httpx.HTTPStatusError as exc:
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"Prometheus HTTP {exc.response.status_code}: {(exc.response.text or '')[:200]}",
            detail={"url": url},
        ) from exc
    except Exception as exc:  # noqa: BLE001
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"Prometheus 不可达：{exc}",
            detail={"url": url},
        ) from exc
    if not isinstance(body, dict) or body.get("status") != "success":
        raise OpsToolError(
            OPS_OBSERVABILITY_UNAVAILABLE,
            f"Prometheus 查询失败：{(body or {}).get('error') or body}",
        )
    results = ((body.get("data") or {}).get("result") or [])[:20]
    samples: list[dict[str, Any]] = []
    for item in results:
        if not isinstance(item, dict):
            continue
        value = item.get("value") or []
        samples.append(
            {
                "metric": item.get("metric") or {},
                "value": value[1] if len(value) > 1 else None,
            }
        )
    return {
        "query": expr,
        "preset": preset if not query.strip() else "",
        "sample_count": len(samples),
        "samples": samples,
        "note": "" if samples else "无样本。确认 Prometheus 在抓该服务，或改用 query= 原始 PromQL。",
    }
