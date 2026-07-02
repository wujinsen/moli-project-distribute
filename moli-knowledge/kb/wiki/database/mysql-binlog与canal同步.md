---
title: MySQL Binlog 与 Canal 同步
slug: mysql-binlog与canal同步
type: article
status: active
tags: [MySQL, Binlog, Canal, 同步]
sources:
 - raw/wujinsen_markdown/架构/消息队列/RocketMQ/Spring Cloud异步场景分布式事务怎样做？试试RocketMQ.note.md
related: [mysql-主从读写分离, elasticsearch-搜索, 知识库-全文检索规划, 缓存双写与一致性策略]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL Binlog 与 Canal 同步

> 主从复制 [[mysql-主从读写分离]]；ES 检索 [[知识库-全文检索规划]]。

## 1. Binlog 格式

| format | 说明 |
|--------|------|
| STATEMENT | SQL 语句，可能不一致 |
| ROW | 行变更（推荐） |
| MIXED | 混合 |

Canal 模拟 **MySQL slave**，订阅 master binlog。

## 2. Canal 典型链路

```
MySQL binlog → Canal Server → MQ/直接写 ES/Redis
```

用途：缓存失效、搜索引擎增量、异构同步。

## 4. 注意

- 表需有主键
- 延迟秒级；顺序性按 binlog 位点
- 与 [[cache/缓存双写与一致性策略]] 配合

## 相关

[[elasticsearch-写入调优]] · [[middleware/消息队列]]
