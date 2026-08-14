from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field


class ResearchOptions(BaseModel):
    max_sections: int = Field(default=6, alias="maxSections")
    max_retrieve_rounds: int = Field(default=2, alias="maxRetrieveRounds")
    latency_budget_ms: int = Field(default=90000, alias="latencyBudgetMs")
    top_k: int = Field(default=8, alias="topK")
    per_section_top_k: int = Field(default=8, alias="perSectionTopK")
    retrieval_strategy: str = Field(default="hybrid", alias="retrievalStrategy")
    graph_expand: bool | None = Field(default=None, alias="graphExpand")
    agentic: bool = False
    coverage_threshold: float = Field(default=0.75, alias="coverageThreshold")

    model_config = {"populate_by_name": True}


class ResearchSidecarRequest(BaseModel):
    run_id: str = Field(alias="runId")
    topic: str
    space_id: int | None = Field(default=None, alias="spaceId")
    space_ids: list[int] | None = Field(default=None, alias="spaceIds")
    auth_token: str | None = Field(default=None, alias="authToken")
    options: ResearchOptions = Field(default_factory=ResearchOptions)

    model_config = {"populate_by_name": True}


class EvidenceHit(BaseModel):
    slug: str
    snippet: str = ""
    score: float = 0.0
    doc_id: int | None = Field(default=None, alias="docId")
    title: str | None = None

    model_config = {"populate_by_name": True}


class SectionEvidence(BaseModel):
    section_id: str = Field(alias="sectionId")
    hits: list[EvidenceHit] = Field(default_factory=list)
    queries_used: list[str] = Field(default_factory=list, alias="queriesUsed")

    model_config = {"populate_by_name": True}


class CitationItem(BaseModel):
    slug: str
    title: str | None = None
    section_ids: list[str] = Field(default_factory=list, alias="sectionIds")

    model_config = {"populate_by_name": True}


class ProgressEvent(BaseModel):
    phase: str
    section_id: str | None = Field(default=None, alias="sectionId")
    message: str = ""
    pct: int = 0

    model_config = {"populate_by_name": True}


class ResearchSidecarResponse(BaseModel):
    run_id: str = Field(alias="runId")
    status: str
    topic: str
    title: str | None = None
    slug: str | None = None
    outline: dict[str, Any] | None = None
    section_evidence: list[SectionEvidence] = Field(default_factory=list, alias="sectionEvidence")
    citations: list[CitationItem] = Field(default_factory=list)
    report_md: str | None = Field(default=None, alias="reportMd")
    coverage: float | None = None
    unsupported_statements: list[str] = Field(default_factory=list, alias="unsupportedStatements")
    progress: list[ProgressEvent] = Field(default_factory=list)
    latency_ms: int = Field(default=0, alias="latencyMs")
    degraded: bool = False
    degrade_reason: str | None = Field(default=None, alias="degradeReason")
    error_message: str | None = Field(default=None, alias="errorMessage")

    model_config = {"populate_by_name": True}


class HealthResponse(BaseModel):
    status: str = "ok"
    version: str = "0.1.0"
    kb_base_url: str = Field(alias="kbBaseUrl")

    model_config = {"populate_by_name": True}
