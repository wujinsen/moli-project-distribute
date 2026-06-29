---
title: MySQL Slow Log 慢查询分析
slug: mysql-slow-log慢查询分析
type: article
status: active
tags: [MySQL, 性能, 排查]
sources:
 - raw/wujinsen_markdown/
related: [mysql-索引, mysql-explain-执行计划进阶, mysql-深分页与慢sql优化]
created: 2026-06-21
updated: 2026-06-21
---

# MySQL Slow Log 慢查询分析

> 索引 [[mysql-索引]]；EXPLAIN [[mysql-explain-执行计划进阶]]；优化案例 [[mysql-深分页与慢sql优化]]。

## 1. 开启 slow log

```ini
slow_query_log=1
long_query_time=1
log_queries_not_using_indexes=1
```

MySQL 8：`slow_query_log` 表 + `pt-query-digest` 聚合。

## 2. 分析流程

```
慢 SQL 样本 → EXPLAIN → 是否走索引 / 回表 / 排序/filesort
 → 改写 SQL 或加索引 → 压测验证
```

## 4. 与监控联动

Druid SQL 统计 + slow log 双轨；Prometheus 告警 QPS/RT [[prometheus-告警规则设计]]。

## 相关

[[druid连接池与监控]] · [[mysql-事务与锁]]
