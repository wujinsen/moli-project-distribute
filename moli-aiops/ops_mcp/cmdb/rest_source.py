"""user-center REST 拓扑源。

复用现有 `/operation/**`：拓扑图、变更流水都已经在那边维护，没必要在
诊断平台里再建一套 CMDB。鉴权沿用 moli-knowledge/mcp 的约定，
`Authorization` 头放 user-center 登录后的 Shiro sessionId。

字段映射对齐 OperationTopology*NodeVo；分页响应各接口形态不完全一致，
所以取列表时按多个候选键探测，拿不到就当空列表而不是抛异常。
"""

from __future__ import annotations

from typing import Any

import httpx

from .. import config
from ..errors import OPS_CMDB_UNAVAILABLE, OpsToolError
from ..schemas import (
    ChangeRecord,
    ComponentRef,
    ProjectRef,
    ServerRef,
    TopologyEdge,
    TopologyGraph,
)


def _as_int(value: Any) -> int | None:
    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def _rows(payload: Any) -> list[dict[str, Any]]:
    if isinstance(payload, list):
        return [r for r in payload if isinstance(r, dict)]
    if isinstance(payload, dict):
        for key in ("rows", "list", "records", "items", "content"):
            candidate = payload.get(key)
            if isinstance(candidate, list):
                return [r for r in candidate if isinstance(r, dict)]
    return []


class RestCmdbSource:
    name = "rest"

    def __init__(self, base_url: str | None = None, token: str | None = None) -> None:
        self.base_url = (base_url or config.USER_CENTER_BASE_URL).rstrip("/")
        self.token = token if token is not None else config.OPS_AUTH_TOKEN

    def _get(self, path: str, params: dict[str, Any] | None = None) -> Any:
        url = f"{self.base_url}{path}"
        headers = {"Accept": "application/json"}
        if self.token:
            headers["Authorization"] = self.token
        try:
            with httpx.Client(timeout=config.CMDB_TIMEOUT_S) as client:
                response = client.get(url, params=params, headers=headers)
                response.raise_for_status()
                body = response.json()
        except Exception as exc:  # noqa: BLE001
            raise OpsToolError(
                OPS_CMDB_UNAVAILABLE,
                f"user-center 不可用：{exc}",
                detail={"url": url},
            ) from exc

        if isinstance(body, dict) and "code" in body:
            code = body.get("code")
            if code not in (200, 0):
                raise OpsToolError(
                    OPS_CMDB_UNAVAILABLE,
                    f"user-center 返回错误 code={code}: {body.get('msg')}",
                    detail={"url": url},
                )
            return body.get("data")
        return body

    def available(self) -> bool:
        try:
            self._get("/operation/topology")
        except OpsToolError:
            return False
        return True

    def topology(self) -> TopologyGraph:
        data = self._get("/operation/topology") or {}
        if not isinstance(data, dict):
            raise OpsToolError(OPS_CMDB_UNAVAILABLE, "拓扑响应格式不是对象")

        servers = [
            ServerRef(
                id=node.get("serverId") or node.get("id"),
                name=node.get("serverName") or "",
                ip=node.get("ip") or "",
                inner_ip=node.get("innerIp") or "",
                role=node.get("serverRole") or "",
                tags=[str(t) for t in (node.get("tags") or [])],
                status=_as_int(node.get("status")) or 0,
            )
            for node in (data.get("servers") or [])
            if isinstance(node, dict)
        ]
        projects = [
            ProjectRef(
                id=node.get("projectId") or node.get("id"),
                name=node.get("projectName") or "",
                port=_as_int(node.get("port")),
                deploy_running=node.get("deployRunning"),
            )
            for node in (data.get("projects") or [])
            if isinstance(node, dict)
        ]
        components = [
            ComponentRef(
                id=node.get("componentId") or node.get("id"),
                name=node.get("componentName") or "",
                port=_as_int(node.get("port")),
                version=node.get("version") or "",
                status=_as_int(node.get("status")) or 0,
            )
            for node in (data.get("components") or [])
            if isinstance(node, dict)
        ]
        edges = [
            TopologyEdge(
                source=str(link.get("source") or ""),
                target=str(link.get("target") or ""),
                kind=str(link.get("type") or ""),
            )
            for link in (data.get("links") or [])
            if isinstance(link, dict)
        ]

        return TopologyGraph(
            servers=servers,
            projects=projects,
            components=components,
            edges=edges,
            source=self.name,
        )

    def recent_changes(self, server_id: str | int | None, limit: int = 20) -> list[ChangeRecord]:
        params: dict[str, Any] = {"pageNum": 1, "pageSize": max(1, min(limit, 100))}
        if server_id is not None:
            params["serverId"] = server_id
        try:
            data = self._get("/operation/task/list", params)
        except OpsToolError:
            # 变更流水拿不到不应该让整次取证失败，诊断可以只用其他证据线
            return []

        records: list[ChangeRecord] = []
        for row in _rows(data)[:limit]:
            records.append(
                ChangeRecord(
                    task_id=row.get("taskId") or row.get("id") or "",
                    task_type=str(row.get("taskType") or ""),
                    action=str(row.get("action") or ""),
                    server_id=row.get("serverId"),
                    project_id=row.get("projectId"),
                    status=str(row.get("status") or ""),
                    operator=str(row.get("createBy") or row.get("operator") or ""),
                    create_time=row.get("createTime"),
                    finish_time=row.get("finishTime"),
                    message=str(row.get("message") or "")[:500],
                )
            )
        return records
