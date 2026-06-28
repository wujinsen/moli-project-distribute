---
title: Ingest 工作台（产品方案）
slug: Ingest工作台产品方案
type: guide
status: active
tags: [知识库, ingest, AI, Web, P0, product]
sources:
  - moli-knowledge/kb/AGENTS.md
  - moli-knowledge/kb/ROADMAP.md
  - moli-knowledge/kb/wiki/guides/AI自我进化与MD审校流程.md
  - moli-knowledge/kb/wiki/guides/增量ingest与raw投喂指南.md
  - docs/api/KNOWLEDGE_API.md
related: [AI自我进化与MD审校流程, Wiki在线编辑与AI协助改稿, 增量ingest与raw投喂指南, 知识库三操作]
created: 2026-06-25
updated: 2026-06-27
---

# Ingest 工作台（产品方案）

> **状态：active / 已交付（T15a–e · 里程碑 M6 · 2026-06-25）**  
> 目标：在 **Web 界面** 完成 **Agent 厚 Ingest** 等价流程——raw 选源 → **规划去重** → 多页 LLM 草稿 → **逐页 diff 审阅** → lint 门禁 → **原子写 wiki**（含 index/log/edges）→ Sync。  
> **HTTP 契约权威**：[`docs/api/KNOWLEDGE_API.md`](../../../docs/api/KNOWLEDGE_API.md) **§9**（20 个接口）。

### 与 [[Wiki在线编辑与AI协助改稿]] / [[AI自我进化与MD审校流程]] 的关系

| | **T14 / M5 单篇编辑** | **T15 / M6 本方案** | **Cursor Agent** |
|---|----------------------|----------------------|------------------|
| 输入 | 已有 slug 单页（+ 可选 raw 生成 patch） | raw 多文件 / 主题簇 | raw / URL / 仓库路径 |
| 输出 | 1 页 diff 或 **Enrich patch** + 治理文件 | **5–15 页** + index/log/edges | 同左（AGENTS §4） |
| 规划 | 无（Enrich 面板填 patch/raw） | **Plan JSON**（去重/enrich/新建） | 读 index 人工+Agent |
| 审阅 | 单页 baseline ↔ 编辑区 / Enrich 合并预览 | **多页 PR Review 式** diff | git diff |
| 适用 | 修稿、**单页 enrich**、修断链 | 运维/editor **批次 ingest** | 大批量、复杂 enrich |
| 门禁 | lint 摘要（T14d） | **lint ERROR 阻塞 commit** | `lint.py --strict` |

**不重复写 Ingest 契约**：frontmatter、`[[slug]]`、sources、只改 `wiki/**`、append log/edges 等以 `AGENTS.md` §4 与 [[增量ingest与raw投喂指南]] 为准；本文只写 **工作台界面、状态机、API、表结构、分阶段交付**。

**已废弃、不得复现**：L1 raw 直写 DB、L2 单篇 `l2-*.md` 批量蒸馏（绕过 index/edges/enrich）。

---

## 1. 用户故事

| 角色 | 场景 |
|------|------|
| editor | 在 raw 树勾选「Redis 哨兵」相关笔记 → 填主题 → **生成规划** → 改 plan → **生成 6 页草稿** → 逐页 diff 批准 → commit → Sync |
| 平台管理员 | 同上 + 查看批次 log、触发 Sync、导出 Cursor 提示词给 Agent 续跑 |
| 运维（只读） | 只看 raw 树与历史批次状态，不能 commit |

**不做**：

- Web 里改 `raw/`（只读浏览）
- 无 plan 一键「全库蒸馏」
- 无 diff 审阅自动落盘
- 默认只写 `kb_document` 不回 wiki（与 ROADMAP 铁律冲突）

---

## 2. 六步状态机

主图（draw.io）：[`docs/diagrams/moli-kb-ingest-workbench.drawio`](../../../docs/diagrams/moli-kb-ingest-workbench.drawio)

| 步骤 | 用户 | 系统 | 质量门禁 | 对应 job.status |
|------|------|------|----------|-----------------|
| **① 选源** | 勾选 raw 文件；填批次#、主题、期望类型 | `GET /kb/ingest/raw-tree` + `POST /kb/ingest/jobs` | — | `created` |
| **② 规划** | 审/改 plan JSON | `POST/PUT .../plan`；LLM 读 raw 摘要 + index 片段 | **无 plan 禁止 generate** | `planned` |
| **③ 生成** | 全量生成或 **续跑**；可单页重试 | `POST .../generate?resume=`；enrich 存 `patch` + 合并 `draft` | 每页独立 PageWriter / EnrichWriter | `reviewing` |
| **④ 审阅** | 逐页 diff、手改全文或 **patch 段**、单页重生成 | baseline ↔ draft；`PUT .../draft` | 逐页 approve/reject | `reviewing` |
| **⑤ lint** | 看报告 | `POST .../lint` | **ERROR 阻塞 commit** | `reviewing` |
| **⑥ 提交** | commit；可选 **一键 Sync** | `POST .../commit?sync=`；写 wiki + log + edges + index 段 | append `log.md` 一行 | `committed` |

