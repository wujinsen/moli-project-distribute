---


title: API契约索引
slug: API契约索引
type: guide
status: active
tags: [API, 契约, 联调, 索引]
sources:
  - docs/api/README.md
  - docs/api/api-iteration-map.md
related: [项目文档总览, swagger接口调试指南, 网关, 知识库服务]
created: 2026-06-20
updated: 2026-07-05
---

# API 契约索引

> **权威位置**：`docs/api/`（改接口必改此目录 + Controller）。  
> **不**将 OpenAPI 全文 ingest 进 wiki；本页为 **联调导航**。

## 跨服务

| 文档 | 说明 |
|------|------|
| `docs/api/api-iteration-map.md` | 各服务 API 成熟度 |
| `docs/api/gateway-routes.md` | 四路由、StripPrefix |
| `docs/api/frontend-routes-map.md` | 前端菜单 ↔ API |

## 平台 v1

| 服务 | 契约 |
|------|------|
| 用户中心 HTTP | `docs/api/user-center-api-map.md` |
| 用户中心 Dubbo | `docs/api/user-center-dubbo.md` |
| 订单秒杀 | `docs/api/order-seckill-api.md` |
| BI | `docs/api/ai-api.md` |
| 知识库 REST | `docs/api/KNOWLEDGE_API.md` |

## 知识库工作台（前端）

| 文档 | 说明 |
|------|------|
| `docs/api/knowledge-workbench-frontend.md` | 总览 B1–B10 |
| `docs/api/ingest-workbench-frontend.md` | Ingest I1–I5 |
| `docs/api/wiki-govern-frontend.md` | Wiki 治理 W1–W8 |
| `docs/api/kb-llm-platform-frontend.md` | LLM 平台 T19 |
| `docs/api/kb-markdown-image-frontend.md` | 插图 Asset R0/F2（T22） |

## 调试

- 经网关：`http://127.0.0.1:21000/{UserCenter|OrderServer|AiServer|KnowledgeServer}/...`
- 详见 [[swagger接口调试指南]]

## 相关

[[项目文档总览]] · [[测试文档索引]]
