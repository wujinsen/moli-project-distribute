from __future__ import annotations

import json
import re
from dataclasses import dataclass, field

from .llm import chat

REVIEWER_SYSTEM = """你是调研报告审校者（Reviewer）。给定【报告 Markdown】与【可用证据 hits(slug+片段)】：
1) 将正文拆成原子陈述，判定是否被某 slug 片段直接支撑。
2) 输出 coverage = supported/(supported+unsupported)；分母 0 时 coverage=1。
3) 对 unsupported / 明显缺口，给出 gaps：建议回补的 sectionId + retrieveQueries（≤3/节）。
4) 不得改写或删除原文句子；只输出审校 JSON。
只输出 JSON：
{
  "coverage": 0.0,
  "unsupported": ["陈述…"],
  "gaps": [{"sectionId":"s2","queries":["…"]}],
  "accept": true
}"""


@dataclass
class ReviewResult:
    coverage: float = 1.0
    unsupported: list[str] = field(default_factory=list)
    gaps: list[dict] = field(default_factory=list)
    accept: bool = True
    parse_failed: bool = False


def parse_review_json(raw: str) -> ReviewResult:
    result = ReviewResult()
    if not raw or not raw.strip():
        result.parse_failed = True
        result.coverage = None  # type: ignore[assignment]
        return result
    text = raw.strip()
    if text.startswith("```"):
        start = text.find("{")
        end = text.rfind("}")
        if start >= 0 and end > start:
            text = text[start : end + 1]
    try:
        obj = json.loads(text)
        sup = obj.get("supported") or []
        uns = obj.get("unsupported") or []
        if sup or uns:
            total = len(sup) + len(uns)
            result.coverage = len(sup) / total if total else 1.0
        else:
            result.coverage = float(obj.get("coverage", 1.0))
        result.unsupported = [str(x).strip() for x in uns if str(x).strip()]
        gaps = obj.get("gaps") or []
        parsed_gaps: list[dict] = []
        for g in gaps:
            if isinstance(g, dict) and g.get("sectionId"):
                queries = [str(q).strip() for q in (g.get("queries") or []) if str(q).strip()]
                parsed_gaps.append({"sectionId": g.get("sectionId"), "queries": queries[:3]})
        result.gaps = parsed_gaps
        result.accept = bool(obj.get("accept", False))
        return result
    except Exception:
        result.parse_failed = True
        result.coverage = None  # type: ignore[assignment]
        return result


def review_heuristic(
    report_md: str,
    section_evidence,
    outline: dict,
    *,
    coverage_threshold: float = 0.75,
) -> ReviewResult:
    allowed_slugs = set()
    for sec in section_evidence:
        for hit in sec.hits:
            if hit.slug:
                allowed_slugs.add(hit.slug.strip())

    unsupported: list[str] = []
    supported_count = 0
    for line in (report_md or "").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or stripped.startswith("---"):
            continue
        if stripped.startswith(">"):
            continue
        if stripped.startswith("source_pages:") or stripped.startswith("query:"):
            continue
        if len(stripped) < 8:
            continue
        slugs_in_line = re.findall(r"\[\[([^\]]+)\]\]", stripped)
        valid = [s for s in slugs_in_line if s.strip() in allowed_slugs]
        if valid:
            supported_count += 1
        elif "知识库暂无" in stripped:
            supported_count += 1
        else:
            unsupported.append(stripped[:200])

    total = supported_count + len(unsupported)
    coverage = 1.0 if total == 0 else supported_count / total
    gaps: list[dict] = []
    if unsupported:
        sections = outline.get("sections") or []
        if sections:
            sec_id = sections[0].get("id") or "s1"
            gaps.append({"sectionId": sec_id, "queries": unsupported[:2]})
    accept = coverage >= coverage_threshold and not gaps
    return ReviewResult(
        coverage=coverage,
        unsupported=unsupported,
        gaps=gaps,
        accept=accept,
    )


def review_report(
    report_md: str,
    section_evidence,
    outline: dict,
    *,
    coverage_threshold: float = 0.75,
    use_llm: bool = True,
) -> ReviewResult:
    if use_llm:
        try:
            user = _build_reviewer_prompt(report_md, section_evidence)
            raw = chat(REVIEWER_SYSTEM, user)
            parsed = parse_review_json(raw)
            if not parsed.parse_failed:
                if parsed.coverage is None:
                    parsed.coverage = 1.0
                if parsed.coverage >= coverage_threshold and not parsed.gaps:
                    parsed.accept = True
                elif parsed.coverage >= coverage_threshold and not parsed.unsupported:
                    parsed.accept = True
                return parsed
        except Exception:
            pass
    result = review_heuristic(
        report_md, section_evidence, outline, coverage_threshold=coverage_threshold
    )
    if result.coverage is not None and result.coverage >= coverage_threshold and not result.gaps:
        result.accept = True
    return result


def _build_reviewer_prompt(report_md: str, section_evidence) -> str:
    parts = ["【报告 Markdown】", report_md, "", "【可用证据 hits】"]
    for sec in section_evidence:
        parts.append(f"sectionId={sec.section_id}")
        for hit in sec.hits:
            parts.append(f"- [[{hit.slug}]] {hit.snippet}")
    return "\n".join(parts)
