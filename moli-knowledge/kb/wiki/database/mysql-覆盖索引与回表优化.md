---
title: MySQL 覆盖索引与回表优化
slug: mysql-覆盖索引与回表优化
type: article
status: active
tags: [mysql, 索引, 覆盖索引, 回表, EXPLAIN]
sources:
- raw/wujinsen_markdown/DataBase/mysql/索引/mysql-覆盖索引.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md
related: [mysql-索引, b-plus树与-innodb索引结构, mysql-复合索引与最左前缀, mysql-索引面试题]
created: 2026-06-22
updated: 2026-07-05
---

# MySQL 覆盖索引与回表优化

> 结构背景 [[database/b-plus树与-innodb索引结构]]；枢纽 [[database/mysql-索引]]。

## 回表

二级索引叶子只存**主键值**。查询 `SELECT * FROM T WHERE k=5` 时：

1. 在 `k` 索引树找到 `k=5` → 得到 `id=500`
2. 再到**主键索引树**取完整行 → **回表**（多一次树查找）

行越宽、回表越多，代价越大。

## 覆盖索引

**索引已包含查询所需的全部列**，无需回表读数据页。

```sql
-- 假设 INDEX(k, id)
SELECT id FROM T WHERE k = 5; -- 可能覆盖：k 索引叶子上已有 id
SELECT k, id FROM T WHERE k = 5; -- 若索引 (k,id) 则覆盖
SELECT * FROM T WHERE k = 5; -- 需要 name 等列 → 必须回表
```

`EXPLAIN` 的 `Extra` 出现 **`Using index`** 即表示覆盖索引扫描。

## 仅 Hash/全文/空间索引不能做覆盖

覆盖索引要求索引结构**存储索引列的值**；InnoDB 只有 **B+Tree** 满足，故覆盖索引讨论限定 B+Tree。

## 典型优化场景

### 1. 计数只扫索引

```sql
SELECT COUNT(staff_id) FROM t1;
-- 对 staff_id 建索引后，type=index, Extra=Using index
-- 只数索引条目，不读堆表
```

### 2. 避免 SELECT *

业务列表页只查展示字段，让所需列落在联合索引内 → 减少回表与 IO。

### 3. 联合索引列顺序

若常查 `WHERE a=?` 且只 `SELECT b,c`，可考虑 `INDEX(a,b,c)` 实现覆盖（仍受最左前缀约束，见 [[database/mysql-复合索引与最左前缀]]）。

## 索引下推 ICP（补充）

MySQL 5.6+ **Index Condition Pushdown**：联合索引中，即使无法覆盖全部 WHERE，也可在存储引擎层先过滤索引列，再回表，减少回表行数（面试加分项，见 [[database/mysql-索引面试题]]）。

## 权衡

- 覆盖索引靠**更宽的联合索引**，写放大、占空间。
- 不要为覆盖而 `SELECT *`；明确列 + 合理联合索引才是正道。
