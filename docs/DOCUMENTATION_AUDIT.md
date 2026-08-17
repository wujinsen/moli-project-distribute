# 文档健康度巡检

> **巡检日期**：2026-07-09（第四轮 · draw.io 主图查漏补缺）  
> **范围**：PRD / 设计 / API / 测试 / 运维 / SQL / 模块 README / wiki 成品 / 架构图  
> **约定**：[`README.md`](README.md) · [`AGENTS.md`](../AGENTS.md) · [`docs/diagrams/README.md`](diagrams/README.md)

## 总览

| 维度 | 状态 | 说明 |
|------|------|------|
| 五类文档索引 | ✅ | `docs/{product,design,api,test,ops}/README.md` |
| 五微服务 wiki 页 | ✅ | `kb/wiki-moli/develop/` |
| 茉莉系统手册 | ✅ | `kb/wiki-moli/`（`moli-ops-manual`） |
| enterprise-kb | ✅ | `kb/wiki/`（通用技术文库，与手册分空间） |
| 网关 / 用户中心 / 订单 / 知识库全链路 | ✅ | PRD·设计·API·测试 |
| AI（moli-ai） | ✅ | 设计 + 产品摘要 + ai-smoke |
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
| wiki-moli develop | 网关·用户中心·订单秒杀·知识库模块·LLM·双入口导入·三操作·ES 同步 — PNG 嵌入 + ASCII/Mermaid → `<details>` |
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
| `docs/diagrams/README.md` | 同上 · 两空间 Sync 表 |

## 仍可选（非阻塞）

| 项 | 说明 |
|----|------|
| meiling-ui 工作台 §10 | 🔵 部分前端规格待代码对齐 |
| raw/design 外部稿 | 有评审 PDF 时再投喂 |
| Meilisearch 生产接入 | 规划已入库，代码 v2+ |
| `docs/design/archive/` | 历史稿保留备查，非冗余 |

## draw.io 主图巡检 (2026-07-09)

**规则**：架构 / 部署 / 调用链 / ER / 业务流程类图 → **PNG 主展示** + 链到 `.drawio` 源文件；ASCII / Mermaid **仅**放在 `<details>` 备查。相对路径从各 Markdown 文件指向 `docs/diagrams/png/`。

### 本轮已完成（wiki-moli develop）

| 文件 | draw.io 图 |
|------|------------|
| `kb/wiki-moli/develop/API网关-概要设计.md` | `moli-deploy-topology` · `moli-gateway-routes` |
| `kb/wiki-moli/develop/用户中心-概要设计.md` | `moli-user-center-position` · `moli-rbac-model` · `moli-deploy-topology` |
| `kb/wiki-moli/develop/订单秒杀-概要设计.md` | `moli-seckill-flow` |
| `kb/wiki-moli/develop/知识库LLM平台设置.md` | `moli-kb-llm-settings-flow` |
| `kb/wiki-moli/develop/知识库双入口导入设计.md` | `moli-kb-import-entry` · `moli-kb-import-entry-api`（并修复 §1.3 / §4.4 表格损坏） |
| `kb/wiki-moli/develop/kb-wiki到es同步流水线.md` | `moli-kb-meilisearch` |
| `kb/wiki-moli/develop/知识库三操作.md` | `moli-kb-functional-flows` |
| `kb/wiki-moli/develop/知识库模块-概要设计.md` | §2 `moli-kb-architecture` · `moli-kb-raw-pipeline`；§5 `moli-knowledge-sync`（修正 `../../../../docs/` 路径） |

### 本轮已完成（docs · 并行批次）

| 文件 | 说明 |
|------|------|
| `docs/zh-CN/ARCHITECTURE.md` · `docs/en/ARCHITECTURE.md` · `docs/ja/ARCHITECTURE.md` | 容器 / 鉴权 / 网关 / 秒杀 / 用户中心 / 部署 PNG；Mermaid → `<details>` |
| `docs/zh-CN/RBAC.md` · `docs/en/RBAC.md` · `docs/ja/RBAC.md` | RBAC / 菜单查询 / 用户中心 PNG |
| `docs/design/user-center-detailed-design.md` | §3.1 登录 → `moli-auth-flow` PNG |

### 仍待迁移（主图缺口）

| 区域 | 文件 | 问题 | 建议 PNG |
|------|------|------|----------|
| `docs/design/` | `gateway-design.md` · `order-seckill-design.md` · `user-center-overview.md` | §2/§3 ASCII 仍作主图 | 已有对应 PNG |
| `docs/design/` | `knowledge-module-overview.md` · `kb-import-entry-design.md` · `kb-llm-platform-settings.md` | PNG 已嵌但 ASCII 未收进 `<details>` | 同上 |
| `docs/design/` | `ai-module-overview.md` · `kb-ops-roadmap.md` 等 | 部分仍 ASCII / 纯文本图引用 | 按需补 draw.io |
| `wiki-moli/develop/` | `AI模块-概要设计.md` · `技术方案与架构索引.md` · `知识库-meilisearch接入规划.md` · `Wiki治理工作台产品方案.md` | 仍为「请在仓库打开 PNG」文本占位 | 各页对应 PNG 已存在 |
| `wiki-moli/develop/` | `用户中心-详细设计.md` | §3.1 Mermaid 作主图 | `moli-auth-flow` |
| `wiki-moli/develop/` | `bi服务.md` | §架构 Mermaid 作主图 | `moli-container-architecture` 或新建 BI 定位图 |
| `wiki-moli/develop/outputs/` | `茉莉微服务全链路一张图.md` · `秒杀全链路与压测要点汇总.md` · `茉莉登录与鉴权故障根因汇总.md` | Mermaid 作主图 | 已有全链路 / 秒杀 / 鉴权 PNG |
| `wiki-moli/guides/` | `查询与体检指南.md` §5 | Mermaid 工作流作主图 | `moli-kb-functional-flows` 或新建 |
| `moli-*/README.md` | `moli-knowledge` · `moli-user-center` · `moli-order` 等 | 箭头字符在正文（多为列表非主图） | 低优先级，按需 |

**PNG 清单**：`docs/diagrams/png/` 共 22 张（含 `moli-kb-meilisearch` · `moli-kb-functional-flows`）；源文件见 [`docs/diagrams/README.md`](diagrams/README.md)。

---

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
