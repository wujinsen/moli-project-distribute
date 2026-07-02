---
title: Java 并发（面试题系列）
slug: java-并发面试题
type: interview
status: active
tags: [java, 并发, 面试题, JMM, 线程池]
sources:
 - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md
 - raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md
 - raw/wujinsen_markdown/面试笔试/Java面试题精选/【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note.md
 - raw/wujinsen_markdown/面试笔试/Java面试题精选/【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note.md
related: [java-并发, jmm与happens-before, synchronized与锁原理, volatile与可见性, concurrenthashmap原理]
created: 2026-06-22
updated: 2026-06-22
---

# Java 并发（面试题系列）

> 枢纽 [[java/java-并发]]；原理页 [[java/jmm与happens-before]] [[java/synchronized与锁原理]] [[java/volatile与可见性]] [[java/concurrenthashmap原理]]。

## Q1. 线程有哪些状态？如何转换？

NEW → start → RUNNABLE；获锁失败 BLOCKED；wait/join → WAITING/TIMED_WAITING；run 结束 TERMINATED。见 [[java/java-并发]]。

## Q2. synchronized 和 volatile 区别？

synchronized：**互斥 + 可见性 + 有序性**（监视器锁）。volatile：**可见性 + 禁止特定重排序**，不互斥，不保证 i++ 原子。见 [[java/synchronized与锁原理]]、[[java/volatile与可见性]]。

## Q3. synchronized 底层怎么实现？

对象头 Mark Word + Monitor；monitorenter/monitorexit；锁升级偏向→轻量→重量。可重入。

## Q4. volatile 能替代 synchronized 吗？

不能。DCL 单例、状态标志适合 volatile；临界区复合操作必须锁或原子类。

## Q5. 什么是 happens-before？

JMM 可见性规则：程序顺序、锁、volatile、传递性等。见 [[java/jmm与happens-before]]。

## Q6. ConcurrentHashMap 1.7 和 1.8？

7：Segment 锁；8：Node 数组 + 桶头 synchronized + CAS，get 靠 volatile 域无锁读。见 [[java/concurrenthashmap原理]]。

## Q7. wait/notify 为什么要在 synchronized 里？

调用 wait/notify 的线程必须持有该对象的监视器锁，否则 IllegalMonitorStateException；且与条件检查、修改同一临界区。

## Q8. 线程池 corePoolSize 和 maximumPoolSize？

任务来先占 core 线程；core 满则入队；队满则开非 core 线程直到 maximum；仍满则拒绝策略。空闲非 core 超时回收。

## Q9. ThreadLocal 原理与泄漏？

每线程一个 ThreadLocalMap，key 弱引用、value 强引用；线程池场景线程复用可能导致 value 泄漏，用完 **remove()**。
