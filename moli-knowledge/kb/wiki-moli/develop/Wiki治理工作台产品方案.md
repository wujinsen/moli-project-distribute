---


title: Wiki 治理工作台（产品方案）
slug: Wiki治理工作台产品方案
type: guide
status: active
tags: [知识库, lint, govern, sync, Web, P1, product]
sources:
  - moli-knowledge/kb/AGENTS.md
  - moli-knowledge/kb/tools/lint.py
  - moli-knowledge/kb/wiki-moli/develop/Wiki在线编辑与AI协助改稿.md
  - moli-knowledge/kb/wiki-moli/develop/Ingest工作台产品方案.md
  - docs/api/KNOWLEDGE_API.md
  - docs/api/wiki-govern-frontend.md
  - docs/product/knowledge-workbench-requirements.md
related: [Wiki在线编辑与AI协助改稿, Ingest工作台产品方案, AI自我进化与MD审校流程, 增量ingest与raw投喂指南]
created: 2026-06-27
updated: 2026-06-27
---

# Wiki 治理工作台（产品方案）

> **状态：active（T16 · 2026-06 现行版）**  
> **操作手册**：`docs/ops/knowledge-workbench-operations.md` · **需求总览**：`docs/product/knowledge-workbench-requirements.md`  
> 目标：**① 文件真值 Lint** → **② 脚本修 metadata** → **③ kb.llm AI 批量修复** → **④ 一键 auto-fix** → **⑤ 可选 Sync**  
> **HTTP 契约**：`docs/api/KNOWLEDGE_API.md` §4.6 + §8.6 · **前端**：`docs/api/wiki-govern-frontend.md`

## 链路总览

> **Wiki 治理工作台链路**：`docs/diagrams/png/moli-kb-wiki-govern.png`（请在 IDE 中打开仓库文件查看）

> 源文件：`docs/diagrams/moli-kb-wiki-govern.drawio`

---

## 0. 决策（已锁定 · 2026-06-27）

| 决策 | 选定 | 理由 |
|------|------|------|
| Lint 数据源 | **文件真值** `lint-space` | 改 wiki 后、Sync 前必须扫磁盘；DB 体检是旧快照 |
| 批量修复 | **script-fix + ai-batch-fix + auto-fix** | metadata 不调 LLM；断链/孤儿才 LLM |
| dup 重复页 | **merge-hint + 手改** | 不调 LLM 批量改；复制 Cursor 指令合并 |
| ingest 新内容 | **旁路** → [[Ingest工作台产品方案]] | 修已有页 ≠ 投喂 raw |
| ~~治理页 enrich~~ | **已废弃** | enrich 会 append 章节，不能修 metadata/断链；仅 T14 单页 / Ingest Plan 保留 |

### 与 T14 / T15 分工

| | T14 单篇编辑 | **T16 本方案** | T15 Ingest |
|---|-------------|----------------|------------|
| 入口 | 单 slug | 空间级批量 | raw 批次 |
| Lint | lint-preview | **lint-space 全量** | 批次草稿 lint |
| 修复 | AI / 手改 / **单页 enrich** | **脚本 / AI 批量 / 一键** | LLM 草稿 + commit |
| 适用 | 改一页 | **空间治理** | 投喂新知识 |

契约细节：`AGENTS.md` §2/§6；单页 enrich 见 [[Wiki在线编辑与AI协助改稿]] §2.2（**不在治理页复用**）。

---

## 1. 数据贯通

`lint.py` 的 **`issue.page` = slug**，直接传入 `script-fix` / `ai-batch-fix` / `auto-fix` 的 `issues[]`，无需 ID 映射。

| space_code | wiki 子目录 |
|------------|-------------|
| `enterprise-kb` | `wiki` |
| `jp-fe-ap-exam` | `wiki-jp-exam` |
| `moli-ops-manual` | `wiki-moli` |

---

## 2. 状态机

```
idle → linted → fixing → relinted → synced
```

| 状态 | 触发 |
|------|------|
| `linted` | `POST /kb/wiki-moli/lint-space` |
| `fixing` | script-fix / ai-batch-fix / auto-fix / merge-hint+手改 |
| `relinted` | auto-fix 内置或手动再 lint |
| `synced` | `syncAfter` 或 `POST /kb/sync/trigger` |

---

## 3. 界面（T16f · 前端待接）

入口：`knowledge/wiki-govern/index`（菜单 910）。健康体检页（DB 快照）**并存、不替代**本页。

| 按钮 | API | LLM |
|------|-----|-----|
| 开始 Lint | `lint-space` | 否 |
| 脚本修复 | `script-fix` | 否 |
| AI 修复 | `ai-batch-fix` | 是 |
| 一键修复已选 | `auto-fix` | 部分 |
| 复制合并指令 | `merge-hint` | 否 |

Lint 后 **默认勾选** 全部 script+AI 可修项；`dup_slug` 仅 merge-hint / 跳转编辑。

完整 TypeScript / 验收：`docs/api/wiki-govern-frontend.md`

---

## 4. 后端 API（已实现）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/kb/wiki-moli/lint-space` | T16a ✅ |
| GET | `/kb/wiki-moli/govern/options` | kind 分类 + 模型列表 |
| POST | `/kb/wiki-moli/govern/script-fix` | T16e ✅ |
| POST | `/kb/wiki-moli/govern/ai-batch-fix` | T16e ✅ |
| POST | `/kb/wiki-moli/govern/auto-fix` | T16e ✅ |
| POST | `/kb/wiki-moli/govern/merge-hint` | T16g ✅ |
| POST | `/kb/sync/trigger` | 可选 Sync |

单页 diff 预览仍可用 `POST /kb/wiki-moli/ai-revise` + `PUT /kb/wiki-moli/page`（T14）。

---

## 5. 交付阶段

| 阶段 | 范围 | 状态 |
|------|------|------|
| T16a | lint-space | ✅ |
| T16e | script / ai-batch / auto-fix 后端 | ✅ |
| T16g | merge-hint + manualOnlyKinds | ✅ |
| T16f | meiling-ui 治理页 | 🔵 |

---

## 6. 风险

| 风险 | 缓解 |
|------|------|
| lint.py 依赖 Python | 同 Sync 配置 |
| 批量 LLM 慢 | auto-fix 单次 HTTP；失败隔离 |
| 改完未 Sync | UI 提示 + optional syncAfter |
| 与 DB 体检口径不同 | 文档明确分工 |
| 并发改同一文件 | baselineHash 乐观锁 |

---

## 相关

[[Wiki在线编辑与AI协助改稿]] · [[Ingest工作台产品方案]] · [[AI自我进化与MD审校流程]] · `docs/test/knowledge-script-vs-llm-matrix.md`
