---
title: 分库分表入门
slug: sharding-分库分表入门
type: article
status: active
tags: [MySQL, 分库分表, 分区表, 水平扩展]
sources:
- raw/wujinsen_markdown/DataBase/mysql/分库分表/互联网公司为啥不使用mysql分区表？.note.md
- raw/wujinsen_markdown/架构/分库分表/分库分表的几种常见形式以及可能遇到的难.note.md
- raw/wujinsen_markdown/源码分析/MyCat/Mycat源码篇 起步,Mycat源码阅读调试环境搭建.note.md
- raw/wujinsen_markdown/源码分析/MyCat/钉钉发送消息.note.md
- raw/wujinsen_markdown/源码分析/MyCat/钉钉发送消息相关文档.note.md
related: [mysql-索引面试题, mysql-深分页与慢sql优化, 分布式id生成, 高并发券系统实战, mysql-binlog与canal同步]
created: 2026-07-05
updated: 2026-07-05
---

# 分库分表入门

> 索引与单表优化优先 [[database/mysql-索引面试题]]、[[database/mysql-深分页与慢sql优化]]；ID 见 [[database/分布式id生成]]。

## 1. 何时水平切分

单表数据量/写入 QPS 接近单机 MySQL 上限（慢查询、备份、DDL 风险）时，考虑**分库分表**或分区。

## 2. 分库分表 vs 分区表

| | 分库分表 | MySQL 分区表 |
|---|----------|--------------|
| 数据位置 | 多实例/多库多表 | **单表**内按规则分文件 |
| 代码 | 常需业务/中间件路由 | **对应用透明** |
| 扩展 | 加库加表 | 受单实例限制 |
| 关联查询 | 跨库 JOIN 难 | 跨分区也可能灾难 |

## 3. 互联网更常自研分库分表的原因（raw 摘要）

1. 分区键设计不灵活，不走分区键易**全表锁**
2. 数据量大时**分区表 JOIN** 性能差
3. 分库分表路由在业务/中间件，**可控**；分区表优化器行为难预期
4. 运维复杂度（备份、迁移）

## 4. 设计要点

| 项 | 建议 |
|----|------|
| **分片键** | 高基数、查询必带（如 `user_id` 查领券记录） |
| **路由** | ShardingSphere、MyCat 或应用层 |
| **全局 ID** | 雪花/号段，见 [[database/分布式id生成]] |
| **扩容** | 翻倍扩容 + 数据迁移方案 |
| **跨片** | 避免；必要时宽表、ES、汇总表 |

## 5. 与分区表选型

- **日志、按时间归档、单库可承受**：可评估分区
- **核心交易、亿级用户维度**：倾向分库分表

实战案例：[[middleware/高并发券系统实战]]（按 user_id 分领券表）。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 MyCat 源码与架构分库分表 raw。

## 批次#1324 增补（wujinsen Phase2 长尾）

合并 MyCat 源码阅读 raw。

原文插图 annex：[[database/annex-分库分表的几种常见形式以及可能遇到的难]]

原文插图 annex：[[database/annex-Mycat源码篇-起步,Mycat源码阅读调试环境搭建]]
