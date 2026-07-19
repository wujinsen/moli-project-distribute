# ai-agent sidecar (ChatBI NL2SQL)

FastAPI sidecar for Java `ai-server` conductor.

## Run

```bash
cd moli-ai/moli-ai-server/ai-agent
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 1130
```

## Endpoints (contract §1.2)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Sidecar health |
| POST | `/agent/generate` | schema retrieve + NL→SQL |
| POST | `/agent/explain` | result summary + chart hint |

## Gradio smoke

```bash
# Terminal 1: ai-agent :1130
# Terminal 2: ai-server :1128
# Terminal 3:
python gradio_app.py
```

Env: `BI_CHAT_BASE=http://127.0.0.1:1128`

Optional LLM: `BI_LLM_BASE_URL`, `BI_LLM_API_KEY`, `BI_LLM_MODEL`
