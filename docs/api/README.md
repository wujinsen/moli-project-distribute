# API 接口文档

> **本目录为 HTTP 接口契约的权威位置**（随代码演进，不依赖 ingest 进 wiki 才能联调）。

## 文件清单

| 文件 | 范围 |
|------|------|
| [KNOWLEDGE_API.md](KNOWLEDGE_API.md) | 知识库模块 `/kb/*`（meiling-ui 对接）；**§9 Ingest 工作台**（T15，21 个接口） |
| [wiki-govern-frontend.md](wiki-govern-frontend.md) | **Wiki 治理工作台前端对接**（T16e：Lint + 脚本/AI/一键修复） |
| [../test/knowledge-ingest-template-mode.md](../test/knowledge-ingest-template-mode.md) | **Ingest 模板模式**（`useLlmGenerate=false`） |
| [../test/knowledge-script-vs-llm-matrix.md](../test/knowledge-script-vs-llm-matrix.md) | 脚本 vs LLM 能力矩阵 |
| [user-center-api-map.md](user-center-api-map.md) | 用户中心 ~70 接口地图（登录/RBAC/系统门户） |
| [user-center-dubbo.md](user-center-dubbo.md) | 用户中心 Dubbo 契约（3 方法） |

各微服务 **Swagger**（运行时）：见 [ops 空间](../ops/README.md) `swagger接口调试指南` 或各服务 `:端口/swagger-ui.html`。

## 原料 / wiki 关系

| 用途 | 位置 |
|------|------|
| 可选投喂副本 | `kb/raw/api/` |
| 服务一页纸 | `kb/wiki/services/`、`kb/wiki-ops/services/`（链到本文档，不复制全文） |

## 维护规则

- 改接口 → **直接改本目录** + 必要时改 Controller/JavaDoc
- 大版本 API 说明 PDF/Word → `raw/api/` → Ingest 只更新 **services/** 摘要页
- 知识库 REST 变更须同步 [KNOWLEDGE_API.md](KNOWLEDGE_API.md) 与 `moli-knowledge-server` README
