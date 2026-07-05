---
title: MySQL 索引（面试题系列）
slug: mysql-索引面试题
type: interview
status: active
tags: [mysql, 索引, 面试题, InnoDB, B+Tree]
sources:
- raw/wujinsen_markdown/DataBase/MySQL外键设置中的的 Cascade、NO ACTION、Restrict、SET NULL.note.md
- raw/wujinsen_markdown/DataBase/left join on 和where条件的放置.note.md
- raw/wujinsen_markdown/DataBase/mysql/MySQL 与 Redis 缓存的同步方案.note.md
- raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md
- raw/wujinsen_markdown/DataBase/mysql/MySQL的binlog日志.note.md
- raw/wujinsen_markdown/DataBase/mysql/MySQL索引背后的数据结构及算法原理.note.md
- raw/wujinsen_markdown/DataBase/mysql/left join加上where条件的困惑，（left join 无用  无效 ）.note.md
- raw/wujinsen_markdown/DataBase/mysql/mysql 同一张表查询 left join.note.md
- raw/wujinsen_markdown/DataBase/mysql/mysql8配置文件 .note.md
- raw/wujinsen_markdown/DataBase/mysql/mysql下删除mysql-bin文件.note.md
- raw/wujinsen_markdown/DataBase/mysql/mysql定时备份数据脚本.note.md
- raw/wujinsen_markdown/DataBase/mysql/mysql规范.note.md
- raw/wujinsen_markdown/DataBase/mysql/两个表join 连接，去掉重复的数据.note.md
- raw/wujinsen_markdown/DataBase/mysql/事务隔离级别中的可重复读能防幻读吗.note.md
- raw/wujinsen_markdown/DataBase/mysql/优化/Mysql5.7配置文件my.cnf优化.note.md
- raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md
- raw/wujinsen_markdown/DataBase/mysql/全局锁和表锁 ：给表加个字段怎么有这么多阻碍？.note.md
- raw/wujinsen_markdown/DataBase/mysql/分库分表/互联网公司为啥不使用mysql分区表？.note.md
- raw/wujinsen_markdown/DataBase/mysql/备份/MySQL数据库备份脚本.note.md
- raw/wujinsen_markdown/DataBase/mysql/安装/Linux安装MySQL5.7.note.md
- raw/wujinsen_markdown/DataBase/mysql/安装/Linux安装Mysql8.note.md
- raw/wujinsen_markdown/DataBase/mysql/安装/MySQL5.6安装步骤.note.md
- raw/wujinsen_markdown/DataBase/mysql/安装/Mysql8创建用户并授权.note.md
- raw/wujinsen_markdown/DataBase/mysql/安装/mac 在终端如何进入名称带空格的目录.note.md
- raw/wujinsen_markdown/DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md
- raw/wujinsen_markdown/DataBase/mysql/正确的理解MySQL的MVCC及实现原理.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/MySQL索引索引不生效的情况.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/mysql-覆盖索引.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/mysql索引命中规则.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/二叉树，B树，B+树.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/复合索引的优点和注意事项.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md
- raw/wujinsen_markdown/DataBase/mysql/读写分离/mysql+mycat搭建稳定高可用集群，负载均衡，主备赋值，读写分离.note.md
- raw/wujinsen_markdown/DataBase/mysql/采坑/MySql的时区（serverTimezone）引发的血案.note.md
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
- raw/wujinsen_markdown/面试笔试/Database/mysql/B树与B+树.note.md
- raw/wujinsen_markdown/面试笔试/Database/mysql/MySQL InnoDB 行记录格式（ROW_FORMAT）.note.md
- raw/wujinsen_markdown/面试笔试/Database/【MySQL】20个经典面试题，全部答对月薪10k+.note.md
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之综合篇.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/MySQL数据库MyISAM和InnoDB存储引擎的比较.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md
related: [mysql-索引, b-plus树与-innodb索引结构, mysql-复合索引与最左前缀, mysql-覆盖索引与回表优化, mysql-索引失效场景]
created: 2026-06-22
updated: 2026-07-05
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
## Q11. InnoDB 行格式 ROW_FORMAT 影响什么？

