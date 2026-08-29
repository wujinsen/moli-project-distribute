"""执行：逐步执行已批准的处置动作。

三条纪律：
- **只执行被勾选的步骤**，人可以只批准前两步
- **顺序执行、失败即停**，不要在前一步失败的情况下继续往下做
- **每步都带对应的审批令牌**，令牌与命令原文绑定，工具层会再校验一次

这里不做任何安全判断，判断全在工具层。编排层重复实现一遍安全逻辑
只会导致两处规则漂移，最后谁也不知道以哪个为准。
"""

from __future__ import annotations

from ..models import StepExecution
from .base import node_span, progress


def make_node(toolbelt_ctx, toolbelt, force_dry_run: bool):
    def executor_node(state: dict) -> dict:
        with node_span(state, "executor") as node_trace:
            approval = state.get("approval") or {}
            plan = state.get("plan") or {}
            steps = plan.get("steps") or []
            incident_id = str(state.get("incident_id") or "")

            if not approval.get("approved"):
                node_trace.status = "skipped"
                return {
                    "executions": [],
                    "status": "rejected",
                    "progress": progress(state, "executor", "预案被否决，未执行任何操作", 88),
                }

            approved = set(approval.get("approved_step_ids") or [])
            tokens = state.get("approval_tokens") or {}
            executions: list[StepExecution] = []
            halted = False

            for step in steps:
                step_id = str(step.get("id"))
                if step_id not in approved:
                    executions.append(StepExecution(step_id=step_id, status="skipped",
                                                    command=step.get("command", "")))
                    continue
                if halted:
                    executions.append(StepExecution(step_id=step_id, status="skipped",
                                                    command=step.get("command", ""),
                                                    error="前序步骤失败，已中止"))
                    continue

                payload = toolbelt.ops_exec_command(
                    toolbelt_ctx,
                    str(step.get("target") or ""),
                    str(step.get("command") or ""),
                    incident_id=incident_id,
                    step_id=step_id,
                    approval_token=str(tokens.get(step_id) or ""),
                    dry_run=force_dry_run,
                )
                node_trace.tool_calls.append(f"ops_exec_command:{step_id}")

                if not payload.get("ok"):
                    executions.append(
                        StepExecution(step_id=step_id, status="blocked",
                                      command=step.get("command", ""),
                                      error=f"{payload.get('code')}: {payload.get('message')}")
                    )
                    halted = True
                    continue

                result = payload.get("result") or {}
                exit_code = result.get("exit_code")
                ok = exit_code == 0 or result.get("dry_run")
                executions.append(
                    StepExecution(
                        step_id=step_id,
                        status="success" if ok else "failed",
                        command=str(result.get("command") or ""),
                        exit_code=exit_code,
                        stdout=str(result.get("stdout") or "")[:4000],
                        stderr=str(result.get("stderr") or "")[:2000],
                        audit_id=str(result.get("audit_id") or ""),
                        duration_ms=int(result.get("duration_ms") or 0),
                    )
                )
                if not ok:
                    halted = True

            succeeded = sum(1 for e in executions if e.status == "success")
            failed = [e for e in executions if e.status in {"failed", "blocked"}]
            message = f"执行完成：成功 {succeeded} 步"
            if failed:
                message += f"，失败 {len(failed)} 步（{failed[0].error or failed[0].stderr[:80]}）"
            if force_dry_run:
                message += "（干跑模式，未真实变更）"

            return {
                "executions": [e.model_dump() for e in executions],
                "progress": progress(state, "executor", message, 90),
            }

    return executor_node
