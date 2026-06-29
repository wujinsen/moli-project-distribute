---
title: MySQL 深分页与慢 SQL 优化
slug: mysql-深分页与慢sql优化
type: article
status: active
tags: [mysql, 慢sql, 分页, 优化]
sources:
 - raw/wujinsen_markdown/DataBase/mysql/索引/一次 SQL 查询优化原理分析（900W+ 数据，从 17s 到 300ms）.note.md
 - raw/wujinsen_markdown/DataBase/mysql/优化/解决mysql占用cpu高的问题.note.md
related: [mysql-索引, mysql-覆盖索引与回表优化, mysql-事务与锁, 故障排查指南]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL 深分页与慢 SQL 优化

> 索引基础 [[mysql-索引]]；覆盖索引 [[mysql-覆盖索引与回表优化]]。

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
| 函数/on 列 | 改写 SQL，见 [[mysql-索引失效场景]] |
| 大事务 | 拆批、缩短锁持有 |
| CPU 高 | 慢 SQL、锁竞争、buffer pool 不足 |

## 面试一句话

> 深分页用「子查询主键 + JOIN」或「上次最大 id 游标」；先 EXPLAIN 再改 SQL/索引。
