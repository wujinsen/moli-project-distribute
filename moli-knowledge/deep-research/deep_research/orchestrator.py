from __future__ import annotations

import time
from pathlib import Path
from typing import Callable

from .config import DEFAULT_COVERAGE_THRESHOLD, HARD_MAX_SECTIONS, RUNS_DIR
from .kb_client import KbRestClient
from .models import (
    CitationItem,
    ProgressEvent,
    ResearchSidecarRequest,
    ResearchSidecarResponse,
    SectionEvidence,
)
from .planner import plan_outline
from .retriever import retrieve_section
from .reviewer import ReviewResult, review_report
from .writer import write_report

ProgressCallback = Callable[[ProgressEvent], None]


def _merge_citations(section_evidence: list[SectionEvidence]) -> list[CitationItem]:
    by_slug: dict[str, CitationItem] = {}
    for sec in section_evidence:
        for hit in sec.hits:
            key = hit.slug.strip().lower()
            if not key:
                continue
            item = by_slug.get(key)
            if item is None:
                item = CitationItem(slug=hit.slug, title=hit.title, sectionIds=[sec.section_id])
                by_slug[key] = item
            elif sec.section_id not in item.section_ids:
                item.section_ids.append(sec.section_id)
    return list(by_slug.values())


def _section_map(outline: dict) -> dict[str, dict]:
    return {str(s.get("id")): s for s in (outline.get("sections") or []) if s.get("id")}


def build_degraded_summary(
    topic: str,
    title: str,
    slug_hint: str,
    outline: dict,
    section_evidence: list[SectionEvidence],
) -> str:
    today = time.strftime("%Y-%m-%d")
    citations = _merge_citations(section_evidence)
    source_pages = [c.slug for c in citations]
    lines = [
        "---",
        f"title: {title}",
        f"slug: {slug_hint}",
        "type: output",
        "status: active",
        "tags: [deep-research, degraded]",
        f"query: {topic}",
        f"source_pages: {source_pages}",
        "sources: []",
        "related: []",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# {title}（降级摘要）",
        "",
        "> 调研超出时间预算，以下为 outline + 各节引用摘要。",
        "",
    ]
    sec_map = {sec.section_id: sec for sec in section_evidence}
    for section in outline.get("sections") or []:
        sec_id = section.get("id")
        heading = section.get("heading") or sec_id
        lines.append(f"## {heading}")
        lines.append("")
        sec = sec_map.get(sec_id)
        if not sec or not sec.hits:
            lines.append("- （本节无检索命中）")
        else:
            for hit in sec.hits[:8]:
                lines.append(f"- [[{hit.slug}]]：{(hit.snippet or hit.title or '')[:120]}")
        lines.append("")
    return "\n".join(lines)


def _save_run_artifact(run_id: str, report_md: str) -> None:
    if not report_md:
        return
    run_dir = RUNS_DIR / run_id
    run_dir.mkdir(parents=True, exist_ok=True)
    (run_dir / "report.md").write_text(report_md, encoding="utf-8")


