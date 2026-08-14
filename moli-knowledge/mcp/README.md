# Moli Knowledge MCP Server (AI-6)

Thin **stdio MCP proxy** for Cursor / Claude → existing Knowledge REST (`POST /kb/ask`, `GET /kb/wiki/graph`).  
No local ACL, no write tools, token via env only (M-INV-1/2/3).

## Tools

| Tool | REST | Notes |
|------|------|--------|
| `kb_search` | `POST /kb/ask` | **Always** `useLlm=false` → `hits` + scope |
| `kb_ask` | `POST /kb/ask` | Default `useLlm=true` (overridable) → full `AskResponse` |
| `kb_graph` | `GET /kb/wiki/graph` | Wiki disk graph → `nodes` / `links` / `meta` (GraphVo) |

## Setup

```bash
cd moli-knowledge/mcp
pip install -r requirements.txt
```

## Environment (§3)

| Variable | Default | Description |
|----------|---------|-------------|
| `MCP_KB_BASE_URL` | `http://127.0.0.1:21000/KnowledgeServer` | Gateway (StripPrefix) or direct `http://127.0.0.1:8090` |
| `MCP_KB_TOKEN` | _(empty)_ | Shiro sessionId → `Authorization` header |
| `MCP_KB_TIMEOUT_MS` | `15000` | REST timeout |
| `MCP_KB_DEFAULT_SPACE_IDS` | _(empty)_ | Comma-separated int64, e.g. `900000000000000003` |
| `MCP_KB_DEFAULT_STRATEGY` | _(empty)_ | `ngram` / `hybrid` / `hybrid-rerank` |

## Get token (login)

Knowledge API uses **`Authorization: <sessionId>`** (same as Web / `eval_ask.py`):

```bash
# user-center login (default dev)
curl -s -X POST http://127.0.0.1:8888/login \
  -H "Content-Type: application/json" \
  -d "{\"userName\":\"admin\",\"password\":\"123456\"}"
# → data.token
```

Set `MCP_KB_TOKEN` to that value. **Do not commit tokens.**

## Run MCP server (stdio)

```bash
cd moli-knowledge/mcp
set MCP_KB_TOKEN=<your-session-token>
set MCP_KB_BASE_URL=http://127.0.0.1:8090
python -m mcp_server
```

## Cursor `mcp.json` sample (§3)

Add to Cursor MCP settings (adjust paths for your machine):

```json
{
  "mcpServers": {
    "moli-kb": {
      "command": "python",
      "args": ["-m", "mcp_server"],
      "cwd": "D:/work/moli_project/moli-project-distribute/moli-knowledge/mcp",
      "env": {
        "MCP_KB_BASE_URL": "http://127.0.0.1:21000/KnowledgeServer",
        "MCP_KB_TOKEN": "<登录后 sessionId，勿入库>"
      }
    }
  }
}
```

Use gateway URL in production-like setups; use `8090` direct when gateway is down.

## Cursor demo (Phase B)

Prerequisites: `moli-knowledge-server` (8090 or gateway), user-center login token in `MCP_KB_TOKEN`, MCP server enabled in Cursor.

1. **Reload MCP** — Settings → MCP → confirm `moli-kb` shows 3 tools (`kb_search`, `kb_ask`, `kb_graph`).

2. **`kb_ask` — 带引用问答**  
   In Agent chat:
   ```
   @moli-kb 用 kb_ask 回答：wiki 怎么同步到数据库？请列出 citations 里的 slug。
   ```
   Expect: `answer` + non-empty `citations` (docId, spaceId, slug, snippet). Answers must cite backend sources, not invented pages.

3. **`kb_search` — 只检索**  
   ```
   @moli-kb 用 kb_search 查「本地启动指南」，只要 hits，不要生成答案。
   ```
   Expect: `hits[]` with slug/snippet, `mode` not used (no LLM answer).

4. **`kb_graph` — 看关联**  
   ```
   @moli-kb 用 kb_graph 看 spaceId=900000000000000003 的 wiki 关联图，mode=summary。
   ```
   Expect: `nodes[]` (slug id, title, deg), `links[]`, `meta.source=wiki_file`.

5. **鉴权** — Remove or expire `MCP_KB_TOKEN`, call any tool → structured `KB_UNAUTHORIZED` (server does not crash).

## Smoke tests

Requires running `moli-knowledge-server` (8090 or gateway) + user-center login:

```bash
cd moli-knowledge/mcp

# Phase B: MCP stdio E2E (list_tools + three tools)
python smoke.py
# list_tools: ['kb_search', 'kb_ask', 'kb_graph']
# kb_search hits=N ...
# kb_ask citations=N ...
# kb_graph nodes=N ...
# OK

# Direct REST dispatch (no stdio subprocess)
python smoke_local.py
```

Optional overrides: `MOLI_LOGIN_BASE`, `MCP_KB_BASE_URL`, `MOLI_EVAL_USER`, `MOLI_EVAL_PASS`.

## Errors (M-INV-5 / M-INV-7)

Structured tool errors (JSON text, `isError=true`):

| code | When |
|------|------|
| `KB_UNAUTHORIZED` | Missing/invalid `MCP_KB_TOKEN` or HTTP 401/403 |
| `KB_UPSTREAM_TIMEOUT` | REST timeout (`MCP_KB_TIMEOUT_MS`) |
| `KB_UPSTREAM_ERROR` | Other HTTP / API / unexpected failures |
| `KB_INVALID_INPUT` | Schema validation |

Messages are sanitized: no token, stack trace, or connection string in tool output.

## Contract

See `docs/design/contracts/AI-6-contract.md`.
