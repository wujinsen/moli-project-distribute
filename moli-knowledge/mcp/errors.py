"""AI-6 §2 M-INV-5/7 structured tool errors."""

from __future__ import annotations

import json
import re
from typing import Any

import httpx
import mcp.types as types


class KbToolError(Exception):
    def __init__(self, code: str, message: str) -> None:
        self.code = code
        self.message = sanitize_message(message)
        super().__init__(self.message)

    def to_payload(self) -> dict[str, str]:
        return {"code": self.code, "message": self.message}


def sanitize_message(text: str, *, limit: int = 160) -> str:
    """Strip tokens, stack traces, and connection strings from user-visible messages."""
    if text is None:
        return "unknown error"
    out = str(text).replace("\n", " ").replace("\r", " ").strip()
    if not out:
        return "unknown error"
    if "Traceback (most recent call last)" in out or re.search(r"\sFile \"[^\"]+\.py\"", out):
        return "An unexpected error occurred."
    out = re.sub(
        r"(?i)(authorization|token|sessionid|password|bearer)\s*[:=]\s*\S+",
        r"\1=***",
        out,
    )
    out = re.sub(r"(?i)\bbearer\s+\S+", "Bearer ***", out)
    out = re.sub(r"jdbc:[^\s]+", "jdbc:***", out)
    out = re.sub(r"(?i)(mysql|postgres|redis)://[^\s]+", r"\1://***", out)
    if len(out) > limit:
        return out[:limit] + "..."
    return out


def ensure_token(token: str) -> None:
    if not token:
        raise KbToolError(
            "KB_UNAUTHORIZED",
            "Missing MCP_KB_TOKEN; set Authorization sessionId from login.",
        )


def map_http_error(exc: httpx.HTTPStatusError) -> KbToolError:
    status = exc.response.status_code
    if status in (401, 403):
        return KbToolError("KB_UNAUTHORIZED", "Knowledge API rejected the token (unauthorized).")
    detail = sanitize_message(_safe_body_snippet(exc.response))
    return KbToolError("KB_UPSTREAM_ERROR", f"Knowledge API HTTP {status}: {detail}")


def map_request_error(exc: httpx.RequestError) -> KbToolError:
    if isinstance(exc, httpx.TimeoutException):
        return KbToolError("KB_UPSTREAM_TIMEOUT", "Knowledge API request timed out.")
    return KbToolError("KB_UPSTREAM_ERROR", "Knowledge API is unreachable.")


def _safe_body_snippet(response: httpx.Response, limit: int = 160) -> str:
    try:
        text = response.text
    except Exception:  # noqa: BLE001
        return "no response body"
    text = text.replace("\n", " ").strip()
    if len(text) > limit:
        return text[:limit] + "..."
    return text or "empty body"


def tool_error_result(err: KbToolError) -> types.CallToolResult:
    return types.CallToolResult(
        content=[types.TextContent(type="text", text=json.dumps(err.to_payload(), ensure_ascii=False))],
        isError=True,
    )


def tool_success_result(payload: dict[str, Any]) -> types.CallToolResult:
    return types.CallToolResult(
        content=[types.TextContent(type="text", text=json.dumps(payload, ensure_ascii=False))],
        structuredContent=payload,
        isError=False,
    )
