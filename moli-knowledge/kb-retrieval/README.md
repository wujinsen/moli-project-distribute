# kb-retrieval · 向量检索 Sidecar（AI-2 W2）

Python FastAPI 服务：bge-m3 embedding + Chroma 本地持久化，供 `moli-knowledge-server` 在 W3 经 HTTP 调用 `/search`（本阶段只做 sidecar + 离线索引）。

契约：[`docs/design/contracts/AI-2-contract.md`](../../docs/design/contracts/AI-2-contract.md) §1 sidecar 接口 · §3 Phase W2。

## 依赖

```powershell
cd moli-knowledge/kb-retrieval
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

首次启动会下载 `BAAI/bge-m3`（约 2GB+），可通过环境变量 `EMBED_MODEL` 覆盖。

## 环境变量

| 变量 | 默认 | 说明 |
|------|------|------|
| `RETRIEVAL_HOST` | `127.0.0.1` | 绑定地址（S1：默认本机，勿暴露 `/embed` 写接口） |
| `RETRIEVAL_PORT` | `8099` | HTTP 端口 |
| `RETRIEVAL_BASE_URL` | `http://127.0.0.1:8099` | CLI 调 sidecar 基址 |
| `EMBED_MODEL` | `BAAI/bge-m3` | sentence-transformers 模型 |
| `EMBED_DIM` | `1024` | 向量维度（bge-m3） |
| `CHROMA_PATH` | `kb-retrieval/.chroma` | Chroma 持久化目录（已 gitignore） |
| `CHROMA_COLLECTION` | `moli_kb_chunks_bgem3_v1` | collection 名 |
| `EMBED_BATCH_SIZE` | `32` | `/embed` 与索引 CLI 批大小 |
| `MYSQL_*` / `--host` 等 | 同 `sync_to_db.py` | 索引 CLI 连库 |

## 启动 Sidecar

```powershell
cd moli-knowledge/kb-retrieval
$env:RETRIEVAL_PORT = "8099"
python -m uvicorn app.main:app --host 127.0.0.1 --port 8099
```

> 启动时会预热 embedding + rerank 模型（S2），避免 Java 首查 `timeout-ms=1500` 误降级。

健康检查：

```powershell
curl http://127.0.0.1:8099/health
```

## 离线索引（Sync 之后）

推荐顺序（见 `kb/AGENTS.md` §8.1）：`sync_to_db.py` → **本索引** →（W3）Java hybrid。

```powershell
# 全量/增量（content_hash 幂等）
python kb-retrieval/scripts/index_chunks.py

# 预览
python kb-retrieval/scripts/index_chunks.py --dry-run

# 强制重嵌
python kb-retrieval/scripts/index_chunks.py --force

# 只索引 moli-ops-manual（冒烟 / 局部重建）
python kb-retrieval/scripts/index_chunks.py --space-id 900000000000000003
```

只索引 `kb_document_chunk` 中 `status=1`（PUBLISHED）且 `is_delete=0` 的切段。

## 冒烟：`/search` 对 dirty query

```powershell
# 1) 启动 sidecar（另开终端）
# 2) 建索引
python kb-retrieval/scripts/index_chunks.py

# 3) 检索（golden M21 口语化问法）
curl -s -X POST http://127.0.0.1:8099/search `
  -H "Content-Type: application/json" `
  -d '{"query":"本地启全套微服雾咋整啊","spaceIds":[900000000000000003],"topN":8}'
```

期望：`results` 非空，且 top 结果含 `guides/本地启动指南` 相关 slug。

## API 摘要

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 + 索引条数 |
| POST | `/embed` | 批量嵌入 + Chroma upsert（幂等 contentHash） |
| POST | `/search` | query 向量召回 top-N |
| POST | `/rerank` | cross-encoder 精排 top-M（`hybrid-rerank` 用） |

错误响应：`{ "error": "<code>", "message": "<脱敏>" }`（非 2xx）。

> W3 Java `KbAskServiceImpl` 经 `KbRetrievalClient` 调 `/search`/`/rerank`；`retrieval-strategy=ngram` 不触 sidecar。

## 目录

```
kb-retrieval/
  app/           FastAPI 应用
  scripts/       index_chunks.py 离线索引 CLI
  .chroma/       向量库（本地，不入 git）
  requirements.txt
```
