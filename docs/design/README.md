# 技术方案 / 架构设计文档

## 权威位置

| 层级 | 路径 | 说明 |
|------|------|------|
| **官方薄层** | `docs/zh-CN/ARCHITECTURE.md`、`TECH_STACK.md`、`RBAC.md` | 多语言：`docs/en/`、`docs/ja/` |
| **可视化** | `docs/diagrams/*.drawio` | C4、ER、RAW 全链路 |
| **新稿投喂** | `kb/raw/design/` | 外部方案、评审稿 |
| **详细知识** | `kb/wiki-moli/develop/concepts/`、`kb/wiki-moli/develop/articles/` | Ingest 后浏览 |
| **综合汇总** | `kb/wiki-moli/develop/outputs/` | Query crystallize |

## 类型映射

| 内容 | wiki 目录 |
|------|-----------|
| 跨模块概念（RBAC、秒杀、网关） | `concepts/` |
| **用户中心** | [`user-center-overview.md`](user-center-overview.md) · [`user-center-detailed-design.md`](user-center-detailed-design.md) · [`portal-system-group.md`](portal-system-group.md) |
| **订单 · 秒杀** | [`order-seckill-design.md`](order-seckill-design.md) |
| **知识库 LLM 平台设置（T19）** | [`kb-llm-platform-settings.md`](kb-llm-platform-settings.md) · 前端 [`../api/kb-llm-platform-frontend.md`](../api/kb-llm-platform-frontend.md) |
| **知识库模块总览** | [`knowledge-module-overview.md`](knowledge-module-overview.md) |
| **BI 模块（v1 骨架）** | [`bi-module-overview.md`](bi-module-overview.md) · [bi-api.md](../api/bi-api.md) |
| **API 网关** | [`gateway-design.md`](gateway-design.md) · 路由 [`../api/gateway-routes.md`](../api/gateway-routes.md) |
| **运维规划（两条独立线）** | [`server-ops-module-roadmap.md`](server-ops-module-roadmap.md)（服务器/基础设施运维·user-center）· [`kb-ops-roadmap.md`](kb-ops-roadmap.md)（知识库内容管道运维·moli-knowledge） |
| 方案长文、踩坑、最佳实践 | `articles/` |
| 微服务边界一页纸 | `services/` |

## 工作流

外部《技术方案 v3》→ `raw/design/` → Ingest → enrich 已有 concept/article → sync。

## 相关

- [ARCHITECTURE.md](../zh-CN/ARCHITECTURE.md)
- [RBAC.md](../zh-CN/RBAC.md)
- [diagrams/README.md](../diagrams/README.md)
- 历史审查纪要：[archive/](archive/)（2026-06-25 Ingest / Sync·Ask）
- [知识库 LLM 平台系统设置（T19）](kb-llm-platform-settings.md)

## 可视化（draw.io）

| 图 | 说明 |
|----|------|
| ![网关路由](../diagrams/png/moli-gateway-routes.png) | [moli-gateway-routes.drawio](../diagrams/moli-gateway-routes.drawio) |
| ![RBAC 模型](../diagrams/png/moli-rbac-model.png) | [moli-rbac-model.drawio](../diagrams/moli-rbac-model.drawio) |
| ![秒杀链路](../diagrams/png/moli-seckill-flow.png) | [moli-seckill-flow.drawio](../diagrams/moli-seckill-flow.drawio) |
| ![本地部署](../diagrams/png/moli-deploy-topology.png) | [moli-deploy-topology.drawio](../diagrams/moli-deploy-topology.drawio) |
