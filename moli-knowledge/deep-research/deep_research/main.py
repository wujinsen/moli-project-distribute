from __future__ import annotations

import logging

from fastapi import FastAPI, HTTPException

from .config import KB_BASE_URL, RESEARCH_HOST, RESEARCH_PORT
from .models import HealthResponse, ResearchSidecarRequest, ResearchSidecarResponse
from .orchestrator import run_research

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
log = logging.getLogger("deep-research")

app = FastAPI(title="Moli DeepResearch Sidecar", version="0.1.0")


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(kbBaseUrl=KB_BASE_URL)


@app.post("/v1/research", response_model=ResearchSidecarResponse)
def research(body: ResearchSidecarRequest) -> ResearchSidecarResponse:
    if not body.topic or not body.topic.strip():
        raise HTTPException(status_code=400, detail="topic required")
    try:
        result = run_research(body)
        log.info(
            "research runId=%s status=%s sections=%s citations=%s latencyMs=%s",
            result.run_id,
            result.status,
            len((result.outline or {}).get("sections") or []),
            len(result.citations),
            result.latency_ms,
        )
        return result
    except Exception as exc:  # noqa: BLE001
        log.exception("research failed runId=%s", body.run_id)
        return ResearchSidecarResponse(
            runId=body.run_id,
            status="FAILED",
            topic=body.topic,
            errorMessage=str(exc),
        )


def main() -> None:
    import uvicorn

    uvicorn.run("deep_research.main:app", host=RESEARCH_HOST, port=RESEARCH_PORT, reload=False)


if __name__ == "__main__":
    main()
