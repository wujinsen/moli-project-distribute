---
title: Redis 备份策略
slug: redis-备份策略
type: article
status: active
tags: ['Redis', '运维']
sources:
  - raw/wujinsen_markdown/
related: [redis-持久化与高可用, redis-集群与哨兵实践]
created: 2026-06-22
updated: 2026-06-22
---

# Redis 备份策略

## 策略

- RDB 定时 + AOF
- Session 丢失可重新登录 [[认证与会话机制]]
- 秒杀库存需 DB 预热 [[秒杀设计]]