COMPACT/DYNAMIC 影响溢出列存储与索引记录大小；大 VARCHAR 可能 off-page 存储，影响二级索引叶子大小。

## Q12. 如何判断索引是否被使用？

`EXPLAIN` 的 `key`/`rows`/`Extra`；`SHOW INDEX FROM t` 看 Cardinality；慢日志 + `pt-query-digest` 验证。
## 批次#1310 增补（wujinsen P0）

合并 `DataBase/mysql/` 索引子目录 + 面试笔试 Database/树/B+树 raw。索引命中规则、900W 优化案例、ROW_FORMAT 见 [[database/mysql-索引失效场景]]。

## 批次#1322 增补（wujinsen Phase2 王树挂接）

合并 `大数据资料-王/mysql/` 安装与调优 raw。

原文插图 annex：[[database/annex-mysql-覆盖索引]]

原文插图 annex：[[database/annex-正确的理解MySQL的MVCC及实现原理]]

原文插图 annex：[[database/annex-事务隔离级别中的可重复读能防幻读吗]]

原文插图 annex：[[database/annex-MySQL索引背后的数据结构及算法原理]]

原文插图 annex：[[database/annex-mysql规范]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 9 组

> 图源 `raw/wujinsen_markdown/DataBase/mysql/索引/复合索引的优点和注意事项.note.md` · T22 **B** 档

### 来自：复合索引的优点和注意事项

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E5%A4%8D%E5%90%88%E7%B4%A2%E5%BC%95%E7%9A%84%E4%BC%98%E7%82%B9%E5%92%8C%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/mysql/数据仓库中的SQL性能优化（Hive篇）.note.md` · T22 **B** 档

### 来自：数据仓库中的SQL性能优化（Hive篇）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/mysql/%E6%95%B0%E6%8D%AE%E4%BB%93%E5%BA%93%E4%B8%AD%E7%9A%84SQL%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96%EF%BC%88Hive%E7%AF%87%EF%BC%89.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md` · T22 **B** 档

### 来自：复合索引的优点和注意事项

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%9D%A2%E8%AF%95%E9%A2%98%E6%95%B4%E7%90%86/%E5%A4%8D%E5%90%88%E7%B4%A2%E5%BC%95%E7%9A%84%E4%BC%98%E7%82%B9%E5%92%8C%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md` · T22 **B** 档

### 来自：解决mysql占用cpu高的问题

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E4%BC%98%E5%8C%96/%E8%A7%A3%E5%86%B3mysql%E5%8D%A0%E7%94%A8cpu%E9%AB%98%E7%9A%84%E9%97%AE%E9%A2%98.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E4%BC%98%E5%8C%96/%E8%A7%A3%E5%86%B3mysql%E5%8D%A0%E7%94%A8cpu%E9%AB%98%E7%9A%84%E9%97%AE%E9%A2%98.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/DataBase/mysql/索引/mysql索引命中规则.note.md` · T22 **B** 档

### 来自：mysql索引命中规则

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/mysql%E7%B4%A2%E5%BC%95%E5%91%BD%E4%B8%AD%E8%A7%84%E5%88%99.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/mysql%E7%B4%A2%E5%BC%95%E5%91%BD%E4%B8%AD%E8%A7%84%E5%88%99.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/mysql/c3p0源码分析.note.md` · T22 **B** 档

### 来自：c3p0源码分析

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/mysql/c3p0%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/mysql/c3p0%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/面试笔试/Database/mysql/B树与B+树.note.md` · T22 **B** 档

### 来自：B树与B+树

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Database/mysql/B%E6%A0%91%E4%B8%8EB%2B%E6%A0%91.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Database/mysql/B%E6%A0%91%E4%B8%8EB%2B%E6%A0%91.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/DataBase/mysql/索引/二叉树，B树，B+树.note.md` · T22 **B** 档

### 来自：二叉树，B树，B+树

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%BA%8C%E5%8F%89%E6%A0%91%EF%BC%8CB%E6%A0%91%EF%BC%8CB%2B%E6%A0%91.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%BA%8C%E5%8F%89%E6%A0%91%EF%BC%8CB%E6%A0%91%EF%BC%8CB%2B%E6%A0%91.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%BA%8C%E5%8F%89%E6%A0%91%EF%BC%8CB%E6%A0%91%EF%BC%8CB%2B%E6%A0%91.note_images/imageFile3.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/a安装文档/MySQL主从安装文档（ok）.note.md` · T22 **B** 档

### 来自：MySQL主从安装文档（ok）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/a%E5%AE%89%E8%A3%85%E6%96%87%E6%A1%A3/MySQL%E4%B8%BB%E4%BB%8E%E5%AE%89%E8%A3%85%E6%96%87%E6%A1%A3%EF%BC%88ok%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/a%E5%AE%89%E8%A3%85%E6%96%87%E6%A1%A3/MySQL%E4%B8%BB%E4%BB%8E%E5%AE%89%E8%A3%85%E6%96%87%E6%A1%A3%EF%BC%88ok%EF%BC%89.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/a%E5%AE%89%E8%A3%85%E6%96%87%E6%A1%A3/MySQL%E4%B8%BB%E4%BB%8E%E5%AE%89%E8%A3%85%E6%96%87%E6%A1%A3%EF%BC%88ok%EF%BC%89.note_images/imageFile3.png)

原文插图 annex：[[database/annex-mysql-覆盖索引]]

原文插图 annex：[[database/annex-正确的理解MySQL的MVCC及实现原理]]

原文插图 annex：[[database/annex-事务隔离级别中的可重复读能防幻读吗]]

原文插图 annex：[[database/annex-MySQL索引背后的数据结构及算法原理]]

原文插图 annex：[[database/annex-mysql规范]]

原文插图 annex：[[database/annex-MySql的时区（serverTimezone）引发的血案]]

原文插图 annex：[[database/annex-MySQL死锁案例，我一口气说了6个]]

原文插图 annex：[[database/annex-数据库事务的四大特性以及事务的隔离级别]]

原文插图 annex：[[database/annex-梳理了一遍MySQL索引，发现也不过如此]]

原文插图 annex：[[database/annex-MySQL索引索引不生效的情况]]

原文插图 annex：[[database/annex-面试小结之综合篇]]

原文插图 annex：[[database/annex-MySQL5.6安装步骤]]

原文插图 annex：[[database/annex-Amoeba搞定mysql主从读写分离]]

原文插图 annex：[[database/annex-MySQL-与-Redis-缓存的同步方案]]

原文插图 annex：[[database/annex-MySQL-InnoDB-行记录格式（ROW_FORMAT）]]

原文插图 annex：[[database/annex-mysql-同一张表查询-left-join]]

原文插图 annex：[[database/annex-全局锁和表锁-：给表加个字段怎么有这么多阻碍？]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md` · T22 **B** 档

### 来自：一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%B8%80%E6%AC%A1%20SQL%20%E6%9F%A5%E8%AF%A2%E4%BC%98%E5%8C%96%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90%EF%BC%88900W%2B%20%E6%95%B0%E6%8D%AE%EF%BC%8C%E4%BB%8E%2017s%20%E5%88%B0%20300ms%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%B8%80%E6%AC%A1%20SQL%20%E6%9F%A5%E8%AF%A2%E4%BC%98%E5%8C%96%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90%EF%BC%88900W%2B%20%E6%95%B0%E6%8D%AE%EF%BC%8C%E4%BB%8E%2017s%20%E5%88%B0%20300ms%EF%BC%89.note_images/imageFile2.png)
