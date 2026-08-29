"""复盘：产出事故报告。

报告按茉莉知识库的 frontmatter 规范生成，可以直接 ingest 进 `wiki-moli/ops/`。
这样诊断时检索知识库、复盘后回写知识库形成闭环，下一次同类故障就有历史可查。

时间线由 trace 和审计确定性拼装，不让模型复述——模型复述过程容易漏掉
被安全策略拦下的尝试，而那恰恰是复盘时最该看到的部分。
"""

from __future__ import annotations

import time

from ..models import IncidentReport
from .base import node_span, progress, try_llm_json

SYSTEM = """你是 SRE 团队的事故复盘撰写人。基于给定的诊断过程写一份复盘。

只输出 JSON：
{
  "root_cause": "根因，写清楚机制而不只是现象",
  "impact": "影响面：哪些服务、多长时间、用户侧表现",
  "resolution": "如何解决的",
  "action_items": ["后续改进项，要具体可执行，最多 5 条"]
}

action_items 要针对**这次暴露出的系统性问题**，比如缺少某项监控、
缺少容量告警、缺少自动化预案。不要写「加强巡检」这种无法验收的空话。"""


def _timeline(state: dict) -> list[dict]:
    events = []
    for item in state.get("progress") or []:
        events.append({"phase": item.get("phase"), "message": item.get("message")})
    for execution in state.get("executions") or []:
        if execution.get("status") == "skipped":
            continue
        events.append(
            {
                "phase": "execute",
                "message": f"[{execution.get('status')}] {execution.get('command')}"
                + (f" — {execution.get('error')}" if execution.get("error") else ""),
            }
        )
    return events


def _markdown(state: dict, report: IncidentReport, trace_summary: dict) -> str:
    today = time.strftime("%Y-%m-%d")
    alert = state.get("alert") or {}
    triage = state.get("triage") or {}
    plan = state.get("plan") or {}
    verification = state.get("verification") or {}
    slug = f"事故复盘-{state.get('incident_id')}"

    lines = [
        "---",
        f"title: 事故复盘 · {alert.get('title') or '未命名告警'}",
        f"slug: {slug}",
        "type: output",
        "status: active",
        "tags: [事故复盘, AIOps, 自动生成]",
        f"query: {alert.get('title') or ''}",
        "sources:",
        f"  - aiops-run://{state.get('run_id')}",
        "related: []",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# 事故复盘 · {alert.get('title') or '未命名告警'}",
        "",
        "> 本报告由 moli-aiops 诊断链路自动生成，执行动作均经过人工确认。",
        "",
        "## 概况",
        "",
        f"- 严重级别：{triage.get('severity') or '未定级'}",
        f"- 影响主机：{', '.join(triage.get('affected_targets') or []) or '未确定'}",
        f"- 影响服务：{', '.join(triage.get('affected_services') or []) or '未确定'}",
        f"- 是否恢复：{'是' if verification.get('recovered') else '否'}",
        "",
        "## 根因",
        "",
        report.root_cause or "未能确认根因。",
        "",
        "## 影响",
        "",
        report.impact or "未评估。",
        "",
        "## 处置过程",
        "",
        report.resolution or plan.get("summary") or "未执行处置动作。",
        "",
        "### 时间线",
        "",
    ]
    for event in report.timeline:
        lines.append(f"- **{event.get('phase')}**：{event.get('message')}")

    lines += ["", "## 候选根因与证伪结论", ""]
    for item in state.get("hypotheses") or []:
        mark = {"confirmed": "采信", "refuted": "已排除", "insufficient": "证据不足"}.get(
            item.get("verdict", ""), "待定"
        )
        lines.append(f"- [{mark}] {item.get('statement')}（置信度 {item.get('confidence')}）")
        if item.get("critic_note"):
            lines.append(f"  - 评审意见：{item['critic_note']}")

    lines += ["", "## 改进项", ""]
    for action in report.action_items or ["（本次未产出改进项）"]:
        lines.append(f"- [ ] {action}")

    lines += [
        "",
        "## 诊断链路开销",
        "",
        f"- 节点数：{trace_summary.get('nodes', 0)}",
        f"- 总耗时：{trace_summary.get('total_duration_ms', 0)} ms",
        f"- token：入 {trace_summary.get('prompt_tokens', 0)} / 出 {trace_summary.get('completion_tokens', 0)}",
        f"- 跨厂商兜底次数：{trace_summary.get('fallback_calls', 0)}",
        "",
    ]
    return "\n".join(lines)


def make_node(router, trace_store):
    def reporter_node(state: dict) -> dict:
        with node_span(state, "reporter") as node_trace:
            hypotheses = state.get("hypotheses") or []
            accepted_id = state.get("accepted_hypothesis_id") or ""
            accepted = next((h for h in hypotheses if h.get("id") == accepted_id), None)
            plan = state.get("plan") or {}
            verification = state.get("verification") or {}
            executions = state.get("executions") or []

            payload = try_llm_json(
                router, node_trace, node="reporter", system=SYSTEM,
                user=(
                    f"告警：{state.get('alert')}\n"
                    f"采信根因：{accepted.get('statement') if accepted else '未确认'}\n"
                    f"因果链：{accepted.get('mechanism') if accepted else ''}\n"
                    f"处置预案：{plan.get('summary')}\n"
                    f"执行结果：{[(e.get('status'), e.get('command')) for e in executions]}\n"
                    f"复核结论：{verification}"
                ),
            )

            if payload is None:
                report = IncidentReport(
                    root_cause=accepted.get("statement") if accepted else "未能确认根因",
                    impact=f"影响主机 {(state.get('triage') or {}).get('affected_targets')}",
                    resolution=plan.get("summary") or "未执行处置",
                    action_items=[],
                )
            else:
                report = IncidentReport(
                    root_cause=str(payload.get("root_cause") or ""),
                    impact=str(payload.get("impact") or ""),
                    resolution=str(payload.get("resolution") or ""),
                    action_items=[str(a) for a in (payload.get("action_items") or [])][:5],
                )

            report.timeline = _timeline(state)
            summary = trace_store.summarize(str(state.get("run_id") or ""))
            report.markdown = _markdown(state, report, summary)

            recovered = bool(verification.get("recovered"))
            rejected = state.get("status") == "rejected"
            if rejected:
                status = "rejected"
            elif recovered:
                status = "succeeded"
            elif executions:
                status = "degraded"
            else:
                status = "degraded"

            return {
                "report": report.model_dump(),
                "status": status,
                "progress": progress(state, "reporter", "复盘报告已生成", 100, status=status),
            }

    return reporter_node
