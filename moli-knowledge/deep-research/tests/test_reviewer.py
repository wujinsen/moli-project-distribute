from deep_research.models import EvidenceHit, SectionEvidence
from deep_research.reviewer import parse_review_json, review_heuristic, review_report


def _report_with_supported_and_unsupported():
    md = """---
title: t
slug: s
type: output
query: q
source_pages: [develop/a]
---
# Title

- 网关说明 [[develop/a]]
- 无引用陈述应标 unsupported
"""
    evidence = [
        SectionEvidence(
            sectionId="s1",
            hits=[EvidenceHit(slug="develop/a", snippet="网关说明", score=0.9)],
        )
    ]
    outline = {"sections": [{"id": "s1", "heading": "架构"}]}
    return md, evidence, outline


def test_parse_review_json_coverage_from_supported_unsupported():
    raw = """
    {
      "supported": ["a", "b"],
      "unsupported": ["c"],
      "gaps": [],
      "accept": true
    }
    """
    result = parse_review_json(raw)
    assert result.coverage == 2 / 3
    assert result.unsupported == ["c"]
    assert result.accept is True


def test_review_heuristic_flags_unsupported_lines():
    md, evidence, outline = _report_with_supported_and_unsupported()
    result = review_heuristic(md, evidence, outline, coverage_threshold=0.75)
    assert result.unsupported
    assert result.coverage is not None
    assert result.coverage < 1.0


def test_review_report_without_llm():
    md, evidence, outline = _report_with_supported_and_unsupported()
    result = review_report(
        md,
        evidence,
        outline,
        coverage_threshold=0.75,
        use_llm=False,
    )
    assert result.unsupported
    assert result.coverage is not None
