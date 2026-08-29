"""入站 Shiro 鉴权：转发 Authorization 到 user-center /auth/capabilities 校验会话与权限。"""

from __future__ import annotations

import logging
import re
from dataclasses import dataclass

import httpx
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

from . import config

log = logging.getLogger("aiops.auth")

MOLI_SUCCESS_CODES = {0, 200}
_SKIP_PATHS = {"/"}


@dataclass(frozen=True)
class AuthContext:
    permissions: frozenset[str]
    full_permission: bool


def required_perm(method: str, path: str) -> str | None:
    if path == "/health":
        return "operation:aiops:list"
    if method == "POST" and path == "/diagnose":
        return "operation:aiops:diagnose"
    if method == "GET" and path == "/runs":
        return "operation:aiops:list"
    if method == "POST" and re.fullmatch(r"/runs/[^/]+/approve", path):
        return "operation:aiops:approve"
    if method == "POST" and re.fullmatch(r"/runs/[^/]+/reject", path):
        return "operation:aiops:approve"
    if method == "POST" and re.fullmatch(r"/runs/[^/]+/rerun", path):
        return "operation:aiops:diagnose"
    if path.startswith("/runs/"):
        return "operation:aiops:list"
    return None


def has_perm(ctx: AuthContext, perm: str) -> bool:
    return ctx.full_permission or perm in ctx.permissions


async def validate_token(token: str) -> AuthContext | None:
    headers = {"Authorization": token}
    try:
        async with httpx.AsyncClient(timeout=config.AUTH_TIMEOUT_S) as client:
            resp = await client.get(config.AUTH_VALIDATE_URL, headers=headers)
    except httpx.HTTPError as exc:
        log.warning("鉴权服务不可达: %s", exc)
        return None

    if resp.status_code == 401:
        return None
    try:
        body = resp.json()
    except ValueError:
        return None

    code = body.get("code")
    if code not in MOLI_SUCCESS_CODES:
        return None

    data = body.get("data") or {}
    perms = data.get("permissions") or []
    full = bool(data.get("fullPermission"))
    if not isinstance(perms, list):
        return None
    return AuthContext(permissions=frozenset(str(p) for p in perms), full_permission=full)


class ShiroAuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next) -> Response:  # noqa: ANN001
        if not config.AUTH_ENABLED:
            return await call_next(request)

        path = request.url.path
        if path in _SKIP_PATHS:
            return await call_next(request)

        perm = required_perm(request.method, path)
        if perm is None:
            return JSONResponse({"detail": "未授权的接口"}, status_code=404)

        token = request.headers.get("Authorization", "").strip()
        if not token:
            return JSONResponse({"detail": "未登录或缺少 Authorization 头"}, status_code=401)

        ctx = await validate_token(token)
        if ctx is None:
            return JSONResponse({"detail": "会话无效或已过期"}, status_code=401)
        if not has_perm(ctx, perm):
            return JSONResponse({"detail": f"缺少权限：{perm}"}, status_code=403)

        request.state.auth = ctx
        return await call_next(request)
