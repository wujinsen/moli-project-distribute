---
title: OkHttp 与 HTTP 客户端选型
slug: okhttp-与-http客户端
type: article
status: active
tags: [HTTP, Java, 网络]
sources:
  - raw/wujinsen_markdown/
related: [openfeign-与-http客户端, webclient-与-resttemplate, https与-tls基础, feign-开发踩坑]
created: 2026-06-21
updated: 2026-06-21
---

# OkHttp 与 HTTP 客户端选型

> Feign 底层 [[openfeign-与-http客户端]]；响应式 [[webclient-与-resttemplate]]；TLS [[https与-tls基础]]。

## 1. 客户端对比

| 客户端 | 特点 | 茉莉 |
|--------|------|------|
| **HttpURLConnection** | JDK 内置，难用 | 不推荐 |
| **Apache HttpClient** | 成熟，连接池 | 老项目 |
| **OkHttp** | 简洁、HTTP/2、拦截器 | Feign/Retrofit 常用 |
| **WebClient** | 响应式非阻塞 | WebFlux 栈 |

## 2. OkHttp 要点

```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
    .addInterceptor(chain -> {
        Request req = chain.request().newBuilder()
            .header("X-Trace-Id", MDC.get("traceId")).build();
        return chain.proceed(req);
    })
    .build();
```

- **连接池**复用 TCP；idle 超时回收
- **Interceptor** 统一 traceId [[mdc-日志链路上下文]]

## 3. 与 Dubbo

内部 RPC 优先 **Dubbo** [[dubbo-调用原理与分层]]；OkHttp 用于第三方 HTTP、Webhook、压测脚本。

## 4. 踩坑

- 未关闭 `Response.body()` → 连接泄漏（类似 JDBC [[druid-连接池泄漏排查]]）
- 同步调用占 Tomcat 线程 → 高并发改 WebClient 或专用池

## 相关

[[gateway-超时与重试配置]] · [[rpc-超时重试与链路]]
