"""处置执行闸门的集成测试。

覆盖人机协同链路上真正会出事的几种情况：熔断开关被绕过、审批令牌被挪用到
另一条命令、令牌被重放、破坏性命令靠审批闯关。
"""

from __future__ import annotations

import time

import pytest

from ops_mcp import config
from ops_mcp.actions import remediate
from ops_mcp.cmdb.base import InventoryEntry
from ops_mcp.errors import (
    OPS_APPROVAL_REQUIRED,
    OPS_COMMAND_BLOCKED,
    OPS_EXEC_DISABLED,
    OpsToolError,
)
from ops_mcp.evidence.ssh import CommandOutput
from ops_mcp.safety import approval
from ops_mcp.schemas import RiskLevel


class FakePool:
    """记录被真正执行的命令，用来断言「不该执行的一条都没跑」。"""

    def __init__(self) -> None:
        self.executed: list[str] = []

    def run(self, target, command, *, timeout=None) -> CommandOutput:  # noqa: ANN001, ARG002
        self.executed.append(command)
        return CommandOutput(exit_code=0, stdout="ok", stderr="", duration_ms=5, truncated=False)


@pytest.fixture
def entry() -> InventoryEntry:
    return InventoryEntry(
        {
            "id": "sandbox-app",
            "server_id": "1",
            "name": "沙箱应用节点",
            "host": "127.0.0.1",
            "ssh_port": 2201,
            "user": "root",
            "password": "aiops",
            "services": [
                {"name": "moli-gateway", "port": 8080, "systemd_unit": "moli-gateway"},
            ],
        }
    )


@pytest.fixture
def pool() -> FakePool:
    return FakePool()


@pytest.fixture(autouse=True)
def _clean_approvals():
    approval.reset_consumed()
    yield
    approval.reset_consumed()


@pytest.fixture
def exec_enabled(monkeypatch):
    monkeypatch.setattr(config, "EXEC_ENABLED", True)


def _token(entry: InventoryEntry, command: str, **kwargs) -> str:
    return approval.issue(
        host=entry.host, command=command, risk="mutating",
        incident_id="inc-1", step_id="s1", approver="wujinsen", **kwargs,
    )["token"]


def test_read_only_runs_without_approval_even_when_kill_switch_off(entry, pool, monkeypatch):
    """熔断开关只卡变更类命令。取证在事故期间必须始终可用。"""
    monkeypatch.setattr(config, "EXEC_ENABLED", False)
    result = remediate.execute(entry, pool, command="df -h")
    assert result.risk is RiskLevel.READ_ONLY
    assert pool.executed == ["df -h"]


def test_mutating_blocked_when_kill_switch_off(entry, pool, monkeypatch):
    monkeypatch.setattr(config, "EXEC_ENABLED", False)
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command="systemctl restart moli-gateway")
    assert exc.value.code == OPS_EXEC_DISABLED
    assert pool.executed == []


def test_mutating_requires_approval_token(entry, pool, exec_enabled):
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command="systemctl restart moli-gateway")
    assert exc.value.code == OPS_APPROVAL_REQUIRED
    assert pool.executed == []


def test_mutating_runs_with_valid_token(entry, pool, exec_enabled):
    command = "systemctl restart moli-gateway"
    result = remediate.execute(entry, pool, command=command, approval_token=_token(entry, command))
    assert result.exit_code == 0
    assert pool.executed == [command]


def test_token_is_bound_to_exact_command(entry, pool, exec_enabled):
    """拿到「重启网关」的批准后，不能改用它去执行别的命令。"""
    approved = "systemctl restart moli-gateway"
    token = _token(entry, approved)
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command="systemctl stop moli-mysql", approval_token=token)
    assert exc.value.code == OPS_APPROVAL_REQUIRED
    assert "不匹配" in exc.value.message
    assert pool.executed == []


def test_token_is_bound_to_host(entry, pool, exec_enabled):
    command = "systemctl restart moli-gateway"
    token = approval.issue(
        host="10.0.0.9", command=command, risk="mutating",
        incident_id="inc-1", step_id="s1", approver="wujinsen",
    )["token"]
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command=command, approval_token=token)
    assert exc.value.code == OPS_APPROVAL_REQUIRED
    assert pool.executed == []


def test_token_cannot_be_replayed(entry, pool, exec_enabled):
    command = "systemctl restart moli-gateway"
    token = _token(entry, command)
    remediate.execute(entry, pool, command=command, approval_token=token)
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command=command, approval_token=token)
    assert exc.value.code == OPS_APPROVAL_REQUIRED
    assert "重放" in exc.value.message
    assert pool.executed == [command]


def test_expired_token_rejected(entry, pool, exec_enabled):
    command = "systemctl restart moli-gateway"
    token = _token(entry, command, ttl_s=-1)
    time.sleep(0.01)
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command=command, approval_token=token)
    assert exc.value.code == OPS_APPROVAL_REQUIRED
    assert "过期" in exc.value.message
    assert pool.executed == []


def test_tampered_token_rejected(entry, pool, exec_enabled):
    command = "systemctl restart moli-gateway"
    token = _token(entry, command)
    payload, _, signature = token.rpartition(".")
    forged = f"{payload}.{'0' * len(signature)}"
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command=command, approval_token=forged)
    assert exc.value.code == OPS_APPROVAL_REQUIRED
    assert pool.executed == []


def test_destructive_blocked_even_with_token(entry, pool, exec_enabled, monkeypatch):
    monkeypatch.setattr(config, "ALLOW_DESTRUCTIVE", False)
    command = "rm -rf /opt/moli"
    token = _token(entry, command)
    with pytest.raises(OpsToolError) as exc:
        remediate.execute(entry, pool, command=command, approval_token=token)
    assert exc.value.code == OPS_COMMAND_BLOCKED
    assert pool.executed == []


def test_dry_run_never_touches_the_host(entry, pool, exec_enabled):
    result = remediate.execute(
        entry, pool, command="systemctl restart moli-gateway", dry_run=True
    )
    assert result.dry_run is True
    assert pool.executed == []


def test_service_command_is_derived_not_freeform(entry):
    from ops_mcp.cmdb.base import Inventory

    inventory = Inventory([entry])
    assert remediate.service_command(inventory, entry, "moli-gateway", "restart") == (
        "systemctl restart moli-gateway"
    )
    with pytest.raises(OpsToolError):
        remediate.service_command(inventory, entry, "moli-gateway", "rm -rf /")
