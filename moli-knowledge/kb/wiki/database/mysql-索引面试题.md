---
title: MySQL 索引（面试题系列）
slug: mysql-索引面试题
type: interview
status: active
tags: [mysql, 索引, 面试题, InnoDB, B+Tree]
sources:
 - raw/wujinsen_markdown/面试笔试/Database/【MySQL】20个经典面试题，全部答对月薪10k+.note.md
 - raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md
 - raw/wujinsen_markdown/DataBase/mysql/MySQL索引背后的数据结构及算法原理.note.md
 - raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md
related: [mysql-索引, b-plus树与-innodb索引结构, mysql-复合索引与最左前缀, mysql-覆盖索引与回表优化, mysql-索引失效场景]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL 索引（面试题系列）

> 概念枢纽 [[database/mysql-索引]]；原理 [[database/b-plus树与-innodb索引结构]]；文章 [[database/mysql-复合索引与最左前缀]] [[database/mysql-覆盖索引与回表优化]] [[database/mysql-索引失效场景]]。

## Q1. 索引是什么？为什么用 B+Tree 不用 Hash/二叉树？

索引是**帮助高效查数据的数据结构**。Hash 不支持范围；二叉树层高、磁盘 I/O 多；B+Tree 矮、叶子链表支持范围扫描，节点大小贴合磁盘页 → InnoDB 默认。见 [[database/b-plus树与-innodb索引结构]]。

## Q2. 聚簇索引和二级索引区别？什么是回表？

- **聚簇（主键）索引**：叶子存**整行**，表只有一个。
- **二级索引**：叶子存**主键值**。
- **回表**：二级索引查到主键后再查聚簇索引取完整行。

## Q3. 为什么推荐自增主键？

顺序插入，减少 B+Tree **页分裂**；主键短则二级索引叶子更小。业务唯一键（如身份证号）做主键会导致随机插入 + 索引膨胀。

## Q4. 什么是覆盖索引？EXPLAIN 怎么看？

查询所需列**全部在索引中**，无需回表。`Extra=Using index`。仅 B+Tree 可做覆盖。见 [[database/mysql-覆盖索引与回表优化]]。

## Q5. 联合索引最左前缀？

`(a,b,c)` 对 `a`、`a+b`、`a+b+c` 有效；单独 `b` 或 `b+c` 无效。条件列顺序优化器可能重排，但设计时仍让高频列靠左。见 [[database/mysql-复合索引与最左前缀]]。

## Q6. 哪些情况索引失效？

索引列上**函数/运算**、**隐式类型转换**、**LIKE '%x'**、**OR 跨列**、**违反最左前缀**、优化器判定全表更省。见 [[database/mysql-索引失效场景]]。

## Q7. EXPLAIN 关键字段？

| 字段 | 关注 |
|------|------|
| `type` | `ALL` 全表 < `index` < `range` < `ref` < `const` |
| `key` | 实际使用的索引 |
| `rows` | 预估扫描行数 |
| `Extra` | `Using index` 覆盖；`Using filesort` 额外排序 |

## Q8. MyISAM vs InnoDB 索引差异（常连带问）

| | MyISAM | InnoDB |
|---|--------|--------|
| 聚簇 | 无，索引存行指针 | 有，主键即数据 |
| 行锁 | 表锁为主 | 行锁 |
| 事务 | 不支持 | 支持 |

InnoDB 必有一个聚簇索引；MyISAM 索引与数据分离。

## Q9. 索引是不是越多越好？

否。每次 INSERT/UPDATE/DELETE 都要维护索引；写多读少表要克制。复合索引 `(a,b)` 存在时不必重复建 `a` 的单列索引（除非只有 `a` 的独立查询）。

## Q10. 如何优化慢 SQL（索引角度）？

1. `EXPLAIN` 定位全表扫与回表。
2. 按 WHERE/ORDER BY 建联合索引，争取覆盖。
3. 避免索引列运算与隐式转换。
4. 必要时 `SHOW PROFILE` / 慢查询日志（深入见 MySQL 优化专题，待 ingest）。
