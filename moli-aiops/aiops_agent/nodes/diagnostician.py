"""根因假设：从证据推出若干候选根因，每条都要给出因果链和支撑证据。

刻意要求多个候选而不是一个结论。单一结论会让后面的 Critic 无从对比，
也容易把「最先想到的」当成「最可能的」。给出竞争性假设并标注置信度，
才能让证伪这一步有意义。
"""

from __future__ import annotations

from ..models import Hypothesis
from .base import node_span, progress, try_llm_json

SYSTEM = """你是资深 SRE，从证据中推断故障根因。给出 2~4 个**互相竞争**的候选根因。

只输出 JSON：
{
  "hypotheses": [
    {
      "statement": "根因的一句话陈述",
      "mechanism": "从根因到观察到的现象，完整因果链",
      "confidence": 0.0~1.0,
      "supporting_evidence": ["证据 id"],
      "contradicting_evidence": ["证据 id"]
    }
  ]
}

硬性要求：
- supporting_evidence 必须填真实存在的证据 id，不能编造，也不能留空
- 找不到证据支撑的猜想不要写进来
- mechanism 要能解释**全部**主要现象，只能解释一部分就在里面说明
- confidence 要有区分度，不要全都给 0.8"""


def _rule_based(evidence: list[dict]) -> list[Hypothesis]:
    """模型不可用时的确定性基线。规则少但都是高频根因，够撑起一次可用的诊断。"""
    found: list[Hypothesis] = []

    def add(statement: str, mechanism: str, confidence: float, ev_id: str) -> None:
        found.append(
            Hypothesis(
                id=f"h{len(found) + 1}",
                statement=statement,
                mechanism=mechanism,
                confidence=confidence,
                supporting_evidence=[ev_id],
            )
        )

    for item in evidence:
        if item.get("error"):
            continue
        ev_id = item.get("id", "")
        kind = item.get("kind")
        data = item.get("data") or {}

        if kind == "service_health":
            for check in data.get("services") or []:
                if check.get("verdict") != "down":
                    continue
                name = check.get("service") or "未知服务"
                port = check.get("expected_port")
                unit_state = check.get("unit_state") or ""
                # 置信度刻意压在 OOM / 磁盘写满 / fd 耗尽这些之下：
                # 「端口未监听」是**症状**，不是根因。日志里有 OutOfMemoryError 时，
                # 真正的根因是 OOM，服务不在只是它的后果。这条假设的作用是在没有
                # 更具体解释时兜住底——它足以驱动一个可执行的预案，但不该把一个
                # 症状顶成结论。
                add(
                    f"服务 {name} 进程未运行，端口 {port} 未监听",
                    f"{check.get('reason') or '服务不在运行状态'}；"
                    f"上游请求无人应答，表现为连接被拒或 502。"
                    f"若日志中另有更具体的失败原因，应以那条为根因",
                    0.65 if unit_state in {"inactive", "failed"} else 0.6,
                    ev_id,
                )

        if kind == "host_facts":
            memory = data.get("memory") or {}
            cpu = data.get("cpu") or {}
            if (memory.get("usage_pct") or 0) >= 90:
                add("内存耗尽导致进程被 OOM Killer 终止或频繁 GC",
                    f"内存使用率 {memory.get('usage_pct')}%，可用内存不足，"
                    "触发 OOM 或长时间 GC 停顿，表现为服务无响应", 0.7, ev_id)
            if (cpu.get("usage_pct") or 0) >= 90:
                add("CPU 饱和导致请求排队超时",
                    f"CPU 使用率 {cpu.get('usage_pct')}%，处理能力饱和，"
                    "请求在队列中积压直到超时", 0.65, ev_id)
            if (cpu.get("iowait_pct") or 0) >= 30:
                add("磁盘 IO 阻塞拖慢整体处理",
                    f"iowait 达 {cpu.get('iowait_pct')}%，CPU 大量时间在等待磁盘", 0.5, ev_id)
            for disk in data.get("disks_over_70pct") or []:
                if (disk.get("usage_pct") or 0) >= 90:
                    add(f"挂载点 {disk.get('mount')} 磁盘写满",
                        f"使用率 {disk.get('usage_pct')}%，写入失败会导致日志无法落盘、"
                        "临时文件创建失败，进而引发服务异常", 0.75, ev_id)
                if (disk.get("inode_usage_pct") or 0) >= 90:
                    add(f"挂载点 {disk.get('mount')} inode 耗尽",
                        f"inode 使用率 {disk.get('inode_usage_pct')}%，"
                        "磁盘看似有空间但无法创建新文件", 0.7, ev_id)

        if kind == "logs":
            text = " ".join(h.get("text", "") for h in (data.get("hits") or [])[:40])
            signatures = (
                ("OutOfMemoryError", "JVM 堆内存溢出", "日志出现 OutOfMemoryError，堆内存不足", 0.85),
                ("Too many open files", "文件描述符耗尽", "达到 fd 上限，无法建立新连接", 0.8),
                ("Connection refused", "依赖服务不可达", "下游端口无人监听，依赖已挂或未启动", 0.7),
                ("Communications link failure", "数据库连接中断", "与数据库的连接被断开", 0.7),
                ("connection pool", "连接池耗尽", "连接池无可用连接，请求排队直至超时", 0.65),
                ("Disk quota exceeded", "磁盘配额超限", "写入被配额拒绝", 0.7),
            )
            for needle, statement, mechanism, confidence in signatures:
                if needle.lower() in text.lower():
                    add(statement, mechanism, confidence, ev_id)

        if kind == "changes":
            records = data.get("changes") or []
            deploys = [r for r in records if r.get("task_type") in {"deploy", "upload"}]
            if deploys:
                add("近期变更引入故障",
                    f"故障前存在 {len(deploys)} 条部署/发布记录，"
                    "时间相关性提示可能是变更导致", 0.55, ev_id)

    # 去重：同一 statement 只留置信度最高的
    best: dict[str, Hypothesis] = {}
    for item in found:
        existing = best.get(item.statement)
        if existing is None or item.confidence > existing.confidence:
            best[item.statement] = item
    ranked = sorted(best.values(), key=lambda h: h.confidence, reverse=True)[:4]
    for index, item in enumerate(ranked, start=1):
        item.id = f"h{index}"

    if not ranked:
        ranked = [
            Hypothesis(
                id="h1",
                statement="现有证据不足以定位根因",
                mechanism="已采集的指标、日志与变更记录中没有出现已知的故障特征",
                confidence=0.2,
                verdict="insufficient",
            )
        ]
    return ranked