def run_research(
    request: ResearchSidecarRequest,
    *,
    on_progress: ProgressCallback | None = None,
) -> ResearchSidecarResponse:
    """Full Phase B pipeline: Planner → Retriever → Writer → Reviewer (+ bounded backfill)."""
    start_ms = int(time.time() * 1000)
    progress: list[ProgressEvent] = []
    opts = request.options
    budget_ms = max(opts.latency_budget_ms, 1000)
    max_sections = min(max(opts.max_sections, 1), HARD_MAX_SECTIONS)
    max_rounds = min(max(opts.max_retrieve_rounds, 1), 3)
    coverage_threshold = getattr(opts, "coverage_threshold", DEFAULT_COVERAGE_THRESHOLD)

    def emit(phase: str, message: str, pct: int, section_id: str | None = None) -> None:
        evt = ProgressEvent(phase=phase, sectionId=section_id, message=message, pct=pct)
        progress.append(evt)
        if on_progress:
            on_progress(evt)

    def over_budget() -> bool:
        return (int(time.time() * 1000) - start_ms) >= budget_ms

    emit("planner", "规划大纲", 5)
    outline = plan_outline(request.topic, max_sections)
    title = outline.get("title") or request.topic
    slug_hint = outline.get("slugHint") or "deep-research-output"

    client = KbRestClient(auth_token=request.auth_token)
    section_evidence: list[SectionEvidence] = []
    sections = outline.get("sections") or []
    section_by_id = _section_map(outline)

    def retrieve_all() -> None:
        nonlocal section_evidence
        section_evidence = []
        total = max(len(sections), 1)
        for idx, section in enumerate(sections):
            if over_budget():
                break
            sec_id = section.get("id") or f"s{idx + 1}"
            pct = 15 + int(35 * (idx + 1) / total)
            emit("retriever", f"检索 {section.get('heading', sec_id)}", pct, sec_id)
            hits, queries_used = retrieve_section(
                section,
                client,
                space_id=request.space_id,
                space_ids=request.space_ids,
                top_k=opts.top_k,
                per_section_top_k=opts.per_section_top_k,
                retrieval_strategy=opts.retrieval_strategy,
                graph_expand=opts.graph_expand,
                agentic=opts.agentic,
            )
            section_evidence.append(
                SectionEvidence(sectionId=sec_id, hits=hits, queriesUsed=queries_used)
            )

    emit("retriever", "首轮分节检索", 15)
    retrieve_all()

    if over_budget() and not section_evidence:
        latency = int(time.time() * 1000) - start_ms
        summary = build_degraded_summary(request.topic, title, slug_hint, outline, section_evidence)
        _save_run_artifact(request.run_id, summary)
        return ResearchSidecarResponse(
            runId=request.run_id,
            status="DEGRADED",
            topic=request.topic,
            title=title,
            slug=slug_hint,
            outline=outline,
            sectionEvidence=section_evidence,
            citations=_merge_citations(section_evidence),
            reportMd=summary,
            progress=progress,
            latencyMs=latency,
            degraded=True,
            degradeReason="BUDGET",
        )

    citations = _merge_citations(section_evidence)
    report_md = ""
    review: ReviewResult | None = None
    round_num = 1

    while round_num <= max_rounds:
        if over_budget():
            break
        emit("writer", f"撰写报告（轮次 {round_num}）", 55 + round_num * 5)
        report_md = write_report(
            request.topic,
            title,
            slug_hint,
            outline,
            section_evidence,
            citations,
        )

        if over_budget():
            break
        emit("reviewer", "审校 grounding", 75 + round_num * 3)
        review = review_report(
            report_md,
            section_evidence,
            outline,
            coverage_threshold=coverage_threshold,
        )
        if review.accept or round_num >= max_rounds or not review.gaps:
            break
        if over_budget():
            break

        emit("retriever", f"Reviewer 回补检索（轮次 {round_num + 1}）", 80)
        for gap in review.gaps[:3]:
            sec_id = gap.get("sectionId")
            section = section_by_id.get(str(sec_id))
            if not section:
                continue
            backfill_section = dict(section)
            backfill_section["retrieveQueries"] = gap.get("queries") or section.get("retrieveQueries") or []
            hits, queries_used = retrieve_section(
                backfill_section,
                client,
                space_id=request.space_id,
                space_ids=request.space_ids,
                top_k=opts.top_k,
                per_section_top_k=opts.per_section_top_k,
                retrieval_strategy=opts.retrieval_strategy,
                graph_expand=opts.graph_expand,
                agentic=opts.agentic,
            )
            existing = next((s for s in section_evidence if s.section_id == sec_id), None)
            if existing:
                pool = {h.slug.lower(): h for h in existing.hits}
                for h in hits:
                    key = h.slug.lower()
                    prev = pool.get(key)
                    if prev is None or h.score > prev.score:
                        pool[key] = h
                existing.hits = sorted(pool.values(), key=lambda x: x.score, reverse=True)[
                    : opts.per_section_top_k
                ]
                existing.queries_used = list(dict.fromkeys(existing.queries_used + queries_used))
            else:
                section_evidence.append(
                    SectionEvidence(sectionId=str(sec_id), hits=hits, queriesUsed=queries_used)
                )
        citations = _merge_citations(section_evidence)
        round_num += 1

    degraded = False
    degrade_reason = None
    if over_budget():
        degraded = True
        degrade_reason = "BUDGET"
        report_md = build_degraded_summary(request.topic, title, slug_hint, outline, section_evidence)
    elif review and not review.accept and (review.coverage or 0) < coverage_threshold:
        degraded = True
        degrade_reason = "REVIEW"

    _save_run_artifact(request.run_id, report_md)
    latency = int(time.time() * 1000) - start_ms
    emit("writer", "完成", 98)

    status = "DEGRADED" if degraded else "SUCCEEDED"
    return ResearchSidecarResponse(
        runId=request.run_id,
        status=status,
        topic=request.topic,
        title=title,
        slug=slug_hint,
        outline=outline,
        sectionEvidence=section_evidence,
        citations=citations,
        reportMd=report_md,
        coverage=review.coverage if review else None,
        unsupportedStatements=review.unsupported if review else [],
        progress=progress,
        latencyMs=latency,
        degraded=degraded,
        degradeReason=degrade_reason,
    )


# Backward-compatible alias for Phase A tests
run_phase_a = run_research
