---
title: MySQL 事务与锁（面试题系列）
slug: mysql-事务面试题
type: interview
status: active
tags: [mysql, 事务, 锁, 面试题, MVCC]
sources:
 - raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md
 - raw/wujinsen_markdown/DataBase/mysql/事务隔离级别中的可重复读能防幻读吗.note.md
 - raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md
related: [mysql-事务与锁, mysql-隔离级别与mvcc, mysql-innodb锁机制, mysql-死锁与排查, spring-事务]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL 事务与锁（面试题系列）

> [[database/mysql-事务与锁]] [[database/mysql-隔离级别与mvcc]] [[database/mysql-innodb锁机制]] [[database/mysql-死锁与排查]]；Spring 层 [[spring/spring-事务]] [[spring/spring-声明式事务]]。

## Q1. ACID 分别是什么？

原子 undo、一致业务约束、隔离并发、持久 redo。见 [[database/mysql-事务与锁]]。

## Q2. 四种隔离级别与默认？

RU / RC / **RR** / Serializable；MySQL InnoDB 默认 **RR**。

## Q3. 脏读、不可重复读、幻读区别？

脏读=未提交；不可重复读=同一行 update；幻读=范围 insert/delete 导致行数变化。

## Q4. MVCC 是什么？

多版本 + Read View + undo；快照读不加锁。见 [[database/mysql-隔离级别与mvcc]]。

## Q5. RR 能防幻读吗？

InnoDB：**快照读**能；**当前读**靠 Next-Key Lock。

## Q6. 当前读和快照读？

FOR UPDATE/UPDATE 是当前读；普通 SELECT 是快照读（非 Serializable）。

## Q7. 什么是 MDL 锁？

表元数据锁；DDL 写锁 vs DML 读锁；长事务阻塞加字段。

## Q8. Record / Gap / Next-Key？

行锁 / 间隙锁 / 行+间隙（RR 防幻读）。

## Q9. 死锁怎么产生？怎么办？

加锁顺序不一致；InnoDB 选一方回滚；应用捕获 1213 重试；固定加锁顺序。见 [[database/mysql-死锁与排查]]。

## Q10. MySQL 与 Spring 事务关系？

Spring 传播+回滚规则是应用层；底层仍是一条 JDBC 连接上的 DB 事务；隔离级别由 DB 决定。
