---
title: Redis 缓存
slug: redis-缓存
type: concept
status: active
tags: [redis, 缓存, 性能, 会话]
sources:
 - raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
 - raw/wujinsen_markdown/moli项目/ES+Redis+MySQL架构轻松搞定10亿用户中心系统.note.md
related: [cache-aside与缓存更新模式, redis-数据结构与使用场景, redis-持久化与高可用, redis-集群与哨兵实践, redis分布式锁实现, 认证与会话机制, 秒杀设计, redis-面试题]
updated: 2026-06-22
---

# Redis 缓存（概念枢纽）

> 更新模式见 [[cache/cache-aside与缓存更新模式]]；数据结构见 [[cache/redis-数据结构与使用场景]]；持久化见 [[cache/redis-持久化与高可用]]；分布式锁见 [[cache/分布式锁]]、[[cache/redis分布式锁实现]]；面试 [[cache/redis-面试题]]。

Redis 在目标系统中是**基础设施级**组件，不只是「可选缓存」：

| 用途 | 场景 |
|------|------|
| **Shiro Session** | 全服务共享登录态，见 [[security/认证与会话机制]] |
| **秒杀库存** | Lua 原子扣减 + 队列，见 |
| **分布式锁** | 跨实例互斥，见 [[cache/分布式锁]] |
| **业务缓存** | 热点读、计数、排行榜（按需） |

## 为什么用 Redis 做缓存？

- **内存 KV**，读写延迟亚毫秒级
- 丰富数据结构（String/Hash/List/Set/ZSet/Bitmap/HyperLogLog/Stream）
- 单线程命令执行 → 无锁竞争（I/O 多线程另说）
- 持久化 + 主从/哨兵/集群 → 可恢复、可扩展（详见 [[cache/redis-集群与哨兵实践]]）

## 缓存设计三问

1. **读路径**：Cache-Aside（旁路）最常见，见 [[cache/cache-aside与缓存更新模式]]
2. **一致性**：强一致需 2PC/XA 或业务补偿；多数场景「最终一致 + TTL」
3. **击穿/穿透/雪崩**：互斥重建、布隆/空值、随机 TTL、多级缓存

## 与 MySQL 的分工

```
读多写少热点 → Redis 扛读，MySQL 权威数据源
写路径 → 先写库再失效缓存（Cache-Aside 标准更新）
秒杀热点 → Redis 预减 + 异步落库，减轻 DB
```

索引与慢 SQL 仍靠 MySQL 侧优化，见 [[database/mysql-索引]]。
