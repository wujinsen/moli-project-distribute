---
title: MySQL 事务与锁
slug: mysql-事务与锁
type: concept
status: active
tags: [mysql, 事务, 锁, InnoDB, MVCC]
sources:
- raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md
- raw/wujinsen_markdown/DataBase/mysql/全局锁和表锁 ：给表加个字段怎么有这么多阻碍？.note.md
- raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md
related: [mysql-隔离级别与mvcc, mysql-innodb锁机制, mysql-死锁与排查, mysql-深分页与慢sql优化, mysql-事务面试题, mysql-索引, spring-声明式事务]
created: 2026-06-22
updated: 2026-07-05
---

# MySQL 事务与锁（概念枢纽）

> 隔离与 MVCC [[database/mysql-隔离级别与mvcc]]；InnoDB 行锁 [[database/mysql-innodb锁机制]]；死锁 [[database/mysql-死锁与排查]]；慢 SQL [[database/mysql-深分页与慢sql优化]]；面试 [[database/mysql-事务面试题]]。索引见 [[database/mysql-索引]]；应用层事务见 [[spring/spring-声明式事务]]。

InnoDB 默认引擎，**支持事务 + 行级锁 + MVCC**。目标系统 MySQL 8.0 + Druid，订单/用户写操作既靠 Spring `@Transactional`，也受数据库隔离级别与锁行为约束。

## ACID（复习）

| 特性 | 含义 |
|------|------|
| **A 原子性** | undo log 回滚 |
| **C 一致性** | 业务+约束，非隔离级别单独保证 |
| **I 隔离性** | 并发事务互不干扰（程度由级别决定） |
| **D 持久性** | redo log 刷盘 |

## 并发问题三件套

| 问题 | 含义 |
|------|------|
| **脏读** | 读到未提交数据 |
| **不可重复读** | 同事务两次读同一行，值变了（update） |
| **幻读** | 两次范围读，行数变了（insert/delete） |

## MySQL 四级隔离（默认 RR）

| 级别 | 脏读 | 不可重复读 | 幻读 |
|------|------|------------|------|
| READ UNCOMMITTED | ✓ | ✓ | ✓ |
| READ COMMITTED | ✗ | ✓ | ✓ |
| **REPEATABLE READ**（默认） | ✗ | ✗ | 快照读基本防；当前读靠间隙锁 |
| SERIALIZABLE | ✗ | ✗ | ✗ |

查看/设置：`SELECT @@transaction_isolation;`（8.0） / `SET SESSION TRANSACTION ISOLATION LEVEL ...`（须在开启事务前）。

## 锁的层次（鸟瞰）

| 级别 | 典型场景 |
|------|----------|
| **全局锁** | FTWRL 全库只读；备份可用 `--single-transaction` |
| **表锁** | `LOCK TABLES`；**MDL** 保护 DDL |
| **行锁** | InnoDB DML：`Record / Gap / Next-Key` |

分布式互斥跨 JVM 用 [[cache/分布式锁]]，不是表锁替代。
