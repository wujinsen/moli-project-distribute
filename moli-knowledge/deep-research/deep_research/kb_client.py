from __future__ import annotations

from typing import Any

import httpx

from .config import KB_AUTH_TOKEN, KB_BASE_URL


class KbClientError(Exception):
    def __init__(self, message: str, status_code: int | None = None) -> None:
        super().__init__(message)
        self.status_code = status_code


def _unwrap_data(body: dict[str, Any]) -> dict[str, Any]:
    data = body.get("data")
    return data if isinstance(data, dict) else body


class KbRestClient:
    """HTTP client for KnowledgeServer /kb/ask REST (D-INV-3 ACL token passthrough)."""

    def __init__(
        self,
        base_url: str | None = None,
        auth_token: str | None = None,
        timeout_ms: int = 60000,
    ) -> None:
        self.base_url = (base_url or KB_BASE_URL).rstrip("/")
        self.auth_token = auth_token if auth_token is not None else KB_AUTH_TOKEN
        self.timeout = timeout_ms / 1000.0

    def ask(
        self,
        question: str,
        *,
        space_id: int | None = None,
        space_ids: list[int] | None = None,
        top_k: int = 8,
        retrieval_strategy: str = "hybrid",
        graph_expand: bool | None = None,
        use_llm: bool = False,
        agentic: bool = False,
    ) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "question": question,
            "topK": top_k,
            "useLlm": use_llm,
        }
        if space_ids:
            payload["spaceIds"] = space_ids
        elif space_id is not None:
            payload["spaceId"] = space_id
        if retrieval_strategy:
            payload["retrievalStrategy"] = retrieval_strategy
        if graph_expand is not None:
            payload["graphExpand"] = graph_expand
        endpoint = "/kb/ask/agentic" if agentic else "/kb/ask"
        if agentic:
            payload["agentic"] = True
            payload["useLlm"] = True
        return self._post(endpoint, payload)

    def _post(self, path: str, payload: dict[str, Any]) -> dict[str, Any]:
        headers = {"Content-Type": "application/json"}
        if self.auth_token:
            headers["Authorization"] = self.auth_token
        url = f"{self.base_url}{path}"
        with httpx.Client(timeout=self.timeout) as client:
            resp = client.post(url, json=payload, headers=headers)
        if resp.status_code >= 400:
            raise KbClientError(f"HTTP {resp.status_code}: {resp.text[:200]}", resp.status_code)
        body = resp.json()
        return _unwrap_data(body)
