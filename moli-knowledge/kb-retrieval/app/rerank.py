"""Lazy-loaded bge-reranker-v2-m3 cross-encoder."""
from __future__ import annotations

import threading
from typing import Sequence

from .config import RERANK_MODEL
from .embedding import resolve_device

_lock = threading.Lock()
_model = None


def _load_model():
    global _model
    if _model is not None:
        return _model
    with _lock:
        if _model is not None:
            return _model
        from sentence_transformers import CrossEncoder

        _model = CrossEncoder(RERANK_MODEL, device=resolve_device())
        return _model


def rerank_pairs(query: str, texts: Sequence[str]) -> list[float]:
    if not texts:
        return []
    model = _load_model()
    pairs = [(query, t) for t in texts]
    scores = model.predict(pairs, show_progress_bar=False)
    return [float(s) for s in scores]


def model_name() -> str:
    return RERANK_MODEL.split("/")[-1] if "/" in RERANK_MODEL else RERANK_MODEL


def is_model_loaded() -> bool:
    return _model is not None
