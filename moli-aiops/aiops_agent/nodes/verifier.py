"""复核：重新取证，确认现象是否真的消失了。

刻意做成确定性检查而不是让模型判断「看起来好了吗」。恢复与否是个事实问题，
拿服务存活状态和资源指标对照就有答案，交给模型只会引入不必要的不确定性。

**服务存活是硬条件**：资源指标全绿但服务还没起来，不算恢复。只看 CPU/内存/磁盘
会在「服务被停掉」这类事故里给出假的「已恢复」——重启失败了也照样报好，
这比不做复核更危险。
"""

from __future__ import annotations

from typing import Any

from .base import node_span, progress


def make_node(toolbelt_ctx, toolbelt):
    def verifier_node(state: dict) -> dict:
        with node_span(state, "verifier") as node_trace:
            executions = state.get("executions") or []
            if not any(e.get("status") == "success" for e in executions):
                node_trace.status = "skipped"
                return {
                    "verification": {
                        "recovered": False,
                        "checks": [],
                        "note": "没有成功执行的步骤，无需复核",
                    },
                    "progress": progress(state, "verifier", "无成功步骤，跳过复核", 94),
                }

            triage = state.get("triage") or {}
            targets = [t for t in (triage.get("affected_targets") or []) if t][:3]
            checks: list[dict[str, Any]] = []

            for target in targets:
                payload = toolbelt.ops_host_facts(toolbelt_ctx, target, full=True)
                node_trace.tool_calls.append(f"ops_host_facts:{target}")
                if not payload.get("ok"):
                    checks.append({"target": target, "ok": False,
                                   "detail": f"复核取证失败：{payload.get('message')}"})
                    continue

                facts = payload.get("facts") or {}
                cpu = (facts.get("cpu") or {}).get("usage_pct")
                memory = (facts.get("memory") or {}).get("usage_pct")
                worst_disk = max(
                    ((d.get("usage_pct") or 0) for d in (facts.get("disks") or [])), default=0
                )
                resources_ok = (
                    (cpu is None or cpu < 90)
                    and (memory is None or memory < 92)
                    and worst_disk < 92
                )

                svc = toolbelt.ops_service_status(toolbelt_ctx, target)
                node_trace.tool_calls.append(f"ops_service_status:{target}")
                if svc.get("ok"):
                    down = svc.get("down") or []
                    declared = svc.get("services") or []
                    # 没声明服务时这一项不参与判定，不能因为无从检查就判失败
                    services_ok = not down
                    service_detail = {
                        "declared": [c.get("service") for c in declared],
                        "down": down,
                    }
                else:
                    services_ok = False
                    down = []
                    service_detail = {"error": svc.get("message", "服务存活检查失败")}

                checks.append(
                    {
                        "target": target,
                        "ok": resources_ok and services_ok,
                        "resources_ok": resources_ok,
                        "services_ok": services_ok,
                        "cpu_pct": cpu,
                        "memory_pct": memory,
                        "worst_disk_pct": worst_disk,
                        "services": service_detail,
                    }
                )

            recovered = bool(checks) and all(c.get("ok") for c in checks)
            if recovered:
                note = "服务已恢复监听，各项指标回到阈值内"
            elif any(not c.get("services_ok", True) for c in checks):
                still_down = sorted(
                    {s for c in checks for s in ((c.get("services") or {}).get("down") or [])}
                )
                note = (
                    f"服务仍未恢复：{', '.join(still_down)}，需要人工介入"
                    if still_down else "服务存活检查未通过，需要人工介入"
                )
            else:
                note = "服务已起但仍有资源指标超阈值，需要人工介入"

            return {
                "verification": {"recovered": recovered, "checks": checks, "note": note},
                "progress": progress(
                    state, "verifier",
                    f"复核完成：{'已恢复' if recovered else '未完全恢复'}", 95,
                    recovered=recovered,
                ),
            }

    return verifier_node