def _compact_evidence(evidence: list[dict]) -> str:
    lines = []
    for item in evidence:
        if item.get("error"):
            lines.append(f"[{item['id']}] {item['kind']} @{item.get('target', '')} 采集失败：{item['error']}")
            continue
        data = item.get("data") or {}
        if item["kind"] == "logs":
            hits = (data.get("hits") or [])[:12]
            body = " | ".join(h.get("text", "")[:180] for h in hits) or "无命中"
        else:
            body = str(data)[:1200]
        lines.append(f"[{item['id']}] {item['kind']} @{item.get('target', '')}：{body}")
    return "\n".join(lines)


def make_node(router, max_hypotheses: int):
    def diagnostician_node(state: dict) -> dict:
        with node_span(state, "diagnostician") as node_trace:
            evidence = state.get("evidence") or []
            triage = state.get("triage") or {}

            payload = try_llm_json(
                router, node_trace, node="diagnostician", system=SYSTEM,
                user=(
                    f"告警摘要：{triage.get('summary')}\n"
                    f"严重级别：{triage.get('severity')}\n\n"
                    f"证据清单：\n{_compact_evidence(evidence)}"
                ),
            )

            valid_ids = {item.get("id") for item in evidence}
            if payload is None:
                hypotheses = _rule_based(evidence)
            else:
                hypotheses = []
                for index, raw in enumerate((payload.get("hypotheses") or [])[:max_hypotheses], start=1):
                    if not isinstance(raw, dict) or not raw.get("statement"):
                        continue
                    # 过滤掉模型编造的证据 id，避免结论看起来有据实则悬空
                    supporting = [e for e in (raw.get("supporting_evidence") or []) if e in valid_ids]
                    hypotheses.append(
                        Hypothesis(
                            id=f"h{index}",
                            statement=str(raw["statement"]),
                            mechanism=str(raw.get("mechanism") or ""),
                            confidence=max(0.0, min(1.0, float(raw.get("confidence") or 0.0))),
                            supporting_evidence=supporting,
                            contradicting_evidence=[
                                e for e in (raw.get("contradicting_evidence") or []) if e in valid_ids
                            ],
                        )
                    )
                if not hypotheses:
                    hypotheses = _rule_based(evidence)
                    node_trace.status = "degraded"
                    node_trace.error = "模型未产出有效假设，已回退规则模式"

            hypotheses.sort(key=lambda h: h.confidence, reverse=True)
            top = hypotheses[0] if hypotheses else None

            return {
                "hypotheses": [h.model_dump() for h in hypotheses],
                "progress": progress(
                    state, "diagnostician",
                    f"生成 {len(hypotheses)} 个候选根因"
                    + (f"，首选：{top.statement}" if top else ""),
                    55,
                ),
            }

    return diagnostician_node
