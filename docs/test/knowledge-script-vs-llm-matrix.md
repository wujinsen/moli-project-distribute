# 知识库 · 脚本 vs LLM 能力矩阵

> **用途**：产品 / 前端 / 运维统一判断「该走脚本还是大模型」。  
> 契约：[KNOWLEDGE_API.md](../api/KNOWLEDGE_API.md) · 治理前端：[wiki-govern-frontend.md](../api/wiki-govern-frontend.md)

---

## 1. 总原则

| 原则 | 说明 |
|------|------|
| 能脚本就脚本 | 规则固定、可重复、毫秒级 → Python/ Java 直跑 |
| 要理解再 LLM | 断链、orphan、正文改写 → `kb.llm` 一键 |
| 结构性重复 | `dup_slug` / 合并页 → **人工 + Cursor 指令**（`merge-hint`），不批量 LLM |

---

## 2. Wiki 治理（T16）

| 步骤 | 能力 | 脚本 | LLM | 人工 |
|------|------|:----:|:---:|:----:|
| 文件体检 | `POST /kb/wiki-moli/lint-space` | ✅ | | |
| 修 metadata | `missing_dates` / `slug_mismatch` / `missing_source` → `script-fix` | ✅ | | |
| 修链接/结构 | `broken_link` / `orphan` / … → `ai-batch-fix` | | ✅ | |
| 一键闭环 | `auto-fix`（脚本→AI→relint→可选 Sync） | 部分 | 部分 | |
| slug 撞车 | `dup_slug` | | | ✅ + `merge-hint` |
| 正文重复 | `dup_content` / `near_dup` | | 可选 | ✅ + `merge-hint` |
| 写库 | `POST /kb/sync/trigger` | ✅ | | |

**kind 列表**：`GET /kb/wiki-moli/govern/options` → `scriptFixableKinds` / `aiFixableKinds` / `manualOnlyKinds`

---

## 3. Ingest 入库（T15/T18）

| 步骤 | 能力 | 脚本 | LLM | 说明 |
|------|------|:----:|:---:|------|
| raw 树 / 覆盖 | `raw-tree` / `raw-coverage` | ✅ | | |
| Plan | `useLlmPlan=false` → Express skeleton | ✅ | | 1 raw → 1 create |
| Plan | `POST .../plan` | | ✅ | 默认 |
| 生成草稿 | `useLlmGenerate=false` → **模板模式** | ✅ | | raw 直贴 frontmatter+正文 |
| 生成草稿 | `useLlmGenerate=true`（默认） | | ✅ | PageWriter |
| 批次 lint / commit | `lint` / `commit` | ✅ | | |
| commit 门禁 | raw 已被 wiki 引用 | ✅ | | `assertRawOpenForCommit` |
| Sync | `sync_to_db.py` | ✅ | | |
| 入库后引导 | `commit.nextSteps` | ✅ | | 链到 Wiki 治理 / 健康体检 |

**模板模式参数**：`useLlmGenerate=false` on `generate` / `prepare` / `express` / `draft/regenerate`  
详见 [knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md) §1

---

## 4. 健康体检（DB 快照 · KBOPS-8/10）

| 步骤 | 能力 | 脚本 | LLM |
|------|------|:----:|:---:|
| 重新体检 | `GET /kb/lint` | ✅ | |
| 扫描并落库 | 写 `kb_lint_issue`（12 类 + assignee/priority） | ✅ | |
| 批量工单 | `batch-status` / `batch-assign` | ✅ | |
| 类型对照 | `GET /kb/lint/issue-types`（Web ↔ lint.py） | ✅ | |
| Wiki 同步 | `POST /kb/sync/trigger` | ✅ | |

与 **Wiki 治理** 分工：治理 = **磁盘真值**（`lint-space`）；健康体检 = **Sync 后 DB 快照**。  
验收：[knowledge-lint-ops-acceptance.md](knowledge-lint-ops-acceptance.md)

---

## 5. 写 wiki 后的推荐顺序

```
改 wiki 文件 / Ingest commit
  → （可选）POST /kb/sync/trigger
  → Wiki 治理 Lint（文件真值）
  → 健康体检 · 扫描并落库
```

响应字段 `nextSteps[]`（`KbWorkflowHintVo`）已嵌入：`IngestCommitResultVo` / `IngestPublishResultVo` / `SyncTriggerVo`。

---

## 6. 相关文档

| 文档 | 内容 |
|------|------|
| [knowledge-lint-ops-acceptance.md](knowledge-lint-ops-acceptance.md) | KBOPS-8/10 健康体检工单验收 |
| [wiki-govern-frontend.md](../api/wiki-govern-frontend.md) | 治理 UI + merge-hint |
| [knowledge-ingest-acceptance.md](knowledge-ingest-acceptance.md) | Ingest 验收（含模板入库 §1） |
| [knowledge-wiki-lint-space.md](knowledge-wiki-lint-space.md) | lint-space 验收 |
