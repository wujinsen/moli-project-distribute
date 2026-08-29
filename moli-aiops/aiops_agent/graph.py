"""诊断状态图与执行引擎。

    START → triage → investigator → diagnostician → critic
                          ↑                            │
                          └──── 证据不足，有界回补 ──────┘
                                                       │ 定稿
                                                       ↓
            planner → ⏸ await_approval → executor → verifier → reporter → END

用 LangGraph 而不是自己串函数，图的是三件手写编排很难做对的事：

- **人工确认**：interrupt() 把图挂起并把状态存进 checkpoint，进程重启也能恢复
- **回放**：get_state_history() 拿到每一步的完整状态快照，事后能复现当时的判断
- **单步重跑**：从任意 checkpoint 改输入后重跑，不必从头再诊断一遍
"""

from __future__ import annotations

import sqlite3
import time
import uuid
from typing import Any, TypedDict

from langgraph.checkpoint.sqlite import SqliteSaver
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command

from . import config, trace
from .llm import LlmRouter
from .models import Alert
from .nodes import approval as approval_node
from .nodes import critic as critic_node
from .nodes import diagnostician as diagnostician_node
from .nodes import executor as executor_node
from .nodes import investigator as investigator_node
from .nodes import planner as planner_node
from .nodes import reporter as reporter_node
from .nodes import verifier as verifier_node


class DiagnosisState(TypedDict, total=False):
    run_id: str
    incident_id: str
    alert: dict[str, Any]
    topology: dict[str, Any]
    triage: dict[str, Any]
    evidence: list[dict[str, Any]]
    hypotheses: list[dict[str, Any]]
    accepted_hypothesis_id: str
    need_more_evidence: bool
    backfill_queries: list[str]
    plan: dict[str, Any]
    approval: dict[str, Any]
    approval_tokens: dict[str, str]
    executions: list[dict[str, Any]]
    verification: dict[str, Any]
    report: dict[str, Any]
    iteration: int
    status: str
    progress: list[dict[str, Any]]
    started_ms: int


def _after_critic(state: DiagnosisState) -> str:
    return "investigator" if state.get("need_more_evidence") else "planner"


def build_graph(router: LlmRouter, toolbelt_ctx, toolbelt, checkpointer):
    builder = StateGraph(DiagnosisState)

    builder.add_node("triage", triage_factory(router, toolbelt_ctx, toolbelt))
    builder.add_node("investigator", investigator_node.make_node(router, toolbelt_ctx, toolbelt))
    builder.add_node("diagnostician", diagnostician_node.make_node(router, config.MAX_HYPOTHESES))
    builder.add_node("critic", critic_node.make_node(router, config.MAX_INVESTIGATE_ROUNDS))
    builder.add_node("planner", planner_node.make_node(router, toolbelt_ctx, toolbelt))
    builder.add_node("await_approval", approval_node.make_node())
    builder.add_node(
        "executor", executor_node.make_node(toolbelt_ctx, toolbelt, config.FORCE_DRY_RUN)
    )
    builder.add_node("verifier", verifier_node.make_node(toolbelt_ctx, toolbelt))
    builder.add_node("reporter", reporter_node.make_node(router, trace))

    builder.add_edge(START, "triage")
    builder.add_edge("triage", "investigator")
    builder.add_edge("investigator", "diagnostician")
    builder.add_edge("diagnostician", "critic")
    builder.add_conditional_edges(
        "critic", _after_critic, {"investigator": "investigator", "planner": "planner"}
    )
    builder.add_edge("planner", "await_approval")
    builder.add_edge("await_approval", "executor")
    builder.add_edge("executor", "verifier")
    builder.add_edge("verifier", "reporter")
    builder.add_edge("reporter", END)

    return builder.compile(checkpointer=checkpointer)


def triage_factory(router, toolbelt_ctx, toolbelt):
    from .nodes import triage as triage_node

    return triage_node.make_node(router, toolbelt_ctx, toolbelt)


