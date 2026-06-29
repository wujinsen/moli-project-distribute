---
title: ThreadLocal 与上下文传递
slug: threadlocal-与上下文传递
type: concept
status: active
tags: [Java, 并发, 鉴权]
sources:
 - raw/wujinsen_markdown/面试笔试/Java面试题精选/【68期】面试官：对并发熟悉吗？说说Synchronized及实现原理.note.md
related: [mdc-日志链路上下文, transmittable-thread-local跨线程, shiro-鉴权体系, java-并发]
created: 2026-06-21
updated: 2026-06-21
---

# ThreadLocal 与上下文传递

> 日志 traceId [[mdc-日志链路上下文]]；跨线程 [[transmittable-thread-local跨线程]]；鉴权 [[shiro-鉴权体系]]。

**ThreadLocal** 为每个线程存一份独立变量副本，避免参数层层传递。

## 1. 原理简述

- 每个 `Thread` 持有 `ThreadLocalMap`（弱引用 key）
- `get/set/remove` 以当前线程为键
- **必须**在 `finally` 中 `remove()`，否则线程池复用导致**脏数据 / 内存泄漏**

```java
private static final ThreadLocal<Long> USER_HOLDER = new ThreadLocal<>();

public void filterChain(...) {
 try {
 USER_HOLDER.set(currentUserId);
 chain.doFilter(request, response);
 } finally {
 USER_HOLDER.remove();
 }
}
```

## 2. InheritableThreadLocal

子线程**创建时**复制父线程值；线程池**复用**线程时不更新 → 异步场景不可靠。

## 4. 异步断裂

`@Async`、线程池、`CompletableFuture` 切换线程后 ThreadLocal **丢失** → 用 **TTL** [[transmittable-thread-local跨线程]] 或显式传参。

## 5. 面试要点

- 弱引用 key 为何仍可能泄漏？（value 强引用 + 线程长期存活）
- 与 synchronized 无关；解决**线程隔离**而非互斥

## 相关

[[spring-async与线程池]] · [[java-并发面试题]]
