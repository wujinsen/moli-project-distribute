# 文档健康度巡检

> **巡检日期**：2026-07-09（第四轮 · draw.io 主图查漏补缺）  
> **范围**：PRD / 设计 / API / 测试 / 运维 / SQL / 模块 README / wiki 成品 / 架构图  
> **约定**：[`README.md`](README.md) · [`AGENTS.md`](../AGENTS.md) · [`docs/diagrams/README.md`](diagrams/README.md)

## draw.io 主图巡检（2026-07-09）

**规则**（[`AGENTS.md`](../AGENTS.md) §3）：架构 / 调用链 / ER / 业务流程 → **PNG 主图 + `.drawio` 链接**；ASCII / Mermaid 仅放 `<details>` 备查。

| 文档 | 状态 | 主图 |
|------|------|------|
| `docs/zh-CN/RBAC.md`（及 en/ja） | ✅ | rbac-model · auth-flow · rbac-menu-query · user-center-position |
| `docs/zh-CN/ARCHITECTURE.md`（及 en/ja） | ✅ | container-architecture · auth-flow · auth-layers · gateway-routes · seckill · deploy-topology |
| `docs/design/user-center-detailed-design.md` | ✅ | auth-flow（§3.1 登录） |
| `moli-knowledge/README.md` | ✅ | kb-architecture |
| `moli-distribute-common/README.md` | ✅ | container-architecture（依赖简图备查） |
| `wiki-moli/develop/*概要设计` | 🔄 | 网关/用户中心/订单/知识库/LLM/导入等 → 见子 agent 批次 |
| `docs/design/user-center-detailed-design.md` §1 分层 | 🔵 | 目录树 ASCII，非架构主图，保留 |
| `docs/api/KNOWLEDGE_API.md` | 🔵 | 已有 category-flow PNG；其余为 JSON 示例非主图 |

**仍可选**：`wiki-moli/develop/BI模块-概要设计.md` 等少量 ASCII 简图；`docs/design/archive/` 历史稿不强制。

## 总览

| 维度 | 状态 | 说明 |
|------|------|------|
| 五类文档索引 | ✅ | `docs/{product,design,api,test,ops}/README.md` |
| 五微服务 wiki 页 | ✅ | `kb/wiki-moli/develop/` |
| 茉莉系统手册 | ✅ | `kb/wiki-moli/`（`moli-ops-manual`） |
| enterprise-kb | ✅ | `kb/wiki/`（通用技术文库，与手册分空间） |
| 网关 / 用户中心 / 订单 / 知识库全链路 | ✅ | PRD·设计·API·测试 |
| BI（moli-ai） | ✅ | 设计 + 产品摘要 + bi-smoke |
| 公共模块 README | ✅ | `moli-distribute-common/README.md` |
| 架构图 PNG | ✅ | `docs/diagrams/png/`（含 T20/T22/Meilisearch） |
| API 迭代地图 | ✅ | [`docs/api/api-iteration-map.md`](api/api-iteration-map.md) |
| 生产部署索引 | ✅ | [`deploy/README.md`](../deploy/README.md) · [`deploy/上线流程.md`](../deploy/上线流程.md) |
| T22 wujinsen 插图回迁 | ✅ | manifest 397/397 · R3 PASS · 见 [`test/knowledge-t22-image-remediation.md`](test/knowledge-t22-image-remediation.md) |
| raw/prd 投喂 | ✅ | gateway / order / user-center 三份 |

## 按模块

| 模块 | PRD | 设计 | API | 测试 | wiki product |
|------|-----|------|-----|------|--------------|
| gateway | ✅ | ✅ | ✅ | ✅ | ✅ |
| user-center | ✅ | ✅ | ✅ | ✅ | ✅ |
| order | ✅ | ✅ | ✅ | ✅ | ✅ |
| moli-ai (BI) | 🟡 scope | ✅ | ✅ | ✅ | ✅ |
| knowledge | ✅ | ✅ | ✅ | ✅ | ✅ |
| common | — | — | — | — | N/A |

## 本轮变更（2026-07-09）

| 项 | 动作 |
|----|------|
| draw.io 主图 | RBAC / ARCHITECTURE（三语）/ user-center 详设 / knowledge README / common README |
| 新增图 | `moli-rbac-menu-query.drawio` · `moli-auth-layers.drawio` |
| 巡检页 | 本文件 §draw.io 主图巡检 |

## 本轮变更（2026-07-05）

| 项 | 动作 |
|----|------|
| `docs/api-iteration-map.md` | 迁入 `docs/api/`，修正全仓断链 |
| `deploy/README.md` | 恢复部署目录索引 |
| `kb/tools/_*.txt`、`lint-*.json` | 删除临时工具产物 |
| `docs/test/knowledge-t22-image-remediation.md` | 新增 T22 验收与自动化测试索引 |
| `wiki-moli/guides/项目文档总览` | 修正 enterprise-kb ↔ `kb/wiki/` 映射 |
| `docs/diagrams/README.md` | 同上 · 三空间 Sync 表 |

## 仍可选（非阻塞）

| 项 | 说明 |
|----|------|
| meiling-ui 工作台 §10 | 🔵 部分前端规格待代码对齐 |
| raw/design 外部稿 | 有评审 PDF 时再投喂 |
| Meilisearch 生产接入 | 规划已入库，代码 v2+ |
| `docs/design/archive/` | 历史稿保留备查，非冗余 |

## 维护节奏

1. 改接口 → `docs/api/` + [`api/api-iteration-map.md`](api/api-iteration-map.md)
2. 改架构图 → `.drawio` → `export-diagrams.ps1`
3. 新服务 PRD → `raw/prd/` + `docs/product/` + wiki `*产品说明.md`
4. 发版前 → 更新本页日期 + [`release-smoke-checklist.md`](test/release-smoke-checklist.md)

## 相关

- [api/api-iteration-map.md](api/api-iteration-map.md)
- [product/README.md](product/README.md)
- [moli-v1-release-scope.md](product/moli-v1-release-scope.md)
- [kb/wiki-moli/guides/项目文档总览.md](../moli-knowledge/kb/wiki-moli/guides/项目文档总览.md)
