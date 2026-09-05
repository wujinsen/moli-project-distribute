"""取证：按分诊圈定的方向拉证据。

默认五路，外加可选的全链路一路（告警带了 trace_id 才走）：
  服务存活 → 服务进程还在不在、声明端口还听不听
  主机指标 → 是不是资源打满了
  日志     → 进程自己报了什么错（SSH grep）
  近期变更 → 故障前刚动过什么
  知识库   → 这个现象以前出过吗，有没有既定处置流程
  全链路   → SkyWalking Span + Loki 同 trace 日志（报障 / Agent 共用）

服务存活单独成一路而不是并进主机指标：一台 CPU 2%、内存 20% 的机器上服务照样
可能是停着的，资源指标全绿不代表服务活着。缺这一路时，「服务被停掉」这类最常见
的事故会一路走到「证据不足」。

任何一路失败都只记成一条 error 证据，不中断整轮取证。诊断经常要靠残缺证据
做判断，为了一条日志读不到就放弃整次诊断是不可接受的。
"""

from __future__ import annotations

import uuid

from ops_mcp.evidence.observability import extract_trace_id

from ..models import EvidenceItem
from .base import node_span, now, progress, try_llm_json

DEFAULT_LOG_PATTERN = "ERROR|FATAL|Exception|OutOfMemory|refused|timeout|Timeout|Caused by"

PATTERN_SYSTEM = """你是 SRE，要把调查方向转成日志检索用的扩展正则。

只输出 JSON：{"patterns": ["正则1", "正则2"]}

要求：最多 3 条；每条是可直接用于 grep -E 的扩展正则；
用 | 连接同类关键词；不要用需要转义的复杂结构；覆盖中英文日志。"""


def _evidence(kind: str, target: str, tool: str, summary: str, data: dict, error: str = "") -> EvidenceItem:
    return EvidenceItem(
        id=f"ev-{uuid.uuid4().hex[:8]}",
        kind=kind,
        target=target,
        tool=tool,
        summary=summary,
        data=data,
        collected_at=now(),
        error=error,
    )


def _patterns(router, node_trace, focus: list[str], requested: list[str]) -> list[str]:
    if requested:
        return requested[:3]
    if not focus:
        return [DEFAULT_LOG_PATTERN]

    payload = try_llm_json(
        router, node_trace, node="investigator",
        system=PATTERN_SYSTEM,
        user="调查方向：\n" + "\n".join(f"- {f}" for f in focus),
    )
    if not payload:
        return [DEFAULT_LOG_PATTERN]
    patterns = [str(p) for p in (payload.get("patterns") or []) if str(p).strip()]
    return patterns[:3] or [DEFAULT_LOG_PATTERN]