class DiagnosisEngine:
    """把 LangGraph 的 thread 概念包成「一次诊断」，对外只暴露 run_id。"""

    def __init__(self, router: LlmRouter | None = None, toolbelt_ctx=None, toolbelt_module=None):
        from ops_mcp import toolbelt as default_toolbelt

        self.router = router or LlmRouter()
        self.toolbelt = toolbelt_module or default_toolbelt
        self.ctx = toolbelt_ctx or self.toolbelt.ToolContext()

        config.CHECKPOINT_PATH.parent.mkdir(parents=True, exist_ok=True)
        self._conn = sqlite3.connect(config.CHECKPOINT_PATH, check_same_thread=False)
        self.checkpointer = SqliteSaver(self._conn)
        self.graph = build_graph(self.router, self.ctx, self.toolbelt, self.checkpointer)

    @staticmethod
    def _cfg(run_id: str) -> dict:
        return {"configurable": {"thread_id": run_id}}

    def start(self, alert: Alert, run_id: str | None = None) -> dict[str, Any]:
        run_id = run_id or f"run-{uuid.uuid4().hex[:12]}"
        incident_id = alert.id or f"inc-{uuid.uuid4().hex[:8]}"
        trace.start_run(run_id, incident_id, alert)

        initial: DiagnosisState = {
            "run_id": run_id,
            "incident_id": incident_id,
            "alert": alert.model_dump(),
            "evidence": [],
            "hypotheses": [],
            "progress": [],
            "iteration": 0,
            "status": "running",
            "started_ms": int(time.time() * 1000),
        }
        result = self.graph.invoke(initial, self._cfg(run_id))
        return self._wrap(run_id, result)

    def resume(self, run_id: str, decision: dict[str, Any]) -> dict[str, Any]:
        """用人的审批结果恢复被 interrupt 挂起的图。"""
        result = self.graph.invoke(Command(resume=decision), self._cfg(run_id))
        return self._wrap(run_id, result)

    def snapshot(self, run_id: str) -> dict[str, Any]:
        state = self.graph.get_state(self._cfg(run_id))
        return {
            "run_id": run_id,
            "next": list(state.next),
            "values": state.values,
            "interrupts": [
                {"value": i.value, "id": getattr(i, "id", "")} for i in (state.interrupts or ())
            ],
        }

    def history(self, run_id: str, limit: int = 40) -> list[dict[str, Any]]:
        """回放：每个 checkpoint 一条，能看出当时图停在哪、状态是什么。"""
        items = []
        for snapshot in self.graph.get_state_history(self._cfg(run_id)):
            checkpoint_id = (snapshot.config or {}).get("configurable", {}).get("checkpoint_id", "")
            values = snapshot.values or {}
            items.append(
                {
                    "checkpoint_id": checkpoint_id,
                    "next": list(snapshot.next),
                    "created_at": getattr(snapshot, "created_at", ""),
                    "iteration": values.get("iteration"),
                    "status": values.get("status"),
                    "progress_count": len(values.get("progress") or []),
                    "evidence_count": len(values.get("evidence") or []),
                    "hypotheses": [h.get("statement") for h in (values.get("hypotheses") or [])],
                }
            )
            if len(items) >= limit:
                break
        return items

    def rerun_from(self, run_id: str, node: str, patch: dict[str, Any] | None = None) -> dict[str, Any]:
        """回到某节点执行前的 checkpoint，可选地改一下输入，再从那里往下跑。

        典型用途：Investigator 那轮日志模式选错了，改掉模式重跑取证之后的全部环节，
        不必从分诊开始重来。
        """
        target = None
        for snapshot in self.graph.get_state_history(self._cfg(run_id)):
            if node in (snapshot.next or ()):
                target = snapshot
                break
        if target is None:
            raise ValueError(f"run {run_id} 的历史中找不到即将执行 {node} 的 checkpoint")

        checkpoint_config = target.config
        if patch:
            checkpoint_config = self.graph.update_state(checkpoint_config, patch)
        result = self.graph.invoke(None, checkpoint_config)
        return self._wrap(run_id, result)

    def _wrap(self, run_id: str, values: dict[str, Any]) -> dict[str, Any]:
        state = self.graph.get_state(self._cfg(run_id))
        interrupts = [
            {"value": i.value, "id": getattr(i, "id", "")} for i in (state.interrupts or ())
        ]
        status = "awaiting_approval" if interrupts else str(values.get("status") or "running")

        trace.update_run(
            run_id,
            status=status,
            severity=str((values.get("triage") or {}).get("severity") or ""),
            root_cause=str((values.get("report") or {}).get("root_cause") or ""),
            report_md=str((values.get("report") or {}).get("markdown") or ""),
        )
        return {
            "run_id": run_id,
            "status": status,
            "interrupts": interrupts,
            "values": values,
            "trace_summary": trace.summarize(run_id),
        }

    def close(self) -> None:
        self.ctx.close()
        self._conn.close()
