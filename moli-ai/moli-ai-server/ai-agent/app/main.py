#!/usr/bin/env python3
"""ai-agent sidecar · 契约 §1.2

  uvicorn app.main:app --host 127.0.0.1 --port 1130
"""
from __future__ import annotations

import logging

from fastapi import FastAPI

from .config import AGENT_HOST, AGENT_PORT, LLM_API_KEY, load_allow_tables
from .explain import explain_result
from .graph import run_generate
from .models import (
    ExplainRequest,
    ExplainResponse,
    GenerateRequest,
    GenerateResponse,
    HealthResponse,
)

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
log = logging.getLogger("ai-agent")

app = FastAPI(title="Moli AI Agent Sidecar", version="0.1.0")


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        llmConfigured=bool(LLM_API_KEY),
        schemaTables=len(load_allow_tables()),
    )


@app.post("/agent/generate", response_model=GenerateResponse)
def agent_generate(body: GenerateRequest) -> GenerateResponse:
    log.info("generate retry=%s question=%s", body.retry, body.question[:80])
    return run_generate(body)


@app.post("/agent/explain", response_model=ExplainResponse)
def agent_explain(body: ExplainRequest) -> ExplainResponse:
    return explain_result(body)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host=AGENT_HOST, port=AGENT_PORT, reload=False)
