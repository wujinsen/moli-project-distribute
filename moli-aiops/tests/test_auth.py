"""Shiro 入站鉴权中间件测试。"""

from __future__ import annotations

import pytest
from httpx import ASGITransport, AsyncClient

from aiops_agent import config as agent_config
from aiops_agent.auth import AuthContext, has_perm, required_perm, validate_token
from aiops_agent.server import app


def test_required_perm_mapping() -> None:
    assert required_perm("GET", "/health") is None
    assert required_perm("GET", "/trace") is None
    assert required_perm("GET", "/trace/") is None
    assert required_perm("GET", "/trace/inspect") == "operation:aiops:list"
    assert required_perm("POST", "/diagnose") == "operation:aiops:diagnose"
    assert required_perm("GET", "/runs/run-abc/stream") == "operation:aiops:list"
    assert required_perm("POST", "/runs/run-abc/approve") == "operation:aiops:approve"
    assert required_perm("POST", "/runs/run-abc/reject") == "operation:aiops:approve"


def test_has_perm_full() -> None:
    ctx = AuthContext(permissions=frozenset(), full_permission=True)
    assert has_perm(ctx, "operation:aiops:diagnose")


@pytest.mark.asyncio
async def test_health_ok_without_auth_when_enabled(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.get("/health")
    assert resp.status_code == 200
    assert resp.json()["ok"] is True
    assert "inventory_targets" in resp.json()


@pytest.mark.asyncio
async def test_health_ok_with_valid_token(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)

    async def fake_validate(token: str) -> AuthContext | None:
        assert token == "login_token_test"
        return AuthContext(
            permissions=frozenset(["operation:aiops:list"]),
            full_permission=False,
        )

    monkeypatch.setattr("aiops_agent.auth.validate_token", fake_validate)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.get("/health", headers={"Authorization": "login_token_test"})
    assert resp.status_code == 200
    assert resp.json()["ok"] is True


@pytest.mark.asyncio
async def test_diagnose_forbidden_without_perm(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)

    async def fake_validate(token: str) -> AuthContext | None:
        return AuthContext(
            permissions=frozenset(["operation:aiops:list"]),
            full_permission=False,
        )

    monkeypatch.setattr("aiops_agent.auth.validate_token", fake_validate)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.post(
            "/diagnose",
            headers={"Authorization": "login_token_test"},
            json={"title": "t", "target": "sandbox-app"},
        )
    assert resp.status_code == 403


@pytest.mark.asyncio
async def test_trace_inspect_forbidden_without_list(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)

    async def fake_validate(token: str) -> AuthContext | None:
        return AuthContext(permissions=frozenset(["operation:aiops:diagnose"]), full_permission=False)

    monkeypatch.setattr("aiops_agent.auth.validate_token", fake_validate)
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.get(
            "/trace/inspect",
            params={"trace_id": "13eef851b9434faa8dfcadbeeaa924a7"},
            headers={"Authorization": "login_token_test"},
        )
    assert resp.status_code == 403


@pytest.mark.asyncio
async def test_trace_inspect_ok_with_list_perm(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)

    async def fake_validate(token: str) -> AuthContext | None:
        return AuthContext(permissions=frozenset(["operation:aiops:list"]), full_permission=False)

    monkeypatch.setattr("aiops_agent.auth.validate_token", fake_validate)
    monkeypatch.setattr(
        "aiops_agent.server.inspect_trace",
        lambda trace_id, lookback_hours=24: {
            "ok": True,
            "root_trace_id": "13eef851b9434faa8dfcadbeeaa924a7",
            "queried_id": trace_id,
            "links": {"grafana_trace_logs": "http://g", "skywalking_ui": "http://s", "ops_console": "http://o"},
        },
    )
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.get(
            "/trace/inspect",
            params={"trace_id": "13eef851b9434faa8dfcadbeeaa924a7"},
            headers={"Authorization": "login_token_test"},
        )
    assert resp.status_code == 200
    assert resp.json()["ok"] is True


@pytest.mark.asyncio
async def test_trace_page_ok_without_auth(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.get("/trace")
    assert resp.status_code == 200
    assert "Trace 排障" in resp.text


@pytest.mark.asyncio
async def test_validate_token_parses_capabilities(monkeypatch: pytest.MonkeyPatch) -> None:
    class FakeResp:
        status_code = 200

        @staticmethod
        def json() -> dict:
            return {
                "code": 200,
                "data": {
                    "permissions": ["operation:aiops:list"],
                    "fullPermission": False,
                },
            }

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):  # noqa: ANN002
            return None

        async def get(self, url: str, headers: dict):  # noqa: ARG002
            return FakeResp()

    monkeypatch.setattr("aiops_agent.auth.httpx.AsyncClient", lambda **kwargs: FakeClient())

    ctx = await validate_token("login_token_x")
    assert ctx is not None
    assert "operation:aiops:list" in ctx.permissions
