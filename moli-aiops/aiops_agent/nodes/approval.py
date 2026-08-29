"""人工确认闸门。

用 LangGraph 的 interrupt() 把图真正挂起：状态存进 checkpoint，进程可以重启，
人隔一小时回来点确认，图从这里继续往下走。这不是在节点里 sleep 轮询，
是编排框架级别的暂停/恢复。

只读预案（全部步骤都是 read_only）自动放行——为了一条 df -h 去打扰值班人
只会让人对确认提示脱敏，反而削弱真正高危操作时的警觉。
"""

from __future__ import annotations

from langgraph.types import interrupt

from .base import node_span, now, progress


def make_node():
    def await_approval_node(state: dict) -> dict:
        with node_span(state, "await_approval") as node_trace:
            plan = state.get("plan") or {}
            steps = plan.get("steps") or []
            risky = [s for s in steps if s.get("requires_approval")]

            if not risky:
                node_trace.status = "skipped"
                return {
                    "approval": {
                        "approved": True,
                        "approver": "auto",
                        "decided_at": now(),
                        "comment": "全部步骤均为只读，按策略自动放行",
                        "approved_step_ids": [s.get("id") for s in steps],
                    },
                    "approval_tokens": {},
                    "progress": progress(
                        state, "await_approval", "预案全部只读，自动放行", 80, auto=True
                    ),
                }

            # 图在这里真正挂起，直到外部用 Command(resume=...) 送回人的决定
            decision = interrupt(
                {
                    "type": "approval_request",
                    "run_id": state.get("run_id"),
                    "incident_id": state.get("incident_id"),
                    "root_cause": plan.get("root_cause"),
                    "summary": plan.get("summary"),
                    "max_risk": plan.get("max_risk"),
                    "steps": steps,
                    "out_of_scope": plan.get("out_of_scope") or [],
                }
            )

            if not isinstance(decision, dict):
                decision = {"approved": False, "comment": f"收到非法的审批结果：{decision!r}"}

            approved_ids = [str(x) for x in (decision.get("approved_step_ids") or [])]
            if decision.get("approved") and not approved_ids:
                # 整体同意但没逐条勾选时，视为同意全部步骤
                approved_ids = [str(s.get("id")) for s in steps]

            approval = {
                "approved": bool(decision.get("approved")),
                "approver": str(decision.get("approver") or ""),
                "decided_at": now(),
                "comment": str(decision.get("comment") or ""),
                "approved_step_ids": approved_ids if decision.get("approved") else [],
            }

            message = (
                f"人工确认通过（{approval['approver']}），批准 {len(approved_ids)} 步"
                if approval["approved"]
                else f"人工否决：{approval['comment'] or '未说明原因'}"
            )

            return {
                "approval": approval,
                # 令牌由 HTTP 层签发后随 resume 一起送进来，Agent 无法自行生成
                "approval_tokens": dict(decision.get("tokens") or {}),
                "progress": progress(state, "await_approval", message, 82),
            }

    return await_approval_node
