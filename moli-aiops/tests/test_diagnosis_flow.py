"""诊断链路端到端测试。

刻意在**不配置任何 LLM 厂商**的情况下跑完整条链路，验证两件事：

1. 规则兜底是真的能跑通全流程，不是文档里的一句承诺
2. 人工确认闸门在真实链路里生效——这里的 ops_exec_command 走的是**真的**
   remediate + classifier + approval，只有 SSH 被替换成假的。
   如果把安全层也 mock 掉，这个测试就只是在测自己写的假货。
"""

from __future__ import annotations

import sqlite3
from types import SimpleNamespace

import pytest

from aiops_agent import config as agent_config
from aiops_agent import trace as trace_store
from aiops_agent.llm import LlmRouter
from aiops_agent.models import Alert
from ops_mcp import config as ops_config
from ops_mcp.actions import remediate
from ops_mcp.cmdb.base import Inventory, InventoryEntry
from ops_mcp.errors import OpsToolError, success_payload
from ops_mcp.evidence.ssh import CommandOutput
from ops_mcp.safety import approval, audit
from ops_mcp.safety.classifier import assess

TARGET = "sandbox-app"
SERVICE = "moli-gateway"

ENTRY_RAW = {
    "id": TARGET,
    "server_id": "1",
    "name": "沙箱应用节点",
    "host": "127.0.0.1",
    "ssh_port": 2201,
    "user": "root",
    "password": "aiops",
    "log_paths": ["/opt/moli/logs/gateway.log"],
    "services": [{"name": SERVICE, "port": 8080, "systemd_unit": SERVICE}],
}


class FakePool:
    def __init__(self) -> None:
        self.executed: list[str] = []

    def run(self, target, command, *, timeout=None):  # noqa: ANN001, ARG002
        self.executed.append(command)
        return CommandOutput(exit_code=0, stdout="ok", stderr="", duration_ms=3, truncated=False)


