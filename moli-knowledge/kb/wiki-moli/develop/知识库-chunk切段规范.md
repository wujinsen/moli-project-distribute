---
title: 知识库 chunk 切段规范
slug: 知识库-chunk切段规范
type: article
status: active
tags: [知识库, 检索, chunk, ask, sync]
sources:
  - moli-knowledge/kb/ROADMAP.md
  - moli-knowledge/kb/eval/README.md
  - moli-knowledge/kb/tools/sync_to_db.py
  - moli-knowledge/moli-knowledge-server/src/main/java/com/moli/knowledge/server/service/impl/KbAskServiceImpl.java
  - docs/sql/KNOWLEDGE_SCHEMA.md
related: [知识库服务, 知识库-meilisearch接入规划, 知识库三操作, wiki同步指南, 查询与体检指南, 知识库设计哲学-docs-as-code]
created: 2026-07-13
updated: 2026-07-13
---

# 知识库 chunk 切段规范

> **状态：已实现**（`chunk_split.py` + `sync_to_db` 写 chunk + `/kb/ask` 按段召回）。  
> 动机：当前 `/kb/ask` 以**整页**召回与组 context，长页噪声大、词面不重合时易漏召回（见 `kb/eval/` 基线 M05/M06）。  
> 背景：[[知识库设计哲学-docs-as-code]] · 检索演进 [[知识库-meilisearch接入规划]] · 评测 [[查询与体检指南]]（`kb/eval/README.md`）。

## 0. 一句话

**chunk = 按 Markdown 结构切出的可检索段落**；wiki 仍是唯一真相，`kb_document` 保留整页，`kb_document_chunk` 供召回与 LLM context，引用仍落到页级 `[[slug]]`。

## 1. 在管线中的位置

```text
wiki/*.md
  → sync_to_db.py
      → kb_document（整页，浏览/详情/图谱不变）
      → kb_document_chunk（切段，ask 召回用）
  → /kb/ask
      → 对 chunk 召回 top-N
      → 精排后取 top-K 段拼 context
      → 引用 citations 仍用 document.slug
```

与 [[wiki同步指南]] 铁律一致：**只在 sync 时从正文派生 chunk**，Web/Agent 不直写 DB chunk 表。

日后若上 Meilisearch（[[知识库-meilisearch接入规划]]），索引粒度改为 **1 chunk = 1 条 Meilisearch 文档**，本切段规则**不用改**，只换召回实现与 sync 后的 bulk 钩子。

## 2. 切分规则（按结构，不按固定字数硬切）

### 2.1 边界优先级

| 顺序 | 规则 | 说明 |
|------|------|------|
| 1 | **chunk-0** | 去掉 YAML frontmatter 后，从正文到**第一个 `##` 之前**：页级 `# 标题`、首段摘要、紧接的引用块 `>` |
| 2 | **主切分** | 每个 **`##` 标题及其下正文** = 1 个 chunk（到下一个同级 `##` 或文末） |
| 3 | **次级切分** | 单个 `##` 节 **> 1500 字**（约 512 token）时，再按 **`###`** 切 |
| 4 | **短文** | 全文无 `##` 时，**整页 1 chunk**（含 chunk-0 合并，不强行拆空节） |
| 5 | **硬上限** | 单 chunk **> 2000 字** 且无 `###` 时，按**空行段落**再拆（最后手段，避免超长节） |

示例（[[知识库三操作]]）：

```text
chunk-0  # 知识库三操作 + 首段说明
chunk-1  ## 1. 架构一览
chunk-2  ## 2. Ingest（提炼入库）
chunk-3  ## 3. Query（向知识库提问）
chunk-4  ## 4. Lint（健康体检）
...
```

### 2.2 尺寸参数

| 参数 | 建议值 | 说明 |
|------|--------|------|
| 目标长度 | 300–1200 字 | ngram 友好；LLM context 不浪费 |
| 最小长度 | < 80 字 | 合并到上一 chunk（避免「## 相关」两行成独立块） |
| 最大长度 | ~2000 字 | 超过则 `###` 或段落再切 |
| overlap（重叠） | **第一版不做** | wiki 有 `[[slug]]` 互链；需要时再给每 chunk 加父标题一行前缀 |

### 2.3 不要切开的内容

| 内容 | 处理 |
|------|------|
| 代码块 ` ``` ... ``` ` | 与所属 `##`/`###` 同 chunk，禁止跨块拆断 |
| Markdown 表格 | 整表跟所属小节 |
| 图片 `![]()` | 保留 alt + 图注在 chunk 内；二进制不进索引 |
| `<details>` 备查（Mermaid/ASCII） | **默认不进入 chunk**（主图用 PNG，见仓库 draw.io 规范）；若保留则单独低权重 chunk |
| YAML frontmatter | 不单独成 chunk；字段冗余到每 chunk 的 filter 列（见 §3） |

### 2.4 按体裁微调（可选，v1 可统一用 `##` 规则）

