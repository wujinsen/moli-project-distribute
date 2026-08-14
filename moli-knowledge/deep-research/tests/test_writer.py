import re

from deep_research.models import CitationItem, EvidenceHit, SectionEvidence
from deep_research.writer import write_report_heuristic, _extract_used_slugs, _valid_slugs


def _sample_evidence():
    hits = [
        EvidenceHit(slug="develop/用户中心", snippet="用户中心鉴权", score=0.9, title="用户中心"),
        EvidenceHit(slug="develop/网关", snippet="网关路由", score=0.8, title="网关"),
    ]
    section_evidence = [SectionEvidence(sectionId="s1", hits=hits)]
    citations = [
        CitationItem(slug="develop/用户中心", title="用户中心", sectionIds=["s1"]),
        CitationItem(slug="develop/网关", title="网关", sectionIds=["s1"]),
    ]
    outline = {
        "title": "茉莉微服务架构调研",
        "slugHint": "茉莉微服务架构",
        "sections": [{"id": "s1", "heading": "总体架构", "subTopics": ["网关"]}],
    }
    return outline, section_evidence, citations


def test_writer_heuristic_only_uses_evidence_pool_slugs():
    outline, section_evidence, citations = _sample_evidence()
    md = write_report_heuristic(
        "茉莉微服务架构",
        "茉莉微服务架构调研",
        "茉莉微服务架构",
        outline,
        section_evidence,
        citations,
    )
    allowed = _valid_slugs(section_evidence)
    used = _extract_used_slugs(md, allowed)
    assert used
    assert all(slug in allowed for slug in used)
    assert "type: output" in md
    assert "query: 茉莉微服务架构" in md
    assert "source_pages:" in md


def test_writer_frontmatter_source_pages_match_wikilinks():
    outline, section_evidence, citations = _sample_evidence()
    md = write_report_heuristic(
        "茉莉微服务架构",
        "茉莉微服务架构调研",
        "茉莉微服务架构",
        outline,
        section_evidence,
        citations,
    )
    wikilinks = re.findall(r"\[\[([^\]]+)\]\]", md)
    match = re.search(r"source_pages: \[(.*)\]", md)
    assert match
    listed = [s.strip().strip("'\"") for s in match.group(1).split(",") if s.strip()]
    assert set(listed).issubset(set(wikilinks))
