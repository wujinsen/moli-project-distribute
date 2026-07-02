---
title: 分布式 ID 面试题
slug: 分布式id面试题
type: interview
status: active
tags: [面试, 分布式, ID]
sources:
 - raw/wujinsen_markdown/面试笔试/高级java/高级java面试.note.md
related: [分布式id生成, 雪花算法与时钟回拨, 接口幂等性实践]
created: 2026-06-22
updated: 2026-06-22
---

# 分布式 ID 面试题

## Q1. 为什么需要分布式 ID？

分库分表、多实例写入时自增 ID 冲突或无法全局唯一。

## Q2. 雪花算法结构？

1+41+10+12 bit；趋势递增。

## Q3. 时钟回拨怎么办？

等待、抛异常、扩展位；生产用 NTP 监控。

## Q4. UUID 缺点？

无序导致 MySQL 页分裂、索引大。

## Q5. Redis 发号？

`INCR` 简单；需高可用 Redis。

## Q6. 号段模式？

批量从 DB 取号段，本地递增，减少 DB 压力。

## Q7. 与幂等关系？

业务单号作幂等键 [[middleware/接口幂等性实践]]。

## Q9. 订单号设计？

可读性+唯一：时间+机器+序列，或纯雪花。

## Q10. Leaf 了解吗？

美团号段+雪花双模式，生产封装。
