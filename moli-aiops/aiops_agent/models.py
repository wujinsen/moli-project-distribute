"""编排层数据模型。

诊断链路上每个阶段的产物都建模成结构化对象而不是自由文本，理由有三个：
Critic 要能逐条证伪假设、人工确认时要能看清每步风险、复盘时要能回溯每个结论
是靠哪条证据支撑的。全是散文的话这三件事都做不了。
"""

from __future__ import annotations

from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field


class Severity(StrEnum):
    P0 = "P0"  # 核心链路不可用
    P1 = "P1"  # 主要功能受损或即将不可用
    P2 = "P2"  # 局部异常，有降级余地
    P3 = "P3"  # 观察项


class RunStatus(StrEnum):
    RUNNING = "running"
    AWAITING_APPROVAL = "awaiting_approval"
    EXECUTING = "executing"
    SUCCEEDED = "succeeded"
    FAILED = "failed"
    REJECTED = "rejected"  # 人工否决了预案
    DEGRADED = "degraded"  # 超预算或证据不足，给出部分结论


class Alert(BaseModel):
    """诊断的输入。可以来自巡检、Webhook，也可以是人直接描述的现象。"""

    id: str = ""
    title: str
    description: str = ""
    target: str = ""  # inventory 中的主机 id
    service: str = ""
    source: str = "manual"  # manual | health_probe | webhook | drill
    fired_at: str = ""
    labels: dict[str, str] = Field(default_factory=dict)


class Triage(BaseModel):
    severity: Severity = Severity.P2
    summary: str = ""
    affected_targets: list[str] = Field(default_factory=list)
    affected_services: list[str] = Field(default_factory=list)
    # 先想清楚要查什么再去查，避免 Investigator 把所有工具盲目跑一遍
    investigation_focus: list[str] = Field(default_factory=list)


class EvidenceItem(BaseModel):
    """一条证据。source 必须能追溯到具体工具调用，否则 Critic 无从验证。"""

    id: str
    kind: str  # topology | host_facts | logs | changes | kb
    target: str = ""
    tool: str = ""
    summary: str = ""
    data: dict[str, Any] = Field(default_factory=dict)
    collected_at: str = ""
    error: str = ""

    @property
    def ok(self) -> bool:
        return not self.error


class Hypothesis(BaseModel):
    id: str
    statement: str
    confidence: float = 0.0  # 0~1
    # 支撑与反驳都要指向 EvidenceItem.id，禁止凭空断言
    supporting_evidence: list[str] = Field(default_factory=list)
    contradicting_evidence: list[str] = Field(default_factory=list)
    mechanism: str = ""  # 从根因到现象的因果链
    verdict: str = "open"  # open | confirmed | refuted | insufficient
    critic_note: str = ""


class PlanStep(BaseModel):
    id: str
    order: int
    intent: str  # 这一步想达成什么
    target: str
    command: str = ""
    service: str = ""
    action: str = ""  # 走 ops_service_action 时用
    risk: str = "mutating"
    risk_reason: str = ""
    blast_radius: str = ""  # 影响面：会动到谁
    rollback: str = ""  # 出错怎么退回去
    verification: str = ""  # 执行完怎么确认生效
    requires_approval: bool = True


class RemediationPlan(BaseModel):
    summary: str = ""
    root_cause: str = ""
    steps: list[PlanStep] = Field(default_factory=list)
    # 明确写出「不做什么」，避免执行阶段擅自扩大范围
    out_of_scope: list[str] = Field(default_factory=list)
    max_risk: str = "mutating"


class ApprovalDecision(BaseModel):
    approved: bool = False
    approver: str = ""
    decided_at: str = ""
    comment: str = ""
    # 人可以只批准其中几步
    approved_step_ids: list[str] = Field(default_factory=list)


class StepExecution(BaseModel):
    step_id: str
    status: str = "pending"  # pending | skipped | success | failed | blocked
    command: str = ""
    exit_code: int | None = None
    stdout: str = ""
    stderr: str = ""
    audit_id: str = ""
    error: str = ""
    duration_ms: int = 0


class Verification(BaseModel):
    recovered: bool = False
    checks: list[dict[str, Any]] = Field(default_factory=list)
    note: str = ""


class NodeTrace(BaseModel):
    """节点级链路追踪。模型和 provider 记实际用到的，兜底切换过要看得出来。"""

    node: str
    iteration: int = 0
    started_at: str = ""
    duration_ms: int = 0
    status: str = "ok"  # ok | error | skipped
    model: str = ""
    provider: str = ""
    prompt_tokens: int = 0
    completion_tokens: int = 0
    fallback_used: bool = False
    attempts: int = 1
    tool_calls: list[str] = Field(default_factory=list)
    error: str = ""


class ProgressEvent(BaseModel):
    phase: str
    message: str
    pct: int = 0
    detail: dict[str, Any] = Field(default_factory=dict)


class IncidentReport(BaseModel):
    timeline: list[dict[str, Any]] = Field(default_factory=list)
    root_cause: str = ""
    impact: str = ""
    resolution: str = ""
    action_items: list[str] = Field(default_factory=list)
    markdown: str = ""