class FakeToolbelt:
    """证据源是假的，安全层是真的。"""

    def __init__(self) -> None:
        self.entry = InventoryEntry(ENTRY_RAW)
        self.inventory = Inventory([self.entry])
        self.pool = FakePool()
        self.recovered = False

    # --- 证据源 ---
    def ops_topology(self, ctx):  # noqa: ARG002
        return success_payload(
            {
                "source": "file",
                "servers": [{"id": "1", "name": "沙箱应用节点", "ip": "127.0.0.1"}],
                "projects": [{"id": "p1", "name": SERVICE, "port": 8080}],
                "components": [],
                "edges": [{"source": "server:1", "target": "p1", "kind": "deploys"}],
            }
        )

    def ops_host_facts(self, ctx, target, full=False):  # noqa: ARG002
        if self.recovered:
            memory = {"total_mb": 4096.0, "used_mb": 1600.0, "usage_pct": 39.1}
            cpu = {"cores": 2, "usage_pct": 18.4, "load1": 0.4}
        else:
            memory = {"total_mb": 4096.0, "used_mb": 3900.0, "usage_pct": 95.2}
            cpu = {"cores": 2, "usage_pct": 88.0, "load1": 7.8}

        if full:
            return success_payload(
                {
                    "facts": {
                        "host": "127.0.0.1", "cpu": cpu, "memory": memory,
                        "disks": [{"mount": "/", "usage_pct": 41.0}],
                        "listen_ports": [{"port": 8080, "process": "java"}] if self.recovered else [],
                        "top_processes": [],
                    }
                }
            )
        return success_payload(
            {
                "facts": {
                    "host": "127.0.0.1", "cpu": cpu, "memory": memory,
                    "disks_over_70pct": [], "disk_count": 1,
                    "top_processes": [
                        {"pid": 4213, "user": "root", "cpu_pct": 87.0,
                         "mem_pct": 71.2, "rss_mb": 2980.0, "command": "java -jar gateway.jar"}
                    ],
                    "listen_port_count": 0 if not self.recovered else 1,
                    "partial_errors": [],
                }
            }
        )

    def ops_service_status(self, ctx, target, services=None):  # noqa: ARG002
        if self.recovered:
            check = {
                "service": SERVICE, "target": TARGET, "expected_port": 8080,
                "port_listening": True, "unit": SERVICE, "unit_state": "active",
                "verdict": "up", "reason": "端口 8080 正在监听；unit active",
            }
        else:
            check = {
                "service": SERVICE, "target": TARGET, "expected_port": 8080,
                "port_listening": False, "unit": SERVICE, "unit_state": "inactive",
                "verdict": "down",
                "reason": "声明端口 8080 无监听；systemd unit moli-gateway 状态 inactive",
            }
        down = [] if self.recovered else [SERVICE]
        return success_payload(
            {"services": [check], "down": down, "all_up": self.recovered}
        )

    def ops_log_search(self, ctx, target, pattern="", paths=None, scan_lines=5000, max_hits=80):  # noqa: ARG002
        return success_payload(
            {
                "result": {
                    "host": "127.0.0.1",
                    "scanned_paths": ["/opt/moli/logs/gateway.log"],
                    "hits": [
                        {
                            "path": "/opt/moli/logs/gateway.log",
                            "ts": "2026-08-18 17:02:11",
                            "level": "ERROR",
                            "text": "2026-08-18 17:02:11 ERROR java.lang.OutOfMemoryError: Java heap space",
                        }
                    ],
                    "truncated": False,
                    "partial_errors": [],
                }
            }
        )

    def ops_recent_changes(self, ctx, server_id=None, limit=20):  # noqa: ARG002
        return success_payload({"source": "file", "changes": [], "note": ""})

    def ops_kb_search(self, ctx, question, top_k=6):  # noqa: ARG002
        return success_payload({"kb": {"mode": "retrieval", "answer": "", "citations": []}})

    # --- 安全与执行：这两个走真实实现 ---
    def ops_assess_command(self, ctx, command):  # noqa: ARG002
        return success_payload(
            {"assessment": assess(command, allow_destructive=ops_config.ALLOW_DESTRUCTIVE).model_dump()}
        )

    def ops_resolve_service_command(self, ctx, target, service, action):  # noqa: ARG002
        return success_payload(
            {"command": remediate.service_command(self.inventory, self.entry, service, action)}
        )

    def ops_exec_command(self, ctx, target, command, *, incident_id="", step_id="",  # noqa: ARG002
                         approval_token="", dry_run=False):
        try:
            result = remediate.execute(
                self.entry, self.pool, command=command, incident_id=incident_id,
                step_id=step_id, approval_token=approval_token, dry_run=dry_run,
            )
        except OpsToolError as exc:
            return exc.to_payload()
        if "restart" in command:
            self.recovered = True
        return success_payload({"result": result.model_dump()})


@pytest.fixture
def isolated_db(tmp_path, monkeypatch):
    """trace / checkpoint / 审计全部落到临时目录，避免测试之间互相污染。"""
    monkeypatch.setattr(agent_config, "DB_PATH", tmp_path / "aiops.db")
    monkeypatch.setattr(agent_config, "CHECKPOINT_PATH", tmp_path / "ckpt.db")
    monkeypatch.setattr(ops_config, "AUDIT_DB", tmp_path / "aiops.db")
    monkeypatch.setattr(trace_store, "_CONN", None)
    monkeypatch.setattr(audit, "_CONN", None)
    approval.reset_consumed()
    yield
    approval.reset_consumed()


@pytest.fixture
def engine(isolated_db, monkeypatch):  # noqa: ARG001
    monkeypatch.setattr(ops_config, "EXEC_ENABLED", True)
    from aiops_agent.graph import DiagnosisEngine

    fake = FakeToolbelt()
    # 空 provider 列表 = 全链路走规则兜底
    instance = DiagnosisEngine(
        router=LlmRouter(providers=[]),
        toolbelt_ctx=SimpleNamespace(close=lambda: None),
        toolbelt_module=fake,
    )
    instance._fake = fake  # noqa: SLF001
    yield instance
    instance._conn.close()  # noqa: SLF001


ALERT = Alert(
    id="inc-test-1",
    title="moli-gateway 不可用，健康检查连续失败",
    description="网关端口无响应，上游返回 502",
    target=TARGET,
    service=SERVICE,
    source="drill",
)


def test_pauses_for_human_approval_before_executing(engine):
    result = engine.start(ALERT, run_id="run-test-1")

    assert result["status"] == "awaiting_approval"
    assert result["interrupts"], "图应当在执行前挂起等待人工确认"

    request = result["interrupts"][0]["value"]
    assert request["type"] == "approval_request"
    assert request["steps"], "审批请求必须带上完整预案"
    assert engine._fake.pool.executed == [], "人工确认之前不允许执行任何命令"  # noqa: SLF001


