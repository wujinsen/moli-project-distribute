---
title: Redis 集群与哨兵实践
slug: redis-集群与哨兵实践
type: article
status: active
tags: [redis, 集群, 哨兵, 高可用]
sources:
 - raw/wujinsen_markdown/大数据资料-王/a安装文档/Redis 3.0 集群安装.note.md
 - raw/wujinsen_markdown/大数据资料-王/redis/redis主从切换的集群管理.note.md
related: [redis-缓存, redis-持久化与高可用, 认证与会话机制, 秒杀设计, redis-面试题]
created: 2026-06-22
updated: 2026-06-22
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
