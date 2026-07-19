from __future__ import annotations

from typing import Any

from .config import MAX_QUERIES_PER_SECTION
from .kb_client import KbRestClient
from .models import EvidenceHit


def _norm_slug(slug: str) -> str:
    return (slug or "").strip().lstrip("/").lower()


def _hits_from_ask_response(data: dict[str, Any], rank_offset: int = 0) -> list[EvidenceHit]:
    citations = data.get("citations") or []
    hits: list[EvidenceHit] = []
    for idx, cite in enumerate(citations, start=1):
        slug = (cite.get("slug") or "").strip()
        if not slug:
            continue
        rank = rank_offset + idx
        score = max(0.01, 1.0 - (rank - 1) * 0.05)
        hits.append(
            EvidenceHit(
                slug=slug,
                snippet=(cite.get("snippet") or "")[:500],
                score=score,
                docId=cite.get("docId"),
                title=cite.get("title"),
            )
        )
    return hits


def merge_hits(existing: dict[str, EvidenceHit], new_hits: list[EvidenceHit]) -> None:
    for hit in new_hits:
        key = _norm_slug(hit.slug)
        if not key:
            continue
        prev = existing.get(key)
        if prev is None or hit.score > prev.score:
            existing[key] = hit


def retrieve_section(
    section: dict[str, Any],
    client: KbRestClient,
    *,
    space_id: int | None,
    space_ids: list[int] | None,
    top_k: int,
    per_section_top_k: int,
    retrieval_strategy: str,
    graph_expand: bool | None,
    agentic: bool,
) -> tuple[list[EvidenceHit], list[str]]:
    queries = [str(q).strip() for q in (section.get("retrieveQueries") or []) if str(q).strip()]
    queries = queries[:MAX_QUERIES_PER_SECTION]
    pool: dict[str, EvidenceHit] = {}
    used: list[str] = []
    for q in queries:
        used.append(q)
        data = client.ask(
            q,
            space_id=space_id,
            space_ids=space_ids,
            top_k=top_k,
            retrieval_strategy=retrieval_strategy,
            graph_expand=graph_expand,
            use_llm=False,
            agentic=agentic,
        )
        merge_hits(pool, _hits_from_ask_response(data))
    ordered = sorted(pool.values(), key=lambda h: h.score, reverse=True)
    return ordered[:per_section_top_k], used
