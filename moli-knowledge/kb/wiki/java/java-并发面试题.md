---
title: Java 并发（面试题系列）
slug: java-并发面试题
type: interview
status: active
tags: [java, 并发, 面试题, JMM, 线程池]
sources:
- raw/wujinsen_markdown/大数据资料-王/x线程/Callable与Future的介绍.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/CountDownLatch的介绍和使用.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/ExecutorService线程池 .note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/Java Callable测试.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/Java Callable用法.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/Java多线程的用法详解.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：Lock.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：synchronized.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/java 多线程 CountDownLatch用法.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/setUncaughtExceptionHandler  .note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/多线程.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/多线程单元测试.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/多线程读取文件.note.md
- raw/wujinsen_markdown/大数据资料-王/x线程/浅析Java中CountDownLatch用法.note.md
- raw/wujinsen_markdown/并发编程/java/Atomic原子类.note.md
- raw/wujinsen_markdown/并发编程/java/Java并发编程：CountDownLatch、CyclicBarrier和 Semaphore.note.md
- raw/wujinsen_markdown/并发编程/java/ReentrantLock.note.md
- raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md
- raw/wujinsen_markdown/并发编程/java/volatile.note.md
- raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md
- raw/wujinsen_markdown/面试笔试/Java/1.你什么时候毕业的 2你为什么离开途牛 3 说一说你做的爬虫的难点 4.你擅长什么？ 5你用过什么开源框架 6 线程中run 和start区别 ，怎么样停止线.note.md
- raw/wujinsen_markdown/面试笔试/Java/JVM/JVM面试题.note.md
- raw/wujinsen_markdown/面试笔试/Java/JVM/垃圾收集器.note.md
- raw/wujinsen_markdown/面试笔试/Java/Java常见的异常类之间的继承关系.note.md
- raw/wujinsen_markdown/面试笔试/Java/StringBuffer和final的问题.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/Java 的这些坑，你踩到了吗？.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/java基础面试题.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/java集合类.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/几张图轻松理解String.intern().note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/在Java的反射中，Class.forName和ClassLoader的区别.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/基础篇.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/并发包/concurrentHashMap.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/并发包/volatile.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/并发包/多线程.note.md
- raw/wujinsen_markdown/面试笔试/Java/基础/金三银四解锁你的开挂人生，208 个最常见 Java 面试题全解析.note.md
- raw/wujinsen_markdown/面试笔试/Java/异常和运行时异常的区别.note.md
- raw/wujinsen_markdown/面试笔试/Java面试题精选/【49期】面试官：SpringMVC的控制器是单例的吗.note.md
- raw/wujinsen_markdown/面试笔试/Java面试题精选/【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note.md
- raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md
- raw/wujinsen_markdown/面试笔试/Java面试题精选/【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note.md
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之综合篇.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集核心篇.note.md
related: [java-并发, jmm与happens-before, synchronized与锁原理, volatile与可见性, concurrenthashmap原理]
created: 2026-06-22
updated: 2026-07-05
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
## Q10. run() 与 start() 区别？

`start()` 新建线程并进入 RUNNABLE 执行 `run()`；直接调 `run()` 只是普通方法调用，不启动新线程。

## Q11. 如何优雅停止线程？

协作式：volatile 标志 + 中断 `interrupt()`；线程池用 `shutdown()`/`awaitTermination()`，避免 `stop()`。
## 批次#1310 增补（wujinsen P0）

合并 `面试笔试/Java/` 并发包（volatile/CHM/多线程）及面试小结并发篇、Java面试题精选 67-70 期 raw sources。

## 批次#1330 增补（wujinsen Phase3 收口）

Phase3：王树 x线程 簇 sources。

原文插图 annex：[[java/annex-volatile]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 5 组

> 图源 `raw/wujinsen_markdown/面试笔试/Java面试题精选/【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note.md` · T22 **B** 档

### 来自：【67期】谈谈ConcurrentHashMap是如何保证线程安全的？

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9067%E6%9C%9F%E3%80%91%E8%B0%88%E8%B0%88ConcurrentHashMap%E6%98%AF%E5%A6%82%E4%BD%95%E4%BF%9D%E8%AF%81%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8%E7%9A%84%EF%BC%9F.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9067%E6%9C%9F%E3%80%91%E8%B0%88%E8%B0%88ConcurrentHashMap%E6%98%AF%E5%A6%82%E4%BD%95%E4%BF%9D%E8%AF%81%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8%E7%9A%84%EF%BC%9F.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md` · T22 **B** 档

### 来自：【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9068%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%AF%B4%E8%AF%B4Synchronized%E5%8F%8A%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9068%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%AF%B4%E8%AF%B4Synchronized%E5%8F%8A%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/面试笔试/Java/JVM/垃圾收集器.note.md` · T22 **B** 档

### 来自：垃圾收集器

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java/JVM/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E5%99%A8.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java/JVM/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E5%99%A8.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java/JVM/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E5%99%A8.note_images/imageFile3.png)

> 图源 `raw/wujinsen_markdown/面试笔试/Java面试题精选/【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note.md` · T22 **B** 档

### 来自：【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9070%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%B0%88%E8%B0%88%E5%AF%B9volatile%E7%9A%84%E4%BD%BF%E7%94%A8%E5%8F%8A%E5%85%B6%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9070%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%B0%88%E8%B0%88%E5%AF%B9volatile%E7%9A%84%E4%BD%BF%E7%94%A8%E5%8F%8A%E5%85%B6%E5%8E%9F%E7%90%86.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/Java%E9%9D%A2%E8%AF%95%E9%A2%98%E7%B2%BE%E9%80%89/%E3%80%9070%E6%9C%9F%E3%80%91%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%9A%E5%AF%B9%E5%B9%B6%E5%8F%91%E7%86%9F%E6%82%89%E5%90%97%EF%BC%9F%E8%B0%88%E8%B0%88%E5%AF%B9volatile%E7%9A%84%E4%BD%BF%E7%94%A8%E5%8F%8A%E5%85%B6%E5%8E%9F%E7%90%86.note_images/imageFile3.png)

> 图源 `raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md` · T22 **B** 档

### 来自：深入理解并发之CompareAndSet(CAS)

![imageFile1.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile1.png)

![imageFile2.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile2.png)

![imageFile3.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile3.png)

![imageFile4.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile4.png)

![imageFile5.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile5.png)

原文插图 annex：[[java/annex-volatile]]

原文插图 annex：[[java/annex-面试小结之并发篇]]

原文插图 annex：[[java/annex-Java并发编程：Lock]]

原文插图 annex：[[java/annex-【49期】面试官：SpringMVC的控制器是单例的吗]]

原文插图 annex：[[java/annex-Java并发编程：synchronized]]

原文插图 annex：[[database/annex-面试小结之综合篇]]

原文插图 annex：[[java/annex-多线程单元测试]]

原文插图 annex：[[java/annex-Java常见的异常类之间的继承关系]]

原文插图 annex：[[database/annex-Java后台面试-常见问题]]

原文插图 annex：[[java/annex-Java-的这些坑，你踩到了吗？]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md` · T22 **B** 档

### 来自：synchronized与static synchronized 的区别

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/synchronized%E4%B8%8Estatic%20synchronized%20%E7%9A%84%E5%8C%BA%E5%88%AB.note_images/imageFile1.png)
