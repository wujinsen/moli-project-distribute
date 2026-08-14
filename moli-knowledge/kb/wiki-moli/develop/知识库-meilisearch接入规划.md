---


title: 知识库 Meilisearch 接入规划
slug: 知识库-meilisearch接入规划
type: article
status: active
tags: [知识库, Meilisearch, 检索, 规划, 体裁]
sources:
  - moli-knowledge/kb/ROADMAP.md
  - moli-knowledge/moli-knowledge-server/src/main/java/com/moli/knowledge/server/service/impl/KbAskServiceImpl.java
  - docs/sql/KNOWLEDGE_SCHEMA.md
related: [知识库设计哲学-docs-as-code, 知识库-chunk切段规范, 知识库服务, kb-wiki到es同步流水线, wiki同步指南, 知识库使用指南]
created: 2026-07-01
updated: 2026-07-13
---

# 知识库 Meilisearch 接入规划

> 蓝图，非已落地。当前检索走 **MySQL ngram 全文索引**（`MATCH AGAINST`，见 [[知识库服务]]）。本页规划「文档量/召回触发后」把**检索那一层**换成 Meilisearch，并明确**体裁(kb_type)/分类(category)** 在其中的位置。  
> 设计哲学背景见 [[知识库设计哲学-docs-as-code]]；ES 版同类规划见 [[kb-wiki到es同步流水线]]。

## 0. 一句话定位

**Meilisearch 替换的是「检索」，不是「数据库」。** wiki 仍是真相，MySQL 仍存储/鉴权/分类/关系，Meilisearch 是**可随时从 MySQL 重建的检索副本**。

> **架构图**：`docs/diagrams/png/moli-kb-meilisearch.png`（请在 IDE 中打开仓库文件查看；源文件 `docs/diagrams/moli-kb-meilisearch.drawio`）

## 1. 为什么不是「全换掉 MySQL」

MySQL 现在同时承担三件事，Meilisearch 只能接其一：

| MySQL 现职责 | Meilisearch 能接？ |
|--------------|--------------------|
| 全文检索 `MATCH AGAINST` | ✅ **换这部分** |
| 文档存储 / `kb_document` 正文 | ❌ 留 MySQL |
| 空间 ACL（`kb_space_member` + Shiro） | ❌ 留 MySQL |
| 分类树 `kb_category` / 关系 `kb_relation` / 标签 | ❌ 留 MySQL |
| 事务、收藏、评论、版本 | ❌ 留 MySQL |

数据流：

```text
wiki/*.md → sync_to_db.py → MySQL（存储/ACL/分类，真相镜像）
                          → Meilisearch indexer（检索副本，可重建）
```

## 2. 索引文档模型

> **与 chunk 切段的关系（2026-07-13）**：若已落地 [[知识库-chunk切段规范]]，Meilisearch 索引粒度为 **1 chunk = 1 条文档**（非整页）。切段规则、字段冗余与 sync 钩子见该页 §3、§6；下文「一页一条」为 chunk 未上时的初版模型。

### 2.1 chunk 未上（初版 / 浏览列表仍可按页）

每篇 wiki 文档 → 一条 Meilisearch 文档：

| 字段 | 来源 | Meilisearch 角色 |
|------|------|------------------|
| `id` | `kb_document.id`（或 slug） | 主键 |
| `slug` | frontmatter | filter / 展示 |
| `title` | frontmatter | **searchable** |
| `summary` | 提炼 | **searchable** |
| `content` | 正文 | **searchable** |
| `space_id` | `kb_document` | **filterable**（ACL 必用） |
| `category_id` | `kb_document` | **filterable**（分类筛选） |
| `kb_type` | frontmatter `type` | **filterable**（体裁筛选 + facet） |
| `status` | frontmatter | **filterable**（排除 archived） |
| `tags` | frontmatter | **filterable**（多维） |
| `update_time` | `kb_document` | **sortable** |

索引设置：

```json
{
  "searchableAttributes": ["title", "summary", "content"],
  "filterableAttributes": ["space_id", "category_id", "kb_type", "status", "tags"],
  "sortableAttributes": ["update_time"]
}
```

### 2.2 chunk 已上（推荐，`/kb/ask` 与精排召回）

每个 `kb_document_chunk` → 一条 Meilisearch 文档：

| 字段 | 来源 | Meilisearch 角色 |
|------|------|------------------|
| `id` | `kb_document_chunk.id` | 主键 |
| `document_id` | chunk 表 | filter / 回查整页 |
| `slug` | 冗余 | filter / 展示 / 引用 |
| `heading` | 节标题 | **searchable**（加权） |
| `content` | 切段正文 | **searchable** |
| `title` | `kb_document.title` | **searchable**（页标题，可选冗余） |
| `space_id` | 冗余 | **filterable**（ACL） |
| `category_id` | 冗余 | **filterable** |
| `kb_type` | 冗余 | **filterable** + facet |
| `status` | 冗余 | **filterable** |
| `chunk_index` | int | sortable / 同页内排序 |

```json
{
  "searchableAttributes": ["heading", "content", "title"],
  "filterableAttributes": ["space_id", "category_id", "kb_type", "status", "document_id"],
  "sortableAttributes": ["chunk_index"]
}
```

