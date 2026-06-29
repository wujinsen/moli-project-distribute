---
title: SQL迁移顺序
slug: SQL迁移顺序
type: guide
status: active
tags: [SQL, 迁移, 数据库, P0]
sources:
  - docs/ops/sql-migration-order.md
related: [数据库初始化指南, SQL与数据字典索引, v1发布Runbook]
created: 2026-06-20
updated: 2026-06-20
---

# SQL 增量脚本 · 执行顺序

> 基线：`scripts/moli.sql` · 一键：[[数据库初始化指南]]

## 新环境（init-db.ps1）

| 顺序 | 文件 |
|------|------|
| 1 | `scripts/moli.sql` |
| 2 | `02_seckill_schema.sql` |
| 3 | `03_knowledge_schema.sql` |
| 4 | `04_knowledge_menu.sql` |
| 5 | `07_kb_space_ops_manual.sql` |

**init-db 后需手动追加**（v1 全功能）：`05`–`06` → `04_kb_space_jp_exam`（可选）→ `08`–`12`（Ingest/治理/LLM）

## 已有库升级

```sql
SHOW TABLES LIKE 'kb_document';
SHOW TABLES LIKE 'kb_ingest_job';
SHOW COLUMNS FROM kb_category LIKE 'dir_slug';
```

推荐顺序见工程原文 `docs/ops/sql-migration-order.md` §2.2。

## 禁止

- PowerShell 管道导入含中文 SQL
- 跳过 `03` 直接跑 `04`

## 相关

[[SQL与数据字典索引]] · [[v1发布Runbook]]
