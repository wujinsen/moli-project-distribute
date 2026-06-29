---
title: Redis 数据结构与使用场景
slug: redis-数据结构与使用场景
type: article
status: active
tags: [redis, 数据结构, 场景]
sources:
 - raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
 - raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md
related: [redis-缓存, 秒杀设计, redis-面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Redis 数据结构与使用场景

> 枢纽 [[redis-缓存]]。

| 类型 | 典型命令 | 场景 |
|------|----------|------|
| **String** | GET/SET/INCR/DECR | 缓存 JSON、计数、分布式锁 value |
| **Hash** | HGET/HSET | 对象字段缓存（用户信息片段） |
| **List** | LPUSH/BRPOP | 简单队列、时间线 |
| **Set** | SADD/SISMEMBER | 去重、共同好友 |
| **ZSet** | ZADD/ZRANGE | **排行榜**、延迟队列（score=时间） |
| **Bitmap** | SETBIT/BITCOUNT | 签到、布隆替代 |
| **HyperLogLog** | PFADD/PFCOUNT | UV 近似统计 |
| **Stream** | XADD/XREADGROUP | 消息流（5.0+） |

## 选型原则

1. 需要排序/范围 → ZSet，不要 List 全量扫描
2. 大 Value 拆 Hash 或压缩，避免单 key 过大阻塞
3. 热点 key 考虑本地缓存（Caffeine）+ Redis 二级
4. TTL 必设，防止冷数据占满内存

## Key 设计

`{业务}:{实体}:{id}`，如 `seckill:stock:1001`。集群下可用 hash tag `{user}:1001` 保同 slot。
