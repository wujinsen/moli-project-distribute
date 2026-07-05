---
title: Redis（面试题系列）
slug: redis-面试题
type: interview
status: active
tags: [redis, 缓存, 面试题]
sources:
- raw/wujinsen_markdown/DataBase/Redis/Codis作者黄东旭细说分布式Redis架构设计和踩过的那些坑们.note.md
- raw/wujinsen_markdown/DataBase/Redis/Jedis/Jedis与Redisson选型对比.note.md
- raw/wujinsen_markdown/DataBase/Redis/Jedis_tedis_redisson/jedis高版本的JedisPoolConfig没有maxActive和maxWait.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis命令问题.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis夺命16问.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis如何通过Spring Session实现分布式Session共享.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis常用技巧.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis杂记.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis的坑.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis相关参数.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis设计与实现读书笔记/简单动态字符串.note.md
- raw/wujinsen_markdown/DataBase/Redis/Redis设计与实现读书笔记/集群部分.note.md
- raw/wujinsen_markdown/DataBase/Redis/redis线程模型.note.md
- raw/wujinsen_markdown/DataBase/Redis/redis里怎么知道key的value大小？或者按照value的大小排序？.note.md
- raw/wujinsen_markdown/DataBase/Redis/一口气说出 Redis 16 个常见使用场景.note.md
- raw/wujinsen_markdown/DataBase/Redis/教程/Redis 数据类型.note.md
- raw/wujinsen_markdown/DataBase/Redis/教程/Redis快速入门.note.md
- raw/wujinsen_markdown/DataBase/Redis/教程/Redis连接问题.note.md
- raw/wujinsen_markdown/DataBase/Redis/教程/redis无法远程连接.note.md
- raw/wujinsen_markdown/DataBase/Redis/教程/一口气说出 Redis 16 个常见使用场景.note.md
- raw/wujinsen_markdown/DataBase/Redis/无标题笔记.note.md
- raw/wujinsen_markdown/DataBase/Redis/监控/Cache-cloud解析.note.md
- raw/wujinsen_markdown/DataBase/Redis/监控/Redis开源监控--python环境依赖.note.md
- raw/wujinsen_markdown/DataBase/Redis/监控/Redis监控方案.note.md
- raw/wujinsen_markdown/DataBase/Redis/缓存策略/缓存更新的套路.note.md
- raw/wujinsen_markdown/DataBase/Redis/集群/Redis集群安装.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/Redis 3.0 集群安装.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/redis安装.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/共享redis安装文档（ok）.note.md
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
- raw/wujinsen_markdown/面试笔试/redis/分布式锁之Redis实现.note.md
- raw/wujinsen_markdown/面试笔试/高级java/缓存更新的套路.note.md
related: [redis-缓存, cache-aside与缓存更新模式, redis-数据结构与使用场景, redis分布式锁实现, 分布式锁面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Redis（面试题系列）

> [[cache/redis-缓存]] [[cache/cache-aside与缓存更新模式]] [[cache/redis-数据结构与使用场景]] [[cache/redis分布式锁实现]]

## Q1. Redis 为什么快？

纯内存、单线程命令无锁、I/O 多路复用、高效数据结构。

## Q2. 五种基本类型及场景？

String/Hash/List/Set/ZSet + Bitmap/HLL/Stream。见 [[cache/redis-数据结构与使用场景]]。

## Q3. 缓存穿透/击穿/雪崩？

- **穿透**：查不存在 → 布隆/缓存空值
- **击穿**：热点 key 过期 → 互斥锁重建
- **雪崩**：大量 key 同时过期 → 随机 TTL、多级缓存

## Q4. Cache-Aside 怎么更新？

**先更新 DB，再删缓存**；别先删缓存。见 [[cache/cache-aside与缓存更新模式]]。

## Q5. RDB vs AOF？

快照 vs 日志；生产常混合。见 [[cache/redis-持久化与高可用]]。

## Q6. 分布式锁怎么做？

SET NX PX + Lua 删锁 + 看门狗；见 [[cache/redis分布式锁实现]]、[[cache/分布式锁面试题]]。

## Q7. Redis 单线程还高效？

命令 CPU 快，瓶颈在内存/网络；6.0+ 多线程处理 I/O。

## Q9. 大 key 问题？

删/序列化阻塞 → 拆分、异步 unlink、避免 HGETALL 超大 Hash。

## Q10. Redis 与 Memcached？

Redis 多结构、持久化、主从；Memcached 简单 KV、多线程。
## Q8. 过期键删除策略？

惰性删除（访问时检查）+ 定期抽样删除。内存满时按 maxmemory-policy（volatile-lru/allkeys-lru 等）淘汰。

## Q11. 主从复制原理？

全量 RDB + 增量 repl_backlog；从库只读。见 [[cache/redis-集群与哨兵实践]]。

## Q12. Cluster 如何分片？

16384 slot；MOVED/ASK 重定向；客户端/smart 路由。多 master 水平扩展。
## 批次#1310 增补（wujinsen P0）

本批合并 `DataBase/Redis/` 下 **Redis 夺命16问**、16 场景、线程模型、Codis 架构等 raw。新增 Q8（过期策略）、Q11（主从/哨兵）、Q12（Cluster 槽位）见上节；细节链 [[cache/redis-持久化与高可用]]、[[cache/redis-集群与哨兵实践]]。

原文插图 annex：[[cache/annex-redis源码分析]]

原文插图 annex：[[cache/annex-Redis夺命16问]]