def make_node(router, toolbelt_ctx, toolbelt):
    def investigator_node(state: dict) -> dict:
        with node_span(state, "investigator") as node_trace:
            triage = state.get("triage") or {}
            alert = state.get("alert") or {}
            targets = [t for t in (triage.get("affected_targets") or []) if t]
            if not targets and alert.get("target"):
                targets = [alert["target"]]

            # Critic 回补时会指定这一轮补查什么
            backfill = state.get("backfill_queries") or []
            patterns = _patterns(router, node_trace, triage.get("investigation_focus") or [], backfill)

            collected: list[EvidenceItem] = []

            svc = str(
                alert.get("service")
                or (alert.get("labels") or {}).get("service")
                or ""
            )
            if svc or alert.get("source") == "webhook":
                for preset in ("up", "http_5xx_ratio", "heap_ratio"):
                    metrics = toolbelt.ops_metrics_query(
                        toolbelt_ctx, service=svc, preset=preset
                    )
                    node_trace.tool_calls.append(f"ops_metrics_query:{preset}")
                    if metrics.get("ok"):
                        dump = metrics.get("metrics") or {}
                        collected.append(
                            _evidence(
                                "metrics",
                                svc,
                                "ops_metrics_query",
                                f"Prometheus {preset} 样本 {dump.get('sample_count') or 0} 条",
                                dump,
                            )
                        )
                    else:
                        collected.append(
                            _evidence(
                                "metrics",
                                svc,
                                "ops_metrics_query",
                                f"Prometheus {preset} 查询失败",
                                {},
                                metrics.get("message", "未知错误"),
                            )
                        )

            trace_id = extract_trace_id(alert)
            if trace_id:
                sw = toolbelt.ops_trace_get(toolbelt_ctx, trace_id)
                node_trace.tool_calls.append("ops_trace_get")
                if sw.get("ok"):
                    dump = sw.get("trace") or {}
                    collected.append(
                        _evidence(
                            "trace",
                            "",
                            "ops_trace_get",
                            (
                                f"SkyWalking {dump.get('span_count') or 0} span，"
                                f"{dump.get('error_spans') or 0} 个错误，"
                                f"服务 {', '.join(dump.get('services') or []) or '无'}"
                            ),
                            dump,
                        )
                    )
                else:
                    collected.append(
                        _evidence(
                            "trace",
                            "",
                            "ops_trace_get",
                            "SkyWalking 按 trace 查询失败",
                            {},
                            sw.get("message", "未知错误"),
                        )
                    )

                loki = toolbelt.ops_logs_by_trace(toolbelt_ctx, trace_id)
                node_trace.tool_calls.append("ops_logs_by_trace")
                if loki.get("ok"):
                    logs = loki.get("logs") or {}
                    collected.append(
                        _evidence(
                            "trace_logs",
                            "",
                            "ops_logs_by_trace",
                            f"Loki 命中 {logs.get('hit_count') or 0} 条（trace {trace_id}）",
                            logs,
                        )
                    )
                else:
                    collected.append(
                        _evidence(
                            "trace_logs",
                            "",
                            "ops_logs_by_trace",
                            "Loki 按 trace 检索失败",
                            {},
                            loki.get("message", "未知错误"),
                        )
                    )

            for target in targets[:5]:
                svc = toolbelt.ops_service_status(toolbelt_ctx, target)
                node_trace.tool_calls.append(f"ops_service_status:{target}")
                if svc.get("ok"):
                    down = svc.get("down") or []
                    checks = svc.get("services") or []
                    summary = (
                        f"{target} 服务存活：{len(down)}/{len(checks)} 个异常"
                        + (f"（{', '.join(down)}）" if down else "")
                    )
                    collected.append(
                        _evidence("service_health", target, "ops_service_status", summary, svc)
                    )
                else:
                    collected.append(
                        _evidence("service_health", target, "ops_service_status",
                                  f"{target} 服务存活检查失败", {}, svc.get("message", "未知错误"))
                    )

                facts = toolbelt.ops_host_facts(toolbelt_ctx, target)
                node_trace.tool_calls.append(f"ops_host_facts:{target}")
                if facts.get("ok"):
                    collected.append(
                        _evidence("host_facts", target, "ops_host_facts",
                                  f"{target} 主机指标", facts.get("facts") or {})
                    )
                else:
                    collected.append(
                        _evidence("host_facts", target, "ops_host_facts",
                                  f"{target} 主机指标采集失败", {}, facts.get("message", "未知错误"))
                    )

                for pattern in patterns:
                    logs = toolbelt.ops_log_search(toolbelt_ctx, target, pattern=pattern, max_hits=40)
                    node_trace.tool_calls.append(f"ops_log_search:{target}")
                    if logs.get("ok"):
                        result = logs.get("result") or {}
                        hits = result.get("hits") or []
                        collected.append(
                            _evidence("logs", target, "ops_log_search",
                                      f"{target} 日志命中 {len(hits)} 条（模式 {pattern}）", result)
                        )
                    else:
                        collected.append(
                            _evidence("logs", target, "ops_log_search",
                                      f"{target} 日志检索失败（模式 {pattern}）", {},
                                      logs.get("message", "未知错误"))
                        )

                changes = toolbelt.ops_recent_changes(toolbelt_ctx, target, 10)
                node_trace.tool_calls.append(f"ops_recent_changes:{target}")
                if changes.get("ok"):
                    records = changes.get("changes") or []
                    collected.append(
                        _evidence("changes", target, "ops_recent_changes",
                                  f"{target} 近期变更 {len(records)} 条", changes)
                    )

            question = alert.get("title") or triage.get("summary") or "运维故障排查"
            kb = toolbelt.ops_kb_search(toolbelt_ctx, question, top_k=5)
            node_trace.tool_calls.append("ops_kb_search")
            if kb.get("ok"):
                kb_data = kb.get("kb") or {}
                citations = kb_data.get("citations") or []
                collected.append(
                    _evidence("kb", "", "ops_kb_search",
                              f"知识库命中 {len(citations)} 篇相关文档", kb_data)
                )

            # 回补轮次保留前几轮证据，让 Critic 能看到全貌
            merged = list(state.get("evidence") or []) + [e.model_dump() for e in collected]
            failed = sum(1 for e in collected if e.error)

            return {
                "evidence": merged,
                "backfill_queries": [],
                "progress": progress(
                    state, "investigator",
                    f"取证完成：{len(collected)} 条证据（{failed} 条失败），累计 {len(merged)} 条",
                    35, patterns=patterns, targets=targets,
                ),
            }

    return investigator_node
