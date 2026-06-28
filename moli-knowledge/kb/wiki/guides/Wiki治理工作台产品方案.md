---
title: Wiki 治理工作台（产品方案）
slug: Wiki治理工作台产品方案
type: guide
status: draft
tags: [知识库, lint, enrich, sync, Web, P1, product]
sources:
  - moli-knowledge/kb/AGENTS.md
  - moli-knowledge/kb/tools/lint.py
  - moli-knowledge/kb/wiki/guides/Wiki在线编辑与AI协助改稿.md
  - moli-knowledge/kb/wiki/guides/Ingest工作台产品方案.md
  - docs/api/KNOWLEDGE_API.md
related: [Wiki在线编辑与AI协助改稿, Ingest工作台产品方案, AI自我进化与MD审校流程, 增量ingest与raw投喂指南]
created: 2026-06-27
updated: 2026-06-27
---

# Wiki 治理工作台（产品方案）

> **状态：active（T16 · 2026-06 改版）**  
> 目标：**① 文件真值 Lint** + **② 脚本修 metadata** + **③ kb.llm AI 批量修复** + **④ 一键 auto-fix（脚本→AI→复检→可选 Sync）**。  
> **HTTP 契约**：[`docs/api/KNOWLEDGE_API.md`](../../../docs/api/KNOWLEDGE_API.md) §4.6 + §8.6。

## 链路总览

![Wiki 治理工作台链路](../../../../docs/diagrams/png/moli-kb-wiki-govern.png)

> 源文件：[`docs/diagrams/moli-kb-wiki-govern.drawio`](../../../../docs/diagrams/moli-kb-wiki-govern.drawio)

## 0. 两条决策（已锁定）

| 决策 | 选定 | 理由 |
|------|------|------|
| Lint 数据源 | **文件真值**（`lint.py --wiki-dir`） | 治理是"改完还没 Sync 就要检"，DB 快照（`GET /kb/lint`）是旧的，必须扫磁盘 wiki |
| 批量处理 | **脚本修 metadata + ai-revise**（修已有页） | `missing_dates` / `slug_mismatch` / `missing_source` 不调 LLM；断链/孤儿等才 LLM |
| ingest | **旁路**：链接到现有 [[Ingest工作台产品方案]] | ingest 是"投喂新源产生新页"，与"修问题页"是两条不同的流程 |

### 与 [[Wiki在线编辑与AI协助改稿]] / [[Ingest工作台产品方案]] 的关系

| | T14 单篇编辑 | **T16 本方案** | T15 Ingest 工作台 |
|---|-------------|----------------|-------------------|
| 入口 | 单页 slug | **空间级批量** | raw 批次 |
| Lint | 保存前单页 lint-preview | **空间全量文件 lint** | 批次草稿 lint |
| 修复 | 单页 AI/手改 | **脚本 + 批量 ai-revise / 一键 auto-fix** | 多页草稿生成 |
| 适用 | 改一页 | **空间治理体检 + 批量修** | 投喂新知识 |

**不重复写契约**：frontmatter、`[[slug]]`、sources、只改 `wiki/**` 等以 `AGENTS.md` §2/§6 为准；enrich 语义见 [[Wiki在线编辑与AI协助改稿]] §2.2；本文只写 **工作台界面、状态机、新增接口、分阶段交付**。

## 1. 数据贯通的关键

`lint.py` 的每条 issue 里 **`page` 字段就是 slug**（如 `guides/本地启动指南`），而 `enrich` / `ai-revise` 的入参也是 `slug` —— 所以 **lint → 修复 → sync 全链路数据天然贯通**，不需要额外 ID 映射。

空间 → wiki 目录映射**已存在**于 `KbWikiProperties.spaceDirs`：

| space_code | wiki 子目录 |
|------------|-------------|
| `enterprise-kb` | `wiki` |
| `jp-fe-ap-exam` | `wiki-jp-exam` |
| `moli-ops-manual` | `wiki-ops` |

文件级 lint 接口据此把 `spaceCode` 解析成 `lint.py --wiki-dir {子目录}`。

## 2. 状态机

