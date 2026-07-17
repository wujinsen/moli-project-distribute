#!/usr/bin/env python3
"""FastAPI entry: uvicorn app.main:app --host 0.0.0.0 --port ${RETRIEVAL_PORT:-8099}"""
from __future__ import annotations

import logging

from fastapi import FastAPI

from .config import CHROMA_PATH, COLLECTION_NAME, EMBED_DIM, MAX_TEXT_CHARS, RETRIEVAL_PORT
from .errors import RetrievalError, retrieval_error_handler
from . import chroma_store, embedding
from .models import (
    EmbedRequest,
    EmbedResponse,
    HealthResponse,
    SearchRequest,
    SearchResponse,
    SearchHit,
    filter_to_lists,
)

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
log = logging.getLogger("kb-retrieval")

app = FastAPI(title="Moli KB Retrieval Sidecar", version="0.1.0")
app.add_exception_handler(RetrievalError, retrieval_error_handler)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        model=embedding.model_name(),
        dim=EMBED_DIM,
        collection=COLLECTION_NAME,
        indexedChunks=chroma_store.count_chunks(),
        chromaPath=str(CHROMA_PATH),
        modelLoaded=embedding.is_model_loaded(),
    )


@app.post("/embed", response_model=EmbedResponse)
def embed_batch(body: EmbedRequest) -> EmbedResponse:
    deleted = chroma_store.delete_chunks(body.deleteChunkIds)

    if not body.items:
        return EmbedResponse(
            model=embedding.model_name(),
            dim=EMBED_DIM,
            upserted=0,
            skipped=0,
            deleted=deleted,
        )

    chunk_ids = [it.chunkId for it in body.items]
    existing = chroma_store.get_existing_hashes(chunk_ids)

    to_embed: list[dict] = []
    skipped = 0
    for it in body.items:
        prev = existing.get(it.chunkId)
        if not body.force and prev == it.contentHash:
            skipped += 1
            continue
        row = it.model_dump()
        if len(row["text"]) > MAX_TEXT_CHARS:
            row["text"] = row["text"][:MAX_TEXT_CHARS]
        to_embed.append(row)

    upserted = 0
    if to_embed:
        texts = [row["text"] for row in to_embed]
        vectors = embedding.embed_texts(texts)
        chroma_store.upsert_chunks(to_embed, vectors)
        upserted = len(to_embed)

    log.info("embed upserted=%s skipped=%s deleted=%s", upserted, skipped, deleted)
    return EmbedResponse(
        model=embedding.model_name(),
        dim=EMBED_DIM,
        upserted=upserted,
        skipped=skipped,
        deleted=deleted,
    )


@app.post("/search", response_model=SearchResponse)
def search(body: SearchRequest) -> SearchResponse:
    query = (body.query or "").strip()
    if not query:
        raise RetrievalError("invalid_query", "query 不能为空", 400)

    kb_types, exclude_kb_types = filter_to_lists(body.filter)
    qvec = embedding.embed_texts([query])[0]
    hits = chroma_store.query_chunks(
        qvec,
        top_n=body.topN,
        space_ids=body.spaceIds or None,
        kb_types=kb_types,
        exclude_kb_types=exclude_kb_types,
    )
    return SearchResponse(
        model=embedding.model_name(),
        results=[SearchHit(**h) for h in hits],
    )


def main() -> None:
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=RETRIEVAL_PORT,
        reload=False,
    )


if __name__ == "__main__":
    main()
