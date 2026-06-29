---
title: Redis 持久化与高可用
slug: redis-持久化与高可用
type: article
status: active
tags: [redis, RDB, AOF, 高可用]
sources:
 - raw/wujinsen_markdown/面试笔试/高级java/高级java面试.note.md
related: [redis-缓存, redis-集群与哨兵实践, redis-面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Redis 持久化与高可用

> 枢纽 [[redis-缓存]]。

## 持久化两种方式

| | RDB | AOF |
|---|-----|-----|
| 方式 | 快照（fork 子进程） | 记录写命令 append |
| 恢复 | 快 | 慢（可 rewrite 压缩） |
| 丢数据 | 两次快照间可能丢 | everysec 最多丢 1s |
| 生产 | 常 RDB+AOF 混合（4.0+） | 开启 appendfsync everysec |

本地 dev 可关持久化换速度；生产 Session/秒杀数据需评估 RTO/RPO。

## 高可用架构

| 方案 | 说明 |
|------|------|
| **主从复制** | 读扩展、故障手动切换 |
| **哨兵 Sentinel** | 自动故障转移，选新 master |
| **Cluster** | 16384 slot 分片，水平扩展 | 详见 [[redis-集群与哨兵实践]] |

## 与缓存角色的关系

- **纯缓存**：可接受重启丢数据，持久化可弱化
- **Session 存储**：重启丢登录态 → 需持久化或用户重新登录
- **秒杀库存**：重启需从 DB 预热重建 Redis，见

## 面试要点

- RDB fork 时内存 copy-on-write，大实例 fork 慢
- AOF rewrite 子进程重写，父进程双写 buffer
- 主从延迟 → 读己之写需走 master 或等待