def test_plan_risk_labels_come_from_the_classifier(engine):
    result = engine.start(ALERT, run_id="run-test-2")
    steps = result["interrupts"][0]["value"]["steps"]

    by_risk = {s["risk"] for s in steps}
    assert "mutating" in by_risk, "预案里应当有需要审批的变更步骤"
    for step in steps:
        assert step["risk"] in {"read_only", "mutating", "destructive"}
        assert step["risk_reason"], "每一步都要说明风险判定理由"
        # 只读步骤不该要求审批，变更步骤必须要求
        assert step["requires_approval"] is (step["risk"] != "read_only")


def test_full_loop_with_approval(engine):
    started = engine.start(ALERT, run_id="run-test-3")
    steps = started["interrupts"][0]["value"]["steps"]

    # 模拟 HTTP 层：人点了同意之后，由服务端为每个变更步骤签发绑定令牌
    tokens = {}
    for step in steps:
        if step["requires_approval"]:
            tokens[step["id"]] = approval.issue(
                host="127.0.0.1", command=step["command"], risk=step["risk"],
                incident_id="inc-test-1", step_id=step["id"], approver="wujinsen",
            )["token"]

    final = engine.resume(
        "run-test-3",
        {
            "approved": True,
            "approver": "wujinsen",
            "approved_step_ids": [s["id"] for s in steps],
            "tokens": tokens,
        },
    )

    values = final["values"]
    executions = values["executions"]
    assert all(e["status"] in {"success", "skipped"} for e in executions), executions
    assert any("restart" in e["command"] for e in executions if e["status"] == "success")

    assert values["verification"]["recovered"] is True
    assert final["status"] == "succeeded"

    report = values["report"]
    assert report["markdown"].startswith("---"), "复盘报告要带知识库 frontmatter"
    assert "OutOfMemory" in report["markdown"] or "内存" in report["markdown"]


def test_execution_blocked_when_token_missing(engine):
    started = engine.start(ALERT, run_id="run-test-4")
    steps = started["interrupts"][0]["value"]["steps"]

    # 人同意了，但没有签发令牌——变更步骤必须被工具层挡下
    final = engine.resume(
        "run-test-4",
        {"approved": True, "approver": "wujinsen",
         "approved_step_ids": [s["id"] for s in steps], "tokens": {}},
    )

    executions = {e["step_id"]: e for e in final["values"]["executions"]}
    mutating = [s for s in steps if s["requires_approval"]]
    assert mutating, "该场景应当产生变更步骤"
    for step in mutating:
        assert executions[step["id"]]["status"] == "blocked"
        assert "APPROVAL_REQUIRED" in executions[step["id"]]["error"]


def test_rejection_skips_execution_but_still_reports(engine):
    engine.start(ALERT, run_id="run-test-5")
    final = engine.resume(
        "run-test-5",
        {"approved": False, "approver": "wujinsen", "comment": "先人工观察一轮"},
    )

    assert engine._fake.pool.executed == []  # noqa: SLF001
    assert final["status"] == "rejected"
    assert final["values"]["report"]["markdown"], "被否决也要留下复盘记录"


def test_trace_records_every_node(engine):
    engine.start(ALERT, run_id="run-test-6")
    traces = trace_store.get_traces("run-test-6")
    nodes = [t["node"] for t in traces]

    for expected in ("triage", "investigator", "diagnostician", "critic", "planner"):
        assert expected in nodes, f"{expected} 应当留下 trace，实际：{nodes}"

    summary = trace_store.summarize("run-test-6")
    assert summary["nodes"] >= 5


def test_history_supports_replay(engine):
    engine.start(ALERT, run_id="run-test-7")
    history = engine.history("run-test-7")

    assert history, "应当能取到 checkpoint 历史用于回放"
    assert any(h["next"] for h in history)


