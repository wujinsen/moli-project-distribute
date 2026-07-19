#!/usr/bin/env python3
"""Moli knowledge base MCP server (stdio) · AI-6."""

from __future__ import annotations

import asyncio
import logging

import mcp.types as types
from mcp.server import Server
from mcp.server.stdio import stdio_server

from config import McpKbConfig
from errors import KbToolError, tool_error_result
from kb_client import KbRestClient
from tools_impl import dispatch_tool, list_tool_definitions

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("moli-kb-mcp")

server = Server("moli-kb")
_cfg = McpKbConfig.from_env()
_client = KbRestClient(_cfg)


@server.list_tools()
async def handle_list_tools() -> list[types.Tool]:
    return list_tool_definitions()


@server.call_tool()
async def handle_call_tool(name: str, arguments: dict | None) -> types.CallToolResult:
    logger.info("tool call name=%s", name)
    try:
        return await dispatch_tool(name, arguments, _client, _cfg)
    except Exception:  # noqa: BLE001
        logger.exception("unexpected tool error")
        return tool_error_result(KbToolError("KB_UPSTREAM_ERROR", "An unexpected error occurred."))


async def main() -> None:
    async with stdio_server() as (read_stream, write_stream):
        await server.run(read_stream, write_stream, server.create_initialization_options())


if __name__ == "__main__":
    asyncio.run(main())
