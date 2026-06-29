---
title: API契约索引
slug: API契约索引
type: guide
status: active
tags: [API, 契约, 联调, 索引]
sources:
  - docs/api/README.md
  - docs/api-iteration-map.md
related: [项目文档总览, swagger接口调试指南, 网关, 知识库服务]
created: 2026-06-20
updated: 2026-06-20
---

# API 契约索引

> **权威位置**：[`docs/api/`](../../../../docs/api/)（改接口必改此目录 + Controller）。  
> **不**将 OpenAPI 全文 ingest 进 wiki；本页为 **联调导航**。

## 跨服务

| 文档 | 说明 |
|------|------|
| [api-iteration-map.md](../../../../docs/api-iteration-map.md) | 各服务 API 成熟度 |
| [gateway-routes.md](../../../../docs/api/gateway-routes.md) | 四路由、StripPrefix |
| [frontend-routes-map.md](../../../../docs/api/frontend-routes-map.md) | 前端菜单 ↔ API |

## 平台 v1

| 服务 | 契约 |
|------|------|
| 用户中心 HTTP | [user-center-api-map.md](../../../../docs/api/user-center-api-map.md) |
| 用户中心 Dubbo | [user-center-dubbo.md](../../../../docs/api/user-center-dubbo.md) |
| 订单秒杀 | [order-seckill-api.md](../../../../docs/api/order-seckill-api.md) |
| BI | [bi-api.md](../../../../docs/api/bi-api.md) |
| 知识库 REST | [KNOWLEDGE_API.md](../../../../docs/api/KNOWLEDGE_API.md) |

## 知识库工作台（前端）

| 文档 | 说明 |
|------|------|
| [knowledge-workbench-frontend.md](../../../../docs/api/knowledge-workbench-frontend.md) | 总览 B1–B10 |
| [ingest-workbench-frontend.md](../../../../docs/api/ingest-workbench-frontend.md) | Ingest I1–I5 |
| [wiki-govern-frontend.md](../../../../docs/api/wiki-govern-frontend.md) | Wiki 治理 W1–W8 |
| [kb-llm-platform-frontend.md](../../../../docs/api/kb-llm-platform-frontend.md) | LLM 平台 T19 |

## 调试

- 经网关：`http://127.0.0.1:21000/{UserCenter|OrderServer|BiServer|KnowledgeServer}/...`
- 详见 [[swagger接口调试指南]]

## 相关

[[项目文档总览]] · [[测试文档索引]]
