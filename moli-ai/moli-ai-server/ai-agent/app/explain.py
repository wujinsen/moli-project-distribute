"""summarize 节点：/agent/explain。"""
from __future__ import annotations

from .models import ChartSuggestion, ExplainRequest, ExplainResponse


def explain_result(req: ExplainRequest) -> ExplainResponse:
    cols = [c.name for c in req.columns]
    sample = req.rowsSample[:5] if req.rowsSample else []
    row_count = req.rowCount or 0

    if row_count == 0:
        explanation = f"针对「{req.question}」，查询未返回数据行。"
        return ExplainResponse(explanation=explanation, chart=ChartSuggestion(type="none", title="无数据"))

    col_text = ", ".join(cols) if cols else "无"
    explanation = (
        f"针对「{req.question}」，共返回 {row_count} 行，结果列包括 {col_text}。"
    )
    if sample:
        explanation += f" 首行样例：{sample[0]}。"

    chart = ChartSuggestion(type="table", title=req.question[:40])
    lower_q = req.question.lower()
    if cols:
        if any(k in lower_q for k in ("趋势", "每日", "每天", "line", "时间", "近")):
            time_col = next(
                (c for c in cols if "time" in c.lower() or "date" in c.lower()), cols[0]
            )
            num_col = next(
                (c for c in cols if "count" in c.lower() or c in ("sold", "stock")), cols[-1]
            )
            chart = ChartSuggestion(type="line", x=time_col, y=[num_col], title="趋势")
        elif any(k in lower_q for k in ("占比", "比例", "pie", "分布")):
            chart = ChartSuggestion(type="pie", x=cols[0], y=cols[1:] if len(cols) > 1 else cols, title="占比")
        elif "count" in cols[0].lower() or "数量" in req.question or "多少" in req.question:
            chart = ChartSuggestion(type="bar", x=cols[0], y=[], title="统计")
        elif len(cols) >= 2:
            chart = ChartSuggestion(type="bar", x=cols[0], y=[cols[1]], title="对比")

    return ExplainResponse(explanation=explanation, chart=chart)
