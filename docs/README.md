# 茉莉项目 · 文档总览

> **统一规则**：按文档类型分区存放；**可浏览、可演进**的正文以 **`kb/wiki*`** 为权威（sync 后进 Web）；**工程契约**（API/SQL）以 **`docs/`** 为权威；**外部新稿**先放 **`kb/raw/{类型}/`** 再 Ingest。  
> **Agent 规则**：全仓库见根目录 [`AGENTS.md`](../AGENTS.md)；知识库 Ingest 另见 [`kb/AGENTS.md`](../moli-knowledge/kb/AGENTS.md)。**架构/流程图一律 draw.io**（[skill](../.cursor/skills/drawio-diagrams/SKILL.md)）。

---

## Agent 规则分层（全项目，非仅知识库）

| 层级 | 路径 | 说明 |
|------|------|------|
| L0 | `.cursor/rules/*.mdc` | Cursor 自动注入（含 draw.io 触发） |
| L1 | **`/AGENTS.md`** | monorepo 总入口：文档落点 + draw.io 强制 |
| L2 | `moli-knowledge/kb/AGENTS.md` | 仅 wiki Ingest / sync |
| L3 | [`.cursor/skills/`](../.cursor/skills/) | 专项流程：draw.io · SQL 基线 · operation 模块 · kb ingest |

各 `moli-user-center`、`moli-order` 等**不需要**单独 `AGENTS.md`；写 `moli-xxx/README.md` + ingest 到 `wiki-moli/develop/` 即可。

---

## 五类文档 · 放哪里 · 谁维护

| 类型 | 工程层（契约/索引） | 原料投喂 `kb/raw/` | 成品 wiki（浏览） | 同步空间 |
|------|---------------------|--------------------|-------------------|----------|
| **PRD / 产品** | [product/](product/) | `raw/prd/` | **`wiki-moli/product/`** | `moli-ops-manual` |
| **技术 / 架构 / 文章** | [design/](design/) · [zh-CN/ARCHITECTURE.md](zh-CN/ARCHITECTURE.md) | `raw/design/` | **`wiki-moli/develop/`**（含 `develop/concepts/`、`develop/articles/`） | `moli-ops-manual` |
| **API 接口** | **[api/](api/)**（权威） | `raw/api/`（可选） | **`wiki-moli/develop/`** 摘要 + `guides/` 联调 | `moli-ops-manual` |
| **测试 / 面试** | [test/](test/) | `raw/test/` | **`wiki-moli/test/`**（含 `test/interview/`） | `moli-ops-manual` |
| **运维 / 操作** | [ops/](ops/)（索引） | `raw/ops/` | **`wiki-moli/ops/`** · **`wiki-moli/guides/`** | `moli-ops-manual` |
| **enterprise-kb** | — | — | **`wiki/index.md` 仅占位** | 勿再落茉莉正文 |

**日常修改**：茉莉项目文档 → **`wiki-moli/`**；API → **`docs/api/`**。改完 → `lint.py` → `sync_to_db.py` / `sync-all`。

**版本再 ingest**（v1→v5）：见 [`kb/AGENTS.md` §4.1](../moli-knowledge/kb/AGENTS.md)。

---

## 目录结构

```
docs/
  README.md              ← 本文（总导航）
  product/               ← 产品文档索引
  design/                ← 技术方案索引
  api/                   ← API 契约（权威）
  test/                  ← 测试文档索引
  ops/                   ← 运维文档索引（正文在 wiki-moli）
  zh-CN/ en/ ja/         ← 官方薄层（架构/RBAC/技术栈，多语言）
  sql/                   ← DDL、表设计、ER
  diagrams/              ← draw.io 架构图（@drawio-diagrams）
  nacos/                 ← Nacos 配置样例

moli-knowledge/kb/
  raw/{prd,design,api,test,ops,...}/  ← 新稿投喂（只读）
  wiki/                  ← 企业知识库成品
  wiki-moli/              ← 茉莉系统手册成品

load-test/               ← 压测脚本 + 操作说明（见 docs/test/）
scripts/                 ← DB 初始化（见 docs/ops/）
```

