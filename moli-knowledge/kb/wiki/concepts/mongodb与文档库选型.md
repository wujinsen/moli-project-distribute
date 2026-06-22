---
title: MongoDB 与文档库选型
slug: mongodb与文档库选型
type: concept
status: active
tags: [MongoDB, NoSQL, 选型]
sources:
  - raw/wujinsen_markdown/DataBase/mongodb/Elasticsearch和MongoDB简要对比.note.md
related: [elasticsearch-搜索, mysql-索引, 技术栈与版本, 知识库服务]
created: 2026-06-22
updated: 2026-06-22
---

# MongoDB 与文档库选型

**茉莉当前主库为 MySQL 8**（业务 + RBAC + 秒杀 + 知识库元数据），**未使用 MongoDB**。本文供扩展选型与面试对照。

## 1. MongoDB 特点

- 文档型 NoSQL（BSON/JSON 模型）
- 灵活 Schema、水平分片
- 适合读多写多、结构多变的 OLTP（弱事务场景）
- 与 ES 对比：Mongo 偏 **CRUD 主存**；ES 偏 **检索/分析** [[elasticsearch-搜索]]

## 2. 与茉莉组件关系

| 需求 | 茉莉方案 |
|------|----------|
| 结构化业务 | MySQL + MyBatis [[mybatis-与-druid持久层]] |
| 全文检索 | ES（知识库规划中）[[知识库服务]] |
| 附件 | MinIO [[minio-附件存储指南]] |
| 缓存/秒杀 | Redis [[redis-缓存]] · [[秒杀设计]] |

若未来「笔记正文/非结构化块」量极大，可评估 Mongo 存 body、MySQL 存元数据；当前 wiki 同步走 MySQL `kb_*` 表。

## 3. MongoDB vs Elasticsearch（摘要）

| 维度 | MongoDB | Elasticsearch |
|------|---------|---------------|
| 定位 | 文档数据库 | 搜索与分析引擎 |
| 事务 | 4.x+ 多文档事务有限 | 非事务型 |
| 全文 | 有，分析能力弱于 ES | 核心强项 |
| 茉莉 | 未采用 | 知识库检索方向 |

## 4. PostgreSQL

茉莉未使用 PostgreSQL。若需 JSON 字段 + 强 SQL，PG 是 MySQL 之外的常见替代，迁移成本高，非当前路线。

## 相关

[[技术栈与版本]] · [[elasticsearch-搜索]]
