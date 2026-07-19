"""LangGraph 风格流水线：retrieve_schema → generate_sql（含 self_lint）。"""
from __future__ import annotations

from .generate import generate_sql
from .models import GenerateRequest, GenerateResponse
from .retrieve import retrieve_schema


def run_generate(req: GenerateRequest) -> GenerateResponse:
    tables, digest = retrieve_schema(req.question)
    return generate_sql(req, tables, digest)
