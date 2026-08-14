"""契约 §1.2 agent 请求/响应 DTO。"""
from __future__ import annotations

from typing import Any, Optional

from pydantic import BaseModel, Field


class GenerateRequest(BaseModel):
    sessionId: Optional[str] = None
    question: str
    retry: int = 0
    priorSql: Optional[str] = None
    priorError: Optional[str] = None


class GenerateResponse(BaseModel):
    draftSql: Optional[str] = None
    usedTables: list[str] = Field(default_factory=list)
    schemaDigest: str = ""
    refusal: Optional[str] = None


class ExplainColumn(BaseModel):
    name: str
    type: Optional[str] = None
    label: Optional[str] = None


class ExplainRequest(BaseModel):
    sessionId: Optional[str] = None
    question: str
    sql: str
    columns: list[ExplainColumn] = Field(default_factory=list)
    rowsSample: list[dict[str, Any]] = Field(default_factory=list)
    rowCount: int = 0


class ChartSuggestion(BaseModel):
    type: str = "table"
    x: Optional[str] = None
    y: list[str] = Field(default_factory=list)
    title: Optional[str] = None


class ExplainResponse(BaseModel):
    explanation: str = ""
    chart: ChartSuggestion = Field(default_factory=ChartSuggestion)


class HealthResponse(BaseModel):
    status: str = "ok"
    llmConfigured: bool = False
    schemaTables: int = 0
