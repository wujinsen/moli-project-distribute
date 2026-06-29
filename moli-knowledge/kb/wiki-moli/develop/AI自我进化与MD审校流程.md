---
title: AI 自我进化与 MD 审校流程
slug: AI自我进化与MD审校流程
type: guide
status: active
tags: [知识库, ingest, query, lint, sync, AI, P0]
sources:
  - moli-knowledge/kb/AGENTS.md
  - moli-knowledge/kb/tools/lint.py
  - moli-knowledge/kb/tools/sync_to_db.py
related: [知识库三操作, 增量ingest与raw投喂指南, 项目文档总览, 项目文档总览, 项目文档总览, Wiki在线编辑与AI协助改稿, 查询与体检指南]
created: 2026-06-24
updated: 2026-06-27
---

# AI 自我进化与 MD 审校流程

> 契约总览 [[知识库三操作]]。本文是 **「自我进化」操作手册**：范式说明、Ingest/Lint/Sync 分工、Crystallize、Web 与 CLI 对照，以及 **AI 审校 MD → lint 门禁 → Sync** 逐步流程。

**原则**：wiki 是唯一权威源；AI **可以改** `wiki/**`，**不改** `raw/`；Lint **只报告**（CLI 不自动写库）；Sync 把 wiki 灌进 MySQL。

### 与 [[Wiki在线编辑与AI协助改稿]] 的分工（避免重复读两份）

| 文档 | 写什么 | 谁用 |
|------|--------|------|
| **本文** | 自我进化**范式**、Ingest/Query/Lint/Sync 闭环；**§4 场景 B** = AI 审校单篇 MD 的**规则与 Cursor 模板**；lint → git diff → Sync 步骤 | 开发者 / Cursor Agent / 运维 CLI |
| **[[Wiki在线编辑与AI协助改稿]]** | 把 **§4 场景 B** 产品化到 **Web**：编辑页、AI 协助、**改前/后 diff**、手改、保存 wiki、API（T14） | 前端 / 后端实现、editor 在浏览器修稿 |

**同一件事、两种入口**：审校约束（frontmatter、`[[slug]]`、sources、只改 wiki）在本文 §4 **场景 B** 定义；Web 的 `ai-revise` prompt 应**复用同一套规则**，界面补上 diff 与保存，不另起一套标准。

## 0. 范式：编译一次，持续保鲜（LLM-Wiki）

朴素 RAG = 每次提问临时拼原始文档。**本库** = Agent 维护的、互链的 markdown wiki：

| 对比 | 朴素 RAG | LLM-Wiki（本库） |
|------|----------|------------------|
| 知识形态 | 散落 raw/PDF | 结构化 `wiki/*.md` + `[[slug]]` |
| 新增原料 | 只进向量库 | **Ingest** 融进已有页、补交叉引用 |
| 提问 | 检索片段 | **Query** 带引用；缺口 → 建议 ingest |
| 质量 | 难治理 | **Lint** 断链/孤儿/缺 sources |
| 对外服务 | — | **Sync** → MySQL → 浏览/问答 API |

**自我进化** = 上述闭环反复转动，而不是无人值守乱改库：

- **人**：投喂 raw、提问、确认 AI diff、触发 Sync
- **Agent**：Ingest、Query、crystallize、审校 MD、修断链
- **脚本**：`lint.py` 门禁、`sync_to_db.py` 灌库

详见 [[知识库三操作]]、`kb/AGENTS.md` §0。

## 1. 三层架构（先建立地图）

```plaintext
┌─────────────┐   Ingest      ┌─────────────┐   Sync        ┌─────────────┐
│  kb/raw/    │ ───────────►  │  kb/wiki-moli/   │ ───────────►  │   MySQL     │
│  原始投喂    │   提炼写页     │  权威 wiki   │  sync_to_db   │ kb_document │
└─────────────┘               └─────────────┘               └─────────────┘
      │                             │                              │
   人/Agent 只读              Agent/人可写                    浏览/问答/图谱
                                                                    │
                                                              DB Lint（Web）
```

| 层 | 路径/表 | 进化动作 | Web 是否直接操作 |
|----|---------|----------|------------------|
| 源 | `raw/` | 投喂新语料 | ❌ |
| 知识页 | `wiki/` | Ingest、crystallize、AI 审校 MD | 🔜 **T14** 单篇编辑+AI（见 [[Wiki在线编辑与AI协助改稿]]）；现网仍靠 Agent/git |
| 运行时 | `kb_document`、`kb_lint_issue` | Sync、DB 体检 | ✅ 同步 / 扫描并落库 |

**加入知识库（可问答）** 的充分条件：内容在 `wiki/` 且已 **Sync** 进 `kb_document`。仅改 raw 或仅 Ingest 未 Sync，线上仍看不到。

### 1.1 易混淆：附件 / Ingest / Sync

| 操作 | 是什么 | 进问答吗 |
|------|--------|----------|
| Web 文档底「附件上传」 | 挂 PDF 等到 MinIO | ❌ |
| **Ingest** | raw → wiki 页 | Sync 后才 ✅ |
| **Sync** | wiki → `kb_document` | ✅ |
| **Crystallize** | 多页综合 → `wiki-moli/develop/outputs/` | Sync 后才 ✅ |

--- 

## 2. 三操作 + Sync + Crystallize（进化动作表）

| 操作 | 输入 | 输出 | 谁执行 | 是否改 MySQL |
|------|------|------|--------|--------------|
| **Ingest** | raw/、URL、项目文档 | 新建/更新 `wiki/**`、index、log、edges | Cursor Agent（`AGENTS.md` §4） | ❌（需再 Sync） |
| **Query** | 自然语言问题 | 带 `[[slug]]` 的答案；暴露缺口 | Web `/kb/ask`、serve.py | 只读 |
| **Crystallize** | Query 多页综合 | `wiki-moli/develop/outputs/{slug}.md` + `derived_from` 边 | Agent（需人确认） | ❌（需再 Sync） |
| **Lint（wiki）** | `wiki/` 文件 | 断链/孤儿/缺 sources 报告；可选 `kb_lint_issue` | `lint.py`、serve.py | ❌ |
| **Sync** | `wiki/` | `kb_document`、`kb_relation`、标签 | `sync_to_db.py`、Web Wiki 同步 | ✅ |
| **Lint（DB）** | 已入库文档 | `kb_lint_issue` 工单 | Web 扫描并落库 | 只写问题表 |

### 2.1 Ingest 标准流程（Agent）

见 `AGENTS.md` §4、[[增量ingest与raw投喂指南]]。摘要：

1. 读源 → 查 `index.md` 去重
2. 写/补 `guides/`、`services/`、`concepts/