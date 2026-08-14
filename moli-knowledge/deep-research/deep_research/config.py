from __future__ import annotations

from pathlib import Path

import os

RESEARCH_HOST = os.environ.get("RESEARCH_HOST", "0.0.0.0")
RESEARCH_PORT = int(os.environ.get("RESEARCH_PORT", "8095"))

KB_BASE_URL = os.environ.get("KB_BASE_URL", "http://127.0.0.1:8090").rstrip("/")
KB_AUTH_TOKEN = os.environ.get("KB_AUTH_TOKEN", "")

OPENAI_API_KEY = os.environ.get("OPENAI_API_KEY", "")
OPENAI_BASE_URL = os.environ.get("OPENAI_BASE_URL", "https://api.openai.com/v1").rstrip("/")
OPENAI_MODEL = os.environ.get("OPENAI_MODEL", "gpt-4o-mini")

DEFAULT_MAX_SECTIONS = int(os.environ.get("DR_MAX_SECTIONS", "6"))
HARD_MAX_SECTIONS = 10
MAX_QUERIES_PER_SECTION = 4
DEFAULT_PER_SECTION_TOP_K = int(os.environ.get("DR_PER_SECTION_TOP_K", "8"))
DEFAULT_TOP_K = int(os.environ.get("DR_TOP_K", "8"))
DEFAULT_RETRIEVAL_STRATEGY = os.environ.get("DR_RETRIEVAL_STRATEGY", "hybrid")
DEFAULT_LATENCY_BUDGET_MS = int(os.environ.get("DR_LATENCY_BUDGET_MS", "90000"))
DEFAULT_COVERAGE_THRESHOLD = float(os.environ.get("DR_COVERAGE_THRESHOLD", "0.75"))
RUNS_DIR = Path(os.environ.get("DR_RUNS_DIR", "runs"))
