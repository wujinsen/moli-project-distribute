# API 接口文档

> **本目录为 HTTP 接口契约的权威位置**（随代码演进，不依赖 ingest 进 wiki 才能联调）。

## 知识库工作台

| 文档 | 用途 |
|------|------|
| [../ops/knowledge-workbench-operations.md](../ops/knowledge-workbench-operations.md) | **操作手册** — 入库 §2、治理 §3、checklist §5 |
| [../product/knowledge-workbench-requirements.md](../product/knowledge-workbench-requirements.md) | 产品需求与现行决策 |
| **[knowledge-workbench-frontend.md](knowledge-workbench-frontend.md)** | 前端总览 + **§8 B1–B10** 联调 FAQ |
| [ingest-workbench-frontend.md](ingest-workbench-frontend.md) | Ingest 前端 · **§11 I1–I5** |
| [wiki-govern-frontend.md](wiki-govern-frontend.md) | Wiki 治理前端 · **§13 W1–W8** |
| [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) | LLM 平台设置（T19）前端 |

## 文件清单

| 文件 | 范围 |
|------|------|
| [KNOWLEDGE_API.md](KNOWLEDGE_API.md) | 知识库 `/kb/*` 契约；§8 Wiki · §9 Ingest |
| [user-center-api-map.md](user-center-api-map.md) | 用户中心 ~70 HTTP 接口 |
| [user-center-dubbo.md](user-center-dubbo.md) | 用户中心 Dubbo 契约（3 方法） |
| [../test/knowledge-ingest-acceptance.md](../test/knowledge-ingest-acceptance.md) | Ingest 验收清单（模板 / Express / T17 / 删批次） |
| [../test/knowledge-script-vs-llm-matrix.md](../test/knowledge-script-vs-llm-matrix.md) | 脚本 vs LLM 能力矩阵 |

各微服务 **Swagger**（运行时）：见 [ops 索引](../ops/README.md) 或各服务 `:端口/swagger-ui.html`。

## 原料 / wiki 关系

| 用途 | 位置 |
|------|------|
| 可选投喂副本 | `kb/raw/api/` |
| 服务一页纸 | `kb/wiki/services/`（链到本文档，不复制全文） |

## 维护规则

- 改接口 → **直接改本目录** + 必要时改 Controller/JavaDoc
- 大版本 API 说明 PDF/Word → `raw/api/` → Ingest 只更新 **services/** 摘要页
- 知识库 REST 变更须同步 [KNOWLEDGE_API.md](KNOWLEDGE_API.md) 与 `moli-knowledge-server` README
- 前端可交付功能 → 同步 **knowledge-workbench-frontend.md** 及对应子文档
