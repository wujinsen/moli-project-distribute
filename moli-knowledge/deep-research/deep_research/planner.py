from __future__ import annotations

import json
import re
from typing import Any

import httpx

from .config import HARD_MAX_SECTIONS, MAX_QUERIES_PER_SECTION, OPENAI_API_KEY, OPENAI_BASE_URL, OPENAI_MODEL

PLANNER_SYSTEM = """你是企业知识库调研规划器（Planner）。给定【主题】与【maxSections】，产出适合分节检索的大纲。
规则：
- 节数 ≤ maxSections；每节 1~4 个 retrieveQueries（短、可检索、不臆造未给定实体）。
- subTopics 为节内要点关键词，供 Writer 覆盖。
- 不写正文、不编造引用页。
只输出 JSON：
{
  "title": "报告标题",
  "slugHint": "短横线或中文短 slug 建议",
  "sections": [
    {
      "id": "s1",
      "heading": "节标题",
      "subTopics": ["…"],
      "retrieveQueries": ["…", "…"]
    }
  ]
}"""


class PlannerError(Exception):
    pass


def _extract_json(raw: str) -> dict[str, Any]:
    text = (raw or "").strip()
    if text.startswith("```"):
        start = text.find("{")
        end = text.rfind("}")
        if start >= 0 and end > start:
            text = text[start : end + 1]
    return json.loads(text)


def _normalize_outline(data: dict[str, Any], topic: str, max_sections: int) -> dict[str, Any]:
    title = (data.get("title") or topic or "调研报告").strip()
    slug_hint = (data.get("slugHint") or _slug_hint_from_title(title)).strip()
    sections_in = data.get("sections") or []
    if not isinstance(sections_in, list) or not sections_in:
        raise PlannerError("sections empty")

    cap = min(max_sections, HARD_MAX_SECTIONS)
    sections: list[dict[str, Any]] = []
    for idx, sec in enumerate(sections_in[:cap], start=1):
        if not isinstance(sec, dict):
            continue
        sec_id = (sec.get("id") or f"s{idx}").strip()
        heading = (sec.get("heading") or f"第{idx}节").strip()
        sub_topics = [str(x).strip() for x in (sec.get("subTopics") or []) if str(x).strip()]
        queries = [str(x).strip() for x in (sec.get("retrieveQueries") or []) if str(x).strip()]
        if not queries:
            queries = [topic] if idx == 1 else [f"{topic} {heading}"]
        queries = queries[:MAX_QUERIES_PER_SECTION]
        if not sub_topics:
            sub_topics = [heading]
        sections.append(
            {
                "id": sec_id,
                "heading": heading,
                "subTopics": sub_topics,
                "retrieveQueries": queries,
            }
        )
    if not sections:
        raise PlannerError("no valid sections")
    return {"title": title, "slugHint": slug_hint, "sections": sections}


def _slug_hint_from_title(title: str) -> str:
    cleaned = re.sub(r"\s+", "-", title.strip())
    return cleaned[:40] or "deep-research-output"


def heuristic_outline(topic: str, max_sections: int) -> dict[str, Any]:
    """Deterministic demo outline when LLM unavailable."""
    cap = min(max_sections, HARD_MAX_SECTIONS)
    base = topic.strip() or "茉莉微服务架构"
    templates = [
        ("s1", "总体架构与模块划分", ["微服务", "模块", "拓扑"], [base, f"{base} 架构图", f"{base} 服务列表"]),
        ("s2", "网关与路由", ["网关", "路由", "鉴权"], [f"{base} 网关", "moli-gateway 路由", "API 网关配置"]),
        ("s3", "核心服务协作", ["Dubbo", "调用链", "依赖"], [f"{base} 用户中心", f"{base} 订单", "服务依赖关系"]),
        ("s4", "知识库与 AI 能力", ["知识库", "检索", "问答"], ["知识库 hybrid 检索", "kb/ask", "Agentic RAG"]),
        ("s5", "部署与运维", ["部署", "运维", "发布"], [f"{base} 部署", "本地启动", "运维 Runbook"]),
        ("s6", "演进路线", ["路线图", "排期", "契约"], ["AI 能力路线图", "能力排期", "微服务 README"]),
    ]
    sections = []
    for sec_id, heading, subs, queries in templates[:cap]:
        sections.append(
            {
                "id": sec_id,
                "heading": heading,
                "subTopics": subs,
                "retrieveQueries": queries[:MAX_QUERIES_PER_SECTION],
            }
        )
    return {
        "title": f"{base}调研报告",
        "slugHint": _slug_hint_from_title(base),
        "sections": sections,
    }


def plan_outline(topic: str, max_sections: int, *, use_llm: bool = True) -> dict[str, Any]:
    cap = min(max(max_sections, 1), HARD_MAX_SECTIONS)
    if use_llm and OPENAI_API_KEY:
        user_prompt = f"【主题】{topic}\n【maxSections】{cap}"
        try:
            raw = _call_openai_compatible(PLANNER_SYSTEM, user_prompt)
            parsed = _extract_json(raw)
            return _normalize_outline(parsed, topic, cap)
        except Exception:
            pass
    return _normalize_outline(heuristic_outline(topic, cap), topic, cap)


def _call_openai_compatible(system: str, user: str) -> str:
    url = f"{OPENAI_BASE_URL}/chat/completions"
    payload = {
        "model": OPENAI_MODEL,
        "temperature": 0.2,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
    }
    headers = {"Authorization": f"Bearer {OPENAI_API_KEY}", "Content-Type": "application/json"}
    with httpx.Client(timeout=60.0) as client:
        resp = client.post(url, json=payload, headers=headers)
        resp.raise_for_status()
        body = resp.json()
    choices = body.get("choices") or []
    if not choices:
        raise PlannerError("empty LLM response")
    return str((choices[0].get("message") or {}).get("content") or "")


def parse_outline_json(raw: str, topic: str, max_sections: int) -> dict[str, Any]:
    """Public helper for tests."""
    return _normalize_outline(_extract_json(raw), topic, max_sections)
