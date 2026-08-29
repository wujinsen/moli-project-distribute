"""主机指标采集（SSH facts）。

填的是现有健康巡检最大的窟窿：巡检只做 TCP 端口可达性，且只存最新一条，
诊断 Agent 光靠「端口通不通」推不出根因。

两个设计取舍：

- **读 /proc 而不是解析 top/free 的文本**：`top` 的输出格式随发行版和版本变化，
  `/proc/stat`、`/proc/meminfo` 是稳定契约。CPU 利用率靠两次采样求差得到，
  比 top 首屏那个「自启动以来平均值」有意义。
- **一次往返取完**：用分节标记把七类证据拼成一条命令，避免七次 SSH 往返。
  单项失败记进 partial_errors 但不中断，残缺证据也比没有强。
"""

from __future__ import annotations

import re
import time
from typing import Any

from ..schemas import (
    CpuFacts,
    DiskFacts,
    HostFacts,
    MemoryFacts,
    PortFacts,
    ProcessFacts,
)
from .ssh import SshPool, SshTarget

# 分节标记。取证命令由代码固定拼装，不接受任何外部字符串，天然免注入
_MARK = "###MOLI:"

_SECTIONS: tuple[tuple[str, str], ...] = (
    ("loadavg", "cat /proc/loadavg"),
    ("uptime", "cat /proc/uptime"),
    ("nproc", "nproc"),
    ("stat1", "head -1 /proc/stat"),
    ("_sleep", "sleep 0.4"),
    ("stat2", "head -1 /proc/stat"),
    ("meminfo", "cat /proc/meminfo"),
    ("df", "df -PTk"),
    ("dfi", "df -PTki"),
    ("ps", "ps -eo pid,user:20,pcpu,pmem,rss,args --sort=-pcpu 2>/dev/null | head -n 16"),
    ("ports", "ss -lntp 2>/dev/null || netstat -lntp 2>/dev/null"),
)

# 伪文件系统不参与磁盘告警判断
_PSEUDO_FS = frozenset(
    {"tmpfs", "devtmpfs", "squashfs", "proc", "sysfs", "cgroup", "cgroup2",
     "devpts", "mqueue", "hugetlbfs", "debugfs", "tracefs", "configfs",
     "fusectl", "pstore", "securityfs", "efivarfs", "binfmt_misc"}
)


def _build_command() -> str:
    parts = []
    for name, cmd in _SECTIONS:
        parts.append(f"echo '{_MARK}{name}'")
        parts.append(cmd)
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


def _f(value: str) -> float | None:
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _parse_cpu(sections: dict[str, str], errors: list[str]) -> CpuFacts:
    cpu = CpuFacts()

    loadavg = sections.get("loadavg", "").split()
    if len(loadavg) >= 3:
        cpu.load1, cpu.load5, cpu.load15 = (_f(loadavg[0]), _f(loadavg[1]), _f(loadavg[2]))
    else:
        errors.append("loadavg 采集失败")

    cores = _f(sections.get("nproc", ""))
    cpu.cores = int(cores) if cores else None

    def _cpu_line(text: str) -> list[float] | None:
        fields = text.split()
        if len(fields) < 5 or not fields[0].startswith("cpu"):
            return None
        return [float(x) for x in fields[1:] if x.replace(".", "").isdigit()]

    try:
        first = _cpu_line(sections.get("stat1", ""))
        second = _cpu_line(sections.get("stat2", ""))
        if first and second and len(first) >= 5 and len(second) >= 5:
            total_delta = sum(second) - sum(first)
            # idle + iowait 都算「没在干活」
            idle_delta = (second[3] + second[4]) - (first[3] + first[4])
            iowait_delta = second[4] - first[4]
            if total_delta > 0:
                cpu.usage_pct = round(100.0 * (1 - idle_delta / total_delta), 2)
                cpu.iowait_pct = round(100.0 * iowait_delta / total_delta, 2)
        else:
            errors.append("/proc/stat 采样不完整，CPU 利用率不可用")
    except Exception as exc:  # noqa: BLE001
        errors.append(f"CPU 利用率计算失败：{exc}")

    return cpu


def _parse_memory(sections: dict[str, str], errors: list[str]) -> MemoryFacts:
    text = sections.get("meminfo", "")
    if not text:
        errors.append("meminfo 采集失败")
        return MemoryFacts()

    values: dict[str, float] = {}
    for line in text.splitlines():
        match = re.match(r"^(\w+):\s+(\d+)\s*kB", line)
        if match:
            values[match.group(1)] = float(match.group(2)) / 1024.0

    total = values.get("MemTotal")
    available = values.get("MemAvailable")
    swap_total = values.get("SwapTotal")
    swap_free = values.get("SwapFree")

    mem = MemoryFacts(
        total_mb=round(total, 1) if total else None,
        available_mb=round(available, 1) if available else None,
        swap_total_mb=round(swap_total, 1) if swap_total is not None else None,
    )
    if total and available is not None:
        used = total - available
        mem.used_mb = round(used, 1)
        mem.usage_pct = round(100.0 * used / total, 2)
    if swap_total is not None and swap_free is not None:
        mem.swap_used_mb = round(swap_total - swap_free, 1)
    return mem