def test_audit_trail_records_blocked_attempts(engine, tmp_path):  # noqa: ARG002
    started = engine.start(ALERT, run_id="run-test-8")
    steps = started["interrupts"][0]["value"]["steps"]
    engine.resume(
        "run-test-8",
        {"approved": True, "approver": "wujinsen",
         "approved_step_ids": [s["id"] for s in steps], "tokens": {}},
    )

    entries = audit.list_for_incident("inc-test-1")
    outcomes = {e["outcome"] for e in entries}
    assert "blocked" in outcomes, "被拦下的执行尝试必须留痕，复盘时要看得到"


def test_service_down_is_diagnosed_from_evidence(engine):
    """服务被停掉是最常见的事故，规则兜底必须能直接定位到它。

    这条用例是为一个真实缺陷加的：主机指标摘要早期只带 listen_port_count 而不带
    端口清单，也没有服务存活检查，于是「网关被 stop」这种最直白的故障一路走到
    「现有证据不足以定位根因」，预案里只剩一条只读确认。
    """
    result = engine.start(ALERT, run_id="run-svc-1")
    values = result["values"]

    kinds = {e["kind"] for e in values["evidence"]}
    assert "service_health" in kinds, "取证必须包含服务存活这一路"

    statements = " ".join(h["statement"] for h in values["hypotheses"])
    assert "未监听" in statements or "未运行" in statements, statements

    # 有了可定位的根因，预案就该给出真正的处置动作而不只是只读确认
    steps = values["plan"]["steps"]
    assert any(s["risk"] == "mutating" for s in steps), steps
    assert "moli-gateway" in " ".join(s["command"] for s in steps)


def test_specific_log_cause_outranks_the_service_down_symptom(engine):
    """「端口未监听」是症状，不是根因。

    日志里有 OutOfMemoryError 时，根因应当是 OOM，服务不在只是它的后果。
    最初把服务存活假设的置信度设成 0.9，结果它盖过了 OOM，根因倒退成症状，
    预案也从「重启释放内存」退化成「启动服务」。
    """
    result = engine.start(ALERT, run_id="run-svc-3")
    hypotheses = result["values"]["hypotheses"]

    top = hypotheses[0]
    assert "内存" in top["statement"] or "OOM" in top["statement"].upper(), top
    service_down = next(
        (h for h in hypotheses if "未监听" in h["statement"]), None
    )
    assert service_down is not None, "症状假设仍应保留作为兜底"
    assert service_down["confidence"] < top["confidence"]


def test_verifier_does_not_report_recovery_while_service_is_down(engine, monkeypatch):
    """资源指标全绿但服务没起来，不能算恢复。

    只看 CPU/内存/磁盘的复核会在重启失败时给出假的「已恢复」，
    那比不做复核更危险——它会让人以为事故已经结束。
    """
    started = engine.start(ALERT, run_id="run-svc-2")
    steps = started["interrupts"][0]["value"]["steps"]

    fake = engine._fake  # noqa: SLF001
    # 让处置「执行成功」但服务其实没起来：资源指标转好，存活检查仍为 down
    original = fake.ops_host_facts

    def healthy_facts(ctx, target, full=False):
        fake.recovered = True
        try:
            return original(ctx, target, full=full)
        finally:
            fake.recovered = False

    monkeypatch.setattr(fake, "ops_host_facts", healthy_facts)

    tokens = {
        s["id"]: approval.issue(
            host="127.0.0.1", command=s["command"], risk=s["risk"],
            incident_id="inc-test-1", step_id=s["id"], approver="wujinsen",
        )["token"]
        for s in steps
        if s["requires_approval"]
    }
    final = engine.resume(
        "run-svc-2",
        {"approved": True, "approver": "wujinsen",
         "approved_step_ids": [s["id"] for s in steps], "tokens": tokens},
    )

    verification = final["values"]["verification"]
    assert verification["recovered"] is False, "服务仍 down 时不得报已恢复"
    assert SERVICE in verification["note"]
    assert any(c.get("resources_ok") and not c.get("services_ok")
               for c in verification["checks"]), verification["checks"]


def test_runs_without_any_llm_provider(engine):
    """整条链路在零 API key 下跑完，是「兜底」这件事的验收标准。"""
    assert engine.router.configured is False
    result = engine.start(ALERT, run_id="run-test-9")

    values = result["values"]
    assert values["triage"]["severity"] == "P0"
    assert values["hypotheses"], "规则兜底也要产出候选根因"
    assert values["plan"]["steps"], "规则兜底也要产出处置预案"
