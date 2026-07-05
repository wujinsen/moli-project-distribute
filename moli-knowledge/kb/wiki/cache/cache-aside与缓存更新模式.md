---
title: Cache-Aside 与缓存更新模式
slug: cache-aside与缓存更新模式
type: article
status: active
tags: [redis, 缓存, Cache-Aside, 一致性]
sources:
- raw/wujinsen_markdown/架构/缓存/REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案.note.md
- raw/wujinsen_markdown/架构/缓存/缓存穿透、缓存击穿、缓存雪崩区别和解决方案.note.md
- raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
related: [redis-缓存, redis-面试题, 缓存双写与一致性策略]
created: 2026-06-22
updated: 2026-07-05
---

# Cache-Aside 与缓存更新模式

> 枢纽 [[cache/redis-缓存]]。

## 四种经典 Pattern

| 模式 | 读 | 写 | 特点 |
|------|----|----|------|
| **Cache-Aside** | miss → 读 DB → 写缓存 | **先更新 DB → 再删/失效缓存** | 最常用，应用维护双存储 |
| Read/Write Through | 缓存层代读 | 缓存层代写并同步 DB | 应用只认一个存储 |
| Write Behind | 读同上 | 只更新缓存，异步刷 DB | 高性能，可能丢数据 |

## Cache-Aside 标准流程

**读**：cache get → hit 返回 → miss 则读 DB → set cache → 返回
**写**：update DB → **invalidate cache**（删 key 或设 TTL 过期）

## 错误写法：先删缓存再写库

并发下：线程 A 删缓存 → 线程 B miss 读旧 DB 写回缓存 → 线程 A 写库 → **缓存长期脏数据**。

正确：**先写库，再让缓存失效**。后续读会 reload 新值。

## 仍存在的极小概率 race

读 miss 正在 load DB 时，写线程写完 DB 并删缓存，读线程仍可能把**旧值**写回缓存。实践中写慢于读、且窗口极短；缓解：**缓存 TTL**、版本号、延时双删（高要求场景）。

## 为什么不「写库后更新缓存」？

Facebook/Memcached 实践：**delete 而非 update**，避免两个并发写交错导致脏值。更新 = delete + 下次读重建。

## 强一致？

Cache + DB 非单事务 → 要么 2PC/XA（慢），要么接受最终一致 + 业务补偿。 用 Redis 预减 + 异步落库是另一种权衡。

## 面试一句话

> Cache-Aside 写路径：**先 DB 后删缓存**；别先删缓存；高并发脏读概率低但非零，靠 TTL 兜底。
## 穿透 / 击穿 / 雪崩（raw 架构/缓存）

| 问题 | 现象 | 常见方案 |
|------|------|----------|
| **穿透** | 查不存在的数据，缓存与 DB 都没有 | 布隆过滤器；缓存空值短 TTL |
| **击穿** | 热点 key 过期，并发打穿 DB | 互斥锁重建；热点永不过期 |
| **雪崩** | 大量 key 同时过期 | TTL 加随机；多级缓存；限流 |

与 [[cache/redis-面试题]] Q3 一致。

## 批次#1312 增补（wujinsen P1）

合并缓存穿透/击穿/雪崩 raw。

原文插图 annex：[[cache/annex-缓存更新的套路]]

原文插图 annex：[[cache/annex-REDIS缓存穿透，缓存击穿，缓存雪崩原因+解决方案]]
