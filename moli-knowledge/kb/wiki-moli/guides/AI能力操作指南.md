---
title: AI 能力操作指南
slug: AI能力操作指南
type: guide
status: active
tags: [知识库, AI, M4, P1]
sources:
  - docs/design/ai-capability-roadmap.md
  - docs/product/ai-capability-prd.md
  - docs/design/contracts/AI-7-contract.md
  - docs/design/contracts/AI-8-contract.md
  - docs/design/contracts/AI-9-contract.md
  - docs/design/contracts/AI-10-contract.md
related: [知识库使用指南, DeepResearch调研指南, 查询与体检指南, BI服务产品说明, 本地启动指南]
created: 2026-07-20
updated: 2026-07-20
---

# AI 能力操作指南

> **M4 收官（v2.0 · 2026-07-20）**：AI-1～AI-10 全部交付。本页汇总**运维/联调**入口；契约与指标见 `docs/design/ai-capability-roadmap.md`。

## 1. 里程碑一览

| 里程碑 | 任务 | 状态 |
|--------|------|------|
| M1 | AI-1 评测 · AI-2 Hybrid · AI-3 门禁看板 | ✅ |
| M2 | AI-4 ChatBI / NL2SQL | ✅ |
| M3 | AI-5 GraphRAG · AI-6 MCP · AI-7 Agentic | ✅ |
| **M4** | **AI-8 LLM 网关 · AI-9 Guardrails · AI-10 DeepResearch** | **✅ 2026-07-20** |

## 2. 知识库侧（moli-knowledge-server）

| 能力 | HTTP / 配置 | 默认 | 操作文档 |
|------|-------------|------|----------|
| 单轮 RAG | `POST /kb/ask` | 开 | [[知识库使用指南]] |
| Hybrid / Rerank | `kb.search.retrieval-strategy` | `hybrid` | `docs/design/kb-hybrid-retrieval.md` |
| GraphRAG | `AskRequest.graphExpand` | 配置默认 | AI-5 契约 |
| Agentic RAG | `POST /kb/ask/agentic` · `kb.agentic.enabled` | **关** | AI-7 契约 |
| Guardrails | `kb.guardrails.enabled` | **关** | AI-9 契约 · 金样 `kb/eval/guardrails_inject.jsonl` |
| LLM 路由/缓存 | `kb.llm.router.*` · `kb.llm.cache.*` | **关** | AI-8 契约 · Ops 看板 |
| **DeepResearch** | `POST /kb/research` · `kb.research.enabled` | **关** | [[DeepResearch调研指南]] |
| 评测门禁 | `python kb/tools/eval_ask.py --gate-from-baselines` | — | `kb/eval/README.md` |
| MCP（Cursor） | `moli-knowledge/mcp/` | — | AI-6 契约 · `mcp/README.md` |

**零回归原则**：上述带「关」的开关默认关闭；开启前跑 AI-10 契约 §6.1（`eval_ask` ngram 门禁 + Guard 金样）。

## 3. ChatBI（moli-ai-server）

| 能力 | 入口 | 说明 |
|------|------|------|
| 自然语言查数 | `POST /AiServer/ai/chat/ask` | 需 `ai:chat:query` |
| 决策链路 | `GET /AiServer/ai/chat/trace/{id}` | 需 `ai:chat:trace` |
| NL2SQL 评测 | `moli-ai/moli-ai-server/bi/eval/eval_nl2sql.py --validator-only --gate` | CI 离线门禁 |

产品说明：[[BI服务产品说明]] · 契约：`docs/design/bi-chatbi-nl2sql-contract.md`

## 4. 发版前检查（M4 相关）

```powershell
# KB 检索未回归
cd moli-knowledge/kb
python tools/eval_ask.py --strategy ngram --gate-from-baselines --gate-at-k 3

# Guard 注入金样（离线）
cd ../moli-knowledge-server
mvn test "-Dtest=KbInjectDetectorGoldenTest,KbInputGuardServiceTest"

# ChatBI 校验器
cd ../../moli-ai/moli-ai-server/bi/eval
python eval_nl2sql.py --validator-only --gate

# DeepResearch 冒烟（需栈 + enabled=true）
# 见 docs/test/knowledge-deep-research-smoke.md
```

## 5. 文档索引

| 类型 | 路径 |
|------|------|
| 产品 PRD | `docs/product/ai-capability-prd.md` |
| 技术路线 | `docs/design/ai-capability-roadmap.md` |
| 排期 | `docs/design/ai-capability-schedule.md` |
| 契约目录 | `docs/design/contracts/AI-*-contract.md` |
| 路线图 PNG | `docs/diagrams/png/moli-ai-capability-roadmap.png` |
