"""Pydantic request/response models (AI-2 contract §1.2)."""
from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field


class EmbedItem(BaseModel):
    chunkId: int
    docId: int
    spaceId: int
    slug: str
    kbType: str | None = None
    categoryId: int | None = None
    contentHash: str
    text: str


class EmbedRequest(BaseModel):
    items: list[EmbedItem] = Field(default_factory=list)
    deleteChunkIds: list[int] = Field(default_factory=list)
    force: bool = False


class EmbedResponse(BaseModel):
    model: str
    dim: int
    upserted: int
    skipped: int
    deleted: int


class SearchFilter(BaseModel):
    kbType: list[str] | None = None
    excludeKbType: list[str] | None = None


class SearchRequest(BaseModel):
    query: str
    spaceIds: list[int] = Field(default_factory=list)
    topN: int = 20
    filter: SearchFilter | None = None


class SearchHit(BaseModel):
    chunkId: int
    docId: int
    spaceId: int
    slug: str
    kbType: str
    score: float
    rank: int


class SearchResponse(BaseModel):
    model: str
    results: list[SearchHit]


class HealthResponse(BaseModel):
    status: str
    model: str
    dim: int
    collection: str
    indexedChunks: int
    chromaPath: str
    modelLoaded: bool


def filter_to_lists(flt: SearchFilter | None) -> tuple[list[str] | None, list[str] | None]:
    if not flt:
        return None, None
    return flt.kbType, flt.excludeKbType
