"""Lazy-loaded bge-m3 embedding (normalize=true, dim=1024)."""
from __future__ import annotations

import threading
from typing import Sequence

import numpy as np

from .config import EMBED_DIM, EMBED_MODEL, EMBED_ENCODE_BATCH_SIZE, RETRIEVAL_DEVICE

_lock = threading.Lock()
_model = None
_device: str | None = None


def resolve_device() -> str:
    import torch

    if RETRIEVAL_DEVICE == "cpu":
        return "cpu"
    if RETRIEVAL_DEVICE == "cuda":
        if not torch.cuda.is_available():
            raise RuntimeError("RETRIEVAL_DEVICE=cuda but torch.cuda.is_available() is False")
        return "cuda"
    return "cuda" if torch.cuda.is_available() else "cpu"


def _load_model():
    global _model, _device
    if _model is not None:
        return _model
    with _lock:
        if _model is not None:
            return _model
        from sentence_transformers import SentenceTransformer

        _device = resolve_device()
        _model = SentenceTransformer(EMBED_MODEL, device=_device)
        return _model


def embed_texts(texts: Sequence[str]) -> list[list[float]]:
    if not texts:
        return []
    model = _load_model()
    vectors = model.encode(
        list(texts),
        normalize_embeddings=True,
        show_progress_bar=False,
        batch_size=max(1, EMBED_ENCODE_BATCH_SIZE),
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


def device_name() -> str:
    if _device is not None:
        return _device
    return resolve_device()
