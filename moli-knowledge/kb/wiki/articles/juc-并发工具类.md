---
title: JUC 并发工具类
slug: juc-并发工具类
type: article
status: active
tags: [Java, 并发, JUC]
sources:
 - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md
related: [java-并发, java-并发面试题, completablefuture-异步编排, 线程池-实战调优]
created: 2026-06-21
updated: 2026-06-21
---

# JUC 并发工具类

> 并发总览 [[java-并发]]；面试 [[java-并发面试题]]；编排 [[completablefuture-异步编排]]。

`java.util.concurrent` 除线程池外的**协调**工具，用于多线程汇合、限流、交换数据。

## 1. CountDownLatch

一次性倒数，主线程等多 worker 完成。

```java
CountDownLatch latch = new CountDownLatch(3);
for (int i = 0; i < 3; i++) {
 pool.execute(() -> { doShard(); latch.countDown(); });
}
latch.await(30, TimeUnit.SECONDS);
mergeResults();
```

****：压测脚本等多实例就绪信号（测试侧）；生产更常用 MQ 消费确认。

## 2. CyclicBarrier

多线程互相等到同一屏障，**可循环**使用。分片计算每轮 barrier。

## 3. Semaphore

许可数控制并发度，如 DB 连接外再限 50 并发写。

```java
Semaphore sem = new Semaphore(50);
sem.acquire();
try { writeDb(); } finally { sem.release(); }
```

与 [[sentinel-限流与熔断]] 区别：Semaphore 进程内；Sentinel 可集群规则。

## 4. Exchanger / Phaser

- **Exchanger**：两线程交换缓冲区（流水线）
- **Phaser**：动态注册参与者的多阶段屏障（复杂仿真）

## 5. 选型速查

| 需求 | 工具 |
|------|------|
| 等多任务完成一次 | CountDownLatch |
| 多轮同步 | CyclicBarrier / Phaser |
| 限制并发数 | Semaphore / 线程池 queue |
| 异步组合 | CompletableFuture |

## 相关

[[concurrenthashmap原理]] · [[分布式锁]]
