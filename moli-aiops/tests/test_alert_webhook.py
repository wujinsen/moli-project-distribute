"""Alertmanager webhook：解析、鉴权、去重。不跑完整诊断图。"""

from __future__ import annotations

import pytest
from httpx import ASGITransport, AsyncClient

from aiops_agent import alert_webhook
from aiops_agent import config as agent_config
from aiops_agent.models import Alert
from aiops_agent.server import app
from ops_mcp.cmdb.base import Inventory, InventoryEntry


AM_FIRING = {
    "status": "firing",
    "alerts": [
        {
            "status": "firing",
            "labels": {
                "alertname": "MoliServiceDown",
                "service": "knowledge-server",
                "instance": "host.docker.internal:28104",
                "severity": "critical",
            },
            "annotations": {
                "summary": "knowledge-server Prometheus 抓取失败",
                "description": "已连续 2 分钟 up=0",
            },
            "startsAt": "2026-09-03T10:00:00Z",
            "generatorURL": "http://prometheus/graph",
        }
    ],
}


def test_parse_firing_alert_maps_service_and_source() -> None:
    alerts = alert_webhook.alerts_from_payload(AM_FIRING, inventory=None)
    assert len(alerts) == 1
    alert = alerts[0]
    assert alert.source == "webhook"
    assert alert.service == "knowledge-server"
    assert alert.target == ""
    assert "MoliServiceDown" in alert.labels["alertname"]
    assert "up=0" in alert.description


def test_resolved_webhook_is_ignored() -> None:
    payload = {**AM_FIRING, "status": "resolved"}
    payload["alerts"] = [{**AM_FIRING["alerts"][0], "status": "resolved"}]
    assert alert_webhook.alerts_from_payload(payload) == []


def test_resolve_target_uses_inventory_alias() -> None:
    entry = InventoryEntry(
        {
            "id": "local-app",
            "host": "127.0.0.1",
            "services": [{"name": "moli-knowledge", "port": 28104}],
        }
    )
    inventory = Inventory([entry])
    assert alert_webhook.resolve_target("knowledge-server", inventory) == "local-app"


def test_dedup_blocks_same_fingerprint() -> None:
    alert_webhook.reset_dedup()
    fp = alert_webhook.fingerprint({"alertname": "MoliServiceDown", "service": "x"})
    assert alert_webhook.should_skip_duplicate(fp, now=1000, ttl_s=600) is False
    assert alert_webhook.should_skip_duplicate(fp, now=1100, ttl_s=600) is True
    assert alert_webhook.should_skip_duplicate(fp, now=2000, ttl_s=600) is False


@pytest.mark.asyncio
async def test_webhook_rejects_when_secret_missing(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)
    monkeypatch.setattr(agent_config, "ALERT_WEBHOOK_SECRET", "")
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.post("/hooks/alertmanager", json=AM_FIRING)
    assert resp.status_code == 503


@pytest.mark.asyncio
async def test_webhook_rejects_bad_token(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)
    monkeypatch.setattr(agent_config, "ALERT_WEBHOOK_SECRET", "correct-secret")
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        resp = await client.post(
            "/hooks/alertmanager",
            json=AM_FIRING,
            headers={"Authorization": "Bearer wrong"},
        )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_webhook_starts_diagnose_once(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(agent_config, "AUTH_ENABLED", True)
    monkeypatch.setattr(agent_config, "ALERT_WEBHOOK_SECRET", "correct-secret")
    monkeypatch.setattr(agent_config, "ALERT_DEDUP_TTL_S", 600)
    alert_webhook.reset_dedup()
    started: list[Alert] = []

    def fake_start(alert: Alert) -> dict[str, str]:
        started.append(alert)
        return {"run_id": "run-am-1", "incident_id": "inc-am-1"}

    monkeypatch.setattr("aiops_agent.server._start_diagnose", fake_start)
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        first = await client.post(
            "/hooks/alertmanager",
            json=AM_FIRING,
            headers={"Authorization": "Bearer correct-secret"},
        )
        second = await client.post(
            "/hooks/alertmanager",
            json=AM_FIRING,
            headers={"Authorization": "Bearer correct-secret"},
        )
    assert first.status_code == 200
    assert first.json()["started"][0]["run_id"] == "run-am-1"
    assert second.json()["skipped"]
    assert len(started) == 1
    assert started[0].source == "webhook"
