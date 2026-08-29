"""证伪：逐条挑战候选根因，决定是定稿还是回补取证。

这一步的价值不在于确认，而在于**否定**。LLM 天然倾向于给出自洽的解释，
一个只会点头的流程会把第一个听起来合理的猜想直接送去执行。
所以 Critic 的提示词要求它优先找反证，并且允许它判定「证据不足」把流程打回去。
"""

from __future__ import annotations

from .base import node_span, progress, try_llm_json

SYSTEM = """你是事故复盘评审，职责是**挑战**候选根因，不是确认它们。

对每个假设逐条判断：
- confirmed：证据充分且没有明显反证
- refuted：存在与之矛盾的证据
- insufficient：证据不足以判断

只输出 JSON：
{
  "verdicts": [{"id": "h1", "verdict": "confirmed|refuted|insufficient", "note": "判断理由，指出具体证据"}],
  "accepted_hypothesis_id": "被采信的假设 id，没有则填空字符串",
  "need_more_evidence": true/false,
  "backfill_queries": ["若需回补，给出具体的日志检索正则，最多 3 条"]
}

判断准则：
- 只有当因果链能解释全部主要现象时才给 confirmed
- 「时间上恰好在故障前发生」不等于因果关系，仅凭相关性不能 confirmed
- 宁可判 insufficient 要求回补，也不要勉强采信一个解释不通的假设
- 全部假设都不成立时 accepted_hypothesis_id 留空并要求回补"""


def _rule_based(hypotheses: list[dict], round_index: int, max_rounds: int) -> dict:
    """兜底：置信度够高就采信，否则回补一轮；轮次用尽就带着不确定性定稿。"""
    if not hypotheses:
        return {"accepted": "", "need_more": round_index < max_rounds, "verdicts": [],
                "queries": [], "note": "没有任何候选根因"}

    top = hypotheses[0]
    confident = float(top.get("confidence") or 0) >= 0.6
    verdicts = [
        {
            "id": h.get("id"),
            "verdict": "confirmed" if (h is top and confident) else "insufficient",
            "note": "规则模式：按置信度阈值 0.6 判定",
        }
        for h in hypotheses
    ]
    if confident:
        return {"accepted": top.get("id", ""), "need_more": False, "verdicts": verdicts,
                "queries": [], "note": ""}
    if round_index < max_rounds:
        return {
            "accepted": "", "need_more": True, "verdicts": verdicts,
            "queries": ["ERROR|FATAL|Exception|Caused by", "refused|timeout|unreachable"],
            "note": "置信度不足，回补取证",
        }
    return {"accepted": top.get("id", ""), "need_more": False, "verdicts": verdicts,
            "queries": [], "note": "回补轮次已用尽，带不确定性采信最高置信度假设"}


def _compact_evidence(evidence: list[dict]) -> str:
    lines = []
    for item in evidence:
        if item.get("error"):
            lines.append(f"[{item['id']}] {item['kind']} 采集失败：{item['error']}")
            continue
        data = item.get("data") or {}
        if item["kind"] == "logs":
            hits = (data.get("hits") or [])[:10]
            body = " | ".join(h.get("text", "")[:150] for h in hits) or "无命中"
        else:
            body = str(data)[:900]
        lines.append(f"[{item['id']}] {item['kind']} @{item.get('target', '')}：{body}")
    return "\n".join(lines)


def make_node(router, max_rounds: int):
    def critic_node(state: dict) -> dict:
        with node_span(state, "critic") as node_trace:
            hypotheses = list(state.get("hypotheses") or [])
            evidence = state.get("evidence") or []
            round_index = int(state.get("iteration") or 0) + 1

            payload = try_llm_json(
                router, node_trace, node="critic", system=SYSTEM,
                user=(
                    "候选根因：\n"
                    + "\n".join(
                        f"[{h.get('id')}] {h.get('statement')}"
                        f"（置信度 {h.get('confidence')}）\n  因果链：{h.get('mechanism')}"
                        f"\n  支撑证据：{h.get('supporting_evidence')}"
                        for h in hypotheses
                    )
                    + f"\n\n证据清单：\n{_compact_evidence(evidence)}"
                    + f"\n\n当前是第 {round_index} 轮，最多 {max_rounds} 轮回补。"
                ),
            )

            if payload is None:
                decision = _rule_based(hypotheses, round_index, max_rounds)
            else:
                verdicts = payload.get("verdicts") or []
                decision = {
                    "accepted": str(payload.get("accepted_hypothesis_id") or ""),
                    "need_more": bool(payload.get("need_more_evidence")),
                    "verdicts": verdicts,
                    "queries": [str(q) for q in (payload.get("backfill_queries") or [])][:3],
                    "note": "",
                }

            verdict_by_id = {
                str(v.get("id")): v for v in decision["verdicts"] if isinstance(v, dict)
            }
            for item in hypotheses:
                verdict = verdict_by_id.get(str(item.get("id")))
                if verdict:
                    item["verdict"] = str(verdict.get("verdict") or "open")
                    item["critic_note"] = str(verdict.get("note") or "")

            # 轮次用尽就必须定稿，否则会无限回补
            need_more = decision["need_more"] and round_index < max_rounds
            accepted = decision["accepted"]
            if not need_more and not accepted and hypotheses:
                accepted = hypotheses[0].get("id", "")

            message = (
                f"第 {round_index} 轮证伪：回补取证"
                if need_more
                else f"第 {round_index} 轮证伪：采信 {accepted or '无'}"
            )

            return {
                "hypotheses": hypotheses,
                "accepted_hypothesis_id": accepted,
                "need_more_evidence": need_more,
                "backfill_queries": decision["queries"] if need_more else [],
                "iteration": round_index,
                "progress": progress(state, "critic", message, 65, accepted=accepted),
            }

    return critic_node
