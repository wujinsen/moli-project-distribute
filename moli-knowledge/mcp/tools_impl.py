"""AI-6 §1.3 kb_search / kb_ask / kb_graph tool handlers."""

from __future__ import annotations

from typing import Any

import mcp.types as types

from config import McpKbConfig
from errors import KbToolError, tool_error_result, tool_success_result
from kb_client import KbRestClient

VALID_STRATEGIES = frozenset({"ngram", "hybrid", "hybrid-rerank"})

KB_SEARCH_DESC = (
    "Search the Moli knowledge base and return ranked citation hits with slugs/snippets only "
    "(no generated answer). Use when you need sources or snippets to cite, not a full answer. "
    "Results include docId/spaceId/slug/title/kbType/snippet; do not invent sources."
)

KB_ASK_DESC = (
    "Ask the Moli knowledge base a question and get an answer with citations (generative when LLM "
    "is available, otherwise retrieval mode). Use when you need a synthesized answer with "
    "traceable [[slug]] sources. Returns answer, mode, citations, provider/model."
)

KB_GRAPH_DESC = (
    "Explore wiki document links in a knowledge space (wikilink, related frontmatter, edges.jsonl). "
    "Use when you need to see how pages relate, not to search text or generate answers. "
    "Returns GraphVo nodes/links/meta from disk wiki files; does not support ego subgraph."
)

VALID_GRAPH_MODES = frozenset({"full", "summary"})

_INT64_ARRAY = {
    "type": "array",
    "items": {"type": "integer", "format": "int64"},
}


def list_tool_definitions() -> list[types.Tool]:
    return [
        types.Tool(
            name="kb_search",
            description=KB_SEARCH_DESC,
            inputSchema={
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "minLength": 1,
                        "maxLength": 500,
                        "description": "Search query (maps to REST question)",
                    },
                    "spaceIds": {
                        **_INT64_ARRAY,
                        "description": "Optional readable space IDs; omit for all readable spaces",
                    },
                    "topK": {
                        "type": "integer",
                        "minimum": 1,
                        "description": "Max citation hits (default backend citation-top-k=8)",
                    },
                    "retrievalStrategy": {
                        "type": "string",
                        "enum": sorted(VALID_STRATEGIES),
                        "description": "Optional: ngram | hybrid | hybrid-rerank",
                    },
                },
                "required": ["query"],
            },
        ),
        types.Tool(
            name="kb_ask",
            description=KB_ASK_DESC,
            inputSchema={
                "type": "object",
                "properties": {
                    "question": {
                        "type": "string",
                        "minLength": 1,
                        "maxLength": 500,
                    },
                    "spaceIds": {
                        **_INT64_ARRAY,
                        "description": "Optional readable space IDs",
                    },
                    "useLlm": {
                        "type": "boolean",
                        "description": "Enable generative answer (MCP default true when omitted)",
                    },
                    "topK": {"type": "integer", "minimum": 1},
                    "llmContextTopK": {"type": "integer", "minimum": 1},
                    "retrievalStrategy": {
                        "type": "string",
                        "enum": sorted(VALID_STRATEGIES),
                    },
                },
                "required": ["question"],
            },
        ),
        types.Tool(
            name="kb_graph",
            description=KB_GRAPH_DESC,
            inputSchema={
                "type": "object",
                "properties": {
                    "spaceId": {
                        "type": "integer",
                        "format": "int64",
                        "description": "Wiki space ID (required)",
                    },
                    "mode": {
                        "type": "string",
                        "enum": sorted(VALID_GRAPH_MODES),
                        "description": "full (default) or summary hub view",
                    },
                    "maxNodes": {
                        "type": "integer",
                        "minimum": 1,
                        "maximum": 2000,
                        "description": "Node cap (REST default full=300 / summary=50)",
                    },
                    "minDeg": {
                        "type": "integer",
                        "minimum": 0,
                        "description": "Keep nodes with degree >= minDeg (default 0)",
                    },
                },
                "required": ["spaceId"],
            },
        ),
    ]


def _require_text(name: str, value: Any, *, max_len: int = 500) -> str:
    if not isinstance(value, str):
        raise KbToolError("KB_INVALID_INPUT", f"{name} must be a string.")
    text = value.strip()
    if not text:
        raise KbToolError("KB_INVALID_INPUT", f"{name} must not be empty.")
    if len(text) > max_len:
        raise KbToolError("KB_INVALID_INPUT", f"{name} must be at most {max_len} characters.")
    return text


def _optional_space_ids(args: dict[str, Any], cfg: McpKbConfig) -> list[int] | None:
    if "spaceIds" in args and args["spaceIds"] is not None:
        raw = args["spaceIds"]
        if not isinstance(raw, list):
            raise KbToolError("KB_INVALID_INPUT", "spaceIds must be an array of int64.")
        return [int(x) for x in raw]
    if cfg.default_space_ids:
        return list(cfg.default_space_ids)
    return None