---

## 快速入口

| 我要… | 打开 |
|--------|------|
| **v1 发布范围 / 冒烟** | **[product/moli-v1-release-scope.md](product/moli-v1-release-scope.md)** · [test/release-smoke-checklist.md](test/release-smoke-checklist.md) · [ops/v1-release-runbook.md](ops/v1-release-runbook.md) |
| **操作知识入库 + Wiki 治理** | **[ops/knowledge-workbench-operations.md](ops/knowledge-workbench-operations.md)** |
| 看产品 PRD | [product/knowledge-workbench-requirements.md](product/knowledge-workbench-requirements.md) · [operation-server-ops-prd.md](product/operation-server-ops-prd.md) · [sso-menu-isolation-prd.md](product/sso-menu-isolation-prd.md) → `kb/wiki-moli/guides/` |
| 看架构/方案 | [design/README.md](design/README.md) → `docs/zh-CN/ARCHITECTURE.md` |
| 前后端对齐索引 | [frontend-gaps.md](frontend-gaps.md) · [api/frontend-backend-dependencies.md](api/frontend-backend-dependencies.md) |
| 对接 HTTP 接口 | [api/README.md](api/README.md) · [api-iteration-map.md](api/api-iteration-map.md) |
| 文档健康度巡检 | [DOCUMENTATION_AUDIT.md](DOCUMENTATION_AUDIT.md) |
| 压测 / 测试 | [test/README.md](test/README.md) |
| 部署 / 排障 / **全项目文档** | [ops/README.md](ops/README.md) → **`kb/wiki-moli/guides/项目文档总览.md`** |
| **监控 / 日志 / Loki** | [ops/monitoring-and-logs.md](ops/monitoring-and-logs.md) · [deploy/observability/README.md](../deploy/observability/README.md) |
| 画架构图 | [diagrams/README.md](diagrams/README.md) · `@drawio-diagrams` · [AGENTS.md §3](../AGENTS.md) |
| 建表 / ER | [sql/KNOWLEDGE_SCHEMA.md](sql/KNOWLEDGE_SCHEMA.md) |
| 知识库 API | [api/KNOWLEDGE_API.md](api/KNOWLEDGE_API.md) |

---

## 与 Web「文档浏览」的关系

前端 **知识库 · 浏览** 读的是 **MySQL `kb_document`**，来源于 **`kb/wiki*`** 的 sync，**不展示 `raw/`**。  
API 文档在 **`docs/api/`**，供开发联调，一般不整篇 ingest 进浏览（服务页 `wiki-moli/develop/` 可链到契约）。

---

## 微服务：统一放还是各项目各自放？

**结论：混合——「入口统一、正文按归属」。** 不是把所有文档堆进一个文件夹，也不是每个服务各写一套重复的全家桶说明。

### 两种极端的问题

| 模式 | 优点 | 微服务下的问题 |
|------|------|----------------|
| **全部统一**（只放平台 `docs/`） | 新人一个入口；跨服务检索方便 | 离代码远；改服务的人懒得更新；易成「文档坟场」 |
| **全部各自放**（每服务一个 README 了事） | 改代码同 PR 改文档；Ownership 清晰 | 碎片化；网关/鉴权/部署要讲 5 遍；说法容易不一致 |

### 推荐分工（茉莉当前 monorepo）

```
                    ┌──────────────────────────────┐
                    │  平台统一入口                 │
                    │  docs/README + kb/wiki*      │
                    └──────────────┬───────────────┘
           ┌───────────────────────┼───────────────────────┐
           ▼                       ▼                       ▼
   产品 / 跨域方案            各服务模块 README          运维 / 全局 API
   kb/wiki-moli/guides            moli-order/README           wiki-moli/
   docs/design               → ingest → wiki-moli/develop/   docs/api/
```

