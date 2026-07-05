---
title: MySQL 索引失效场景
slug: mysql-索引失效场景
type: article
status: active
tags: [mysql, 索引, 优化, EXPLAIN, 踩坑]
sources:
- raw/wujinsen_markdown/DataBase/mysql/索引/MySQL索引索引不生效的情况.note.md
- raw/wujinsen_markdown/DataBase/mysql/索引/mysql索引命中规则.note.md
related: [mysql-索引, mysql-复合索引与最左前缀, mysql-索引面试题]
created: 2026-06-22
updated: 2026-07-05
---

# MySQL 索引失效场景

> 枢纽 [[database/mysql-索引]]；最左前缀 [[database/mysql-复合索引与最左前缀]]。

MySQL 使用**基于成本的优化器**，以下情况常导致不走索引或索引部分失效。

## 1. 对索引列运算 / 函数

```sql
-- ❌ id 列上有运算，索引失效
SELECT * FROM t WHERE id - 1 = 1;
SELECT * FROM t WHERE id + client_type = 1;

-- ✅ 写成无运算形式
SELECT * FROM t WHERE id = 2;
```

**原则**：把运算放在**常量侧**，索引列保持「裸列」。

## 2. 隐式类型转换

字符串列与数字比较时，MySQL 可能把**列**转成数字 → 索引失效。

```sql
-- security_code 是 VARCHAR
SELECT * FROM t WHERE security_code = 688688; -- ❌ 可能全表扫
SELECT * FROM t WHERE security_code = '688688'; -- ✅
```

## 3. LIKE 前缀通配

| 模式 | 索引 |
|------|------|
| `'abc%'` | ✅ 可用（前缀匹配） |
| `'%abc'` / `'%abc%'` | ❌ 通常全表扫 |

高频模糊搜考虑 [[search/elasticsearch-搜索]]（知识库/BI 扩展方向）。

## 4. OR 条件

- 同一列 OR：可能用索引（看成本估算）。
- **不同列 OR**：常导致全表扫描；若一列无索引则必全表。
- 可改写为 `UNION ALL` 分别走索引再合并。

## 5. 联合索引：违反最左前缀

```sql
-- INDEX(a,b,c)
WHERE b=1 AND c=2; -- ❌ 缺 a
```

详见 [[database/mysql-复合索引与最左前缀]]。

## 6. ORDER BY / GROUP BY 无合适索引

`ORDER BY` / `GROUP BY` 代价高；若 WHERE 无索引而排序列有索引，优化器可能「为了排序而扫索引但 rows 仍很大」。**WHERE + ORDER BY 列一起设计联合索引**。

## 7. 优化器认为全表更快

小表、返回行数占比高时，优化器可能主动放弃索引 → `type=ALL` 不一定是 bug，用 `EXPLAIN` + 实际耗时判断。

## 排查习惯

1. `EXPLAIN` 看 `type`（`ref`/`range`/`index`/`ALL`）、`key`、`rows`、`Extra`。
2. 避免在 SQL 里对索引列做函数/运算；能在应用层算就在应用层算。
3. 字符列比较用**同类型字面量**。
4. 联合索引按最左前缀设计；模糊查询 `%` 放后面或换搜索引擎。

## 面试速记

见 [[database/mysql-索引面试题]] Q6～Q8。
