---
title: 分布式 ID 生成
slug: 分布式id生成
type: concept
status: active
tags: [分布式, ID, 雪花算法]
sources:
- raw/wujinsen_markdown/面试笔试/高级java/高级java面试.note.md
related: [雪花算法与时钟回拨, 接口幂等性实践, 分布式id面试题]
created: 2026-06-22
updated: 2026-07-05
---

# 分布式 ID 生成

> 雪花细节 [[database/雪花算法与时钟回拨]]；幂等 [[middleware/接口幂等性实践]]；面试 [[middleware/分布式id面试题]]。

分布式环境下，数据库自增 ID **无法保证全局唯一**（分库分表、多实例写入）。常见方案：

| 方案 | 优点 | 缺点 |
|------|------|------|
| UUID | 简单、本地生成 | 无序、索引性能差 |
| DB 号段 | 趋势递增 | DB 依赖、单点 |
| Redis INCR | 快 | 需 Redis 高可用 |
| **雪花 Snowflake** | 趋势递增、高性能 | 时钟回拨、机器 ID 管理 |
| 美团 Leaf / 百度 UID | 生产级封装 | 组件运维 |

## 选型建议

| 场景 | 建议 |
|------|------|
| 单库 dev | AUTO_INCREMENT 够用 |
| 多实例写同一表 | 雪花或 Redis 号段 |
| 对外暴露 ID | 避免连续自增（防爬） |

## 相关

[[database/sharding-分库分表入门]] ·
