---
name: kb-ingest-sync
description: >-
  Runs Moli knowledge base wiki ingest, lint, and sync workflows under
  moli-knowledge/kb/. Use when editing kb/raw, wiki*, ingest batches, lint.py,
  sync_to_db.py, chunk splitting, eval_ask.py, wiki space boundaries, or user
  says 知识库 ingest / wiki 同步 / lint strict / chunk 切段 / ask 评测 /
  enterprise-kb vs wiki-moli.
---

# Moli · 知识库 Ingest / Lint / Sync

> **完整契约（必读）**：[`moli-knowledge/kb/AGENTS.md`](../../moli-knowledge/kb/AGENTS.md)（L2）  
> 全仓文档规则：[`AGENTS.md`](../../AGENTS.md)（L1）  
> 运维操作：[`docs/ops/knowledge-workbench-operations.md`](../../docs/ops/knowledge-workbench-operations.md)

## 何时使用

- `kb/raw` 投喂、wiki 增删改、Ingest 批次
- `lint.py --strict`、`sync_to_db.py`、Web 同步前门禁
- **检索演进**：chunk 切段、`eval_ask.py` 问答回归（见下文 §检索演进）
- 空间边界：`wiki/` vs `wiki-moli/`
- 用户说 `@kb-ingest-sync` 或「ingest 这批 raw」

**本 skill 是速查 + 决策树**；细节以 `kb/AGENTS.md` 为准，勿重复编造规则。

## 空间铁律（写盘前自检）

| 目标 | 允许 | 禁止 |
|------|------|------|
| **`wiki-moli/`** | 茉莉项目 PRD/运维/服务实体 | 通用八股整库、wujinsen 批量骨架 |
| **`wiki/`** | 通用技术 articles/concepts/interview | 「茉莉触点」节、项目 wikilink |

治理：`python kb/tools/kb_space_governance.py` · **禁止**再跑 `_gen_batches_287_1286.py`。

## 三操作决策

```
改 kb/raw 或新素材？     → Ingest（先规划 enrich/create/skip/conflicts）
问知识库问题？           → Query（定 type 作用域，≤15 页，带 [[slug]]）
改完 wiki 要对外可用？   → Lint → Sync（顺序不可颠倒）
```

## Ingest 最短流程

1. 读 `kb/AGENTS.md` §4 + 目标空间 `index.md`
2. **规划**（写盘前）：enrich / create / skip / conflicts（默认策略 A 原地 enrich）
3. 写 `wiki*/{dir_slug}/{slug}.md`（frontmatter 必填，见 AGENTS §2）
4. 更新 `index.md`、`log.md`（append-only）、按需 `graph/edges.jsonl`
5. `lint.py --strict` → `sync_to_db.py`

**禁止**：未规划批量新建 `xxx-v2` 平行页；**禁止**修改 `raw/`。

## Lint → Sync（§8.1 推荐顺序）

```powershell
cd moli-knowledge
python kb/tools/lint.py --strict
# 人工确认 git diff
python kb/tools/sync_to_db.py
```

| 阶段 | CLI | Web |
|------|-----|-----|
| wiki 门禁 | `lint.py --strict` | 无等价 |
| 写入 MySQL | `sync_to_db.py` | Wiki 同步 |
| DB 问题工单 | — | 扫描并落库 |

**禁止**跳过 lint 直接 sync。

## 常用命令

```powershell
# 严格 lint（发布前必跑）
python kb/tools/lint.py --strict

# wiki → kb_document
python kb/tools/sync_to_db.py

# 空间治理自检
python kb/tools/kb_space_governance.py
```

工作目录：`moli-knowledge/` 或仓库根（脚本路径 `kb/tools/...`）。

## 检索演进：chunk 切段（实现 / 改 ask 时）

> **规范权威**：[`wiki-moli/develop/知识库-chunk切段规范.md`](../../moli-knowledge/kb/wiki-moli/develop/知识库-chunk切段规范.md)（切段规则、表结构、ask 行为）。  
> Meilisearch 接入见同目录 [[知识库-meilisearch接入规划]]；**先 chunk、后 Meili**，索引粒度 1 chunk = 1 条。

```
wiki/*.md
  → sync_to_db.py (+ chunk_split.py)
      → kb_document（整页，浏览不变）
      → kb_document_chunk（按 ## 切段，ask 召回）
  → /kb/ask 对 chunk 召回 → citations 仍页级 [[slug]]
```

| 步骤 | 动作 |
|------|------|
| 1 读规范 | `知识库-chunk切段规范.md` §2 切分规则、§3 表字段 |
| 2 实现切段 | `kb/tools/chunk_split.py`；sync 在 `content_hash` 变时删旧 chunk、插新 chunk |
| 3 改 ask | `KbAskServiceImpl` 按 chunk 召回；同 `document_id` 最多 2 段进 top-K |
| 4 回归 | `python kb/tools/eval_ask.py`（`kb/eval/golden.jsonl`）；改检索前后各跑，报告在 `kb/eval/reports/` |
| 5 门禁 | 第一版目标 hit@8 ≥ 75%；可用 `--min-hit 0.75` 作 CI 退出码 |

```powershell
# 问答评测（需网关 + KnowledgeServer；账号见 kb/eval/README.md）
python kb/tools/eval_ask.py --username superadmin --password ***
python kb/tools/eval_ask.py --only M05 --min-hit 0.75
```

**勿**：固定字数滑动窗口切 chunk；Web 直写 chunk 表（仅 sync 派生）。

## Ingest 规划模板（对话用）

```
主题：{xxx}
空间：wiki-moli / wiki
策略：A 原地 enrich（默认）

enrich: [slug, ...]
create: [{ slug, categoryId }, ...]
skip:   [...]
conflicts: [...]  # 等人确认再改
```

## 版本再 Ingest

同一主题 v2+ raw：**enrich 原 slug**，不新建 `xxx-v5`；`sources` 追加 raw 路径。见 AGENTS §4.1。

## 常见错误

| 错误 | 修复 |
|------|------|
| 项目文档写入 `wiki/articles/` | 迁到 `wiki-moli/` |
| enterprise-kb 正文含「茉莉」 | 删改或 `revert_corpus_to_enterprise_kb.py` |
| 断链 `[[slug]]` | lint 报告后修链或建页 |
| sync 失败未分类 | 查 `kb_sync_log`、[`kb-sync-failure-runbook.md`](../../docs/ops/kb-sync-failure-runbook.md) |

## 用户怎么说

```
@kb-ingest-sync 对 raw/design/xxx.md 做 ingest，先出规划
```

```
wiki 改完了，跑 lint 和 sync
```