def _parse_disks(sections: dict[str, str], errors: list[str]) -> list[DiskFacts]:
    text = sections.get("df", "")
    if not text:
        errors.append("df 采集失败")
        return []

    disks: dict[str, DiskFacts] = {}
    for line in text.splitlines()[1:]:
        fields = line.split()
        if len(fields) < 7:
            continue
        filesystem, fstype, total_k, used_k, avail_k, capacity, mount = fields[:7]
        if fstype in _PSEUDO_FS:
            continue
        disks[mount] = DiskFacts(
            mount=mount,
            filesystem=filesystem,
            total_gb=round(float(total_k) / 1024 / 1024, 2) if total_k.isdigit() else None,
            used_gb=round(float(used_k) / 1024 / 1024, 2) if used_k.isdigit() else None,
            avail_gb=round(float(avail_k) / 1024 / 1024, 2) if avail_k.isdigit() else None,
            usage_pct=_f(capacity.rstrip("%")),
        )

    # inode 耗尽是「磁盘看着有空间但写不进去」的经典成因，单独补一列
    for line in sections.get("dfi", "").splitlines()[1:]:
        fields = line.split()
        if len(fields) < 7:
            continue
        mount, capacity = fields[6], fields[5]
        if mount in disks:
            disks[mount].inode_usage_pct = _f(capacity.rstrip("%"))

    return list(disks.values())


def _parse_processes(sections: dict[str, str], errors: list[str]) -> list[ProcessFacts]:
    text = sections.get("ps", "")
    if not text:
        errors.append("ps 采集失败")
        return []

    procs: list[ProcessFacts] = []
    for line in text.splitlines()[1:]:
        fields = line.split(maxsplit=5)
        if len(fields) < 6 or not fields[0].isdigit():
            continue
        rss_kb = _f(fields[4])
        procs.append(
            ProcessFacts(
                pid=int(fields[0]),
                user=fields[1],
                cpu_pct=_f(fields[2]),
                mem_pct=_f(fields[3]),
                rss_mb=round(rss_kb / 1024, 1) if rss_kb else None,
                command=fields[5][:300],
            )
        )
    return procs


_SS_PROCESS = re.compile(r'\("([^"]+)",pid=(\d+)')


def _parse_ports(sections: dict[str, str], errors: list[str]) -> list[PortFacts]:
    text = sections.get("ports", "")
    if not text:
        errors.append("监听端口采集失败")
        return []

    ports: list[PortFacts] = []
    for line in text.splitlines():
        if not line.strip() or line.startswith(("State", "Active", "Proto", "Netid")):
            continue
        fields = line.split()
        if len(fields) < 4:
            continue
        # ss 的 Local Address:Port 在第 4 列，netstat 在第 4 列，形态一致
        local = fields[3] if fields[0].upper() == "LISTEN" else fields[3]
        if ":" not in local:
            continue
        addr, _, port_text = local.rpartition(":")
        match = _SS_PROCESS.search(line)
        ports.append(
            PortFacts(
                proto="tcp",
                local_addr=addr,
                port=int(port_text) if port_text.isdigit() else None,
                process=match.group(1) if match else "",
                pid=int(match.group(2)) if match else None,
            )
        )
    return ports


def collect(target: SshTarget, pool: SshPool, *, timeout: float = 25.0) -> HostFacts:
    result = pool.run(target, _build_command(), timeout=timeout)
    sections = _split_sections(result.stdout)
    errors: list[str] = []
    if result.stderr.strip():
        errors.append(f"stderr: {result.stderr.strip()[:300]}")

    uptime_fields = sections.get("uptime", "").split()
    uptime_s = _f(uptime_fields[0]) if uptime_fields else None

    return HostFacts(
        server_id=target.id,
        host=target.host,
        collected_at=time.strftime("%Y-%m-%dT%H:%M:%S"),
        uptime_s=uptime_s,
        cpu=_parse_cpu(sections, errors),
        memory=_parse_memory(sections, errors),
        disks=_parse_disks(sections, errors),
        top_processes=_parse_processes(sections, errors),
        listen_ports=_parse_ports(sections, errors),
        partial_errors=errors,
    )


# 监听端口通常几个到几十个，全带上也就几百 token，但它是「服务在不在」的
# 决定性证据。之前只给计数，导致服务停掉这类事故里诊断只能给出「证据不足」。
_MAX_SUMMARY_PORTS = 40


def summarize(facts: HostFacts) -> dict[str, Any]:
    """给 LLM 的压缩视图：只留异常和头部进程，避免整张表塞进上下文。"""
    hot_disks = [d for d in facts.disks if (d.usage_pct or 0) >= 70 or (d.inode_usage_pct or 0) >= 70]
    ports = sorted(facts.listen_ports, key=lambda p: p.port or 0)
    return {
        "host": facts.host,
        "collected_at": facts.collected_at,
        "cpu": facts.cpu.model_dump(exclude_none=True),
        "memory": facts.memory.model_dump(exclude_none=True),
        "disks_over_70pct": [d.model_dump(exclude_none=True) for d in hot_disks],
        "disk_count": len(facts.disks),
        "top_processes": [p.model_dump(exclude_none=True) for p in facts.top_processes[:5]],
        "listen_port_count": len(facts.listen_ports),
        "listen_ports": [
            p.model_dump(exclude_none=True) for p in ports[:_MAX_SUMMARY_PORTS]
        ],
        "listen_ports_truncated": len(ports) > _MAX_SUMMARY_PORTS,
        "partial_errors": facts.partial_errors,
    }
