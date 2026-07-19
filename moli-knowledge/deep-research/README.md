# DeepResearch Sidecar (AI-10)

Python sidecar for multi-agent DeepResearch: **Planner → Retriever → Writer → Reviewer** (+ bounded backfill). Java 薄壳经 `POST /kb/research` + SSE 调用本服务。

## Quick start

```bash
cd moli-knowledge/deep-research
python -m venv .venv
.venv\Scripts\activate        # Windows
pip install -r requirements.txt

# KnowledgeServer must be running; pass session token for ACL (D-INV-3)
set KB_BASE_URL=http://127.0.0.1:8090
set KB_AUTH_TOKEN=<Authorization header value>

uvicorn deep_research.main:app --host 0.0.0.0 --port 8095
```

## CLI smoke

```bash
python -m deep_research.cli --topic "茉莉微服务架构" --space-id 900000000000000003 --auth-token "%KB_AUTH_TOKEN%"
```

## Phase B smoke（slug 稳定性）

```bash
python smoke.py --topic "茉莉微服务架构" --space-id 900000000000000003 --json-out smoke-report.json
```

同一主题连续两次运行，对比 `citations` / `source_pages` 的 slug 集合（允许正文措辞漂移）。

## HTTP API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Sidecar health |
| POST | `/v1/research` | Full pipeline → outline + sectionEvidence + reportMd + coverage |

Retriever **only** calls existing Knowledge REST (`POST /kb/ask` with `useLlm=false`, optional `/kb/ask/agentic` when `agentic=true`). No hybrid/graph reimplementation in sidecar.

Writer 正文 `[[slug]]` 仅来自证据池；Reviewer 标 `unsupported` **不删句**。`writeback` 由 Java 壳走 `/kb/ingest/*`（D-INV-1）。

## Environment

| Variable | Default | Description |
|----------|---------|-------------|
| `KB_BASE_URL` | `http://127.0.0.1:8090` | KnowledgeServer base URL |
| `KB_AUTH_TOKEN` | empty | Forwarded as `Authorization` on `/kb/ask` |
| `RESEARCH_PORT` | `8095` | Sidecar listen port |
| `OPENAI_API_KEY` | empty | Optional Planner/Writer/Reviewer LLM; heuristic fallback when unset |

## Tests

```bash
pytest tests/ -q
```

## Java integration

KnowledgeServer exposes `POST /kb/research/start` + `GET /kb/research/{runId}/stream` (SSE). Configure:

```yaml
kb:
  research:
    enabled: false
    sidecar-base-url: http://127.0.0.1:8095
    writeback-space-id: 900000000000000003
    writeback-raw-path: deep-research/writeback-stub.md
```

See `docs/design/contracts/AI-10-contract.md` · API：`docs/api/KNOWLEDGE_API.md` §3 DeepResearch.
