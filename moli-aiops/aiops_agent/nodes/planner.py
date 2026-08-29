"""处置预案：把采信的根因转成分步、可审批、可回滚的操作序列。

**每一步的风险等级一律由确定性分级器重新判定，不采信模型的自我评估。**
模型说「这步风险很低」是没有约束力的——它完全可能把一条 rm -rf 标成低风险
来换取通过。风险标签必须来自代码，人工确认时看到的才是真实风险。
"""

from __future__ import annotations

from ..models import PlanStep, RemediationPlan
from .base import node_span, progress, try_llm_json

SYSTEM = """你是资深 SRE，为已确认的根因编写处置预案。

只输出 JSON：
{
  "summary": "预案一句话概述",
  "root_cause": "被采信的根因",
  "steps": [
    {
      "intent": "这一步要达成什么",
      "target": "主机 id",
      "command": "具体 shell 命令；若用服务动作则留空",
      "service": "服务名，仅当用服务动作时填",
      "action": "start|stop|restart|status，仅当用服务动作时填",
      "blast_radius": "影响面：会影响谁、影响多久",
      "rollback": "这一步出错怎么退回去",
      "verification": "执行完怎么确认生效"
    }
  ],
  "out_of_scope": ["本次明确不做的事"]
}

硬性要求：
- 步骤按执行顺序排列，最多 6 步
- 优先最小影响面的手段。能重启单个服务就不要重启整机
- 每一步都必须填 rollback 和 verification，填不出来说明这一步不该做
- 禁止出现 rm -rf、mkfs、dd、reboot、shutdown 这类不可逆操作
- 第一步通常应该是一个只读确认，避免在错误前提上动手"""


def _rule_based(root_cause: str, target: str, service: str) -> RemediationPlan:
    """兜底预案。只覆盖几种高频根因，且一律保守——宁可只给确认步骤。"""
    text = root_cause.lower()
    steps: list[PlanStep] = []

    def step(intent: str, *, command: str = "", svc: str = "", action: str = "",
             blast: str = "", rollback: str = "", verification: str = "") -> None:
        steps.append(
            PlanStep(
                id=f"s{len(steps) + 1}", order=len(steps) + 1, intent=intent,
                target=target, command=command, service=svc, action=action,
                blast_radius=blast, rollback=rollback, verification=verification,
            )
        )

    # 不写死 java：同一套预案要能用在 JVM 服务、Python sidecar 和容器上
    confirm = "ss -lntp; ps -eo pid,pcpu,pmem,args --sort=-pcpu | head -15"
    if service:
        confirm = f"systemctl is-active {service}; " + confirm
    step("确认当前进程与端口状态，避免在错误前提上动手",
         command=confirm,
         blast="无，只读", rollback="无需回滚", verification="能看到进程与监听端口清单")

    if "磁盘" in root_cause or "disk" in text:
        step("定位占用空间最大的目录",
             command="du -sh /var/log/* /opt/moli/logs/* 2>/dev/null | sort -h | tail -20",
             blast="无，只读", rollback="无需回滚", verification="得到体积排序清单")
    elif "内存" in root_cause or "oom" in text:
        step("确认内存占用最高的进程与 JVM 堆状态",
             command="ps -eo pid,pmem,rss,args --sort=-pmem | head -10",
             blast="无，只读", rollback="无需回滚", verification="得到内存占用排序")
        if service:
            step(f"重启 {service} 释放内存",
                 svc=service, action="restart",
                 blast=f"{service} 短暂不可用，通常数十秒",
                 rollback="重启失败则查看日志并回滚到上一版本",
                 verification="进程重新监听端口且健康检查通过")
    elif ("不可达" in root_cause or "未监听" in root_cause or "refused" in text) and service:
        step(f"启动 {service}",
             svc=service, action="start",
             blast=f"仅影响 {service}",
             rollback="启动失败则停止并检查配置",
             verification="端口进入监听状态")

    return RemediationPlan(
        summary="规则兜底预案：先确认现状，再执行最小影响面的处置",
        root_cause=root_cause,
        steps=steps,
        out_of_scope=["不做任何删除操作", "不重启主机", "不修改配置文件"],
    )


