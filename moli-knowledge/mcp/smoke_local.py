#!/usr/bin/env python3
"""Local smoke: login → kb_search/kb_ask via REST client (Phase A exit check)."""

from __future__ import annotations

import asyncio
import json
import os
import sys
import urllib.request

from config import McpKbConfig
from kb_client import KbRestClient
from tools_impl import dispatch_tool, list_tool_definitions


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


async def main() -> int:
    login_base = os.environ.get("MOLI_LOGIN_BASE", "http://127.0.0.1:8888")
    kb_base = os.environ.get("MCP_KB_BASE_URL", "http://127.0.0.1:8090")
    user = os.environ.get("MOLI_EVAL_USER", "admin")
    password = os.environ.get("MOLI_EVAL_PASS", "123456")

    token = login(login_base, user, password)
    os.environ["MCP_KB_TOKEN"] = token
    os.environ["MCP_KB_BASE_URL"] = kb_base

    cfg = McpKbConfig.from_env()
    client = KbRestClient(cfg)

    tools = list_tool_definitions()
    names = [t.name for t in tools]
    print("list_tools:", names)
    if names != ["kb_search", "kb_ask", "kb_graph"]:
        print("FAIL: expected kb_search + kb_ask + kb_graph")
        return 1

    search = await dispatch_tool(
        "kb_search",
        {"query": "本地怎么启动整套茉莉微服务", "spaceIds": [900000000000000003], "topK": 5},
        client,
        cfg,
    )
    if search.isError:
        print("kb_search error:", search.content[0].text)
        return 1
    search_payload = json.loads(search.content[0].text)
    hits = search_payload.get("hits") or []
    print(f"kb_search hits={len(hits)} scope={search_payload.get('scope')!r}")
    if not hits:
        print("FAIL: kb_search citations/hits empty")
        return 1

    ask = await dispatch_tool(
        "kb_ask",
        {"question": "wiki 怎么同步到数据库", "spaceIds": [900000000000000003], "useLlm": False, "topK": 5},
        client,
        cfg,
    )
    if ask.isError:
        print("kb_ask error:", ask.content[0].text)
        return 1
    ask_payload = json.loads(ask.content[0].text)
    cites = ask_payload.get("citations") or []
    print(f"kb_ask mode={ask_payload.get('mode')} citations={len(cites)}")
    if not cites:
        print("FAIL: kb_ask citations empty")
        return 1

    graph = await dispatch_tool(
        "kb_graph",
        {"spaceId": 900000000000000003, "mode": "summary", "maxNodes": 50},
        client,
        cfg,
    )
    if graph.isError:
        print("kb_graph error:", graph.content[0].text)
        return 1
    graph_payload = json.loads(graph.content[0].text)
    nodes = graph_payload.get("nodes") or []
    print(f"kb_graph nodes={len(nodes)} links={len(graph_payload.get('links') or [])}")
    if not nodes:
        print("FAIL: kb_graph nodes empty")
        return 1

    print("OK")
    return 0


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))
