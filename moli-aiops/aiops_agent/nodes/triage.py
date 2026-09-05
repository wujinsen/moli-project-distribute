"""分诊：定级、圈定影响面、决定这次要重点查什么。

先想清楚查什么再去查。否则 Investigator 会把所有工具对所有主机盲跑一遍，
既慢又把无关证据塞满上下文，反而稀释了真正的信号。
"""

from __future__ import annotations

from ..models import Severity, Triage
from .base import node_span, progress, try_llm_json

SYSTEM = """你是资深 SRE，负责事故分诊。根据告警和运维拓扑判断严重级别、影响面，
并决定接下来重点收集哪些证据。

严重级别定义：
- P0：核心链路不可用，用户明显感知
- P1：主要功能受损，或正在恶化且很快会不可用
- P2：局部异常，有降级余地
- P3：观察项，暂不影响业务

只输出 JSON：
{
  "severity": "P0|P1|P2|P3",
  "summary": "一句话说清发生了什么",
  "affected_targets": ["主机 id"],
  "affected_services": ["服务名"],
  "investigation_focus": ["要查什么，每条一个具体方向，最多 5 条"]
}

investigation_focus 要具体。写「查 CPU、内存、磁盘、日志、变更」是没用的，
要写「确认 gateway 进程是否存活」「检查磁盘是否写满导致日志无法落盘」这种。"""


def _heuristic(alert: dict, topology: dict) -> Triage:
    """模型不可用时的兜底：按关键词定级，按告警字面圈影响面。"""
    text = f"{alert.get('title', '')} {alert.get('description', '')}".lower()
    if any(k in text for k in ("不可用", "宕", "down", "502", "503", "拒绝连接", "refused")):
        severity = Severity.P0
    elif any(k in text for k in ("超时", "timeout", "慢", "堆积", "满", "oom")):
        severity = Severity.P1
    else:
        severity = Severity.P2

    target = alert.get("target") or ""
    targets = [target] if target else [s.get("id") for s in (topology.get("servers") or [])][:3]
    service = alert.get("service") or ""

    focus = [
        "采集主机指标，确认 CPU、内存、磁盘是否有资源瓶颈",
        "检索最近日志中的 ERROR 与异常堆栈",
        "查询故障前的近期变更",
    ]
    if alert.get("source") == "webhook" or (alert.get("labels") or {}).get("alertname"):
        focus.insert(0, "对照 Prometheus 抓取、5xx 占比与堆内存窗口")
    if alert.get("trace_id") or (alert.get("labels") or {}).get("trace_id"):
        focus.insert(0, "按 trace_id 拉取 SkyWalking Span 与 Loki 全链路日志")

    return Triage(
        severity=severity,
        summary=alert.get("title") or "未提供告警标题",
        affected_targets=[str(t) for t in targets if t],
        affected_services=[service] if service else [],
        investigation_focus=focus,
    )


def make_node(router, toolbelt_ctx, toolbelt):
    def triage_node(state: dict) -> dict:
        with node_span(state, "triage") as node_trace:
            alert = state.get("alert") or {}

            topology_payload = toolbelt.ops_topology(toolbelt_ctx)
            topology = topology_payload if topology_payload.get("ok") else {}
            node_trace.tool_calls.append("ops_topology")

            payload = try_llm_json(
                router,
                node_trace,
                node="triage",
                system=SYSTEM,
                user=(
                    f"告警：\n{alert}\n\n"
                    f"拓扑（服务器 {len(topology.get('servers') or [])} 台，"
                    f"服务 {len(topology.get('projects') or [])} 个）：\n"
                    f"{topology.get('servers')}\n{topology.get('projects')}\n"
                    f"关系边：{topology.get('edges')}"
                ),
            )

            if payload is None:
                triage = _heuristic(alert, topology)
            else:
                try:
                    triage = Triage(
                        severity=Severity(str(payload.get("severity", "P2")).upper()),
                        summary=str(payload.get("summary") or ""),
                        affected_targets=[str(t) for t in (payload.get("affected_targets") or [])],
                        affected_services=[str(s) for s in (payload.get("affected_services") or [])],
                        investigation_focus=[
                            str(f) for f in (payload.get("investigation_focus") or [])
                        ][:5],
                    )
                except ValueError:
                    triage = _heuristic(alert, topology)
                    node_trace.status = "degraded"
                    node_trace.error = "模型返回的 severity 非法，已回退规则定级"

            # 告警指明了主机就以它为准，模型不该把范围随意扩大
            if alert.get("target") and alert["target"] not in triage.affected_targets:
                triage.affected_targets.insert(0, str(alert["target"]))

            return {
                "triage": triage.model_dump(mode="json"),
                "topology": topology,
                "progress": progress(
                    state, "triage", f"分诊完成：{triage.severity.value} · {triage.summary}", 12,
                    severity=triage.severity.value,
                ),
            }

    return triage_node
