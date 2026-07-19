"""AI-6 §3 env configuration (M-INV-3: no hardcoded tokens)."""

from __future__ import annotations

import os
from dataclasses import dataclass


def _parse_space_ids(raw: str) -> list[int]:
    if not raw or not raw.strip():
        return []
    out: list[int] = []
    for part in raw.split(","):
        part = part.strip()
        if not part:
            continue
        out.append(int(part))
    return out


@dataclass(frozen=True)
class McpKbConfig:
    base_url: str
    token: str
    timeout_ms: int
    default_space_ids: list[int]
    default_strategy: str | None

    @classmethod
    def from_env(cls) -> McpKbConfig:
        base = (os.environ.get("MCP_KB_BASE_URL") or "http://127.0.0.1:21000/KnowledgeServer").rstrip("/")
        token = (os.environ.get("MCP_KB_TOKEN") or "").strip()
        timeout_ms = int(os.environ.get("MCP_KB_TIMEOUT_MS") or "15000")
        if timeout_ms <= 0:
            timeout_ms = 15000
        default_space_ids = _parse_space_ids(os.environ.get("MCP_KB_DEFAULT_SPACE_IDS") or "")
        default_strategy = (os.environ.get("MCP_KB_DEFAULT_STRATEGY") or "").strip() or None
        return cls(
            base_url=base,
            token=token,
            timeout_ms=timeout_ms,
            default_space_ids=default_space_ids,
            default_strategy=default_strategy,
        )
