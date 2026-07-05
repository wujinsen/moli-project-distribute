---
title: MySQL 深分页与慢 SQL 优化
slug: mysql-深分页与慢sql优化
type: article
status: active
tags: [mysql, 慢sql, 分页, 优化]
sources:
- raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md
related: [mysql-索引, mysql-覆盖索引与回表优化, mysql-事务与锁]
created: 2026-06-22
updated: 2026-07-05
---

# MySQL 深分页与慢 SQL 优化

> 索引基础 [[database/mysql-索引]]；覆盖索引 [[database/mysql-覆盖索引与回表优化]]。

## 慢 SQL 排查顺序

1. **慢查询日志** / Druid 监控 SQL 面板
2. **`EXPLAIN`** — type、key、rows、Extra
3. 是否 **深分页**、**回表**、**filesort**、锁等待

## 深分页：`LIMIT offset, n`

`LIMIT 300000, 10` 需扫描/排序丢弃前 30 万行 → 越翻越慢。

### 优化：延迟关联（子查询只查主键）

```sql
-- 优化前
SELECT * FROM t WHERE val = 4 LIMIT 300000, 10;

-- 优化后：内层 LIMIT 只走覆盖索引取 id，再 JOIN 回表
SELECT a.*
FROM t AS a
INNER JOIN (
 SELECT id FROM t WHERE val = 4 LIMIT 300000, 10
) AS b ON a.id = b.id;
```

原理：内层若 `(val, id)` 索引覆盖，只扫索引；外层 10 次回表。

### 更优：游标分页

`WHERE id > last_id ORDER BY id LIMIT 10` — 业务列表推荐，无 offset。

## 其他常见优化

| 问题 | 手段 |
|------|------|
| SELECT * | 改列 + 覆盖索引 |
| 函数/on 列 | 改写 SQL，见 [[database/mysql-索引失效场景]] |
| 大事务 | 拆批、缩短锁持有 |
| CPU 高 | 慢 SQL、锁竞争、buffer pool 不足 |

## 面试一句话

> 深分页用「子查询主键 + JOIN」或「上次最大 id 游标」；先 EXPLAIN 再改 SQL/索引。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md` · T22 **B** 档

### 来自：解决mysql占用cpu高的问题

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E4%BC%98%E5%8C%96/%E8%A7%A3%E5%86%B3mysql%E5%8D%A0%E7%94%A8cpu%E9%AB%98%E7%9A%84%E9%97%AE%E9%A2%98.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E4%BC%98%E5%8C%96/%E8%A7%A3%E5%86%B3mysql%E5%8D%A0%E7%94%A8cpu%E9%AB%98%E7%9A%84%E9%97%AE%E9%A2%98.note_images/imageFile2.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md` · T22 **B** 档

### 来自：一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%B8%80%E6%AC%A1%20SQL%20%E6%9F%A5%E8%AF%A2%E4%BC%98%E5%8C%96%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90%EF%BC%88900W%2B%20%E6%95%B0%E6%8D%AE%EF%BC%8C%E4%BB%8E%2017s%20%E5%88%B0%20300ms%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/DataBase/mysql/%E7%B4%A2%E5%BC%95/%E4%B8%80%E6%AC%A1%20SQL%20%E6%9F%A5%E8%AF%A2%E4%BC%98%E5%8C%96%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90%EF%BC%88900W%2B%20%E6%95%B0%E6%8D%AE%EF%BC%8C%E4%BB%8E%2017s%20%E5%88%B0%20300ms%EF%BC%89.note_images/imageFile2.png)
