"""Lazy-loaded bge-m3 embedding (normalize=true, dim=1024)."""
from __future__ import annotations

import threading
from typing import Sequence

import numpy as np

from .config import EMBED_DIM, EMBED_MODEL

_lock = threading.Lock()
_model = None


def _load_model():
    global _model
    if _model is not None:
        return _model
    with _lock:
        if _model is not None:
            return _model
        from sentence_transformers import SentenceTransformer

        _model = SentenceTransformer(EMBED_MODEL)
        return _model


def embed_texts(texts: Sequence[str]) -> list[list[float]]:
    if not texts:
        return []
    model = _load_model()
    vectors = model.encode(
        list(texts),
        normalize_embeddings=True,
        show_progress_bar=False,
    )
    arr = np.asarray(vectors, dtype=np.float32)
    if arr.ndim == 1:
        arr = arr.reshape(1, -1)
    if arr.shape[1] != EMBED_DIM:
        raise RuntimeError(
            f"embedding dim {arr.shape[1]} != expected {EMBED_DIM} for {EMBED_MODEL}"
        )
    return arr.tolist()


def model_name() -> str:
    return EMBED_MODEL.split("/")[-1] if "/" in EMBED_MODEL else EMBED_MODEL


def is_model_loaded() -> bool:
    return _model is not None
