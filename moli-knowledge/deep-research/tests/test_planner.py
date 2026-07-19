import json

import pytest

from deep_research.planner import parse_outline_json, plan_outline, heuristic_outline


SAMPLE_JSON = """
{
  "title": "茉莉微服务架构调研",
  "slugHint": "茉莉微服务架构",
  "sections": [
    {
      "id": "s1",
      "heading": "总体架构",
      "subTopics": ["网关", "微服务"],
      "retrieveQueries": ["茉莉微服务架构", "moli 容器架构", "网关路由", "多余query"]
    },
    {
      "id": "s2",
      "heading": "知识库",
      "subTopics": ["hybrid"],
      "retrieveQueries": ["知识库 hybrid 检索"]
    }
  ]
}
"""


def test_parse_outline_json_caps_queries_and_sections():
    outline = parse_outline_json(SAMPLE_JSON, "茉莉微服务架构", max_sections=2)
    assert outline["title"] == "茉莉微服务架构调研"
    assert len(outline["sections"]) == 2
    assert len(outline["sections"][0]["retrieveQueries"]) <= 4
    assert outline["sections"][0]["retrieveQueries"][0] == "茉莉微服务架构"


def test_heuristic_outline_respects_max_sections():
    outline = heuristic_outline("茉莉微服务架构", max_sections=3)
    assert len(outline["sections"]) == 3
    for sec in outline["sections"]:
        assert sec["id"]
        assert sec["heading"]
        assert 1 <= len(sec["retrieveQueries"]) <= 4


def test_plan_outline_without_llm():
    outline = plan_outline("茉莉微服务架构", 4, use_llm=False)
    assert outline["sections"]
    assert outline["slugHint"]
