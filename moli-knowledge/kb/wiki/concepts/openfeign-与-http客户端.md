---
title: OpenFeign 与 HTTP 客户端
slug: openfeign-与-http客户端
type: concept
status: active
tags: [Feign, Spring Cloud, HTTP, REST]
sources:
 - raw/wujinsen_markdown/源码分析/OpenFeign/什么是Feign.note.md
related: [feign-开发踩坑, dubbo-与-nacos, 服务调用与架构, spring-cloud-gateway]
created: 2026-06-22
updated: 2026-06-22
---

# OpenFeign 与 HTTP 客户端

> 跨服务主通道是 **Dubbo RPC** [[dubbo-与-nacos]]，不是 Feign；Feign 适用于 **HTTP REST** 微服务或调第三方 OpenAPI。

## 1. Feign 是什么

声明式 HTTP 客户端：接口 + 注解 → 动态代理发 HTTP，集成 Ribbon（负载）、Hystrix/Sentinel（熔断）。

```java
@FeignClient(name = "order-service")
public interface OrderClient {
 @GetMapping("/orders/{id}")
 OrderDto get(@PathVariable("id") Long id);
}
```

## 2. vs Dubbo

| | OpenFeign | Dubbo（） |
|---|-----------|---------------|
| 协议 | HTTP/JSON | Dubbo 二进制 |
| 注册 | Eureka/Nacos | Nacos |
| 性能 | 较低 | 较高 |
| 场景 | 对外 REST、异构 | 内部 Java 服务 |

## 3. 与 Gateway

Browser → [[spring-cloud-gateway]] → 各服务 HTTP；**服务间**用 Dubbo 调。

## 4. 常见配置

- 超时、重试（写操作慎开重试）
- 请求头传递 `Authorization`
- 日志 `Logger.Level.FULL` 仅 dev

## 相关

[[feign-开发踩坑]] · [[跨域与前后端分离]]
