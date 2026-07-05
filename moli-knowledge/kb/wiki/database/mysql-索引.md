---
title: MySQL 索引
slug: mysql-索引
type: concept
status: active
tags: [mysql, 索引, 性能优化, InnoDB, B+Tree]
sources:
- raw/wujinsen_markdown/DataBase/mysql/MySQL索引背后的数据结构及算法原理.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/MySQL索引索引不生效的情况.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/mysql-覆盖索引.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/mysql索引命中规则.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/二叉树，B树，B+树.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/复合索引的优点和注意事项.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/MySQL主从安装文档（ok）.note.attach/Amoeba搞定mysql主从读写分离.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/MySQL主从安装文档（ok）.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/mysql cluster集群安装.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/mysql集群安装--参考网上.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/MySql常用命令总结.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/Oracle JDBC连接oracle.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/QueryRun  DBUtils  UUID.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/c3p0-config.xml.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/c3p0源码分析.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/jdbc注册、链接、连接池.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/mysql cluster集群安装.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/mysql explain 详解.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/mysql引擎.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/mysql高速缓冲.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/oracle(1).note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/oracle常用经典sql查询.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/powerDesinger使用手册.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/sql 查询结果if else.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/sql优化方法 .note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/常用SQL语句大全.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/数据仓库中的SQL性能优化（Hive篇）.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/索引.note.md
- raw/wujinsen_markdown/大数据资料-王/mysql/详解MySQL Cluster管理结点的config.ini配置文件.note.md
- raw/wujinsen_markdown/性能优化/DATABASE/mysql left join 慢如何优化.note.md
- raw/wujinsen_markdown/性能优化/DATABASE/总结   慢 SQL 问题经验总结！.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md
related: [b-plus树与-innodb索引结构, mysql-复合索引与最左前缀, mysql-覆盖索引与回表优化, mysql-索引失效场景, mysql-索引面试题, mysql-事务与锁]
created: 2026-06-22
updated: 2026-07-05
---

# MySQL 索引（概念枢纽）

> 本页是「MySQL 索引」主题枢纽。结构原理见 [[database/b-plus树与-innodb索引结构]]；复合索引与最左前缀见 [[database/mysql-复合索引与最左前缀]]；覆盖索引与回表见 [[database/mysql-覆盖索引与回表优化]]；失效场景见 [[database/mysql-索引失效场景]]；面试速记见 [[database/mysql-索引面试题]]；事务与锁见 [[database/mysql-事务与锁]]。
> 由 `DataBase/mysql/索引/` 簇 7 篇 + 面试题整理 2 篇 + B+Tree 原理 1 篇去重提炼（与「分布式锁」批次同范式）。

## 索引是什么？

**索引是帮助 MySQL 高效获取数据的数据结构**（官方定义提炼）。数据库在数据之外额外维护一棵（或多棵）查找结构，以空间换时间，避免全表扫描。

InnoDB 默认用 **B+Tree** 实现索引；Memory 引擎可用 Hash。日常 OLTP 几乎只谈 B+Tree。

## 三种常见索引模型（对比记忆）

| 模型 | 等值查询 | 范围查询 | 更新代价 | 典型场景 |
|------|----------|----------|----------|----------|
| **Hash** | O(1) 快 | ❌ 不支持 | 低 | Memcached、Memory 引擎 |
| **有序数组** | O(log N) | ✅ 快 | 插入要挪动，高 | 静态数据、极少写 |
| **B+Tree** | O(log N) | ✅ 快（叶子链表） | 中等，页分裂/合并 | **InnoDB 默认** |

## InnoDB 两类索引（必背）

| 类型 | 别名 | 叶子节点存什么 | 表中有几个 |
|------|------|----------------|------------|
| **主键索引** | 聚簇索引 clustered | **整行数据** | 1 个 |
| **二级索引** | 非聚簇 / secondary | **主键值**（书签） | 可多个 |

**回表**：用二级索引查到主键后，再回聚簇索引取完整行 → 多扫一棵 B+Tree。优化方向见 [[database/mysql-覆盖索引与回表优化]]。

## 建索引的原则（精炼）

1. **WHERE / ORDER BY / GROUP BY 高频列**优先；联合索引列顺序与查询条件一致（最左前缀，见 [[database/mysql-复合索引与最左前缀]]）。
2. **主键尽量短**：二级索引叶子都存主键，主键越长索引越大。
3. **自增主键**通常更优：顺序插入，减少页分裂（见 [[database/b-plus树与-innodb索引结构]]）。
4. **能窄不宽**：复合索引 `(a,b)` 存在时，往往不必再单列索引 `a`；但别堆 5～6 列宽索引。
5. **索引不是越多越好**：每次写都要维护索引，DML 变慢。

## 排查工具

- **`EXPLAIN`**：看 `type`、`key`、`rows`、`Extra`（`Using index` = 覆盖索引）。
- **避免在索引列上运算/函数**（否则优化器可能放弃索引，见 [[database/mysql-索引失效场景]]）。

## 与本项目的关系

目标系统 MySQL 8.0 + Druid 连接池（见 ）。用户中心、订单、知识库等服务的慢 SQL 排查，索引设计是第一步；秒杀场景 Redis 扛热点，但订单落库仍依赖合理索引。

## 批次#1312 增补（wujinsen P1）

合并 `DataBase/mysql/索引/` 七篇索引原理 raw。

## 批次#1324 增补（wujinsen Phase2 长尾）

合并性能优化 DATABASE raw。
