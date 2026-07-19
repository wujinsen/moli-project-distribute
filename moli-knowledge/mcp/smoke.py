#!/usr/bin/env python3
"""Phase B smoke: MCP stdio E2E — list_tools + kb_search / kb_ask / kb_graph."""

from __future__ import annotations

import asyncio
import json
import os
import sys
import urllib.request
from pathlib import Path

from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client

MCP_DIR = Path(__file__).resolve().parent
DEFAULT_SPACE_ID = 900000000000000003


def login(base: str, username: str, password: str) -> str:
    url = f"{base.rstrip('/')}/login"
    payload = json.dumps({"userName": username, "password": password}).encode()
    req = urllib.request.Request(url, data=payload, method="POST", headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=15) as resp:
        body = json.loads(resp.read().decode())
    data = body.get("data") or {}
    token = data.get("token") or data.get("accessToken") or body.get("token")
    if not token:
        raise RuntimeError(f"login failed: {body}")
    return str(token)


def _payload(result) -> dict:
    text = result.content[0].text if result.content else "{}"
    return json.loads(text)


async def _call(session: ClientSession, name: str, arguments: dict) -> dict:
    result = await session.call_tool(name, arguments)
    if result.isError:
        raise RuntimeError(f"{name} error: {result.content[0].text if result.content else 'unknown'}")
    return _payload(result)


async def main() -> int:
    login_base = os.environ.get("MOLI_LOGIN_BASE", "http://127.0.0.1:8888")
    kb_base = os.environ.get("MCP_KB_BASE_URL", "http://127.0.0.1:8090")
    user = os.environ.get("MOLI_EVAL_USER", "admin")
    password = os.environ.get("MOLI_EVAL_PASS", "123456")

    token = login(login_base, user, password)
    env = os.environ.copy()
    env["MCP_KB_TOKEN"] = token
    env["MCP_KB_BASE_URL"] = kb_base

    params = StdioServerParameters(
        command=sys.executable,
        args=["-m", "mcp_server"],
        cwd=str(MCP_DIR),
        env=env,
    )

    async with stdio_client(params) as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()

            tools = await session.list_tools()
            names = [t.name for t in tools.tools]
            print("list_tools:", names)
            expected = ["kb_search", "kb_ask", "kb_graph"]
            if names != expected:
                print(f"FAIL: expected {expected}")
                return 1

            search_payload = await _call(
                session,
                "kb_search",
                {
                    "query": "本地怎么启动整套茉莉微服务",
                    "spaceIds": [DEFAULT_SPACE_ID],
                    "topK": 5,
                },
            )
            hits = search_payload.get("hits") or []
            print(f"kb_search hits={len(hits)} scope={search_payload.get('scope')!r}")
            if not hits:
                print("FAIL: kb_search hits empty")
                return 1

            ask_payload = await _call(
                session,
                "kb_ask",
                {
                    "question": "wiki 怎么同步到数据库",
                    "spaceIds": [DEFAULT_SPACE_ID],
                    "useLlm": False,
                    "topK": 5,
                },
            )
            cites = ask_payload.get("citations") or []
            print(f"kb_ask mode={ask_payload.get('mode')} citations={len(cites)}")
            if not cites:
                print("FAIL: kb_ask citations empty")
                return 1

            graph_payload = await _call(
                session,
                "kb_graph",
                {
                    "spaceId": DEFAULT_SPACE_ID,
                    "mode": "summary",
                    "maxNodes": 50,
                    "minDeg": 0,
                },
            )
            nodes = graph_payload.get("nodes") or []
            links = graph_payload.get("links") or []
            meta = graph_payload.get("meta") or {}
            print(
                f"kb_graph nodes={len(nodes)} links={len(links)} "
                f"meta.source={meta.get('source')!r} mode={meta.get('mode')!r}"
            )
            if not nodes:
                print("FAIL: kb_graph nodes empty")
                return 1

    print("OK")
    return 0


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))