```
idle → linted → fixing → relinted → synced
                  ↑__________|（仍有问题回到 fixing）
```

| 状态 | 触发 |
|------|------|
| `idle` | 进入页面，已选空间未 lint |
| `linted` | `POST /kb/wiki/lint-space` 返回问题清单 |
| `fixing` | 勾选问题页执行 enrich / ai-revise / 跳转手改 |
| `relinted` | 复检再次 lint，问题数下降/清零 |
| `synced` | `POST /kb/sync/trigger` 完成，DB 可见 |

## 3. 界面交互

入口：知识库侧栏新增 **「Wiki 治理」**（或健康体检页加第三 Tab）。建议**独立页** `knowledge/wiki-govern/index`，现有 DB 快照体检页（`KnowledgeLintView`）保留不动。

```
┌ 选空间 [enterprise-kb ▾]              [① 开始 Lint] [一键修复已选] ┐
├ ① Lint 结果（文件真值）                                            │
│   ▸ missing_dates (4)  ▸ broken_link (2)  ▸ orphan (3) ...       │
│     ☑ guides/xxx  缺 created/updated                              │
├ ② 批量修复（已勾选 N 项）                                          │
│     [脚本修复]  metadata 秒修，不调 LLM                            │
│     [AI 修复]   断链/孤儿等；可选 model                            │
│     [一键修复]  脚本 → AI → 自动复检 → （可选）Sync                │
├ ③ 复检  问题数 9 → 2（auto-fix 内可自动执行）                      │
└ 旁路：投喂新 raw → [打开 Ingest 工作台]                            ┘
```

### 3.1 批量修复编排（核心 UX）

- 按 issue.kind 分三类（`GET /kb/wiki/govern/options` 返回 `scriptFixableKinds` / `aiFixableKinds`）：
  - **脚本**：`missing_dates` / `slug_mismatch` / `missing_source` → `POST /kb/wiki/govern/script-fix`
  - **LLM**：`broken_link` / `orphan` / `missing_concept` / … → `POST /kb/wiki/govern/ai-batch-fix`
  - **手改**：`dup_slug` → 跳转单页编辑或 Cursor
- **一键**：`POST /kb/wiki/govern/auto-fix`（默认 scriptFix+aiFix+relintAfter；syncAfter 可选）
- Lint 完成后 **默认勾选全部可脚本+可 AI 项**（前端）

## 4. 后端改动

### 4.1 新增：文件级空间 Lint（唯一核心缺口）

