---
title: Redis（面试题系列）
slug: redis-面试题
type: interview
status: active
tags: [redis, 缓存, 面试题]
sources:
  - raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
  - raw/wujinsen_markdown/面试笔试/redis/分布式锁之Redis实现.note.md
related: [redis-缓存, cache-aside与缓存更新模式, redis-数据结构与使用场景, redis分布式锁实现, 分布式锁面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Redis（面试题系列）

> [[redis-缓存]] [[cache-aside与缓存更新模式]] [[redis-数据结构与使用场景]] [[redis分布式锁实现]]

## Q1. Redis 为什么快？

纯内存、单线程命令无锁、I/O 多路复用、高效数据结构。

## Q2. 五种基本类型及场景？

String/Hash/List/Set/ZSet + Bitmap/HLL/Stream。见 [[redis-数据结构与使用场景]]。

## Q3. 缓存穿透/击穿/雪崩？

- **穿透**：查不存在 → 布隆/缓存空值  
- **击穿**：热点 key 过期 → 互斥锁重建  
- **雪崩**：大量 key 同时过期 → 随机 TTL、多级缓存

## Q4. Cache-Aside 怎么更新？

**先更新 DB，再删缓存**；别先删缓存。见 [[cache-aside与缓存更新模式]]。

## Q5. RDB vs AOF？

快照 vs 日志；生产常混合。见 [[redis-持久化与高可用]]。

## Q6. 分布式锁怎么做？

SET NX PX + Lua 删锁 + 看门狗；见 [[redis分布式锁实现]]、[[分布式锁面试题]]。

## Q7. Redis 单线程还高效？

命令 CPU 快，瓶颈在内存/网络；6.0+ 多线程处理 I/O。

## Q8. 茉莉项目 Redis 用途？

Shiro Session 共享、秒杀 Lua 扣库存、分布式锁；见 [[认证与会话机制]] [[秒杀设计]]。

## Q9. 大 key 问题？

删/序列化阻塞 → 拆分、异步 unlink、避免 HGETALL 超大 Hash。

## Q10. Redis 与 Memcached？

Redis 多结构、持久化、主从；Memcached 简单 KV、多线程。
