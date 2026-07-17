"""Environment-driven configuration for kb-retrieval sidecar."""
from __future__ import annotations

import os
from pathlib import Path

RETRIEVAL_ROOT = Path(__file__).resolve().parent.parent

RETRIEVAL_PORT = int(os.environ.get("RETRIEVAL_PORT", "8099"))
EMBED_MODEL = os.environ.get("EMBED_MODEL", "BAAI/bge-m3")
EMBED_DIM = int(os.environ.get("EMBED_DIM", "1024"))
CHROMA_PATH = Path(os.environ.get("CHROMA_PATH", str(RETRIEVAL_ROOT / ".chroma")))
COLLECTION_NAME = os.environ.get(
    "CHROMA_COLLECTION", "moli_kb_chunks_bgem3_v1"
)
EMBED_BATCH_SIZE = int(os.environ.get("EMBED_BATCH_SIZE", "32"))
MAX_TEXT_CHARS = int(os.environ.get("RETRIEVAL_MAX_TEXT_CHARS", "8192"))
