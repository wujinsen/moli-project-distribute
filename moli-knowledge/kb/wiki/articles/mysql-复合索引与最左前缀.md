---
title: MySQL 复合索引与最左前缀
slug: mysql-复合索引与最左前缀
type: article
status: active
tags: [mysql, 索引, 复合索引, 最左前缀, 性能优化]
sources:
 - raw/wujinsen_markdown/DataBase/mysql/索引/复合索引的优点和注意事项.note.md
 - raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md
 - raw/wujinsen_markdown/DataBase/mysql/索引/mysql索引命中规则.note.md
related: [mysql-索引, mysql-覆盖索引与回表优化, mysql-索引失效场景, mysql-索引面试题]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL 复合索引与最左前缀

> 概念枢纽 [[mysql-索引]]；失效场景 [[mysql-索引失效场景]]。

## 复合索引是什么？

在**多个列**上建立的单一索引，如：

```sql
CREATE INDEX idx_abc ON t1 (col_a, col_b, col_c);
```

相对多个单列索引：维护开销更小，且可能一次索引扫描覆盖多列条件（配合覆盖索引见 [[mysql-覆盖索引与回表优化]]）。

## 最左前缀原则（核心）

联合索引 `(a, b, c)` 相当于逻辑上建立了 `(a)`、`(a,b)`、`(a,b,c)` 三棵前缀树，**必须从最左列开始连续匹配**才可用索引：

| WHERE 条件 | 能否用 `idx_abc` |
|--------------|------------------|
| `a=1 AND b=2 AND c=3` | ✅ 全列 |
| `a=1 AND b=2` | ✅ 前缀 |
| `a=1` | ✅ 前缀 |
| `b=2 AND c=3`（缺 a） | ❌ |
| `b=2` | ❌ |
| `a=1 AND c=3`（跳过 b） | ⚠️ 通常只用 a |

**条件顺序**：`WHERE b=? AND a=?` 优化器一般会重排，但**不要依赖**；设计时让高频过滤列靠左。

## 窄索引 vs 宽索引

- **窄索引**：1～2 列 → 优先使用，更省空间、更灵活。
- **宽索引**：3 列以上 → 仅在确有 `(a,b,c)` 组合查询时建；5～6 列宽索引收益递减。

## 复合索引 vs 多个单列索引

已有 `(col1, col2)` 时，通常**不必**再建单列 `col1`（前缀已覆盖）。

但若查询**只有** `col2` 条件，复合索引 `(col1,col2)` **帮不上忙** → 需单独索引或调整列顺序（把高选择性/高频列放左）。

## 设计 checklist

1. 分析真实 SQL 的 WHERE / ORDER BY，按**组合出现频率**建联合索引。
2. 等值列放前，范围列放后（范围后的列索引往往用不上）。
3. 索引总数克制：写多读少的表尤其谨慎。
4. 用 `EXPLAIN` 验证 `key` 与 `rows`。

## 面试一句话

> 联合索引遵循最左前缀；`(a,b,c)` 索引对 `a`、`a+b`、`a+b+c` 有效，对单独 `b` 无效。

详见 [[mysql-索引面试题]] Q4～Q5。
