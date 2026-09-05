"""SkyWalking / Loki 只读取证：ID 归一化 + HTTP 客户端（全部 mock）。"""

from __future__ import annotations

import re

import pytest

from ops_mcp.errors import OPS_INVALID_INPUT, OPS_OBSERVABILITY_UNAVAILABLE, OpsToolError
from ops_mcp.evidence.observability import (
    extract_trace_id,
    fetch_logs_by_trace,
    fetch_trace,
    normalize_trace_id,
    query_duration,
)


ROOT = "13eef851b9434faa8dfcadbeeaa924a7"


def test_normalize_strips_tid_prefix_and_segment_suffix() -> None:
    assert normalize_trace_id(f"TID:{ROOT}.128.17882712296580005") == ROOT
    assert normalize_trace_id(f"{ROOT}.128.1") == ROOT
    assert normalize_trace_id(ROOT.upper()) == ROOT


def test_normalize_rejects_garbage() -> None:
    with pytest.raises(OpsToolError) as exc:
        normalize_trace_id("not-a-trace")
    assert exc.value.code == OPS_INVALID_INPUT


def test_extract_trace_id_from_alert_fields() -> None:
    assert extract_trace_id({"trace_id": f"TID:{ROOT}.1.2"}) == ROOT
    assert extract_trace_id({"labels": {"traceId": ROOT}}) == ROOT
    assert extract_trace_id({"description": f"报障 {ROOT} 请查"}) == ROOT
    assert extract_trace_id({"title": "网关 502"}) == ""


def test_query_duration_uses_shanghai_minute_format() -> None:
    duration = query_duration(24)
    assert duration["step"] == "MINUTE"
    assert re.fullmatch(r"\d{4}-\d{2}-\d{2} \d{4}", duration["start"])
    assert re.fullmatch(r"\d{4}-\d{2}-\d{2} \d{4}", duration["end"])


def test_fetch_trace_posts_query_traces(monkeypatch) -> None:
    captured: dict = {}

    class FakeResp:
        def raise_for_status(self) -> None:
            return None

        def json(self) -> dict:
            return {
                "data": {
                    "queryTraces": {
                        "traces": [
                            {
                                "spans": [
                                    {
                                        "serviceCode": "knowledge-server",
                                        "endpointName": "GET:/kb/index",
                                        "type": "Entry",
                                        "isError": True,
                                        "startTime": 1000,
                                        "endTime": 1042,
                                        "peer": "",
                                    }
                                ]
                            }
                        ]
                    }
                }
            }

    class FakeClient:
        def __init__(self, *args, **kwargs):  # noqa: ARG002
            pass

        def __enter__(self):
            return self

        def __exit__(self, *args):  # noqa: ARG002
            return False

        def post(self, url, json=None, headers=None):  # noqa: ARG002
            captured["url"] = url
            captured["json"] = json
            return FakeResp()

    monkeypatch.setattr("ops_mcp.evidence.observability.httpx.Client", FakeClient)
    dump = fetch_trace(f"TID:{ROOT}.128.1", lookback_hours=6)
    assert dump["trace_id"] == ROOT
    assert dump["oap_trace_id"] == f"{ROOT}.128.1"
    assert dump["span_count"] == 1
    assert dump["error_spans"] == 1
    assert dump["services"] == ["knowledge-server"]
    query = captured["json"]["query"]
    assert "queryTraces" in query
    assert "queryBasicTraces" not in query
    # OAP 要完整 UI ID；根段单独查会空
    assert captured["json"]["variables"]["condition"]["traceId"] == f"{ROOT}.128.1"


def test_fetch_trace_maps_oap_down(monkeypatch) -> None:
    class BoomClient:
        def __init__(self, *args, **kwargs):  # noqa: ARG002
            pass

        def __enter__(self):
            return self

        def __exit__(self, *args):  # noqa: ARG002
            return False

        def post(self, *args, **kwargs):  # noqa: ARG002
            raise ConnectionError("refused")

    monkeypatch.setattr("ops_mcp.evidence.observability.httpx.Client", BoomClient)
    with pytest.raises(OpsToolError) as exc:
        fetch_trace(ROOT)
    assert exc.value.code == OPS_OBSERVABILITY_UNAVAILABLE
    assert exc.value.retryable is True


def test_fetch_logs_by_trace_uses_body_filter(monkeypatch) -> None:
    captured: dict = {}

    class FakeResp:
        def raise_for_status(self) -> None:
            return None

        def json(self) -> dict:
            return {
                "status": "success",
                "data": {
                    "resultType": "streams",
                    "result": [
                        {
                            "stream": {"service": "knowledge-server"},
                            "values": [
                                ["1756861200000000000", f"trace_id=TID:{ROOT}.1.1 ERROR boom"]
                            ],
                        }
                    ],
                },
            }

    class FakeClient:
        def __init__(self, *args, **kwargs):  # noqa: ARG002
            pass

        def __enter__(self):
            return self

        def __exit__(self, *args):  # noqa: ARG002
            return False

        def get(self, url, params=None):  # noqa: ARG002
            captured["url"] = url
            captured["params"] = params
            return FakeResp()

    monkeypatch.setattr("ops_mcp.evidence.observability.httpx.Client", FakeClient)
    dump = fetch_logs_by_trace(f"{ROOT}.128.9", lookback_hours=1, limit=10)
    assert dump["trace_id"] == ROOT
    assert dump["hit_count"] == 1
    assert ROOT in dump["query"]
    assert 'level=' not in dump["query"]
    assert dump["hits"][0]["service"] == "knowledge-server"
    assert captured["params"]["query"].endswith(f'|= "{ROOT}"')
