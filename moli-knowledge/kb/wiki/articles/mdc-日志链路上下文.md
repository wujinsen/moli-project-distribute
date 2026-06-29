---
title: MDC 日志链路上下文
slug: mdc-日志链路上下文
type: article
status: active
tags: [日志, 可观测性, 并发]
sources:
 - raw/wujinsen_markdown/
related: [logback-日志配置, threadlocal-与上下文传递, skywalking-链路追踪, 故障排查指南]
created: 2026-06-21
updated: 2026-06-21
---

# MDC 日志链路上下文

> Logback 配置 [[logback-日志配置]]；ThreadLocal [[threadlocal-与上下文传递]]；APM [[skywalking-链路追踪]]。

**MDC**（Mapped Diagnostic Context）在日志 pattern 中注入 `traceId` / `userId`，grep 全链路日志。

## 1. Logback 配置

```xml
<pattern>%d{HH:mm:ss} [%thread] [%X{traceId}] %-5level %logger - %msg%n</pattern>
```

Gateway 或 Filter 入口：

```java
String traceId = request.getHeader("X-Trace-Id");
if (traceId == null) traceId = UUID.randomUUID().toString().replace("-", "");
MDC.put("traceId", traceId);
try {
 filterChain.doFilter(request, response);
} finally {
 MDC.remove("traceId");
}
```

## 2. 与 SkyWalking 关系

| 能力 | MDC | SkyWalking |
|------|-----|------------|
| 成本 | 低，改 log 即可 | Agent 无侵入 |
| 跨服务 | 需透传 Header | 自动 TraceId |
| | dev 够用 | 生产推荐 |

可两者并存：SW 生成 traceId 写入 MDC。

## 3. 异步与线程池

子线程默认无 MDC → **TTL MDC 包装** 或任务提交时拷贝 Map：

```java
Map<String, String> ctx = MDC.getCopyOfContextMap();
executor.execute(() -> {
 if (ctx != null) MDC.setContextMap(ctx);
 try { work(); } finally { MDC.clear(); }
});
```

见 [[transmittable-thread-local跨线程]]。

## 相关

[[micrometer-与指标暴露]] · [[spring-async与线程池]]