---

## 3. 界面交互（建议）

**入口**：知识库 → **Ingest 工作台**（与「编辑 wiki」并列，不从 T14 编辑页做批次 ingest）

**布局**：

```
┌──────────────────────────────────────────────────────────────────┐
│ 批次#1292 · Redis 哨兵    空间: enterprise-kb    [导出Agent提示词] │
├─────────────┬────────────────────────────┬───────────────────────┤
│ ① raw 树    │  ② Plan 表 / ③ 草稿列表     │  ④ Diff + Markdown 编辑 │
│ (勾选)      │  新建|enrich|跳过|冲突提示   │  baseline | 当前稿     │
├─────────────┴────────────────────────────┴───────────────────────┤
│ ⑤ Lint 报告（ERROR 红 / WARN 黄）  [重新生成] [批准本页] [提交批次] │
└──────────────────────────────────────────────────────────────────┘
```

### 3.1 Plan 表（②）

LLM **只输出 JSON**。Web **T17c** 提供可视化表：分类下拉（`GET /kb/category/tree`）+ 裸 `slug` + 落盘路径预览；与 **文档管理** `kb_category.dir_slug` 单一真相一致。

**T17 · create 落盘（2026-06）**

| 模式 | Plan 字段 | 落盘路径 |
|------|-----------|----------|
| **推荐** | `categoryId` + 裸 `slug` | `kb/{wikiDir}/{dir_slug}/{slug}.md` |
| legacy | `type` + 裸 `slug` | `kb/{wikiDir}/{typeDir(type)}/{slug}.md` |
| legacy | `slug` 含 `/` | `kb/{wikiDir}/{slug}.md`（不叠 typeDir） |

**jp-fe-ap-exam 示例**：

```json
{
  "batchNo": "726295221004025856",
  "topic": "FE 科目B 样题",
  "create": [
    {
      "categoryId": "900000000000000010",
      "slug": "fe_kamoku_b_set_sample_qs",
      "title": "科目B 样题集",
      "sources": ["raw/fe/fe_kamoku_b_set_sample_qs.md"]
    }
  ],
  "enrich": [],
  "skip": [],
  "edges": [],
  "conflicts": []
}
```

→ `wiki-jp-exam/fe/fe_kamoku_b_set_sample_qs.md`；Sync 后文档管理 `category_id` 对应 FE 分类。

**enterprise-kb legacy 示例**：

```json
{
  "batchNo": 1292,
  "topic": "Redis 哨兵",
  "create": [
    {"type": "article", "slug": "redis-哨兵部署", "title": "...", "sources": ["raw/.../sentinel.note.md"]}
  ],
  "enrich": [
    {"slug": "redis-缓存", "action": "append_section", "reason": "已有枢纽，补哨兵节"}
  ],
  "skip": [{"raw": "raw/.../duplicate.md", "reason": "与 articles/xxx 重复"}],
  "edges": [
    {"from": "redis-缓存", "to": "redis-哨兵部署", "type": "relates_to", "evidence": "..."}
  ],
  "conflicts": ["与 articles/xxx 端口描述不一致"]
}
```

UI：表格可编辑；**疑似重复**（index + 全文检索 top 相似）高亮；用户确认后锁定 plan 版本。骨架 Plan（LLM 未配置）从 raw 文件名预填 `slug`；`raw/fe/...` 可自动推断 `fe` 分类。

### 3.2 多页 Diff（④）

- 左侧批次树：每页状态 `draft | approved | rejected`
- 新建页：baseline 为空；enrich 页：baseline = 当前 wiki 全文，**patch** = 追加段，**draft** = 合并预览
- enrich 页提供 **Patch 段落** Tab，可只改 patch 不重写整页
- **禁止**「一键全部批准」（Expert 逐步模式）；**T18 Express** 在二次确认后可 `approveAll=true` 入库

### 3.3 Express 一键流（T18 · 2026-06）

| 步骤 | 用户 | 系统 |
|------|------|------|
| **预览** | 列表勾选 raw →「一键预览」 | `POST /kb/ingest/jobs/express`：创建批次 + Express Plan（骨架 + `raw/fe/`→FE 分类推断）+ 生成草稿 |
| **确认** | 详情 diff 扫一眼 →「确认入库」 | `POST .../publish?sync=true&approveAll=true`：全批准 + lint + commit + Sync |

