"""httpx REST client for KnowledgeServer (M-INV-1 token passthrough)."""

from __future__ import annotations

from typing import Any

import httpx

from config import McpKbConfig
from errors import KbToolError, ensure_token, map_http_error, map_request_error


class KbRestClient:
    def __init__(self, cfg: McpKbConfig) -> None:
        self._cfg = cfg

    async def _request(
        self,
        method: str,
        path: str,
        *,
        params: dict[str, Any] | None = None,
        json_body: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        ensure_token(self._cfg.token)
        url = f"{self._cfg.base_url}{path}"
        headers = {"Authorization": self._cfg.token}
        if json_body is not None:
            headers["Content-Type"] = "application/json"
        timeout = self._cfg.timeout_ms / 1000.0
        try:
            async with httpx.AsyncClient(timeout=timeout) as client:
                resp = await client.request(method, url, params=params, json=json_body, headers=headers)
                resp.raise_for_status()
                payload = resp.json()
        except httpx.HTTPStatusError as exc:
            raise map_http_error(exc) from exc
        except httpx.RequestError as exc:
            raise map_request_error(exc) from exc

        if not isinstance(payload, dict):
            raise KbToolError("KB_UPSTREAM_ERROR", "Unexpected response from Knowledge API.")
        # Moli HTTP often 200 + business code (e.g. 10006 请登录) — map auth to KB_UNAUTHORIZED
        biz = payload.get("code")
        if biz is not None and biz != 0 and biz != 200 and str(biz) not in ("0", "200"):
            msg = str(payload.get("message") or payload.get("msg") or f"business code {biz}")
            # 10006=token 无效；10009=无访问权限（ACL）
            if int(biz) in (10006, 10009, 401, 403) or "登录" in msg:
                raise KbToolError("KB_UNAUTHORIZED", "Knowledge API rejected the token (unauthorized).")
            raise KbToolError("KB_UPSTREAM_ERROR", msg)
        data = payload.get("data")
        if data is None:
            msg = str(payload.get("message") or payload.get("msg") or "empty data")
            raise KbToolError("KB_UPSTREAM_ERROR", msg)
        if not isinstance(data, dict):
            raise KbToolError("KB_UPSTREAM_ERROR", "Knowledge API data is not an object.")
        return data

    async def post_ask(self, body: dict[str, Any]) -> dict[str, Any]:
        return await self._request("POST", "/kb/ask", json_body=body)

    async def get_wiki_graph(
        self,
        space_id: int,
        *,
        mode: str | None = None,
        max_nodes: int | None = None,
        min_deg: int | None = None,
    ) -> dict[str, Any]:
        params: dict[str, Any] = {"spaceId": space_id}
        if mode is not None:
            params["mode"] = mode
        if max_nodes is not None:
            params["maxNodes"] = max_nodes
        if min_deg is not None:
            params["minDeg"] = min_deg
        return await self._request("GET", "/kb/wiki/graph", params=params)
