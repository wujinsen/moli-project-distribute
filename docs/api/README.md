# API 接口文档

> **本目录为 HTTP 接口契约的权威位置**（随代码演进，不依赖 ingest 进 wiki 才能联调）。

## 平台 v1

| 文档 | 范围 |
|------|------|
| **[api-iteration-map.md](api-iteration-map.md)** | 跨服务 API 成熟度 / v1→v2 索引 |
| **[gateway-routes.md](gateway-routes.md)** | 网关四路由、StripPrefix、Swagger 经网关 |
| [frontend-routes-map.md](frontend-routes-map.md) | meiling-ui 菜单 ↔ 后端 API |
| [gateway-design.md](../design/gateway-design.md) | 网关概要设计 |
| [user-center-api-map.md](user-center-api-map.md) | 用户中心 ~70 HTTP |
| **[operation-frontend.md](operation-frontend.md)** | **服务器运维 · 运营管理 · meiling-ui 对接（S0–S9）** |
| **[operation-deploy-api.md](operation-deploy-api.md)** | **部署中心 HTTP 契约（SVR-13~20，后端权威）** |
| [user-center-dubbo.md](user-center-dubbo.md) | Dubbo 三方法 |
| [order-seckill-api.md](order-seckill-api.md) | 秒杀 `/seckill/*` |
| [bi-api.md](bi-api.md) | BI 骨架 `/demo/test` |

## 知识库工作台

| 文档 | 用途 |
|------|------|
| [KNOWLEDGE_API.md](KNOWLEDGE_API.md) | `/kb/*` 契约 |
| [knowledge-workbench-frontend.md](knowledge-workbench-frontend.md) | 前端总览 B1–B10 |
| **[knowledge-ops-frontend.md](knowledge-ops-frontend.md)** | **KB 运维 · Sync O1–O4 · 排期（给前端）** |
| [ingest-workbench-frontend.md](ingest-workbench-frontend.md) | Ingest I1–I5 · Tab2 |
| [kb-import-entry-frontend.md](kb-import-entry-frontend.md) | **T20** Tab1 Raw 投喂 + Tab3 成品导入 |
| [wiki-govern-frontend.md](wiki-govern-frontend.md) | Wiki 治理 W1–W8 |
| [kb-markdown-image-frontend.md](kb-markdown-image-frontend.md) | T22 F1 · `KbMarkdownImage` 插图鉴权 |
| [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) | LLM 平台 T19 |

## 测试配套

| 文档 | 用途 |
|------|------|
| [../test/knowledge-ingest-acceptance.md](../test/knowledge-ingest-acceptance.md) | Ingest 验收 |
| [../test/order-seckill.md](../test/order-seckill.md) | 秒杀手测 |
| [../test/release-smoke-checklist.md](../test/release-smoke-checklist.md) | 上线冒烟 |

Swagger（运行时）：各服务 `:端口/swagger-ui.html` 或经网关（见 gateway-routes）。

## 维护规则

- 改接口 → **直接改本目录** + Controller
- 知识库 REST → 同步 KNOWLEDGE_API + `moli-knowledge-server` README