- URL 带 `?express=1` 显示 Express 横幅；Expert 仍可用逐步 Plan / 逐页 approve / `commit`。
- `useLlmPlan=true` 时 prepare 走 LLM Planner（与 T15 相同）。
- API 契约：[`docs/api/KNOWLEDGE_API.md`](../../../docs/api/KNOWLEDGE_API.md) §9.6.6。

### 3.4 批次模板（T15e）

- **列表页**：展示已保存模板；「从模板创建」复制 raw 范围 / 期望类型 / 可选 Plan
- **批次详情**：「另存为模板」；可选附带当前 Plan 快照
- API：`GET/POST /kb/ingest/templates`、`POST .../jobs/from-template/{id}`、`POST .../save-as-template`

### 3.5 断点续跑（T15e）

- **续跑生成**：`POST .../generate?resume=true`，跳过已有 content 的草稿，返回 `{ total, generated, skipped, drafts }`
- **全量重生成**：`resume=false`，清空旧草稿后重新生成（需二次确认）

### 3.6 Commit（⑥）

原子写入（同一事务语义 / 同一 git commit 前操作）：

1. 写/更新 `wiki/**` 各页  
2. append `wiki/log.md`（`ingest | 批次#N ...`）  
3. append `wiki/graph/edges.jsonl`  
4. 更新 `wiki/index.md` 相关条目  
5. 可选：`POST /kb/ingest/jobs/{id}/commit?sync=true`（落盘后立即 Sync）  
6. 展示 insert/update/skip 统计

**T17d · 落盘预览**：③ Lint 区在 commit 前列出已批准页的 `kb/{wikiDir}/{slug}.md`；确认对话框同步展示路径列表。

**前端实现**：`meiling-ui` → `KnowledgeIngestWorkbenchView.vue`（路由 `knowledge/ingest/index`，菜单 906）；Plan 可视化表 → `IngestPlanCreateTable.vue`（T17c）。

---

## 4. API 与数据（已实现 · T15）

> **契约权威**：[`docs/api/KNOWLEDGE_API.md`](../../../docs/api/KNOWLEDGE_API.md) **§9**（接口总览、请求/响应示例、DTO 字段）。  
> 下文为产品视角摘要；联调以 API 文档为准。

### 4.1 接口清单（20 个）

| 分组 | 路径前缀 | 要点 |
|------|----------|------|
| 选源 | `/kb/ingest/raw-tree`、`/kb/ingest/raw-coverage`、`/jobs` | raw 只读树 + **覆盖索引（筛未 ingest）**；创建/分页/详情 |
| 规划 | `/jobs/{id}/plan`、`/export-agent-prompt` | LLM 或 skeleton Plan；导出 Cursor 提示词 |
| 草稿 | `/jobs/{id}/generate`、`/drafts`、`/draft?slug=` | 含 **resume**；slug 走 **query**（含 `/`） |
| 审阅 | `/draft/regenerate`、`/draft/approval` | 单页重生成；approve/reject |
| 落盘 | `/lint`、`/commit?sync=` | lint 预检；原子写 wiki；**sync 合并进 commit** |
| 模板 | `/templates`、`/jobs/from-template/{id}`、`/save-as-template` | T15e 批次模板 |

### 4.2 与初版 PRD 的差异（已知、可接受）

| 初版 PRD | 当前实现 | 说明 |
|----------|----------|------|
| generate 异步 + SSE 进度 | **同步** HTTP，返回 `IngestGenerateResultVo` | 前端 loading + 续跑弥补大批量 |
| `POST .../sync` 独立 | **`commit?sync=true`** | 减少一步操作 |
| draft slug 在 path | **`?slug=` query** | slug 含 `articles/xxx` 路径段 |
| index 按类型插入 | **追加批次段** | 满足 log 追溯，后续可增强 |

### 4.3 任务表

| 表 | 用途 |
|----|------|
| `kb_ingest_job` | 批次状态、操作人、主题、raw 范围 JSON、`space_id` |
| `kb_ingest_plan` | plan JSON 版本（v1/v2…） |
| `kb_ingest_draft` | 每页 baseline、**patch**、draft 合并预览、approval |
| `kb_ingest_commit` | 落盘时间、写入文件列表 |
| `kb_ingest_template` | 批次模板（raw 范围 / 可选 Plan 快照） |

DDL：[`docs/sql/08_kb_ingest_workbench.sql`](../../../docs/sql/08_kb_ingest_workbench.sql)、[`09_kb_ingest_t15e.sql`](../../../docs/sql/09_kb_ingest_t15e.sql)。

草稿在 **commit 前只存 DB**，不写 wiki 文件。

### 4.4 配置项

