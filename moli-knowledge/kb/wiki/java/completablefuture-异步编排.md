---
title: CompletableFuture 异步编排
slug: completablefuture-异步编排
type: article
status: active
tags: [Java, 并发, 异步]
sources:
 - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之并发篇.note.md
related: [spring-async与线程池, java-并发, rpc-超时重试与链路, 线程池-实战调优]
created: 2026-06-21
updated: 2026-06-21
---

# CompletableFuture 异步编排

> 基础 [[java/java-并发]]；Spring 封装 [[spring/spring-async与线程池]]；RPC 超时 [[middleware/rpc-超时重试与链路]]。

JDK 8+ 声明式组合多个异步步骤，替代嵌套 `Future.get()` 回调地狱。

## 1. 常用 API

| 方法 | 作用 |
|------|------|
| `supplyAsync(Supplier, Executor)` | 有返回值异步 |
| `thenApply` / `thenAccept` | 串行转换 |
| `thenCombine` | 两路结果合并 |
| `allOf` / `anyOf` | 等多路 / 任一完成 |
| `exceptionally` / `handle` | 异常分支 |
| `orTimeout` / `completeOnTimeout` (9+) | 超时兜底 |

## 2. 示例：并行拉权限 + 用户信息

```java
CompletableFuture<User> userF = CompletableFuture.supplyAsync(
 () -> userClient.getById(uid), queryPool);
CompletableFuture<List<String>> permF = CompletableFuture.supplyAsync(
 () -> permClient.listByUser(uid), queryPool);

return userF.thenCombine(permF, (u, perms) -> {
 u.setPermissions(perms);
 return u;
}).orTimeout(2, TimeUnit.SECONDS)
 .exceptionally(ex -> User.fallback(uid));
```

## 3. 线程池选择

- **禁止** `supplyAsync` 无参（共用 `ForkJoinPool.commonPool()`）做 IO 密集
- 独立 `Executor` 与 Tomcat/Dubbo 线程隔离 [[线程池-实战调优]]

## 5. 与响应式对比

阻塞式 + CF 在现有 Spring MVC 栈改动小；全链路非阻塞见 [[webflux-响应式入门]] [[reactor-mono与-flux]]。

## 相关

[[java/juc-并发工具类]] · [[java/异步编程面试题]]
