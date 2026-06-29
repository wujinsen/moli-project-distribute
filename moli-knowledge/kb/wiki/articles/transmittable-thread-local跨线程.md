---
title: TransmittableThreadLocal 跨线程传递
slug: transmittable-thread-local跨线程
type: article
status: active
tags: [Java, 并发, 可观测性]
sources:
 - raw/wujinsen_markdown/
related: [threadlocal-与上下文传递, mdc-日志链路上下文, spring-async与线程池, dubbo-调用原理与分层]
created: 2026-06-21
updated: 2026-06-21
---

# TransmittableThreadLocal 跨线程传递

> ThreadLocal 基础 [[threadlocal-与上下文传递]]；MDC [[mdc-日志链路上下文]]；@Async [[spring-async与线程池]]。

阿里 **TTL**（TransmittableThreadLocal）在任务提交到线程池时**捕获并回放**上下文，解决池化线程复用导致上下文丢失。

## 1. 问题回顾

```
HTTP 线程 set(traceId) → 提交 Runnable 到 pool → worker 线程 get() 为空
```

`InheritableThreadLocal` 仅在线程**新建**时复制，池化无效。

## 2. 用法

```java
TransmittableThreadLocal<String> TTL_TRACE = new TransmittableThreadLocal<>();

ExecutorService pool = TtlExecutors.getTtlExecutorService(
 Executors.newFixedThreadPool(8));

TTL_TRACE.set(traceId);
pool.submit(() -> log.info("trace={}", TTL_TRACE.get())); // 有值
```

Spring：`TaskDecorator` 包装 `ThreadPoolTaskExecutor` 亦可手动拷贝 MDC。

## 3. Dubbo / RPC

`RpcContext` attachment 在 consumer/provider 间传递；异步调用需 TTL 或显式 `setAttachment`，否则链路断 [[rpc-超时重试与链路]]。

## 5. 注意

- TTL 增加每次 submit 拷贝开销，高 QPS 路径评估
- 仍要在任务结束 `remove()`，防泄漏

## 相关

[[logback-日志配置]] · [[异步编程面试题]]
