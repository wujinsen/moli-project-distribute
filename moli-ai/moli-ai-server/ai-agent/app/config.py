"""ai-agent sidecar 配置（契约 §3.3 / §1.2）。"""
from __future__ import annotations

import json
import os
from pathlib import Path

AGENT_DIR = Path(__file__).resolve().parent.parent
SCHEMA_FILE = AGENT_DIR / "schema" / "allow_tables.json"

AGENT_HOST = os.environ.get("AGENT_HOST", "127.0.0.1")
AGENT_PORT = int(os.environ.get("AGENT_PORT", "1130"))

RETRIEVAL_BASE_URL = os.environ.get("BI_RETRIEVAL_BASE_URL", "http://127.0.0.1:8099")
SCHEMA_TOP_K = int(os.environ.get("BI_SCHEMA_TOP_K", "8"))
MAX_ROWS = int(os.environ.get("BI_AGENT_MAX_ROWS", "100"))

# 可选 LLM（OpenAI 兼容）；未配置则走规则 NL→SQL
LLM_BASE_URL = os.environ.get("BI_LLM_BASE_URL", "")
LLM_API_KEY = os.environ.get("BI_LLM_API_KEY", os.environ.get("OPENAI_API_KEY", ""))
LLM_MODEL = os.environ.get("BI_LLM_MODEL", "glm-4-flash")


def load_allow_tables() -> list[dict]:
    with SCHEMA_FILE.open(encoding="utf-8") as f:
        data = json.load(f)
    return data.get("tables") or []
