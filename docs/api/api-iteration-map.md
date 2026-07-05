# API 迭代地图（跨服务）

> **用途**：联调前看「哪条接口已稳定、哪条仍是骨架」；详细字段以各服务契约为准。  
> **权威契约**：本目录各 `*-api*.md` + 运行时 Swagger。

## 图例

| 状态 | 含义 |
|------|------|
| **v1 稳定** | 纳入 [v1 发布范围](../product/moli-v1-release-scope.md) 冒烟 |
| **v1 骨架** | 路由/占位可用，业务未验收 |
| **工作台** | 知识库 Ingest / Wiki 治理 / LLM 平台 |
| **规划** | v2+，文档或代码未落地 |

## 网关 · `moli-gateway` :21000

| 能力 | 契约 | 状态 |
|------|------|------|
| 四路由转发 | [gateway-routes.md](gateway-routes.md) | **v1 稳定** |
| StripPrefix / Swagger 聚合 | 同上 | **v1 稳定** |

## 用户中心 · `user-center-server` :8888

| 域 | 契约 | 状态 |
|----|------|------|
| 登录 / 会话 / RBAC | [user-center-api-map.md](user-center-api-map.md) | **v1 稳定** |
| 门户 / SSO / 系统切换 | 同上 | **v1 稳定** |
| Dubbo 鉴权三方法 | [user-center-dubbo.md](user-center-dubbo.md) | **v1 稳定** |

## 订单 · `order-server` :8087

| 域 | 契约 | 状态 |
|----|------|------|
| 秒杀 `/seckill/*` | [order-seckill-api.md](order-seckill-api.md) | **v1 稳定** |
| 通用订单 CRUD | Swagger | **规划** |

## BI · `bi-server` :1128（模块 `moli-ai`）

| 域 | 契约 | 状态 |
|----|------|------|
| `GET /demo/test` | [bi-api.md](bi-api.md) | **v1 骨架** |
| 报表 / 大屏 | — | **规划** |

## 知识库 · `knowledge-server`

| 域 | 契约 | 状态 |
|----|------|------|
| 空间 / 文档 / 分类 / 标签 / 搜索 | [KNOWLEDGE_API.md](KNOWLEDGE_API.md) | **v1 稳定** |
| 问答 Ask | 同上 | **v1 稳定** |
| Ingest 工作台 | KNOWLEDGE_API + [ingest-workbench-frontend.md](ingest-workbench-frontend.md) | **工作台** |
| Wiki 治理 | [wiki-govern-frontend.md](wiki-govern-frontend.md) | **工作台** |
| 平台 LLM 配置 | [kb-llm-platform-frontend.md](kb-llm-platform-frontend.md) | **工作台** |
| 前端路由总表 | [frontend-routes-map.md](frontend-routes-map.md) | **工作台** |
| Raw / Wiki 插图 Asset（R0） | [kb-markdown-image-frontend.md](kb-markdown-image-frontend.md) · KNOWLEDGE_API §Asset | **v1 稳定**（T22） |
| Wiki 编辑页上传（F2） | 同上 · `POST /kb/wiki/asset` | **v1 稳定**（T22） |

## 前端 · meiling-ui

| 域 | 契约 | 状态 |
|----|------|------|
| 菜单 ↔ API 映射 | [frontend-routes-map.md](frontend-routes-map.md) | **工作台**（随菜单演进） |

## 维护规则

1. 新增/变更 HTTP 接口 → 改对应 `docs/api/*.md` + 本表一行。
2. v1 范围变更 → 同步 [moli-v1-release-scope.md](../product/moli-v1-release-scope.md) 与 [release-smoke-checklist.md](../test/release-smoke-checklist.md)。
3. 知识库大版本 → 同步 [KNOWLEDGE_API.md](KNOWLEDGE_API.md) 章节号与 `moli-knowledge-server` README。

## 相关

- [api/README.md](README.md) — 契约索引
- [docs/README.md](../README.md) — 五类文档地图
