#!/usr/bin/env python3
"""moli-aiops 工具层 MCP server（stdio）。

薄壳：只负责把 toolbelt 里的函数登记成 MCP 工具，业务逻辑一行不写。
换编排框架、或把工具挂给 Claude Desktop / Cursor 这类 MCP 客户端时，
只动这一层。

  python -m ops_mcp.mcp_server

工具的 readOnlyHint / destructiveHint 与内部危险分级保持一致，
让 MCP 客户端在协议层面就能看出哪些工具会改变生产状态。
"""

from __future__ import annotations

import logging
from typing import Any

from mcp.server import MCPServer
from mcp.types import ToolAnnotations

from . import toolbelt
from .errors import dumps

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
log = logging.getLogger("ops-mcp")

server = MCPServer(name="moli-aiops-ops", version="0.1.0")
CTX = toolbelt.ToolContext()

_READ_ONLY = ToolAnnotations(readOnlyHint=True, destructiveHint=False, openWorldHint=True)
_MUTATING = ToolAnnotations(readOnlyHint=False, destructiveHint=True, idempotentHint=False)


def _out(payload: dict[str, Any]) -> str:
    return dumps(payload)


@server.tool(
    name="ops_topology",
    description=(
        "获取运维拓扑：服务器、项目、组件三类实体及 deploys / depends_on 关系边。"
        "诊断的第一步，用来确定故障影响面和上下游。数据来自 user-center 或本地 inventory。"
    ),
    annotations=_READ_ONLY,
)
def ops_topology() -> str:
    return _out(toolbelt.ops_topology(CTX))


@server.tool(
    name="ops_host_facts",
    description=(
        "SSH 采集主机实时指标：CPU 利用率与负载、内存与 swap、各挂载点磁盘与 inode 占用、"
        "CPU 占用最高的进程、监听端口清单。target 传 inventory 里的主机 id 或 IP。"
        "full=true 返回完整明细，默认返回压缩摘要（只含异常项、头部进程与端口清单）。"
        "判断某个服务在不在应改用 ops_service_status，它带「本该监听哪个端口」的先验。"
    ),
    annotations=_READ_ONLY,
)
def ops_host_facts(target: str, full: bool = False) -> str:
    return _out(toolbelt.ops_host_facts(CTX, target, full=full))


@server.tool(
    name="ops_service_status",
    description=(
        "检查目标主机上声明的服务是否真的活着：把 inventory 里声明的端口与 systemd unit "
        "和机器实际状态逐项对照，给出 up / down / degraded 判定。"
        "主机指标回答不了「服务在不在」——CPU 2% 的机器上服务照样可能停着，"
        "所以排查「服务不可用」类故障时应优先调这个。services 留空则检查该主机全部声明服务。"
    ),
    annotations=_READ_ONLY,
)
def ops_service_status(target: str, services: list[str] | None = None) -> str:
    return _out(toolbelt.ops_service_status(CTX, target, services=services))


@server.tool(
    name="ops_log_search",
    description=(
        "在目标主机上检索日志。pattern 为扩展正则，留空则返回最近若干行。"
        "paths 只能取该主机在 inventory 中声明过的日志路径，传其他路径会被拒绝。"
    ),
    annotations=_READ_ONLY,
)
def ops_log_search(
    target: str,
    pattern: str = "",
    paths: list[str] | None = None,
    scan_lines: int = 5000,
    max_hits: int = 80,
) -> str:
    return _out(
        toolbelt.ops_log_search(
            CTX, target, pattern=pattern, paths=paths,
            scan_lines=scan_lines, max_hits=max_hits,
        )
    )


@server.tool(
    name="ops_recent_changes",
    description=(
        "查询近期运维变更流水（部署、上传、命令执行、巡检），来自 operation_task。"
        "「故障前刚改过什么」通常是最快的根因线索。"
    ),
    annotations=_READ_ONLY,
)
def ops_recent_changes(server_id: str | None = None, limit: int = 20) -> str:
    return _out(toolbelt.ops_recent_changes(CTX, server_id, limit))


@server.tool(
    name="ops_kb_search",
    description=(
        "检索企业知识库中的历史事故记录与运维 Runbook，返回带出处的片段。"
        "用于判断当前现象是否似曾相识，以及是否已有既定处置流程。"
    ),
    annotations=_READ_ONLY,
)
def ops_kb_search(question: str, top_k: int = 6) -> str:
    return _out(toolbelt.ops_kb_search(CTX, question, top_k=top_k))


@server.tool(
    name="ops_assess_command",
    description=(
        "评估一条命令的危险等级（read_only / mutating / destructive）并说明理由，"
        "不执行任何操作。生成处置预案时逐步调用，好让人工确认时看得见每步的风险。"
    ),
    annotations=_READ_ONLY,
)
def ops_assess_command(command: str) -> str:
    return _out(toolbelt.ops_assess_command(CTX, command))


@server.tool(
    name="ops_exec_command",
    description=(
        "在目标主机上执行命令。只读命令直接执行；变更类命令必须带人工审批令牌，"
        "且令牌与「主机+命令原文」绑定，换一个字符即失效。"
        "破坏性命令默认拒绝。dry_run=true 只做安全校验不实际执行。"
    ),
    annotations=_MUTATING,
)
def ops_exec_command(
    target: str,
    command: str,
    incident_id: str = "",
    step_id: str = "",
    approval_token: str = "",
    dry_run: bool = False,
) -> str:
    return _out(
        toolbelt.ops_exec_command(
            CTX, target, command, incident_id=incident_id, step_id=step_id,
            approval_token=approval_token, dry_run=dry_run,
        )
    )


@server.tool(
    name="ops_service_action",
    description=(
        "对 inventory 中声明的服务执行 start / stop / restart / status。"
        "命令由服务声明推导（systemd 或 docker），不接受自由拼接。"
        "status 之外的动作需要人工审批令牌。"
    ),
    annotations=_MUTATING,
)
def ops_service_action(
    target: str,
    service: str,
    action: str,
    incident_id: str = "",
    step_id: str = "",
    approval_token: str = "",
    dry_run: bool = False,
) -> str:
    return _out(
        toolbelt.ops_service_action(
            CTX, target, service, action, incident_id=incident_id, step_id=step_id,
            approval_token=approval_token, dry_run=dry_run,
        )
    )


@server.tool(
    name="ops_audit_trail",
    description="按事故 id 查询本次诊断的全部工具调用审计，含被安全策略拦下的尝试。",
    annotations=_READ_ONLY,
)
def ops_audit_trail(incident_id: str, limit: int = 200) -> str:
    return _out(toolbelt.ops_audit_trail(CTX, incident_id, limit))


def main() -> None:
    log.info("ops-mcp starting, inventory=%s", len(CTX.inventory))
    server.run()


if __name__ == "__main__":
    main()
