---
title: WebClient 与 RestTemplate 对比
slug: webclient-与-resttemplate
type: article
status: active
tags: [Spring, HTTP, WebFlux]
sources:
 - raw/wujinsen_markdown/
related: [webflux-响应式入门, okhttp-与-http客户端, openfeign-与-http客户端, reactor-mono与-flux]
created: 2026-06-21
updated: 2026-06-21
---

# WebClient 与 RestTemplate 对比

> 响应式 [[webflux-响应式入门]]；OkHttp [[middleware/okhttp-与-http客户端]]；Reactor [[reactor-mono与-flux]]。

## 1. 定位

| | RestTemplate | WebClient |
|---|--------------|-----------|
| 模型 | 阻塞 | 非阻塞（Reactor） |
| Spring 状态 | 维护模式 | 推荐替代 |
| 典型现状 | MVC 可用 | 新 HTTP 边界可选 |

## 2. WebClient 示例

```java
WebClient client = WebClient.builder()
 .baseUrl("http://user-center")
 .defaultHeader("Authorization", token)
 .build();

Mono<User> user = client.get()
 .uri("/api/user/{id}", id)
 .retrieve()
 .bodyToMono(User.class)
 .timeout(Duration.ofSeconds(5));
```

## 3. 何时仍用 RestTemplate / Feign

- 全栈 **Spring MVC + Dubbo**，无阻塞瓶颈 → Feign/Dubbo 足够
- 团队不熟悉 Reactor → 勿为炫技全改 WebClient

## 4. 迁移注意

- 阻塞式 `block()` 在 MVC 线程可接受；在 event loop 线程 **禁止 block**
- 错误映射：`onStatus` / `ExchangeFilterFunction`
- 超时：与 Gateway 层级对齐 [[middleware/gateway-超时与重试配置]]

## 相关

[[java/completablefuture-异步编排]] · [[middleware/feign-超时重试配置]]
