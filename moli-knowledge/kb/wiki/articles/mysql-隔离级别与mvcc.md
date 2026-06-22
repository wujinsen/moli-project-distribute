---
title: MySQL 隔离级别与 MVCC
slug: mysql-隔离级别与mvcc
type: article
status: active
tags: [mysql, MVCC, 隔离级别, 快照读]
sources:
  - raw/wujinsen_markdown/DataBase/mysql/正确的理解MySQL的MVCC及实现原理.note.md
  - raw/wujinsen_markdown/DataBase/mysql/事务隔离级别中的可重复读能防幻读吗.note.md
  - raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md
related: [mysql-事务与锁, mysql-innodb锁机制, mysql-事务面试题]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL 隔离级别与 MVCC

> 枢纽 [[mysql-事务与锁]]；行锁细节 [[mysql-innodb锁机制]]。

## 当前读 vs 快照读

| 类型 | 语句示例 | 行为 |
|------|----------|------|
| **当前读** | `SELECT ... FOR UPDATE/LOCK IN SHARE MODE`、`UPDATE/DELETE` | 读最新版本并**加锁** |
| **快照读** | 普通 `SELECT`（非串行化） | 读 **Read View** 可见的历史版本，不加锁 |

MVCC 主要优化**快照读**的读-写并发。

## MVCC 三要素（InnoDB）

1. **隐藏列**：`DB_TRX_ID`（最后修改事务 id）、`DB_ROLL_PTR`（undo 链）、`DB_ROW_ID`
2. **undo log**：旧版本链，支持回滚与快照
3. **Read View**：事务开始时（RR）或语句开始时（RC）生成，判断版本可见性

## RR 能防幻读吗？（MySQL InnoDB 结论）

- **快照读**：RR 下同一事务多次 SELECT，Read View 不变 → **看不到**其他事务新插入行（防幻读）
- **当前读**：若只用 `SELECT ... FOR UPDATE` 等，仍可能需 **Next-Key Lock（间隙锁）** 防幻读

教科书「RR 不能防幻读」多指标准 SQL 或当前读场景；**InnoDB 普通 SELECT 在 RR 下可防幻读**。

## RC vs RR 快照读

| | READ COMMITTED | REPEATABLE READ |
|---|----------------|-----------------|
| Read View | 每条语句新建 | 事务第一次快照读时创建，之后复用 |
| 不可重复读 | 可能 | 快照读不会 |

## binlog 与隔离

主从复制下，RC 可能导致语句级复制与行级复制语义差异；生产常保持 **RR**（与 MySQL 默认一致）。

## 面试一句话

> MVCC = 多版本 + Read View 无锁快照读；当前读走锁；InnoDB RR 快照读可防幻读，写/锁读靠 Next-Key。
