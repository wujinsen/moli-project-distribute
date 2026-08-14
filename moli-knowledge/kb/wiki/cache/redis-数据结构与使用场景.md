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

原文插图 annex：[[cache/annex-redis源码分析]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 6 组

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/MongoDB—readme-王森丰.note.md` · T22 **B** 档

### 来自：MongoDB—readme-王森丰

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/MongoDB%E2%80%94readme-%E7%8E%8B%E6%A3%AE%E4%B8%B0.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md` · T22 **B** 档

### 来自：游戏排行榜算法设计实现比较

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%9D%A2%E8%AF%95%E9%A2%98%E6%95%B4%E7%90%86/%E6%B8%B8%E6%88%8F%E6%8E%92%E8%A1%8C%E6%A6%9C%E7%AE%97%E6%B3%95%E8%AE%BE%E8%AE%A1%E5%AE%9E%E7%8E%B0%E6%AF%94%E8%BE%83.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/Jedis类图及方法说明.note.md` · T22 **B** 档

### 来自：Jedis类图及方法说明

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Jedis%E7%B1%BB%E5%9B%BE%E5%8F%8A%E6%96%B9%E6%B3%95%E8%AF%B4%E6%98%8E.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Jedis%E7%B1%BB%E5%9B%BE%E5%8F%8A%E6%96%B9%E6%B3%95%E8%AF%B4%E6%98%8E.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/MongoDB——windows2.note.md` · T22 **B** 档

### 来自：MongoDB——windows2

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/MongoDB%E2%80%94%E2%80%94windows2.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/MongoDB%E2%80%94%E2%80%94windows2.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/MongoDB%E2%80%94%E2%80%94windows2.note_images/imageFile3.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/redis主从切换的集群管理.note.md` · T22 **B** 档

### 来自：redis主从切换的集群管理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/redis%E4%B8%BB%E4%BB%8E%E5%88%87%E6%8D%A2%E7%9A%84%E9%9B%86%E7%BE%A4%E7%AE%A1%E7%90%86.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/redis%E4%B8%BB%E4%BB%8E%E5%88%87%E6%8D%A2%E7%9A%84%E9%9B%86%E7%BE%A4%E7%AE%A1%E7%90%86.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/redis%E4%B8%BB%E4%BB%8E%E5%88%87%E6%8D%A2%E7%9A%84%E9%9B%86%E7%BE%A4%E7%AE%A1%E7%90%86.note_images/imageFile3.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/一致性哈希算法与Java实现.note.md` · T22 **B** 档

### 来自：一致性哈希算法与Java实现

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/%E4%B8%80%E8%87%B4%E6%80%A7%E5%93%88%E5%B8%8C%E7%AE%97%E6%B3%95%E4%B8%8EJava%E5%AE%9E%E7%8E%B0.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/%E4%B8%80%E8%87%B4%E6%80%A7%E5%93%88%E5%B8%8C%E7%AE%97%E6%B3%95%E4%B8%8EJava%E5%AE%9E%E7%8E%B0.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/%E4%B8%80%E8%87%B4%E6%80%A7%E5%93%88%E5%B8%8C%E7%AE%97%E6%B3%95%E4%B8%8EJava%E5%AE%9E%E7%8E%B0.note_images/imageFile3.png)

原文插图 annex：[[cache/annex-redis源码分析]]

原文插图 annex：[[cache/annex-缓存更新的套路]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/redis/Redis-------------------------1遍.note.attach/Redis新手入门详解.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/Redis-------------------------1遍.note.attach/Redis新手入门详解.md` · T22 **B** 档

### 来自：Redis新手入门详解

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile1.png)

![imageFile10.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile10.png)

![imageFile11.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile11.png)

![imageFile12.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile12.png)

![imageFile13.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile13.png)

![imageFile14.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile14.png)

![imageFile15.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile15.png)

![imageFile16.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile16.png)

![imageFile17.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile17.png)

![imageFile18.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile18.png)

![imageFile19.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile19.png)

![imageFile2.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile2.png)

![imageFile20.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile20.png)

![imageFile21.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile21.png)

![imageFile22.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile22.png)

![imageFile23.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile23.png)

![imageFile24.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile24.png)

![imageFile25.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile25.png)

![imageFile26.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile26.png)

![imageFile27.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile27.png)

![imageFile28.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile28.png)

![imageFile29.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile29.png)

![imageFile3.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile3.png)

![imageFile30.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile30.png)

![imageFile4.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile4.png)

![imageFile5.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile5.png)

![imageFile6.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile6.png)

![imageFile7.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile7.png)

![imageFile8.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile8.png)

![imageFile9.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis-------------------------1%E9%81%8D.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile9.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/redis/Redis.note.attach/Redis新手入门详解.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/Redis.note.attach/Redis新手入门详解.md` · T22 **B** 档

### 来自：Redis新手入门详解

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile1.png)

![imageFile10.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile10.png)

![imageFile11.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile11.png)

![imageFile12.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile12.png)

![imageFile13.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile13.png)

![imageFile14.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile14.png)

![imageFile15.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile15.png)

![imageFile16.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile16.png)

![imageFile17.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile17.png)

![imageFile18.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile18.png)

![imageFile19.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile19.png)

![imageFile2.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile2.png)

![imageFile20.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile20.png)

![imageFile21.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile21.png)

![imageFile22.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile22.png)

![imageFile23.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile23.png)

![imageFile24.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile24.png)

![imageFile25.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile25.png)

![imageFile26.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile26.png)

![imageFile27.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile27.png)

![imageFile28.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile28.png)

![imageFile29.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile29.png)

![imageFile3.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile3.png)

![imageFile30.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile30.png)

![imageFile4.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile4.png)

![imageFile5.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile5.png)

![imageFile6.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile6.png)

![imageFile7.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile7.png)

![imageFile8.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile8.png)

![imageFile9.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/Redis.note.attach/Redis%E6%96%B0%E6%89%8B%E5%85%A5%E9%97%A8%E8%AF%A6%E8%A7%A3_images/imageFile9.png)

原文插图 annex：[[cache/annex-redis-session共享]]

原文插图 annex：[[cache/annex-redis-学习笔记(4)-HA高可用方案Sentinel配置]]
