---
title: Java 并发
slug: java-并发
type: concept
status: active
tags: [java, 并发, 多线程, JMM, 线程安全]
sources:
  - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md
  - raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md
related: [jmm与happens-before, synchronized与锁原理, volatile与可见性, concurrenthashmap原理, java-并发面试题, 认证与会话机制, 秒杀设计]
created: 2026-06-22
updated: 2026-06-22
---

# Java 并发（概念枢纽）

> JMM 与 happens-before 见 [[jmm与happens-before]]；synchronized 见 [[synchronized与锁原理]]；volatile 见 [[volatile与可见性]]；ConcurrentHashMap 见 [[concurrenthashmap原理]]；面试速记 [[java-并发面试题]]。

Java 并发要解决两件事：**线程如何通信**、**线程如何同步**。Java 采用**共享内存模型**（JMM）：堆上的实例域/静态域/数组元素在线程间共享，通信对程序员多为隐式（读写字段）。

## 线程状态（6 种）

| 状态 | 含义 |
|------|------|
| NEW | 已创建未 start |
| RUNNABLE | 可运行（含就绪与运行） |
| BLOCKED | 等待监视器锁（synchronized） |
| WAITING | 无限期等待（wait/join/park） |
| TIMED_WAITING | 超时等待 |
| TERMINATED | 结束 |

## 协调多线程的经典手段

| 机制 | 用途 |
|------|------|
| **synchronized** | 互斥 + 可见性 + 禁止部分重排序 |
| **volatile** | 可见性 + 禁止特定重排序（不保证复合操作原子性） |
| **wait/notify** | 在 synchronized 块内协调条件变量 |
| **Lock / AQS** | 显式锁、可中断、公平性（JUC） |
| **线程池** | 复用线程，控制并发度 |

## 茉莉项目中的并发触点

- **Shiro Session 存 Redis**：多服务共享会话，见 [[认证与会话机制]]。
- **秒杀**：Redis+Lua 原子扣减，避免 JVM 内锁扛不住流量，见 [[秒杀设计]]、[[分布式锁]]。
- **Dubbo 多线程 RPC**：Provider/Consumer 线程池配置影响吞吐与超时。

## 选型速记

| 需求 | 首选 |
|------|------|
| 简单互斥 | synchronized |
| 状态标志 / DCL 单例 | volatile |
| 高并发 Map | ConcurrentHashMap（勿用 Collections.synchronizedMap 扛写热点） |
| 批量异步任务 | ThreadPoolExecutor（明确 core/max/queue/拒绝策略） |
| 跨 JVM 互斥 | [[分布式锁]]（Redis/ZK），不是 synchronized |

## 常见坑

1. **`i++` 非原子**：用 AtomicInteger 或 synchronized/Lock。
2. **先删缓存再写库**：脏读概率高，见 [[cache-aside与缓存更新模式]]。
3. **在锁外 wait/notify**：必须持有同一把锁。
4. **线程池无界队列**：OOM 风险；应设上限 + 拒绝策略。
