# 产品文档（PRD / 产品方案）

## 权威位置

| 阶段 | 路径 |
|------|------|
| **需求总览（现行）** | [`knowledge-workbench-requirements.md`](knowledge-workbench-requirements.md) — **冲突时以此为准** |
| **新稿投喂** | `moli-knowledge/kb/raw/prd/` |
| **维护 / 浏览** | `moli-knowledge/kb/wiki/guides/`（`type: guide`，`tags` 含 `product`） |
| **Web 浏览** | 空间 `enterprise-kb`，sync 后 `/kb/page?slug=...` |

## 已有页面

| 文档 | 路径 | 状态 |
|------|------|------|
| **知识库工作台需求总览** | [`knowledge-workbench-requirements.md`](knowledge-workbench-requirements.md) | 2026-06-27 现行 |
| **Ingest 工作台产品方案** | [`kb/wiki/guides/Ingest工作台产品方案.md`](../../moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md) | ✅ T15 + T18 + 模板模式 |
| **Wiki 治理工作台产品方案** | [`kb/wiki/guides/Wiki治理工作台产品方案.md`](../../moli-knowledge/kb/wiki/guides/Wiki治理工作台产品方案.md) | T16 后端 ✅；前端 T16f 🔵 |
| Wiki 在线编辑与 AI 改稿 | `kb/wiki/guides/Wiki在线编辑与AI协助改稿.md` | T14 ✅ |
| 路线图 | `kb/ROADMAP.md`（模块级，非 wiki 页） | — |
| 用户中心 PRD | `kb/raw/prd/user-center-prd-v1.md` → ingest 后 `wiki/guides/` | — |

**Ingest / 治理配套工程文档**（非 PRD 正文，联调必看）：

| 文档 | 路径 |
|------|------|
| HTTP API §8–9 | [`docs/api/KNOWLEDGE_API.md`](../api/KNOWLEDGE_API.md) |
| Wiki 治理前端 | [`docs/api/wiki-govern-frontend.md`](../api/wiki-govern-frontend.md) |
| 脚本 vs LLM | [`docs/test/knowledge-script-vs-llm-matrix.md`](../test/knowledge-script-vs-llm-matrix.md) |
| 模板入库 | [`docs/test/knowledge-ingest-template-mode.md`](../test/knowledge-ingest-template-mode.md) |
| 开发任务 | [`moli-knowledge/TASKS.md`](../../moli-knowledge/TASKS.md) |
| DDL | [`docs/sql/08_kb_ingest_workbench.sql`](../sql/08_kb_ingest_workbench.sql)、[`09`](../sql/09_kb_ingest_t15e.sql) |

## 工作流

1. 新版 PRD → 放入 `raw/prd/`（新文件，不覆盖旧 raw）
2. Agent Ingest，默认 **§4.1 策略 A**（enrich 已有 slug）
3. `lint.py` → `sync_to_db.py`

## 不要

- 在 `docs/product/` 写长篇 PRD 正文（本目录仅索引）
- 为 v2/v5 新建平行 slug（除非策略 C）
