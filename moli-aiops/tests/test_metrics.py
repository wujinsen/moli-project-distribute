"""Prometheus 只读查询（HTTP mock）。"""

from __future__ import annotations

import pytest

from ops_mcp.errors import OPS_INVALID_INPUT, OpsToolError
from ops_mcp.evidence.metrics import instant_query


def test_preset_requires_service() -> None:
    with pytest.raises(OpsToolError) as exc:
        instant_query(preset="up")
    assert exc.value.code == OPS_INVALID_INPUT


def test_instant_query_parses_vector(monkeypatch) -> None:
    class FakeResp:
        def raise_for_status(self) -> None:
            return None

        def json(self) -> dict:
            return {
                "status": "success",
                "data": {
                    "resultType": "vector",
                    "result": [{"metric": {"service": "knowledge-server"}, "value": [1, "1"]}],
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
            assert "up{service=" in params["query"]
            return FakeResp()

    monkeypatch.setattr("ops_mcp.evidence.metrics.httpx.Client", FakeClient)
    dump = instant_query(service="knowledge-server", preset="up")
    assert dump["sample_count"] == 1
    assert dump["samples"][0]["value"] == "1"
