---
title: MySQL 死锁与排查
slug: mysql-死锁与排查
type: article
status: active
tags: [mysql, 死锁, InnoDB, 排查]
sources:
 - raw/wujinsen_markdown/DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md
related: [mysql-事务与锁, mysql-innodb锁机制, 故障排查指南, mysql-事务面试题]
created: 2026-06-22
updated: 2026-06-22
---

# MySQL 死锁与排查

> 锁机制 [[mysql-innodb锁机制]]；枢纽 [[mysql-事务与锁]]。

## 死锁条件

两个及以上事务**以不同顺序**持有/等待锁 → 循环等待。InnoDB **行锁**会死锁；表锁一般不会。

## 典型案例（提炼）

### 1. 加锁顺序不一致

多笔 `SELECT ... FOR UPDATE` 按随机借款人 id 顺序加锁 → 交叉等待。
**修复**：`WHERE id IN (...)` 让 InnoDB **按主键排序加锁**，或固定排序后再锁。

### 2. 先查后插（gap 锁）

事务 A：`SELECT id=22 FOR UPDATE`（不存在）→ gap 锁；事务 B 插 id=23 → 死锁。
**修复**：`INSERT ... ON DUPLICATE KEY UPDATE` 或统一锁策略，避免「空查 + 并发插」。

### 3. 二级索引与主键锁顺序

多索引路径加锁顺序不同 → 死锁。
**修复**：缩短事务、统一访问路径、合适索引减少锁范围。

## 排查手段

```sql
SHOW ENGINE INNODB STATUS; -- LATEST DETECTED DEADLOCK 段
SELECT * FROM performance_schema.data_locks; -- 8.0
SELECT * FROM performance_schema.data_lock_waits;
```

应用侧：捕获 `1213 Deadlock`，**重试**有限次数（订单写库可借鉴）。

## 预防 checklist

1. **固定加锁顺序**（主键升序）
2. **缩短事务**（别在事务里调 Dubbo/HTTP）
3. **索引到位**，避免锁升级成大面积 next-key
4. 高并发写考虑队列串行化（秒杀用 Redis，见 ）

## 与 Spring 事务

`@Transactional` 方法过长 → 持锁时间拉长 → 死锁概率升。见 [[spring-声明式事务]]。