| `kb_type` | 建议 |
|-----------|------|
| `guide` / `service` | 严格 `##`，一步一节 |
| `concept` | `##` + 必要时 `###` |
| `article` / `interview` | 长文多用 `###`；面试题可按「一题一块」（`## 问` / 题号标题） |
| `output` | 节少时长文，1–3 chunk 即可 |

## 3. 存储模型（规划：`kb_document_chunk`）

> 表结构落地时合并进 `docs/sql/KNOWLEDGE_SCHEMA.md` 与增量迁移脚本；此处为契约草案。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint | 主键 |
| `document_id` | bigint | 所属 `kb_document.id` |
| `space_id` | bigint | 冗余，ACL / filter |
| `slug` | varchar | 冗余，引用与调试 |
| `kb_type` | varchar | 冗余，作用域过滤 |
| `category_id` | bigint | 冗余，可选 filter |
| `status` | tinyint | 与文档一致，archived 不参与 ask |
| `chunk_index` | int | 0-based，页内顺序 |
| `heading` | varchar | 节标题文本（chunk-0 可为空或页标题） |
| `heading_level` | tinyint | 0=页首，2=`##`，3=`###` |
| `content` | text | 切段正文（含该节标题行） |
| `char_count` | int | 字符数 |
| `content_hash` | char(64) | SHA-256，增量更新 |
| `is_delete` | tinyint | 软删 |

**幂等**：`(document_id, chunk_index)` 或 `content_hash` 驱动 upsert；页 `content_hash` 变 → 删该文档旧 chunk 全量重建（对齐 `kb_relation` 全量重建策略）。

**索引（MySQL 阶段）**：`FULLTEXT(content, heading)` ngram；filter 列走普通索引。

## 4. `/kb/ask` 行为变更（规划）

当前（整页）：`searchAskCandidates` → top-N **文档** → bigram 精排 → top-K **整页** 进 prompt。

目标（chunk）：

1. **作用域**不变：`detectScope` → `kb_type` include/exclude + ACL `space_id`
2. **召回**：对 `kb_document_chunk` 全文/LIKE，limit ≈ `ask-candidate-limit`（默认 100 **段**）
3. **精排**：对 chunk 做 bigram（可叠加 heading 加权）
4. **去重**：同一 `document_id` 最多保留 **2 段**进 top-K（默认 K=8），避免一页霸榜
5. **context**：每段格式  
   `## 节：[[slug]]（title）\n{heading}\n{content片段}`
6. **citations**：仍输出 **页级** `slug`/`title`（用户点进浏览页）；可选在 snippet 中带 `heading`

检索式降级、LLM 失败降级逻辑不变（见 [[知识库服务]]）。

## 5. Sync 实现要点（`sync_to_db.py`）

1. 解析 frontmatter + 正文（已有逻辑）
2. 调用 `split_markdown_chunks(body)` → `List[ChunkDraft]`
3. upsert `kb_document` 后，对该 `document_id`：**DELETE 旧 chunk → INSERT 新 chunk**
4. `content_hash` 未变的文档 **skip chunk 重写**（与文档 skip 一致）
5. 日志：`kb_sync_log` 可增 `chunk_insert` / `chunk_delete` 计数（可选）

切段函数建议独立模块 `kb/tools/chunk_split.py`，供 sync 与单测共用。

## 6. 与 Meilisearch / 向量的关系

| 阶段 | 索引对象 | 切段规则 |
|------|----------|----------|
| 现在 | 整页 `kb_document` | — |
| chunk v1 | MySQL `kb_document_chunk` | **本规范** |
| Meilisearch | 1 chunk = 1 索引文档 | **本规范不变**，indexer 读 chunk 表 bulk |
| 向量（更后） | 对 chunk embedding | 同一 chunk 边界，避免重复切分 |

Meilisearch 文档模型需从「一页一条」改为「一段一条」——见 [[知识库-meilisearch接入规划]] §2 修订说明（下文 §7）。

## 7. 验收与回归

- **门禁脚本**：`python kb/tools/eval_ask.py`（`kb/eval/golden.jsonl`）
- **基线**（2026-07-13 检索式）：hit@8=66.67%，MRR=0.456；chunk 上线后目标 **M05/M06 命中**，整体 hit@8 ≥ 75% 为第一版目标
- **Lint**：chunk 表不为空；每篇已发布 `kb` 文档至少 1 chunk；`chunk_index` 连续
- **手工**：长页 ask 后 context 不再整页复述；引用 slug 仍可浏览打开

## 8. 反例（禁止）

| ❌ 不要 | ✅ 要 |
|--------|------|
| 固定 500 字滑动窗口 | 按 `##` / `###` 结构切 |
| 代码块/表格拦腰切断 | 整段跟所属小节 |
| 把 `<details>` 里 Mermaid 当主检索正文 | 主图 PNG + 正文；备查进 details |
| 每页只 1 chunk（等于没做） | 长页多节多 chunk |
| Web 直写 chunk 表 | 仅 sync 派生 |

## 相关

[[知识库服务]] · [[知识库-meilisearch接入规划]] · [[wiki同步指南]] · [[知识库三操作]] · `kb/eval/README.md` · `docs/sql/KNOWLEDGE_SCHEMA.md`
