"""服务存活检查。

补的是主机指标覆盖不到的那一半：`ops_host_facts` 回答「机器累不累」，
但一台 CPU 2%、内存 20% 的机器上服务照样可能是停着的。对「服务不可用」这类
事故，决定性证据是「声明端口无监听 + 进程不在 + unit inactive」三件事对上，
而不是任何一条资源指标。

判定逻辑刻意做成确定性的：拿 inventory 里声明的 `port` / `systemd_unit` 与机器
实际状态逐项对照，得出 up / down / degraded。这类事实问题交给模型判断只会引入
不确定性，而且它没有「本该监听哪个端口」这个先验。
"""

from __future__ import annotations

import re

from ..cmdb.base import InventoryEntry, ServiceSpec
from ..schemas import HostFacts, ServiceHealth, ServiceVerdict
from .ssh import SshPool, SshTarget

_MARK = "###MOLI:"
# systemd unit 名允许的字符，用于挡住 inventory 里写进奇怪值时的命令注入
_SAFE_UNIT = re.compile(r"^[A-Za-z0-9._@:-]+$")


def _unit_probe_command(units: list[str]) -> str:
    parts: list[str] = []
    for unit in units:
        parts.append(f"echo '{_MARK}{unit}'")
        # is-active 在分级器里属只读子命令；unit 名已校验过字符集
        parts.append(f"systemctl is-active {unit} 2>/dev/null || echo unknown")
    return "; ".join(parts)


def _split_sections(raw: str) -> dict[str, str]:
    sections: dict[str, str] = {}
    current: str | None = None
    buf: list[str] = []
    for line in raw.splitlines():
        if line.startswith(_MARK):
            if current is not None:
                sections[current] = "\n".join(buf).strip()
            current = line[len(_MARK):].strip()
            buf = []
            continue
        buf.append(line)
    if current is not None:
        sections[current] = "\n".join(buf).strip()
    return sections


def _port_state(facts: HostFacts, port: int | None) -> tuple[bool | None, int | None]:
    if not port:
        return None, None
    for entry in facts.listen_ports:
        if entry.port == port:
            return True, entry.pid
    return False, None


def _process_matches(facts: HostFacts, spec: ServiceSpec) -> int | None:
    """在 top_processes 里数一下服务名出现几次。

    只是辅助信号：top_processes 只取了 CPU 前 15 名，数到 0 不等于进程不存在，
    所以它永远不单独作为判定依据。
    """
    needles = [n for n in (spec.name, spec.systemd_unit, spec.container) if n]
    if not needles:
        return None
    count = 0
    for proc in facts.top_processes:
        if any(n in proc.command for n in needles):
            count += 1
    return count


def _decide(health: ServiceHealth) -> ServiceHealth:
    """只用**读到了**的信号做判定，读不到的信号不参与。

    信号缺失和信号为负是两件事：容器化部署没有 systemd unit，纯进程型服务可能不
    声明端口，SSH 探测也可能失败。把「没读到」当成「不健康」会造成大量假告警，
    当成「健康」则更糟——会漏掉真故障。所以按已知信号判定，一个都没有就明确
    承认判不了。
    """
    reasons: list[str] = []
    signals: list[bool] = []

    if health.port_listening is True:
        signals.append(True)
        reasons.append(f"端口 {health.expected_port} 正在监听")
    elif health.port_listening is False:
        signals.append(False)
        reasons.append(f"声明端口 {health.expected_port} 无监听")

    if health.unit_state == "active":
        signals.append(True)
        reasons.append(f"unit {health.unit} active")
    elif health.unit_state in {"inactive", "failed"}:
        signals.append(False)
        reasons.append(f"systemd unit {health.unit} 状态 {health.unit_state}")
    elif health.unit:
        reasons.append(f"unit {health.unit} 状态未知，不参与判定")

    if not signals:
        health.verdict = ServiceVerdict.UNKNOWN
        reasons.append("没有任何可用信号，无法判定")
    elif all(signals):
        health.verdict = ServiceVerdict.UP
    else:
        # 任一已知信号为负即判故障。端口在听但 unit 已停是典型的端口被占场景，
        # 那同样是故障而不是「部分健康」。
        health.verdict = ServiceVerdict.DOWN

    health.reason = "；".join(reasons)
    return health


def check(
    entry: InventoryEntry,
    facts: HostFacts,
    pool: SshPool,
    target: SshTarget,
    *,
    services: list[str] | None = None,
    timeout: float = 15.0,
) -> list[ServiceHealth]:
    """对照 inventory 声明与实际状态，逐个服务给出判定。

    端口信息直接复用已采集的 HostFacts，不重复跑 ss；只有 systemd unit 状态
    需要额外一次 SSH 往返，且所有 unit 合并成一条命令。
    """
    wanted = [s for s in entry.services if not services or s.name in services]
    if not wanted:
        return []

    units = [s.systemd_unit for s in wanted if s.systemd_unit and _SAFE_UNIT.match(s.systemd_unit)]
    unit_states: dict[str, str] = {}
    if units:
        try:
            result = pool.run(target, _unit_probe_command(units), timeout=timeout)
            unit_states = {k: v.splitlines()[0].strip() if v else "unknown"
                           for k, v in _split_sections(result.stdout).items()}
        except Exception:  # noqa: BLE001
            # unit 状态拿不到不影响端口判定，残缺证据也比没有强
            unit_states = {}

    out: list[ServiceHealth] = []
    for spec in wanted:
        listening, pid = _port_state(facts, spec.port)
        state = ""
        if spec.systemd_unit:
            state = unit_states.get(spec.systemd_unit, "unknown")
        out.append(
            _decide(
                ServiceHealth(
                    service=spec.name,
                    target=entry.id,
                    expected_port=spec.port,
                    port_listening=listening,
                    listening_pid=pid,
                    unit=spec.systemd_unit,
                    unit_state=state,
                    process_matches=_process_matches(facts, spec),
                )
            )
        )
    return out
