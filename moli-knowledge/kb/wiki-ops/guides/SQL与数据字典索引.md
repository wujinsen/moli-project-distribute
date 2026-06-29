---
title: SQL与数据字典索引
slug: SQL与数据字典索引
type: guide
status: active
tags: [SQL, 数据库, 索引]
sources:
  - docs/sql/README.md
related: [项目文档总览, 数据库初始化指南, SQL迁移顺序, 技术方案与架构索引]
created: 2026-06-20
updated: 2026-06-20
---

# SQL 与数据字典索引

> 工程契约：[`docs/sql/`](../../../../docs/sql/) · 一键初始化：[[数据库初始化指南]] · 顺序：[[SQL迁移顺序]]

## 表设计说明

| 文档 | 范围 |
|------|------|
| [USER_CENTER_SCHEMA.md](../../../../docs/sql/USER_CENTER_SCHEMA.md) | sys_*、operation_* |
| [KNOWLEDGE_SCHEMA.md](../../../../docs/sql/KNOWLEDGE_SCHEMA.md) | kb_* 14 表 |
| [KNOWLEDGE_SCHEMA_ER.png](../../../../docs/sql/KNOWLEDGE_SCHEMA_ER.png) | ER 图 PNG |

## 基线与增量脚本

| 文件 | 说明 |
|------|------|
| [scripts/moli.sql](../../../../scripts/moli.sql) | **推荐** 全库基线 |
| `02_seckill_schema.sql` | 秒杀 |
| `03_knowledge_schema.sql` | 知识库表 |
| `04_knowledge_menu.sql` | 菜单 |
| `07_kb_space_ops_manual.sql` | 本空间种子 |
| `08`–`12` | Ingest / 治理 / LLM 增量 |

完整清单见 [sql/README.md](../../../../docs/sql/README.md) 与 [[SQL迁移顺序]]。

## 字符集

含中文种子 **必须** `utf8mb4` + `source`，禁止 PowerShell 管道导入。见 [[数据库初始化指南]]。

## 相关

[[项目文档总览]] · [[v1发布Runbook]]
