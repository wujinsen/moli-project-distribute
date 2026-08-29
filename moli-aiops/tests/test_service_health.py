"""服务存活判定测试。

这一层的价值全在判定逻辑上：容器化部署常常没有 systemd，纯进程型服务可能不声明
端口，判定必须在信息残缺时也给出有用结论，而不是一律 unknown。
"""

from __future__ import annotations

import pytest

from ops_mcp.cmdb.base import InventoryEntry
from ops_mcp.evidence import service as service_mod
from ops_mcp.evidence.ssh import CommandOutput, SshTarget
from ops_mcp.schemas import HostFacts, PortFacts, ProcessFacts

SERVICE = "moli-gateway"


def _entry(**service_overrides) -> InventoryEntry:
    spec = {"name": SERVICE, "port": 8080, "systemd_unit": SERVICE}
    spec.update(service_overrides)
    return InventoryEntry(
        {
            "id": "sandbox-app",
            "server_id": "1",
            "host": "127.0.0.1",
            "ssh_port": 2201,
            "user": "root",
            "password": "aiops",
            "services": [spec],
        }
    )


def _facts(*, ports: list[int], processes: list[str] | None = None) -> HostFacts:
    return HostFacts(
        server_id="1",
        host="127.0.0.1",
        collected_at="2026-08-18T21:00:00",
        listen_ports=[PortFacts(proto="tcp", local_addr="0.0.0.0", port=p, pid=100 + p)
                      for p in ports],
        top_processes=[ProcessFacts(pid=200 + i, command=c)
                       for i, c in enumerate(processes or [])],
    )


class StubPool:
    """只回放 systemctl is-active 的输出，不碰网络。"""

    def __init__(self, state: str = "active", *, explode: bool = False) -> None:
        self.state = state
        self.explode = explode
        self.commands: list[str] = []

    def run(self, target, command, *, timeout=None):  # noqa: ANN001, ARG002
        self.commands.append(command)
        if self.explode:
            raise OSError("ssh 掉线")
        lines = []
        for line in command.split("; "):
            if line.startswith("echo '###MOLI:"):
                lines.append(line[len("echo '"):-1])
                lines.append(self.state)
        return CommandOutput(exit_code=0, stdout="\n".join(lines), stderr="",
                             duration_ms=2, truncated=False)


def _check(entry, facts, pool):
    return service_mod.check(entry, facts, pool, SshTarget(id="sandbox-app", host="127.0.0.1"))


def test_port_listening_and_unit_active_is_up() -> None:
    result = _check(_entry(), _facts(ports=[22, 8080]), StubPool("active"))
    assert len(result) == 1
    assert result[0].verdict == "up"
    assert result[0].port_listening is True


def test_port_missing_is_down_even_if_unit_claims_active() -> None:
    """unit 说 active 但端口没起来，属于「进程活着但没干活」，仍要当故障处理。"""
    result = _check(_entry(), _facts(ports=[22]), StubPool("active"))
    assert result[0].verdict == "down"
    assert "无监听" in result[0].reason


def test_unit_inactive_is_down() -> None:
    result = _check(_entry(), _facts(ports=[22]), StubPool("inactive"))
    assert result[0].verdict == "down"
    assert "inactive" in result[0].reason


def test_container_service_without_systemd_still_judged_by_port() -> None:
    """容器化部署没有 systemd unit，判定必须只靠端口，不能退化成 unknown。"""
    entry = _entry(systemd_unit="", container="moli-gateway")
    up = _check(entry, _facts(ports=[8080]), StubPool())
    down = _check(entry, _facts(ports=[22]), StubPool())
    assert up[0].verdict == "up"
    assert down[0].verdict == "down"


def test_service_without_port_falls_back_to_unit_state() -> None:
    entry = _entry(port=None)
    assert _check(entry, _facts(ports=[]), StubPool("active"))[0].verdict == "up"
    assert _check(entry, _facts(ports=[]), StubPool("failed"))[0].verdict == "down"


def test_no_port_and_no_unit_is_unknown_not_a_false_ok() -> None:
    """既没端口也没 unit 时必须承认判不了，不能默认当成健康。"""
    entry = _entry(port=None, systemd_unit="")
    result = _check(entry, _facts(ports=[]), StubPool())
    assert result[0].verdict == "unknown"
    assert "无法判定" in result[0].reason


def test_unit_probe_failure_still_yields_port_verdict() -> None:
    """SSH 探 unit 失败不能让整个检查报废，端口证据仍然有效。

    关键是别把「探不到」当成「不健康」：unit 状态未知时它不参与判定，
    结论只由端口给出。
    """
    up = _check(_entry(), _facts(ports=[8080]), StubPool(explode=True))
    assert up[0].unit_state == "unknown"
    assert up[0].verdict == "up"

    down = _check(_entry(), _facts(ports=[22]), StubPool(explode=True))
    assert down[0].verdict == "down"


def test_unit_name_with_shell_metacharacters_is_not_probed() -> None:
    """inventory 里写了危险字符时不能拼进命令。"""
    entry = _entry(systemd_unit="evil; rm -rf /")
    pool = StubPool()
    result = _check(entry, _facts(ports=[8080]), pool)
    assert pool.commands == [], "不合法的 unit 名不该产生任何 SSH 命令"
    assert result[0].verdict == "up"


def test_only_declared_services_can_be_filtered() -> None:
    entry = _entry()
    assert _check_filtered(entry, ["other"]) == []
    assert len(_check_filtered(entry, [SERVICE])) == 1


def _check_filtered(entry, services):
    return service_mod.check(
        entry, _facts(ports=[8080]), StubPool(),
        SshTarget(id="sandbox-app", host="127.0.0.1"), services=services,
    )


def test_process_matches_counts_declared_names() -> None:
    facts = _facts(ports=[8080], processes=["python3 /opt/moli/services/moli-gateway", "sshd"])
    result = _check(_entry(), facts, StubPool())
    assert result[0].process_matches == 1


@pytest.mark.parametrize("state", ["active", "inactive", "failed", "unknown"])
def test_unit_state_is_carried_through(state: str) -> None:
    result = _check(_entry(), _facts(ports=[8080]), StubPool(state))
    assert result[0].unit_state == state