| 配置 | 说明 |
|------|------|
| `kb.wiki.root` | enterprise-kb 的 wiki 根（与 T14 共用） |
| `kb.raw.root` | 只读 raw 根，默认 `kb/raw` |
| `kb.ingest.max-pages-per-batch` | 默认 15 |
| `kb.llm.*` | 与 `/kb/ask`、T14 共用 |

### 4.5 LLM Prompt 分工（保证「厚」）

| 阶段 | System 角色 | 约束 |
|------|-------------|------|
| **Planner** | 只做规划 | 输出 JSON；必须引用 index 片段 + 相似页 slug；禁止写正文 |
| **PageWriter** | 单页完整 md | frontmatter 齐全；`[[slug]]` 仅来自 plan 已知 slug |
| **EnrichWriter** | patch | 只输出追加段落或 unified diff；禁止整页覆盖（需 UI 二次确认） |

温度 0.2–0.3；context = raw 分块 + 相关已有页摘要。

### 4.6 权限

| 操作 | ACL |
|------|-----|
| raw 树只读 | 空间 viewer |
| 创建/规划/生成/commit | 空间 **editor** + `kb:ingest:*` |
| Sync | 现有 `kb:sync:trigger` 或 editor |

菜单建议：知识库 → Ingest 工作台；动作权限 `kb:ingest:job`、`kb:ingest:commit`（可合并）。

---

## 5. 质量红线（产品 + 后端强制）

1. **禁止** raw 直写 `kb_document`  
2. **禁止** 无 plan 调用 generate  
3. **禁止** 存在未批准页时 commit  
4. **禁止** commit 时 lint 有 ERROR  
5. **禁止** 只写 articles 不更新 index/log/edges（commit 前校验清单）  
6. enrich 默认 patch；整页覆盖需 UI 二次确认  

与 Phase 0 一致：CI 已 `lint-strict` 门禁；工作台 commit 前应跑同等口径。

---

## 6. 与 serve.py「提炼」Tab 的分工

| | serve.py 提炼 | 本工作台 |
|---|---------------|----------|
| 输入 | 已有 wiki（按 tag） | raw + index |
| 输出 | 单页 hub 草稿 | 多页 + 治理文件 |
| 落盘 | `save_draft`（不覆盖、不更 index） | 完整 AGENTS §4 交付物 |
| 定位 | **本地辅助** | **Web 产品主线（批次 ingest）** |

---

## 7. 分阶段交付（T15）— 已全部完成

| 子任务 | 范围 | 状态 |
|--------|------|------|
| **T15a** | raw 树 + job CRUD + Plan 生成/编辑 + export-agent-prompt | ✅ |
| **T15b** | 多页 generate + diff UI | ✅ |
| **T15c** | lint 预检 + 原子 commit（wiki/log/index/edges） | ✅ |
| **T15d** | commit 后一键 Sync + 批次报告 | ✅ |
| **T15e** | enrich patch、断点续跑、批次模板 | ✅ |

**依赖**（均已满足）：

- ✅ Phase 0 治理（375 页、`lint-strict` 通过）  
- ✅ **T14a**（`KbWikiFileService` 读写 wiki）  
- ✅ T9 ACL（editor）、T2 LLM（`kb.llm.*`）

**开发任务明细**：`moli-knowledge/TASKS.md` **T15**

---

## 8. 相关文档

- 开发任务：`moli-knowledge/TASKS.md` **T15**
- 规划：`moli-knowledge/kb/ROADMAP.md` **M6**
- 契约：`moli-knowledge/kb/AGENTS.md` §4、§8
- **架构图（draw.io）**：
  - 六步流程 + 与 T14/Agent 分工：`docs/diagrams/moli-kb-ingest-workbench.drawio`
  - 写入轨总览（含 M6 路径 B）：`docs/diagrams/moli-kb-raw-pipeline.drawio`
  - 功能流程 ⑥：`docs/diagrams/moli-kb-functional-flows.drawio`
- 单篇 Web 编辑 + **Enrich 治理**：[[Wiki在线编辑与AI协助改稿]]（T14 / T14f）
- 闭环手册：[[AI自我进化与MD审校流程]]
- **HTTP API 契约**：[`docs/api/KNOWLEDGE_API.md`](../../../docs/api/KNOWLEDGE_API.md) §9（Ingest）、§8.4（单页 Enrich）
- **DDL**：[`docs/sql/08_kb_ingest_workbench.sql`](../../../docs/sql/08_kb_ingest_workbench.sql)、[`09_kb_ingest_t15e.sql`](../../../docs/sql/09_kb_ingest_t15e.sql)

---

## 相关

[[AI自我进化与MD审校流程]] · [[Wiki在线编辑与AI协助改稿]] · [[增量ingest与raw投喂指南]] · [[知识库三操作]]
