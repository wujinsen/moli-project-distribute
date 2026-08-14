"""JSON error responses per AI-2 contract §1.2."""
from __future__ import annotations

from fastapi import Request
from fastapi.responses import JSONResponse


class RetrievalError(Exception):
    def __init__(self, code: str, message: str, status_code: int = 400):
        self.code = code
        self.message = message
        self.status_code = status_code
        super().__init__(message)


async def retrieval_error_handler(_request: Request, exc: RetrievalError) -> JSONResponse:
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.code, "message": exc.message},
    )
