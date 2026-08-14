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

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 5 组

> 图源 `raw/wujinsen_markdown/大数据资料-王/redis/MongoDB—readme-王森丰.note.md` · T22 **B** 档

### 来自：MongoDB—readme-王森丰

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/redis/MongoDB%E2%80%94readme-%E7%8E%8B%E6%A3%AE%E4%B8%B0.note_images/imageFile1.png)

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

原文插图 annex：[[cache/annex-Redis夺命16问]]

原文插图 annex：[[cache/annex-redis线程模型]]

原文插图 annex：[[cache/annex-Redis监控方案]]

原文插图 annex：[[cache/annex-缓存更新的套路]]

原文插图 annex：[[cache/annex-Codis作者黄东旭细说分布式Redis架构设计和踩过的那些坑们]]

原文插图 annex：[[cache/annex-Redis连接问题]]

原文插图 annex：[[cache/annex-Redis开源监控-python环境依赖]]

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

原文插图 annex：[[cache/annex-一口气说出-Redis-16-个常见使用场景]]

原文插图 annex：[[cache/annex-Redis如何通过Spring-Session实现分布式Session共享]]

原文插图 annex：[[cache/annex-redis-学习笔记(4)-HA高可用方案Sentinel配置]]

原文插图 annex：[[cache/annex-Redis-数据类型]]
