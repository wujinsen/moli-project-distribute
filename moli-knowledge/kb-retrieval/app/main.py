#!/usr/bin/env python3
"""FastAPI entry: uvicorn app.main:app --host 0.0.0.0 --port ${RETRIEVAL_PORT:-8099}"""
from __future__ import annotations

import logging

from fastapi import FastAPI

from .config import (
    CHROMA_PATH,
    COLLECTION_NAME,
    EMBED_DIM,
    MAX_TEXT_CHARS,
    RETRIEVAL_HOST,
    RETRIEVAL_PORT,
)
from .errors import RetrievalError, retrieval_error_handler
from . import chroma_store, embedding, rerank
from .models import (
    EmbedQueryRequest,
    EmbedQueryResponse,
    EmbedRequest,
    EmbedResponse,
    HealthResponse,
    RerankRequest,
    RerankResponse,
    RerankHit,
    SearchRequest,
    SearchResponse,
    SearchHit,
    filter_to_lists,
)

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
log = logging.getLogger("kb-retrieval")

app = FastAPI(title="Moli KB Retrieval Sidecar", version="0.1.0")
app.add_exception_handler(RetrievalError, retrieval_error_handler)


@app.on_event("startup")
def warmup_models() -> None:
    """S2：启动预热 embedding/rerank，避免首查冷加载超过 Java timeout-ms 误降级。"""
    try:
        embedding.embed_texts(["warmup"])
        log.info("embedding model warmed up")
    except Exception as exc:  # noqa: BLE001
        log.warning("embedding warmup failed: %s", exc)
    try:
        rerank.rerank_pairs("warmup", ["warmup"])
        log.info("rerank model warmed up")
    except Exception as exc:  # noqa: BLE001
        log.warning("rerank warmup failed: %s", exc)


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
        rerankModel=rerank.model_name(),
        rerankModelLoaded=rerank.is_model_loaded(),
        device=embedding.device_name(),
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


@app.post("/embed-query", response_model=EmbedQueryResponse)
def embed_query(body: EmbedQueryRequest) -> EmbedQueryResponse:
    text = (body.text or "").strip()
    if not text:
        raise RetrievalError("invalid_query", "text 不能为空", 400)
    if len(text) > MAX_TEXT_CHARS:
        text = text[:MAX_TEXT_CHARS]
    vec = embedding.embed_texts([text])[0]
    return EmbedQueryResponse(
        model=embedding.model_name(),
        dim=EMBED_DIM,
        embedding=vec,
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


@app.post("/rerank", response_model=RerankResponse)
def rerank_candidates(body: RerankRequest) -> RerankResponse:
    query = (body.query or "").strip()
    if not query:
        raise RetrievalError("invalid_query", "query 不能为空", 400)
    if not body.candidates:
        return RerankResponse(model=rerank.model_name(), results=[])

    from .config import RERANK_MAX_TEXT_CHARS

    texts: list[str] = []
    chunk_ids: list[int] = []
    for c in body.candidates:
        chunk_ids.append(c.chunkId)
        text = c.text or ""
        if len(text) > RERANK_MAX_TEXT_CHARS:
            text = text[:RERANK_MAX_TEXT_CHARS]
        texts.append(text)

    scores = rerank.rerank_pairs(query, texts)
    ranked = sorted(
        zip(chunk_ids, scores, strict=False),
        key=lambda x: x[1],
        reverse=True,
    )
    top_m = max(1, body.topM)
    results = [
        RerankHit(chunkId=cid, score=round(float(sc), 4), rank=i + 1)
        for i, (cid, sc) in enumerate(ranked[:top_m])
    ]
    log.info("rerank query_len=%s candidates=%s topM=%s", len(query), len(chunk_ids), top_m)
    return RerankResponse(model=rerank.model_name(), results=results)


def main() -> None:
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=RETRIEVAL_HOST,
        port=RETRIEVAL_PORT,
        reload=False,
    )


if __name__ == "__main__":
    main()