| 文档 | 放哪 | 谁维护 | 原因 |
|------|------|--------|------|
| **PRD / 产品路线图** | 平台 `kb/wiki-moli/guides/`、`kb/ROADMAP.md` | 产品 + 平台 | 功能跨多个服务 |
| **跨服务架构**（网关、鉴权、部署拓扑） | `docs/zh-CN/` + `kb/wiki-moli/develop/concepts/` | 平台/架构 | 全局一份真相 |
| **单服务设计**（秒杀 Lua、Shiro 接入） | **`moli-xxx/README.md`** + **`wiki-moli/develop/xxx`** | **该服务负责人** | 离实现最近；ingest 后可在 Web 浏览 |
| **HTTP 联调契约** | **`docs/api/`** + 各服务 Swagger | 接口变更者 | 前端/测试只认一处清单 |
| **运维 SOP** | **`kb/wiki-moli/`** | 运维/SRE | 启动顺序、DB、Nacos 天然跨服务 |
| **测试 / 压测** | `load-test/` + `wiki/guides/` | 对应模块 | 脚本与代码同仓；指南进知识库 |

### 决策树（写新文档前先问）

```
这篇文档主要讲什么？
├─ 整个产品 / 多服务怎么协作？       → 平台统一（docs/ + kb/wiki）
├─ 只跟一个服务的实现强相关？         → 该模块 README，再 ingest → wiki-moli/develop/
├─ 给前端/测试联调的 HTTP 契约？       → docs/api/（统一）+ Swagger（运行时）
└─ 怎么部署、排障、开权限？           → wiki-moli/（统一）
```

### 单服务 README 与 wiki 的关系

1. **第一稿**：在 `moli-order/README.md` 等模块内写（随代码 PR 更新）。
2. **汇总浏览**：Agent Ingest 或人工维护 → `kb/wiki-moli/develop/订单服务.md`（链到模块 README 作 `sources`）。
3. **不要**：每个服务各写一份《本地启动全家桶》；统一看 `wiki-moli/guides/本地启动指南.md`。

模块 README 模板建议包含：职责边界、端口、关键配置、依赖服务、本仓库路径、指向 `docs/api/` 的接口索引。

### 未来拆成多 Git 仓库时

若 `moli-order`、`moli-user-center` 等独立成仓：

| 留在各服务仓 | 留在平台仓 / 知识库仓 |
|--------------|------------------------|
| `README`、CHANGELOG、模块设计 | PRD、架构、运维手册 |
| Swagger / 接口实现说明 | `docs/api/` 联调契约（或 OpenAPI 聚合） |
| 服务专属测试说明 | `wiki-moli/`、`docs/zh-CN/` |

各仓 README 通过 **Ingest** 汇总到 `wiki-moli/develop/`，避免用户在五个 repo 里找文档。

### 与 §「五类文档」的对应

- **偏统一**：产品、跨域技术方案、运维、全局 API 契约  
- **偏各服务**：单服务 README、模块级设计；经 ingest **汇总**到统一知识库供检索  
- **权威规则**不变：浏览看 `wiki*`；API 改 `docs/api/`；运维改 `wiki-moli/`

---

## 相关

- **全仓库 Agent 规则**：[`AGENTS.md`](../AGENTS.md)（§3 draw.io）
- 知识库契约：[`moli-knowledge/kb/AGENTS.md`](../moli-knowledge/kb/AGENTS.md)（§1.1 五类落点 · **§1.2 微服务归属**）
- 模块总览：[`moli-knowledge/README.md`](../moli-knowledge/README.md)
- AIOps 故障诊断平台：[`moli-aiops/README.md`](../moli-aiops/README.md)（架构图 `docs/diagrams/moli-aiops-architecture.drawio`）
- 项目入口：[`README.md`](../README.md)（默认中文）· [`README.zh-CN.md`](../README.zh-CN.md)（同内容别名）