照搬 `KbSyncServiceImpl.executeSync` 的 `ProcessBuilder` 封装，新建 `KbWikiLintService`：

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/kb/wiki/lint-space` | 跑 `lint.py --wiki-dir {spaceDir} --json {tmp}`，解析回 JSON | 空间 **editor** |

请求 `{ spaceId?, spaceCode?, strict? }`；响应：

```json
{
  "spaceCode": "enterprise-kb",
  "wikiDir": "wiki",
  "stats": { "pages": 375, "errors": 2, "warnings": 9 },
  "issues": [
    { "level": "error", "kind": "broken_link", "page": "guides/本地启动指南",
      "detail": "→ [[不存在的页]]", "suggest": "建该页或改链" }
  ],
  "exitCode": 1,
  "outputTail": "..."
}
```

要点：复用 `kb.sync.python` / `timeout-seconds`；新增 `kb.wiki.lint-script-path`（默认 `moli-knowledge/kb/tools/lint.py`）；纯文件扫描、无 LLM、快。

### 4.2 新增：脚本修复 + 一键 auto-fix（T16e ✅）

| 方法 | 路径 | 说明 | LLM |
|------|------|------|-----|
| POST | `/kb/wiki/govern/script-fix` | 批量修 `missing_dates` / `slug_mismatch` / `missing_source` | 否 |
| POST | `/kb/wiki/govern/ai-batch-fix` | 按页合并 issue 调 ai-revise 并写盘 | 是 |
| POST | `/kb/wiki/govern/auto-fix` | 脚本 → AI → relint → 可选 Sync | 部分 |
| POST | `/kb/wiki/govern/merge-hint` | dup_slug/dup_content → Cursor 指令 | 否 |

### 4.3 复用（无需改后端）

- **ai-revise**（单页预览）：`POST /kb/wiki/ai-revise` 仍可用于 diff 审阅后再 PUT。
- **sync**：`POST /kb/sync/trigger?spaceId=` 或 auto-fix 的 `syncAfter=true`。

## 5. 前端改动（meiling-ui）

> **对接权威文档（给前端）**：[`docs/api/wiki-govern-frontend.md`](../../../docs/api/wiki-govern-frontend.md)  
> 含 TypeScript 类型、API 封装、按钮逻辑、i18n、验收清单。

```
KnowledgeWikiGovernView.vue          # 编排 + 状态机
├─ KbSpaceSelector                   # 复用
├─ GovernLintPanel                   # ① lint-space，按 kind 分组 + 勾选（默认全选可修项）
├─ GovernFixPanel                    # ② 脚本 / AI / 一键 + 模型 + Sync 勾选
└─ （可选）问题数 before→after 摘要
```

| API 模块 | 路径 |
|----------|------|
| `lintWikiSpaceApi` | `POST /kb/wiki/lint-space` |
| `getWikiGovernOptionsApi` | `GET /kb/wiki/govern/options` |
| `wikiGovernScriptFixApi` | `POST /kb/wiki/govern/script-fix` |
| `wikiGovernAiBatchFixApi` | `POST /kb/wiki/govern/ai-batch-fix` |
| `wikiGovernAutoFixApi` | `POST /kb/wiki/govern/auto-fix` |

- types：见 `wiki-govern-frontend.md` §6
- i18n：zh / en / ja `knowledge.wikiGovern.*`
- **移除**：治理页批量 enrich、Cloud Agent 模式

## 6. 权限

| 操作 | ACL |
|------|-----|
| lint-space / script-fix / ai-batch-fix / auto-fix | 空间 **editor** |
| ai-batch-fix / auto-fix（AI 部分） | 同上 + `kb.llm.usable()` |
| sync | `kb:sync:trigger` 或空间 admin（或 auto-fix `syncAfter`） |

## 7. 分阶段交付（T16a–d）

| 阶段 | 范围 | 验收 |
|------|------|------|
| **T16a** ✅ | `POST /kb/wiki/lint-space` + 前端 lint 展示 | 选空间扫文件真值 |
| **T16e** ✅ | `script-fix` / `ai-batch-fix` / `auto-fix` 后端 | API 联调通过 |
| **T16f** 🔵 | 前端按 [`wiki-govern-frontend.md`](../../../docs/api/wiki-govern-frontend.md) 接 UI | Lint→脚本/AI/一键→merge-hint→可选 Sync |
| **T16g** ✅ | `merge-hint` API + `manualOnlyKinds` | dup 合并 Cursor 指令 |

## 8. 风险与约束

| 风险 | 缓解 |
|------|------|
| `lint.py` 依赖部署机 Python + kb 目录 | 复用 `kb.sync.python`/路径；无环境时报错降级（同 sync 现状） |
| 批量 LLM 调用慢 | 串行 + 进度 + 失败隔离 + 可取消；不并发轰炸 LLM |
| 改完未 Sync → DB 旧 | 强制链路③④：复检通过才 Sync；UI 提示「已改未同步」 |
| 文件 lint 与 DB 体检口径不同 | 文档明确：治理用文件真值，DB 体检页另算，并存不互斥 |
| Web 与 Cursor Agent 并改同文件 | enrich 读 baseline；以 git 为准，必要时 contentHash 校验 |

## 9. 工作量

| 模块 | 估算 |
|------|------|
| 后端 lint-space 接口 + service + 配置 | 0.5–1 天 |
| 前端治理页 + 4 子组件 + 编排 | 2–3 天 |
| i18n + 联调 + 文档 | 0.5–1 天 |
| **合计** | **约 3.5–5 人日**（enrich/sync/ai-revise 全复用） |

## 相关

[[Wiki在线编辑与AI协助改稿]] · [[Ingest工作台产品方案]] · [[AI自我进化与MD审校流程]] · [[增量ingest与raw投喂指南]]
