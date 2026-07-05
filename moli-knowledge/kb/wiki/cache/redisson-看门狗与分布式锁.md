---
title: Redisson 看门狗与分布式锁
slug: redisson-看门狗与分布式锁
type: article
status: active
tags: [Redisson, Redis, 分布式锁]
sources:
- raw/wujinsen_markdown/DataBase/Redis/Jedis/Jedis与Redisson选型对比.note.md
- raw/wujinsen_markdown/架构/分布式事务/redis/Redisson基本用法.note.md
related: [redis分布式锁实现, 分布式锁, 分布式锁面试题, redis-缓存]
created: 2026-06-22
updated: 2026-07-05
---

# Redisson 看门狗与分布式锁

> 手写 Lua 锁 [[cache/redis分布式锁实现]]；对比 Jedis 仅客户端，Redisson 提供**分布式数据结构**。

## 1. RLock 基本用法

```java
RLock lock = redisson.getLock("order:" + orderId);
try {
 if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
 // 业务
 }
} finally {
 if (lock.isHeldByCurrentThread()) lock.unlock();
}
```

## 2. 看门狗（Watchdog）

- `leaseTime = -1` 时，Redisson 默认 **30s 续期**
- 业务执行超过 30s 仍持有锁 → 自动延长，防误释放
- 进程 crash → 锁最终过期

## 3. vs 手写 SET NX PX

| | 手写 Lua | Redisson |
|---|----------|----------|
| 续期 | 需自实现 | 看门狗 |
| 可重入 | 需自实现 | 支持 |
| 红锁 | 自行多节点 | RedLock 争议同 [[cache/redis分布式锁实现]] |

## 相关

[[cache/分布式锁]] · [[cache/redis-集群与哨兵实践]]

原文插图 annex：[[cache/annex-Redisson基本用法]]
