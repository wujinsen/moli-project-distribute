---
title: B+Tree 与 InnoDB 索引结构
slug: b-plus树与-innodb索引结构
type: concept
status: active
tags: [mysql, B+Tree, InnoDB, 聚簇索引, 数据结构]
sources:
- raw/wujinsen_markdown/DataBase/mysql/MySQL索引背后的数据结构及算法原理.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/梳理了一遍MySQL索引，发现也不过如此.note.md
- raw/wujinsen_markdown/面试笔试/Database/mysql/B树与B+树.note.md
- raw/wujinsen_markdown/面试笔试/树/B+树介绍.note.md
- raw/wujinsen_markdown/面试笔试/树/B树和B+树的总结.note.md
related: [mysql-索引, mysql-覆盖索引与回表优化, mysql-索引面试题]
created: 2026-06-22
updated: 2026-07-05
---

# B+Tree 与 InnoDB 索引结构

> 枢纽页 [[database/mysql-索引]]；覆盖索引如何减少回表见 [[database/mysql-覆盖索引与回表优化]]。

## 为什么不用二叉树 / 红黑树？

索引在磁盘上，查找代价主要是 **磁盘 I/O 次数**。树越高，I/O 越多。二叉树容易退化成链表；红黑树层高仍偏高。**B+Tree 矮胖**：一个节点存大量 key，3～4 层即可覆盖千万级行（InnoDB 页默认 16KB）。

## B+Tree vs B-Tree（考点）

| 特性 | B-Tree | B+Tree（MySQL 用） |
|------|--------|---------------------|
| 非叶节点 | 也存 data | **只存 key**，不存行数据 |
| 叶节点 | 存 data | 存 data + **双向链表** |
| 范围查询 | 需中序遍历 | 沿叶子链表扫描，极快 |

**顺序访问指针**：叶子节点链成链表，`WHERE id BETWEEN 18 AND 49` 找到起点后顺链扫即可。

## InnoDB 索引组织表

InnoDB 表**按主键顺序**以 B+Tree 存放 → **索引即数据**（Index Organized Table）。

建表示例（来自原文）：

```sql
CREATE TABLE T (
 id INT PRIMARY KEY,
 k INT NOT NULL,
 name VARCHAR(16),
 INDEX (k)
) ENGINE=InnoDB;
```

- **主键索引**：叶子 = 整行 `(id, k, name, …)`
- **k 上的二级索引**：叶子 = `(k值, id)`，查 `WHERE k=5` 得 `id=500` 后**回表**查主键树

## 聚簇 vs 非聚簇

- **聚簇索引**：数据行物理顺序与索引顺序一致；InnoDB **一张表只有一个**（主键）。
- **非聚簇**：索引顺序与数据物理顺序无关；MyISAM 索引存行指针，InnoDB 二级索引存主键。

无显式主键时 InnoDB 会选：唯一非空列 → 否则隐式 6 字节 row_id。

## 页分裂与自增主键

B+Tree 插入需保持有序。向已满页中间插入 → **页分裂**，空间利用率下降、性能抖动。

**自增主键**（`AUTO_INCREMENT`）总是追加到最大页，几乎不分裂；用 UUID/身份证号等业务主键可能随机插入，分裂更频繁。主键还影响二级索引大小（叶子存主键副本）→ 宜短，见 [[database/mysql-索引]]。

## 磁盘 I/O 与 B+Tree 选型（一句话）

数据库按**页**（通常 16KB）读写磁盘。B+Tree 节点大小 ≈ 页大小，一次 I/O 读一整节点，最大化单次 I/O 的 key 数量 → 降低树高与 I/O 次数。
## B 树 vs B+ 树（raw 面试笔试/树）

| | B 树 | B+ 树 |
|---|------|-------|
| 数据存储 | 内部节点也可存数据 | **只在叶子存数据** |
| 叶子链表 | 无 | **有**，范围扫描友好 |
| InnoDB | — | **聚簇索引默认 B+** |

见 [[database/mysql-索引面试题]] Q1。

## 批次#1312 增补（wujinsen P1）

合并 B/B+ 树面试 raw。
