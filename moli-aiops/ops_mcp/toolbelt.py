"""工具实现层。

与 MCP 协议解耦：这里全是普通 Python 函数，编排层既可以进程内直调（低延迟），
也可以经 mcp_server.py 走 MCP 协议（换编排框架时工具层不用动）。

**这里刻意不提供签发审批令牌的函数。** 令牌只能由人在 FastAPI 那一侧签发；
一旦 Agent 能自己签发，人工确认就退化成了一道自动盖章的流程。
"""

from __future__ import annotations

from functools import cached_property
from typing import Any

from . import config
from .actions import remediate
from .cmdb import Inventory, build_source
from .cmdb.base import CmdbSource, InventoryEntry
from .errors import OpsToolError, success_payload
from .evidence import facts as facts_mod
from .evidence import logs as logs_mod
from .evidence import service as service_mod
from .evidence.kb import KbClient
from .evidence.ssh import SshPool
from .safety import audit
from .safety.classifier import assess


class ToolContext:
    def __init__(self, inventory: Inventory | None = None, cmdb: CmdbSource | None = None) -> None:
        self._inventory = inventory
        self._cmdb = cmdb
        self.pool = SshPool()

    @cached_property
    def inventory(self) -> Inventory:
        if self._inventory is not None:
            return self._inventory
        return Inventory.load(config.INVENTORY_PATH)

    @cached_property
    def cmdb(self) -> CmdbSource:
        if self._cmdb is not None:
            return self._cmdb
        return build_source(self.inventory)

    @cached_property
    def kb(self) -> KbClient:
        return KbClient()

    def entry(self, target: str) -> InventoryEntry:
        return self.inventory.resolve(target)

    def close(self) -> None:
        self.pool.close_all()


def _guard(fn):
    """把 OpsToolError 统一转成结构化载荷，让 Agent 能读到 code 和 retryable。"""

    def wrapper(*args, **kwargs) -> dict[str, Any]:
        try:
            return fn(*args, **kwargs)
        except OpsToolError as exc:
            return exc.to_payload()

    wrapper.__name__ = fn.__name__
    wrapper.__doc__ = fn.__doc__
    return wrapper


# --- 证据源 --------------------------------------------------------------


@_guard
def ops_topology(ctx: ToolContext) -> dict[str, Any]:
    graph = ctx.cmdb.topology()
    return success_payload(
        {
            "source": graph.source,
            "servers": [s.model_dump(exclude_none=True) for s in graph.servers],
            "projects": [p.model_dump(exclude_none=True) for p in graph.projects],
            "components": [c.model_dump(exclude_none=True) for c in graph.components],
            "edges": [e.model_dump() for e in graph.edges],
        }
    )


@_guard
def ops_host_facts(ctx: ToolContext, target: str, *, full: bool = False) -> dict[str, Any]:
    entry = ctx.entry(target)
    host_facts = facts_mod.collect(entry.ssh_target(), ctx.pool)
    audit.record(tool="ops_host_facts", outcome="success", host=entry.host)
    if full:
        return success_payload({"facts": host_facts.model_dump(exclude_none=True)})
    return success_payload({"facts": facts_mod.summarize(host_facts)})


@_guard
def ops_service_status(
    ctx: ToolContext, target: str, *, services: list[str] | None = None
) -> dict[str, Any]:
    """检查目标上声明的服务是否真的活着。

    主机指标回答不了「服务在不在」——CPU 2% 的机器上服务照样可能是停着的。
    这里把 inventory 声明的端口/unit 与机器实际状态对上，给出确定性判定。
    """
    entry = ctx.entry(target)
    ssh_target = entry.ssh_target()
    host_facts = facts_mod.collect(ssh_target, ctx.pool)
    checks = service_mod.check(
        entry, host_facts, ctx.pool, ssh_target, services=services
    )
    audit.record(
        tool="ops_service_status", outcome="success", host=entry.host,
        detail={"services": [c.service for c in checks],
                "verdicts": [str(c.verdict) for c in checks]},
    )
    down = [c.service for c in checks if c.verdict == "down"]
    return success_payload(
        {
            "services": [c.model_dump(mode="json", exclude_none=True) for c in checks],
            "down": down,
            "all_up": bool(checks) and not down,
        }
    )


