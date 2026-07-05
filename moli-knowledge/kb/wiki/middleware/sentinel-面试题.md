---
title: Sentinel 面试题
slug: sentinel-面试题
type: interview
status: active
tags: [sentinel, 面试, 限流, 熔断]
sources:
- raw/wujinsen_markdown/面试笔试/精尽面试题/dubbo/精尽 Dubbo 面试题.note.md
- raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md
related: [sentinel-限流与熔断, 限流算法与令牌桶, sentinel-接入与规则配置, dubbo-面试题, redis-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Sentinel 面试题

> 枢纽 [[middleware/sentinel-限流与熔断]]；算法 [[middleware/限流算法与令牌桶]]；接入 [[middleware/sentinel-接入与规则配置]]。

## Q1. 限流、降级、熔断区别？

**限流**：控制进入速率/并发，防备流量过大。**降级**：关闭或简化非核心能力，返回兜底。**熔断**：依赖故障时快速失败，停止调用坏下游。见 [[middleware/sentinel-限流与熔断]]。

## Q2. 令牌桶和漏桶区别？

令牌桶按固定速率放令牌，允许突发（桶内有令牌即可）。漏桶固定速率流出，平滑输入。Guava RateLimiter 属令牌桶。见 [[middleware/限流算法与令牌桶]]。

## Q3. Sentinel 和 Hystrix 对比？

Hystrix 已停维；Sentinel 侧重**流控+熔断+系统保护**，Dashboard 实时规则，与 Dubbo/Gateway 集成更好。父 POM 引 Sentinel 但未配 Hystrix。

## Q4. Sentinel 滑动窗口做什么？

`StatisticSlot` 用 `LeapArray` 分桶统计 pass/block/RT/exception，供 Flow/Degrade 规则判断。见 [[middleware/sentinel-接入与规则配置]]。

## Q5. Dubbo 怎么做限流？

① 原生 `TpsLimitFilter`（Provider）；② **Sentinel**（推荐）；③ 自定义 Filter + Guava RateLimiter。精尽 Dubbo 笔记推荐 Sentinel。

## Q6. 分布式限流怎么做？

Redis+Lua 原子计数、Sentinel 集群流控、Gateway 统一 QPS。秒杀库存用 Redis Lua（[[cache/redis-面试题]]），接口 QPS 常用 Sentinel/nginx。

## Q7. 流控规则 controlBehavior 有哪些？

直接拒绝、冷启动（Warm Up）、匀速排队。对应突发流量是否允许、是否排队。

## Q8. 熔断策略有哪几种？

慢调用比例、异常比例、异常数。需最小请求数、统计时长、熔断时长。

## Q9. 热点参数限流场景？

秒杀商品 id、恶意刷某一 SKU。对参数值单独 QPS 限制。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/面试笔试/高级java/2018年一线互联网公司Java高级面试题总结.note.md` · T22 **B** 档

### 来自：2018年一线互联网公司Java高级面试题总结

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%AB%98%E7%BA%A7java/2018%E5%B9%B4%E4%B8%80%E7%BA%BF%E4%BA%92%E8%81%94%E7%BD%91%E5%85%AC%E5%8F%B8Java%E9%AB%98%E7%BA%A7%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%AB%98%E7%BA%A7java/2018%E5%B9%B4%E4%B8%80%E7%BA%BF%E4%BA%92%E8%81%94%E7%BD%91%E5%85%AC%E5%8F%B8Java%E9%AB%98%E7%BA%A7%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E9%AB%98%E7%BA%A7java/2018%E5%B9%B4%E4%B8%80%E7%BA%BF%E4%BA%92%E8%81%94%E7%BD%91%E5%85%AC%E5%8F%B8Java%E9%AB%98%E7%BA%A7%E9%9D%A2%E8%AF%95%E9%A2%98%E6%80%BB%E7%BB%93.note_images/imageFile3.png)

原文插图 annex：[[middleware/annex-精尽-Dubbo-面试题]]
