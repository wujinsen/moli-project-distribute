from __future__ import annotations

import re
from datetime import date

from .llm import chat
from .models import CitationItem, SectionEvidence

WRITER_SYSTEM = """你是知识库调研报告撰写者（Writer）。只依据【各节证据 hits】撰写 Markdown 正文。
规则：
1) 按大纲分节（## 节标题）；每条实质结论后标注 [[slug]]，slug 必须出现在该节或全局证据池中。
2) 证据不足处写「知识库暂无」并列出缺口，禁止臆造页名或补假引用。
3) 文首可有简短导语；不要输出与证据无关的营销套话。
4) 同时给出 frontmatter 字段草案：title、slug、query、source_pages（= 全文用到的 slug 去重列表）。
输出 Markdown 全文（含 YAML frontmatter），不要包在 JSON 里。"""


def _valid_slugs(section_evidence: list[SectionEvidence]) -> set[str]:
    slugs: set[str] = set()
    for sec in section_evidence:
        for hit in sec.hits:
            if hit.slug:
                slugs.add(hit.slug.strip())
    return slugs


def _extract_used_slugs(markdown: str, allowed: set[str]) -> list[str]:
    found: list[str] = []
    seen: set[str] = set()
    for match in re.finditer(r"\[\[([^\]]+)\]\]", markdown or ""):
        slug = match.group(1).strip()
        if slug in allowed and slug not in seen:
            seen.add(slug)
            found.append(slug)
    return found


def write_report_heuristic(
    topic: str,
    title: str,
    slug_hint: str,
    outline: dict,
    section_evidence: list[SectionEvidence],
    citations: list[CitationItem],
) -> str:
    allowed = _valid_slugs(section_evidence)
    source_pages = [c.slug for c in citations if c.slug in allowed]
    today = date.today().isoformat()
    lines = [
        "---",
        f"title: {title}",
        f"slug: {slug_hint}",
        "type: output",
        "status: active",
        "tags: [deep-research]",
        f"query: {topic}",
        f"source_pages: {source_pages}",
        "sources: []",
        "related: []",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# {title}",
        "",
        f"> DeepResearch 调研：{topic}",
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
            lines.append("知识库暂无足够证据支撑本节要点。")
            lines.append("")
            continue
        for hit in sec.hits[:5]:
            snippet = (hit.snippet or hit.title or "相关要点").strip()
            if len(snippet) > 160:
                snippet = snippet[:157] + "..."
            lines.append(f"- {snippet} [[{hit.slug}]]")
        lines.append("")
    body = "\n".join(lines)
    used = _extract_used_slugs(body, allowed)
    if used != source_pages:
        body = re.sub(
            r"source_pages: .*",
            f"source_pages: {used}",
            body,
            count=1,
        )
    return body


def write_report(
    topic: str,
    title: str,
    slug_hint: str,
    outline: dict,
    section_evidence: list[SectionEvidence],
    citations: list[CitationItem],
    *,
    use_llm: bool = True,
) -> str:
    if use_llm:
        try:
            user = _build_writer_user_prompt(topic, title, slug_hint, outline, section_evidence)
            raw = chat(WRITER_SYSTEM, user)
            allowed = _valid_slugs(section_evidence)
            used = _extract_used_slugs(raw, allowed)
            if used:
                return raw
        except Exception:
            pass
    return write_report_heuristic(topic, title, slug_hint, outline, section_evidence, citations)


def _build_writer_user_prompt(
    topic: str,
    title: str,
    slug_hint: str,
    outline: dict,
    section_evidence: list[SectionEvidence],
) -> str:
    parts = [
        f"【主题】{topic}",
        f"【title】{title}",
        f"【slugHint】{slug_hint}",
        "【大纲】",
        str(outline),
        "【各节证据 hits】",
    ]
    for sec in section_evidence:
        parts.append(f"### sectionId={sec.section_id}")
        for hit in sec.hits:
            parts.append(f"- [[{hit.slug}]] {hit.snippet}")
    return "\n".join(parts)