浏览侧栏 `/kb/document/search` 可继续按**整页**索引或 hit 聚合回 `document_id`（产品二选一，见 [[知识库-chunk切段规范]] §4）。

## 3. 体裁(kb_type)与分类(category)在检索中的位置

二者都是 **filter**，不是搜索词；导航仍以分类为主、体裁为辅（详见 [[知识库设计哲学-docs-as-code]]）。

| 维度 | 字段 | UI 角色 | Meilisearch |
|------|------|---------|-------------|
| 分类 | `category_id` | 左侧主导航树 | `filter: category_id=?` |
| 体裁 | `kb_type` | 内容区筛选 chip | `filter: kb_type=?` + facet |
| 标签 | `tags` | 辅助筛选 | `filter: tags IN [...]` |

查询示例：

```text
POST /indexes/kb/search
{
  "q": "缓存一致性",
  "filter": "space_id IN [900...3] AND category_id=900...135 AND kb_type='article'",
  "facets": ["kb_type", "category_id"]
}
```

**facet 红利**：Meilisearch 搜索时顺带返回 `facetDistribution`（每个 `kb_type`/`category_id` 的命中数），体裁 chip 的数字白拿，无需单独 `GROUP BY` 查询——比 MySQL 版省一次往返。

## 4. 接口契约不变（底层换实现）

上一阶段先按 MySQL 实现的接口，迁 Meilisearch 时**契约不变**，只换内部实现：

| 接口 | MySQL 实现 | Meilisearch 实现 |
|------|------------|------------------|
| `/kb/document/search?kbType=` | `WHERE kb_type=? AND MATCH AGAINST` | `filter: kb_type=...` |
| `/kb/index/types`（体裁计数） | `GROUP BY kb_type` | `facetDistribution.kb_type` |
| `/kb/meta/kb-types`（白名单） | 常量 | 常量（不变） |
| `/kb/ask` 候选召回 | `searchAskCandidates` ngram | Meilisearch search + filter |

> `/kb/ask` 已有「关键词 → include/exclude kb_type 作用域」逻辑（`KbAskServiceImpl.detectScope`），迁移后把作用域翻译成 Meilisearch `filter` 即可，识别规则可复用。

## 5. 索引同步（reindex 钩子）

复用现有 Sync 闭环（见 [[wiki同步指南]]），在 `sync_to_db` 成功后追加一步：

| 事件 | 动作 |
|------|------|
| 新增 / 改 wiki 页 | `content_hash` 变 → bulk 写 Meilisearch（chunk 已上时写 **chunk 行**，见 [[知识库-chunk切段规范]]） |
| 删 / 移 wiki 页 | DB `is_delete=1` → Meilisearch **按 slug/id delete** |
| 改体裁 `type:` | frontmatter 改 → Sync 更新 `kb_type` → reindex 该文档 |
| 全量重建 | 从 MySQL `kb_document` 扫一遍 bulk（灾备/首次） |

原则：**Meilisearch 永远可从 MySQL 重建**，不作为真相，丢了重灌即可。

## 6. 必须注意（迁移风险点）

1. **ACL 进 filter（最易漏）**：每次 search 都要 `filter: space_id IN [当前用户可读空间]`，复用 `KbAclService` 的可读空间解析；否则跨空间越权。
2. **同步一致性**：MySQL 软删/改体裁后必须 reindex，否则搜索结果与浏览不一致。建议挂在 Sync 钩子，sync 成功才 bulk。
3. **type 白名单仍在 Python**：`KB_TYPES` 6 种（`sync_to_db.py`）是底线；Meilisearch 不校验取值，脏 `type` 会原样进索引，故 `lint.py --strict` 门禁更重要。
4. **正文回查**：Meilisearch 返回命中 `id`，正文/关系/收藏仍回 MySQL 取（或直接用 Meilisearch 存的 content 渲染摘要，正文详情走 MySQL）。
5. **体裁维护方式不变**：仍改 wiki frontmatter `type:` → Sync → 下游 reindex；编辑页下拉、列表快速改体裁。

## 7. 触发信号与落地顺序

按 `kb/ROADMAP.md` §五③「检索后端演进」：

```
MySQL ngram 全文（已上）
   → 文档量 >1000~2000 / 召回变差 / 多人产品
   → Meilisearch / Typesense（本规划）
   → ES（海量）→ 向量库（语义）
```

建议顺序：

1. **先落地 chunk 切段**（MySQL `kb_document_chunk` + ask 按段召回，见 [[知识库-chunk切段规范]]）并用 `kb/eval/golden.jsonl` 回归。
2. **先按 MySQL** 落地体裁过滤 + facet（`/kb/document/search?kbType=`、`/kb/index/types`、`/kb/meta/kb-types`）——小、即用。
3. 信号触发后，**只换 search 实现层**为 Meilisearch，接口与前端零改动；索引读 chunk 表 bulk。

## 相关

[[知识库设计哲学-docs-as-code]] · [[知识库服务]] · [[kb-wiki到es同步流水线]] · [[wiki同步指南]] · [[知识库使用指南]] · 工程契约 `docs/sql/KNOWLEDGE_SCHEMA.md`
