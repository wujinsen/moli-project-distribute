---
title: MySQL InnoDB 锁机制
slug: mysql-innodb锁机制
type: article
status: active
tags: [mysql, InnoDB, 行锁, MDL, 间隙锁]
sources:
- raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md
- raw/wujinsen_markdown/DataBase/mysql/全局锁和表锁 ：给表加个字段怎么有这么多阻碍？.note.md
related: [mysql-事务与锁, mysql-隔离级别与mvcc, mysql-死锁与排查, mysql-索引]
created: 2026-06-22
updated: 2026-07-05
---

# MySQL InnoDB 锁机制

> 枢纽 [[database/mysql-事务与锁]]；MVCC [[database/mysql-隔离级别与mvcc]]。

## 全局锁

`FLUSH TABLES WITH READ LOCK`（FTWRL）→ 整库只读。
逻辑备份：`mysqldump --single-transaction`（InnoDB RR 下一致性视图，不阻塞 DML）。

## 表级锁

| 锁 | 说明 |
|----|------|
| **表锁** | `LOCK TABLES t READ/WRITE` |
| **MDL** | 访问表自动加 MDL 读锁；**DDL 加写锁**，与读锁互斥 |

**加字段踩坑**：长事务占 MDL 读锁 → 后续 DDL 阻塞 → 若应用重试连接，线程暴涨。
处理：杀长事务、`ALTER TABLE ... ALGORITHM=INPLACE, LOCK=NONE`（视版本/操作为定）、MariaDB `NOWAIT/WAIT n`。

## 行锁（InnoDB）

| 锁 | 范围 |
|----|------|
| **Record Lock** | 锁定索引记录 |
| **Gap Lock** | 锁定间隙，不锁记录 |
| **Next-Key Lock** | Record + Gap（左开右闭） |

RR 下当前读常用 Next-Key 防幻读。无索引时可能**锁表/锁大量行** → 索引很重要，见 [[database/mysql-索引]]。

## 意向锁

表级 IS/IX，与行锁协调，快速判断表是否有行锁冲突。

## 与 Redis 锁对比

| | InnoDB 行锁 | Redis 分布式锁 |
|---|-------------|----------------|
| 范围 | 单库内 | 跨 JVM/跨服务 |
| 场景 | 事务内数据一致性 | 秒杀、定时任务互斥 |

见 [[cache/分布式锁]]、。

## 批次#1313 增补（wujinsen P2）

补充表锁/全局锁 raw。
