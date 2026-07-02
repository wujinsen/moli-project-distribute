---
title: Spring @Async 与线程池
slug: spring-async与线程池
type: concept
status: active
tags: [Spring, 并发, 异步]
sources:
 - raw/wujinsen_markdown/面试笔试/Spring/69道Spring面试题和答案.note.md
related: [spring-事件机制, 线程池-实战调优, completablefuture-异步编排, java-并发]
created: 2026-06-21
updated: 2026-06-21
---

# Spring @Async 与线程池

> 事件解耦 [[spring-事件机制]]；线程池参数 [[线程池-实战调优]]；编排 [[java/completablefuture-异步编排]]。

**@Async** 让方法在独立线程执行，适合邮件、审计、非关键 IO，**不能**替代 MQ 做可靠异步。

## 1. 启用与用法

```java
@EnableAsync
@Configuration
class AsyncConfig {
 @Bean("bizExecutor")
 Executor bizExecutor() {
 ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
 ex.setCorePoolSize(8);
 ex.setMaxPoolSize(32);
 ex.setQueueCapacity(500);
 ex.setThreadNamePrefix("biz-async-");
 ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
 ex.initialize();
 return ex;
 }
}

@Service
class NotifyService {
 @Async("bizExecutor")
 public void sendAfterOrder(Long orderId) { /* ... */ }
}
```

- 默认代理：**Spring AOP**，同类自调用不生效（同 [[spring/spring-aop与代理]] 自调用陷阱）
- 返回值：`void` / `Future` / `CompletableFuture`

## 2. 与 @Transactional 顺序

`@Async` 方法内**新开线程**，事务上下文**不会**自动传播 → 需要数据一致性时改 **MQ + 消费者事务** 或显式传参。

## 4. 常见坑

- **无界队列** → OOM；秒杀/网关路径禁用默认 SimpleAsyncTaskExecutor（每任务一新线程）
- **异常吞掉**：`AsyncUncaughtExceptionHandler` 必须打日志
- **ThreadLocal 丢失**：Shiro Session、traceId → [[security/threadlocal-与上下文传递]] [[java/mdc-日志链路上下文]]

## 相关

[[java-虚拟线程]] · [[java/异步编程面试题]]
