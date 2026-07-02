---
title: volatile 与可见性
slug: volatile与可见性
type: article
status: active
tags: [java, volatile, 可见性, 有序性, DCL]
sources:
 - raw/wujinsen_markdown/面试笔试/Java面试题精选/【70期】面试官：对并发熟悉吗？谈谈对volatile的使用及其原理.note.md
 - raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md
related: [java-并发, jmm与happens-before, synchronized与锁原理, concurrenthashmap原理, java-并发面试题]
created: 2026-06-22
updated: 2026-06-22
---

# volatile 与可见性

> JMM 规则 [[java/jmm与happens-before]]；需要互斥时用 [[java/synchronized与锁原理]]。

## volatile 保证什么？

1. **可见性**：写 volatile 会强制刷到主内存；读会从主内存加载（配合 MESI 缓存一致性，其他 CPU 缓存行失效）。
2. **有序性**：禁止 volatile 写与前后某些内存操作的重排序（内存屏障）。
3. **原子性（有限）**：对 volatile 变量的**单次**读或写是原子的（含 long/double），但 **`i++` 仍是读+写两步**，不原子。

## 经典场景

### 1. 状态标志

```java
volatile boolean running = true;
// 线程 A: running = false;
// 线程 B: while (running) { ... } // 能及时看到 false
```

### 2. DCL 单例防重排序

```java
private static volatile Singleton instance;
public static Singleton getInstance() {
 if (instance == null) {
 synchronized (Singleton.class) {
 if (instance == null) {
 instance = new Singleton(); // 1分配 2初始化 3引用赋值，可能重排
 }
 }
 }
 return instance;
}
```

无 volatile 时，其他线程可能看到「引用非 null 但对象未初始化」。volatile 禁止 3 在 2 之前对其他线程可见。

## 与 ConcurrentHashMap

CHM 1.8 的 `Node.val`、`Node.next` 为 **volatile**，保证 get 无锁时读到最新链表/值（见 [[java/concurrenthashmap原理]]）。

## 不适用场景

- 需要「读-改-写」复合操作 → AtomicInteger / synchronized / Lock
- 需要互斥临界区 → synchronized

## 底层简述

- 写 volatile → 插入 **StoreStore** + **StoreLoad** 屏障
- 读 volatile → **LoadLoad** + **LoadStore** 屏障
- 触发缓存一致性协议，使其他核上对应缓存行失效

## 面试一句话

> volatile 轻量，解决可见性与特定有序性；不替代锁，不能当原子计数器用。