def _optional_strategy(args: dict[str, Any], cfg: McpKbConfig) -> str | None:
    if "retrievalStrategy" in args and args["retrievalStrategy"] is not None:
        strategy = str(args["retrievalStrategy"]).strip()
        if strategy not in VALID_STRATEGIES:
            raise KbToolError("KB_INVALID_INPUT", "retrievalStrategy must be ngram|hybrid|hybrid-rerank.")
        return strategy
    return cfg.default_strategy


def _citation_to_hit(item: dict[str, Any]) -> dict[str, Any]:
    return {
        "docId": item.get("docId"),
        "spaceId": item.get("spaceId"),
        "slug": item.get("slug"),
        "title": item.get("title"),
        "kbType": item.get("kbType"),
        "snippet": item.get("snippet"),
    }


async def handle_kb_search(client: KbRestClient, cfg: McpKbConfig, args: dict[str, Any]) -> types.CallToolResult:
    query = _require_text("query", args.get("query"))
    body: dict[str, Any] = {
        "question": query,
        "useLlm": False,
    }
    space_ids = _optional_space_ids(args, cfg)
    if space_ids:
        body["spaceIds"] = space_ids
    if "topK" in args and args["topK"] is not None:
        body["topK"] = int(args["topK"])
    strategy = _optional_strategy(args, cfg)
    if strategy:
        body["retrievalStrategy"] = strategy

    data = await client.post_ask(body)
    citations = data.get("citations") or []
    hits = [_citation_to_hit(c) for c in citations if isinstance(c, dict)]
    return tool_success_result(
        {
            "hits": hits,
            "scope": data.get("scope"),
            "scopeReason": data.get("scopeReason"),
        }
    )


async def handle_kb_ask(client: KbRestClient, cfg: McpKbConfig, args: dict[str, Any]) -> types.CallToolResult:
    question = _require_text("question", args.get("question"))
    use_llm = True if "useLlm" not in args or args["useLlm"] is None else bool(args["useLlm"])
    body: dict[str, Any] = {
        "question": question,
        "useLlm": use_llm,
    }
    space_ids = _optional_space_ids(args, cfg)
    if space_ids:
        body["spaceIds"] = space_ids
    if "topK" in args and args["topK"] is not None:
        body["topK"] = int(args["topK"])
    if "llmContextTopK" in args and args["llmContextTopK"] is not None:
        body["llmContextTopK"] = int(args["llmContextTopK"])
    strategy = _optional_strategy(args, cfg)
    if strategy:
        body["retrievalStrategy"] = strategy

    data = await client.post_ask(body)
    citations = data.get("citations") or []
    if not isinstance(citations, list):
        citations = []
    out = {
        "answer": data.get("answer"),
        "mode": data.get("mode"),
        "scope": data.get("scope"),
        "scopeReason": data.get("scopeReason"),
        "citations": citations,
        "provider": data.get("provider"),
        "model": data.get("model"),
        "qaLogId": data.get("qaLogId"),
    }
    return tool_success_result(out)


def _require_int64(name: str, value: Any) -> int:
    if value is None:
        raise KbToolError("KB_INVALID_INPUT", f"{name} is required.")
    try:
        return int(value)
    except (TypeError, ValueError) as exc:
        raise KbToolError("KB_INVALID_INPUT", f"{name} must be an int64.") from exc


async def handle_kb_graph(client: KbRestClient, _cfg: McpKbConfig, args: dict[str, Any]) -> types.CallToolResult:
    space_id = _require_int64("spaceId", args.get("spaceId"))
    mode: str | None = None
    if "mode" in args and args["mode"] is not None:
        mode = str(args["mode"]).strip()
        if mode not in VALID_GRAPH_MODES:
            raise KbToolError("KB_INVALID_INPUT", "mode must be full or summary.")
    max_nodes: int | None = None
    if "maxNodes" in args and args["maxNodes"] is not None:
        max_nodes = int(args["maxNodes"])
    min_deg: int | None = None
    if "minDeg" in args and args["minDeg"] is not None:
        min_deg = int(args["minDeg"])

    data = await client.get_wiki_graph(space_id, mode=mode, max_nodes=max_nodes, min_deg=min_deg)
    out = {
        "nodes": data.get("nodes") or [],
        "links": data.get("links") or [],
        "meta": data.get("meta") or {},
    }
    return tool_success_result(out)


async def dispatch_tool(
    name: str,
    arguments: dict[str, Any] | None,
    client: KbRestClient,
    cfg: McpKbConfig,
) -> types.CallToolResult:
    args = arguments or {}
    try:
        if name == "kb_search":
            return await handle_kb_search(client, cfg, args)
        if name == "kb_ask":
            return await handle_kb_ask(client, cfg, args)
        if name == "kb_graph":
            return await handle_kb_graph(client, cfg, args)
        raise KbToolError("KB_UNKNOWN_TOOL", f"Unknown tool: {name}")
    except KbToolError as exc:
        return tool_error_result(exc)