def make_node(router, toolbelt_ctx, toolbelt):
    def planner_node(state: dict) -> dict:
        with node_span(state, "planner") as node_trace:
            hypotheses = state.get("hypotheses") or []
            accepted_id = state.get("accepted_hypothesis_id") or ""
            accepted = next((h for h in hypotheses if h.get("id") == accepted_id), None)
            root_cause = accepted.get("statement") if accepted else "根因未确认"

            triage = state.get("triage") or {}
            alert = state.get("alert") or {}
            target = (triage.get("affected_targets") or [alert.get("target") or ""])[0]
            service = (triage.get("affected_services") or [alert.get("service") or ""])[0]

            payload = try_llm_json(
                router, node_trace, node="planner", system=SYSTEM,
                user=(
                    f"根因：{root_cause}\n"
                    f"因果链：{accepted.get('mechanism') if accepted else ''}\n"
                    f"目标主机：{target}\n受影响服务：{service}\n"
                    f"严重级别：{triage.get('severity')}\n"
                    f"可用服务动作：start / stop / restart / status"
                ),
            )

            if payload is None:
                plan = _rule_based(str(root_cause), str(target), str(service))
            else:
                steps: list[PlanStep] = []
                for index, raw in enumerate((payload.get("steps") or [])[:6], start=1):
                    if not isinstance(raw, dict):
                        continue
                    steps.append(
                        PlanStep(
                            id=f"s{index}", order=index,
                            intent=str(raw.get("intent") or ""),
                            target=str(raw.get("target") or target),
                            command=str(raw.get("command") or ""),
                            service=str(raw.get("service") or ""),
                            action=str(raw.get("action") or ""),
                            blast_radius=str(raw.get("blast_radius") or ""),
                            rollback=str(raw.get("rollback") or ""),
                            verification=str(raw.get("verification") or ""),
                        )
                    )
                if not steps:
                    plan = _rule_based(str(root_cause), str(target), str(service))
                    node_trace.status = "degraded"
                    node_trace.error = "模型未产出有效步骤，已回退规则预案"
                else:
                    plan = RemediationPlan(
                        summary=str(payload.get("summary") or ""),
                        root_cause=str(payload.get("root_cause") or root_cause),
                        steps=steps,
                        out_of_scope=[str(x) for x in (payload.get("out_of_scope") or [])],
                    )

            # 风险标签一律由分级器重判，模型无权自评
            order = {"read_only": 0, "mutating": 1, "destructive": 2}
            max_risk = "read_only"
            for item in plan.steps:
                # 服务动作在这里就固化成确定命令：后面的风险评估、审批令牌绑定、
                # 实际执行三处必须是同一个字符串，否则令牌校验会失配
                if not item.command and item.service and item.action:
                    resolved = toolbelt.ops_resolve_service_command(
                        toolbelt_ctx, item.target, item.service, item.action
                    )
                    if resolved.get("ok"):
                        item.command = str(resolved.get("command") or "")
                    else:
                        item.command = f"systemctl {item.action} {item.service}"
                        item.risk_reason = f"服务动作解析失败：{resolved.get('message', '')}"

                assessment = toolbelt.ops_assess_command(toolbelt_ctx, item.command)
                node_trace.tool_calls.append("ops_assess_command")
                if assessment.get("ok"):
                    detail = assessment.get("assessment") or {}
                    item.risk = str(detail.get("risk") or "mutating")
                    item.risk_reason = str(detail.get("reason") or "")
                    item.requires_approval = bool(detail.get("requires_approval", True))
                else:
                    item.risk = "mutating"
                    item.risk_reason = "风险评估失败，按变更处理"
                    item.requires_approval = True
                if order.get(item.risk, 1) > order.get(max_risk, 0):
                    max_risk = item.risk
            plan.max_risk = max_risk

            needs_approval = sum(1 for s in plan.steps if s.requires_approval)
            return {
                "plan": plan.model_dump(mode="json"),
                "progress": progress(
                    state, "planner",
                    f"预案生成：{len(plan.steps)} 步，其中 {needs_approval} 步需人工确认，"
                    f"最高风险 {max_risk}",
                    75, max_risk=max_risk,
                ),
            }

    return planner_node
