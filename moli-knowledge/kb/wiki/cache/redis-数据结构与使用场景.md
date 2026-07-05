---
title: Redis 数据结构与使用场景
slug: redis-数据结构与使用场景
type: article
status: active
tags: [redis, 数据结构, 场景]
sources:
- raw/wujinsen_markdown/DataBase/Redis/Jedis/Jedis与Redisson选型对比.note.md
- raw/wujinsen_markdown/DataBase/Redis/Jedis_tedis_redisson/jedis高版本的JedisPoolConfig没有maxActive和maxWait.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/10 个 Redis 建议/技巧 .note.md
- raw/wujinsen_markdown/大数据资料-王/redis/DataCenter功能简述.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/Java Memcache基本应用  .note.md
- raw/wujinsen_markdown/大数据资料-王/redis/Jedis类图及方法说明.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/MongoDB—readme-王森丰.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/MongoDB——linux.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/MongoDB——windows2.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/Redis-------------------------1遍.note.attach/Redis新手入门详解.md
- raw/wujinsen_markdown/大数据资料-王/redis/Redis-------------------------1遍.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/Redis.note.attach/Redis新手入门详解.md
- raw/wujinsen_markdown/大数据资料-王/redis/Redis.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/Redis主从自动failover.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/jedis源码----------------------1遍.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/jedis源码理解-------王森丰.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/new DMS.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis session共享.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis 学习笔记(4)-HA高可用方案Sentinel配置.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis 开机启动.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis 添加访问密码.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis-JedisPoolConfig配置.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis.conf 配置事例.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis.conf 配置详解.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis3.0.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis主从切换的集群管理.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis之如何配置jedisPool参数.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis协议.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis命令.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/redis源码分析.note.md
- raw/wujinsen_markdown/大数据资料-王/redis/一致性哈希算法与Java实现.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md
- raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
related: [redis-缓存, redis-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Redis 数据结构与使用场景

> 枢纽 [[cache/redis-缓存]]。

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
## Jedis vs Redisson（raw）

| | Jedis | Redisson |
|---|-------|----------|
| 模型 | 轻量客户端 | 封装分布式对象/锁 |
| 分布式锁 | 需自写 Lua | `RLock` + 看门狗 [[cache/redisson-看门狗与分布式锁]] |
| 连接池 | `JedisPool`；高版本配置项更名需注意 | 开箱即用 |

选型：简单 KV 用 Jedis/Lettuce；锁/队列/对象语义用 Redisson。

## 批次#1313 增补（wujinsen P2）

合并 Jedis/Redisson 选型 raw。
