"""Chroma persistence for kb_document_chunk vectors."""
from __future__ import annotations

from typing import Any

import chromadb

from .config import CHROMA_PATH, COLLECTION_NAME, EMBED_DIM

_client: chromadb.PersistentClient | None = None
_collection = None


def get_collection():
    global _client, _collection
    if _collection is not None:
        return _collection
    CHROMA_PATH.mkdir(parents=True, exist_ok=True)
    _client = chromadb.PersistentClient(path=str(CHROMA_PATH))
    _collection = _client.get_or_create_collection(
        name=COLLECTION_NAME,
        metadata={"hnsw:space": "cosine", "embed_dim": str(EMBED_DIM)},
    )
    return _collection


def chunk_text(heading: str | None, content: str) -> str:
    parts = []
    if heading and heading.strip():
        parts.append(heading.strip())
    if content and content.strip():
        parts.append(content.strip())
    return "\n".join(parts)


def metadata_from_item(item: dict[str, Any]) -> dict[str, str | int | float]:
    meta: dict[str, str | int | float] = {
        "chunkId": int(item["chunkId"]),
        "docId": int(item["docId"]),
        "spaceId": int(item["spaceId"]),
        "slug": str(item["slug"]),
        "contentHash": str(item["contentHash"]),
    }
    if item.get("kbType") is not None:
        meta["kbType"] = str(item["kbType"])
    if item.get("categoryId") is not None:
        meta["categoryId"] = int(item["categoryId"])
    return meta


def get_existing_hashes(chunk_ids: list[int]) -> dict[int, str]:
    if not chunk_ids:
        return {}
    col = get_collection()
    ids = [str(i) for i in chunk_ids]
    try:
        got = col.get(ids=ids, include=["metadatas"])
    except Exception:  # noqa: BLE001
        return {}
    out: dict[int, str] = {}
    for cid, meta in zip(got.get("ids") or [], got.get("metadatas") or [], strict=False):
        if meta and "contentHash" in meta:
            out[int(cid)] = str(meta["contentHash"])
    return out


def upsert_chunks(
    items: list[dict[str, Any]],
    embeddings: list[list[float]],
) -> None:
    col = get_collection()
    ids = [str(it["chunkId"]) for it in items]
    documents = [str(it["text"]) for it in items]
    metadatas = [metadata_from_item(it) for it in items]
    col.upsert(ids=ids, embeddings=embeddings, documents=documents, metadatas=metadatas)


def delete_chunks(chunk_ids: list[int]) -> int:
    if not chunk_ids:
        return 0
    col = get_collection()
    ids = [str(i) for i in chunk_ids]
    col.delete(ids=ids)
    return len(ids)


def list_indexed_chunk_ids() -> set[int]:
    col = get_collection()
    try:
        got = col.get(include=[])
    except Exception:  # noqa: BLE001
        return set()
    ids = got.get("ids") or []
    return {int(i) for i in ids}


def count_chunks() -> int:
    return get_collection().count()


def build_where_filter(
    space_ids: list[int] | None,
    kb_types: list[str] | None,
    exclude_kb_types: list[str] | None,
) -> dict[str, Any] | None:
    clauses: list[dict[str, Any]] = []
    if space_ids:
        clauses.append({"spaceId": {"$in": [int(s) for s in space_ids]}})
    if kb_types:
        clauses.append({"kbType": {"$in": [str(t) for t in kb_types]}})
    if exclude_kb_types:
        clauses.append({"kbType": {"$nin": [str(t) for t in exclude_kb_types]}})
    if not clauses:
        return None
    if len(clauses) == 1:
        return clauses[0]
    return {"$and": clauses}


def query_chunks(
    query_embedding: list[float],
    top_n: int,
    space_ids: list[int] | None,
    kb_types: list[str] | None,
    exclude_kb_types: list[str] | None,
) -> list[dict[str, Any]]:
    col = get_collection()
    where = build_where_filter(space_ids, kb_types, exclude_kb_types)
    kwargs: dict[str, Any] = {
        "query_embeddings": [query_embedding],
        "n_results": max(1, top_n),
        "include": ["metadatas", "distances"],
    }
    if where:
        kwargs["where"] = where
    raw = col.query(**kwargs)
    metas = (raw.get("metadatas") or [[]])[0]
    dists = (raw.get("distances") or [[]])[0]
    results: list[dict[str, Any]] = []
    for rank, (meta, dist) in enumerate(zip(metas, dists, strict=False), start=1):
        if not meta:
            continue
        # cosine distance in Chroma → similarity score ≈ 1 - dist (vectors normalized)
        score = round(max(0.0, 1.0 - float(dist)), 4)
        results.append(
            {
                "chunkId": int(meta["chunkId"]),
                "docId": int(meta["docId"]),
                "spaceId": int(meta["spaceId"]),
                "slug": str(meta.get("slug", "")),
                "kbType": str(meta.get("kbType", "")),
                "score": score,
                "rank": rank,
            }
        )
    return results
