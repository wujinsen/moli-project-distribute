---
title: Redis 集群与哨兵实践
slug: redis-集群与哨兵实践
type: article
status: active
tags: [redis, 集群, 哨兵, 高可用]
sources:
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
related: [redis-缓存, redis-持久化与高可用, 认证与会话机制, redis-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Redis 集群与哨兵实践

> 持久化基础 [[cache/redis-持久化与高可用]]；单机 dev 见。

## 1. 三种拓扑怎么选

| 拓扑 | 适用 | 典型现状 |
|------|------|----------|
| **单机** | 本地 dev、小规模 | ✅ 默认 6379 |
| **主从 + 哨兵** | Session/缓存要 HA、数据量中等 | 生产可选 |
| **Cluster** | 大数据量、水平分片 | 秒杀超大规模时再评估 |

## 2. 主从复制要点

- 异步复制 → 主从延迟，「读己之写」走 master
- 全量同步 RDB + 增量命令流
- 从库只读可分担读，**写仍走 master**

## 3. Sentinel 哨兵

- 监控 master/ slave，**自动故障转移**（选举新 master）
- 客户端需支持 Sentinel 协议或配置 VIP/代理
- **脑裂**：旧 master 分区仍写 → 需 `min-replicas-to-write` 等策略

## 4. Redis Cluster

- **16384 slot**，CRC16 路由；每 master 管一段 slot
- 无中央节点，Gossip 交换状态
- 跨 slot 多 key 操作受限（需 hash tag `{user}:1` `{user}:2`）
- 安装：`redis-cli --cluster create ... --cluster-replicas 1`

## 6. 运维 checklist

- [ ] 持久化策略与 RPO 对齐（Session 能否接受丢 1s）
- [ ] 内存 maxmemory + 淘汰策略（`volatile-lru` 等）
- [ ] 慢日志 `slowlog`、内存 `INFO memory`
- [ ] 主从切换后应用连接串是否自动发现新 master

## 相关

[[cache/redis-缓存]] ·

## 批次#1322 增补（wujinsen Phase2 王树挂接）

合并 `大数据资料-王/redis/` 与 a安装文档 Redis 集群 raw。

原文插图 annex：[[cache/annex-redis源码分析]]

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