@_guard
def ops_log_search(
    ctx: ToolContext,
    target: str,
    *,
    pattern: str = "",
    paths: list[str] | None = None,
    scan_lines: int = logs_mod.DEFAULT_SCAN_LINES,
    max_hits: int = logs_mod.DEFAULT_MAX_HITS,
) -> dict[str, Any]:
    entry = ctx.entry(target)
    result = logs_mod.search(
        entry.ssh_target(),
        ctx.pool,
        allowed_paths=entry.log_paths,
        paths=paths,
        pattern=pattern,
        scan_lines=scan_lines,
        max_hits=max_hits,
    )
    audit.record(
        tool="ops_log_search", outcome="success", host=entry.host,
        detail={"pattern": pattern, "paths": result.scanned_paths, "hits": len(result.hits)},
    )
    return success_payload({"result": result.model_dump(exclude_none=True)})


@_guard
def ops_recent_changes(ctx: ToolContext, server_id: str | None = None, limit: int = 20) -> dict[str, Any]:
    records = ctx.cmdb.recent_changes(server_id, limit)
    return success_payload(
        {
            "source": ctx.cmdb.name,
            "changes": [r.model_dump(exclude_none=True) for r in records],
            "note": "" if records else "该数据源没有变更流水（本地 inventory 模式下 operation_task 不可用）",
        }
    )


@_guard
def ops_kb_search(ctx: ToolContext, question: str, *, top_k: int = 6) -> dict[str, Any]:
    answer = ctx.kb.ask(question, top_k=top_k)
    return success_payload({"kb": answer.model_dump(exclude_none=True)})


# --- 安全评估（只判不执行）-------------------------------------------------


@_guard
def ops_assess_command(ctx: ToolContext, command: str) -> dict[str, Any]:
    """给预案里的每一步标注风险等级，供人工确认时判断。不执行任何东西。"""
    assessment = assess(command, allow_destructive=config.ALLOW_DESTRUCTIVE)
    return success_payload({"assessment": assessment.model_dump(mode="json")})


@_guard
def ops_resolve_service_command(
    ctx: ToolContext, target: str, service: str, action: str
) -> dict[str, Any]:
    """把服务动作解析成确定的命令字符串，不执行。

    审批令牌绑定的是命令原文，所以风险评估、令牌签发、实际执行三处必须拿到
    **同一个字符串**。预案阶段就固化下来，避免三处各自推导出细微差异。
    """
    entry = ctx.entry(target)
    return success_payload(
        {"command": remediate.service_command(ctx.inventory, entry, service, action)}
    )


# --- 处置 ---------------------------------------------------------------


@_guard
def ops_exec_command(
    ctx: ToolContext,
    target: str,
    command: str,
    *,
    incident_id: str = "",
    step_id: str = "",
    approval_token: str = "",
    dry_run: bool = False,
) -> dict[str, Any]:
    entry = ctx.entry(target)
    result = remediate.execute(
        entry,
        ctx.pool,
        command=command,
        incident_id=incident_id,
        step_id=step_id,
        approval_token=approval_token,
        dry_run=dry_run,
    )
    # mode="json" 让 RiskLevel 这类枚举落成字面值，跨进程传输时不会变成 "RiskLevel.MUTATING"
    return success_payload({"result": result.model_dump(exclude_none=True, mode="json")})


@_guard
def ops_service_action(
    ctx: ToolContext,
    target: str,
    service: str,
    action: str,
    *,
    incident_id: str = "",
    step_id: str = "",
    approval_token: str = "",
    dry_run: bool = False,
) -> dict[str, Any]:
    entry = ctx.entry(target)
    command = remediate.service_command(ctx.inventory, entry, service, action)
    result = remediate.execute(
        entry,
        ctx.pool,
        command=command,
        incident_id=incident_id,
        step_id=step_id,
        approval_token=approval_token,
        dry_run=dry_run,
    )
    return success_payload(
        {"result": result.model_dump(exclude_none=True, mode="json"), "command": command}
    )


# --- 审计 ---------------------------------------------------------------


@_guard
def ops_audit_trail(ctx: ToolContext, incident_id: str, limit: int = 200) -> dict[str, Any]:
    return success_payload({"entries": audit.list_for_incident(incident_id, limit)})
