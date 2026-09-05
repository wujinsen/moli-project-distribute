"""MCP 工具注册的冒烟测试。

保证工具能被 MCP 客户端正确发现，且协议层的 readOnlyHint 与内部危险分级不脱节：
凡是会改生产状态的工具，都不能对外宣称自己是只读的。
"""

from __future__ import annotations

import pytest

EXPECTED_READ_ONLY = {
    "ops_topology",
    "ops_host_facts",
    "ops_service_status",
    "ops_log_search",
    "ops_recent_changes",
    "ops_kb_search",
    "ops_trace_get",
    "ops_logs_by_trace",
    "ops_metrics_query",
    "ops_assess_command",
    "ops_audit_trail",
}
EXPECTED_MUTATING = {"ops_exec_command", "ops_service_action"}


@pytest.fixture(scope="module")
def tools():
    from ops_mcp import mcp_server

    import anyio

    return {t.name: t for t in anyio.run(mcp_server.server.list_tools)}


def test_all_tools_registered(tools) -> None:
    assert set(tools) == EXPECTED_READ_ONLY | EXPECTED_MUTATING


@pytest.mark.parametrize("name", sorted(EXPECTED_READ_ONLY))
def test_read_only_tools_declare_read_only_hint(tools, name: str) -> None:
    annotations = tools[name].annotations
    assert annotations is not None, f"{name} 缺少 annotations"
    assert annotations.read_only_hint is True


@pytest.mark.parametrize("name", sorted(EXPECTED_MUTATING))
def test_mutating_tools_are_not_advertised_read_only(tools, name: str) -> None:
    annotations = tools[name].annotations
    assert annotations is not None, f"{name} 缺少 annotations"
    assert annotations.read_only_hint is False
    assert annotations.destructive_hint is True


def test_no_tool_can_issue_approval_tokens(tools) -> None:
    """签发审批令牌若成为 Agent 可调用的工具，人工确认就形同虚设。"""
    for name in tools:
        assert "approval" not in name.lower()
        assert "approve" not in name.lower()

    from ops_mcp import toolbelt

    exported = {n for n in dir(toolbelt) if not n.startswith("_")}
    assert not {n for n in exported if "issue" in n.lower() or "approve" in n.lower()}


def test_exec_tool_exposes_dry_run_and_approval_params(tools) -> None:
    schema = tools["ops_exec_command"].input_schema or {}
    properties = set((schema.get("properties") or {}).keys())
    assert {"target", "command", "approval_token", "dry_run"} <= properties


def test_metrics_tool_exposes_preset(tools) -> None:
    props = set(((tools["ops_metrics_query"].input_schema or {}).get("properties") or {}).keys())
    assert {"service", "preset", "query"} <= props


def test_trace_tools_expose_trace_id(tools) -> None:
    assert "trace_id" in ((tools["ops_trace_get"].input_schema or {}).get("properties") or {})
    assert "trace_id" in ((tools["ops_logs_by_trace"].input_schema or {}).get("properties") or {})
