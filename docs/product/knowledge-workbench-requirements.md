# 知识库工作台 · 需求总览（现行版）

> **本文是产品需求的导航页**（2026-06-27 对齐实现）。长篇正文在 wiki `guides/`，HTTP 契约在 `docs/api/`。  
> **若与旧文档冲突，以本页 + 下表「现行决策」为准。**  
> **按文档操作入库 + 治理** → **[`docs/ops/knowledge-workbench-operations.md`](../ops/knowledge-workbench-operations.md)**

---

## 1. 三条产品线

| 产品线 | 菜单 | 产品方案 | 前端对接 | 后端状态 |
|--------|------|----------|----------|----------|
| **Ingest 入库** | Ingest 工作台 | [[Ingest工作台产品方案]] | [ingest-workbench-frontend.md](../api/ingest-workbench-frontend.md) | T15+T18+T19 ✅ |
| **Wiki 治理** | Wiki 治理 | [[Wiki治理工作台产品方案]] | [knowledge-workbench-frontend.md](../api/knowledge-workbench-frontend.md) · [wiki-govern-frontend.md](../api/wiki-govern-frontend.md) | T16a/e/g ✅；**T16f 前端 🔵** |
| **单页编辑** | Wiki 编辑 | [[Wiki在线编辑与AI协助改稿]] | KNOWLEDGE_API §8.2–8.4 | T14 ✅ |
| **健康体检** | 健康体检 | wiki-moli / 查询与体检指南 | KNOWLEDGE_API §4 | DB 快照，与治理分工 |

**脚本 vs LLM 怎么选**：[`docs/test/knowledge-script-vs-llm-matrix.md`](../test/knowledge-script-vs-llm-matrix.md)

---

## 2. 现行决策（ supersede 旧 PRD）

| 主题 | ❌ 旧说法 / 已废弃 | ✅ 现行 |
|------|-------------------|--------|
| Wiki 治理批量修复 | 治理页 **批量 enrich** 补章 | **script-fix**（metadata）+ **ai-batch-fix** + **auto-fix** |
| Wiki 治理 LLM | Cloud Agent / Cursor 嵌入 Web | **kb.llm**（OpenAI 兼容）；dup 用 **merge-hint** 复制 Cursor 指令 |
| 治理 Lint 数据源 | DB `GET /kb/lint` | 磁盘 **`lint-space`**（文件真值） |
| Ingest 正文生成 | 永远 LLM | 默认 LLM；**`useLlmGenerate=false`** 模板模式（raw 直贴） |
| Ingest commit | 可重复 ingest 已 covered raw | **raw 覆盖门禁**（已被其它 wiki 引用则拒绝） |
| 入库 / Sync 后 | 无引导 | **`nextSteps`** → Wiki 治理 Lint、健康体检 |
| enrich 一词 | 治理页 enrich | **仅**：T14 单页 `POST /kb/wiki-moli/enrich`、Ingest Plan `enrich[]` |

---

## 3. Wiki 治理需求摘要（T16）

**目标**：空间级 **文件真值 Lint** → **脚本 / AI / 一键** 修复 → **复检** → **可选 Sync**。

| 能力 | API | LLM |
|------|-----|-----|
| Lint | `POST /kb/wiki-moli/lint-space` | 否 |
| 脚本修 metadata | `POST /kb/wiki-moli/govern/script-fix` | 否 |
| AI 批量修 | `POST /kb/wiki-moli/govern/ai-batch-fix` | 是 |
| 一键 | `POST /kb/wiki-moli/govern/auto-fix` | 部分 |
| dup 合并提示 | `POST /kb/wiki-moli/govern/merge-hint` | 否 |
| Sync | `syncAfter` 或 `POST /kb/sync/trigger` | 否 |

**不做**：治理页批量 enrich；ingest 新 raw（走 Ingest 旁路）。

---

## 4. Ingest 需求摘要（T15 + T18 + 模板模式）

**目标**：raw → Plan → 草稿 → 审阅 → lint → commit → Sync。

| 模式 | 用户 | 关键参数 |
|------|------|----------|
| **Expert** | 六步逐步 | 默认 `useLlmGenerate=true` |
| **Express** | 一键预览 + 确认入库 | `useLlmPlan=false` + `publish` |
| **模板入库** | raw 已是 md，不需 LLM 改写 | **`useLlmGenerate=false`** |

详见 [knowledge-ingest-acceptance.md](../test/knowledge-ingest-acceptance.md) §1。

---

## 5. 文档地图

| 类型 | 路径 |
|------|------|
| **操作手册（入库+治理）** | **[`docs/ops/knowledge-workbench-operations.md`](../ops/knowledge-workbench-operations.md)** |
| 产品方案（wiki） | `moli-knowledge/kb/wiki-moli/guides/*产品方案.md` |
| HTTP 契约 | `docs/api/KNOWLEDGE_API.md` |
| **前端总览 + B1–B10** | `docs/api/knowledge-workbench-frontend.md` |
| 治理前端（W1–W8） | `docs/api/wiki-govern-frontend.md` |
| Ingest 前端（I1–I5） | `docs/api/ingest-workbench-frontend.md` |
| 测试 / 验收 | `docs/test/knowledge-*.md` |
| 架构图 | `docs/diagrams/moli-kb-wiki-govern.drawio`、`moli-kb-ingest-workbench.drawio` |

---

## 6. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-06-28 | 操作手册统一至 `docs/ops/knowledge-workbench-operations.md` |
| 2026-06-27 | T16e/g：治理 script/AI/auto/merge-hint；Ingest 模板模式 + nextSteps + raw 门禁 |
| 2026-06-25 | T15 Ingest 工作台交付 |
| 2026-06（初稿） | T16 草案含 enrich 批量（**已废弃**） |
