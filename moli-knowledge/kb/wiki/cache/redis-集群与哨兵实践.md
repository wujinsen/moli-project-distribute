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
