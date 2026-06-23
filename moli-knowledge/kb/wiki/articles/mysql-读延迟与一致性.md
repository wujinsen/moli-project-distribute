---
title: MySQL 读延迟与一致性
slug: mysql-读延迟与一致性
type: article
status: active
tags: [MySQL]
sources:
  - raw/wujinsen_markdown/
related: [mysql-主从读写分离, 缓存双写与一致性策略]
created: 2026-06-21
updated: 2026-06-21
---

# MySQL 读延迟与一致性

> 参见 [[mysql-主从读写分离]] · [[缓存双写与一致性策略]]。

## 1. 核心概念

- 主从延迟监控
- 读己之写：走主或缓存
- 秒杀走 Redis

## 2. 茉莉触点

- 对照 [[本地启动指南]] · [[服务调用与架构]] 落地。

## 相关

[[mysql-主从读写分离]] · [[缓存双写与一致性策略]]
