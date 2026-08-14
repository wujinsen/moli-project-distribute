---
title: Feign 超时重试配置
slug: feign-超时重试配置
type: article
status: active
tags: [Feign, Spring Cloud, 超时]
sources:
- raw/wujinsen_markdown/ (enterprise-kb/middleware 专题页)
related: [feign-开发踩坑, openfeign-与-http客户端, rpc-超时重试与链路]
created: 2026-06-21
updated: 2026-07-05
---

# Feign 超时重试配置

> 踩坑 [[middleware/feign-开发踩坑]]；概念 [[middleware/openfeign-与-http客户端]]；全链路 [[middleware/rpc-超时重试与链路]]。

## 1. 超时

```yaml
feign.client.config.default:
 connectTimeout: 3000
 readTimeout: 10000
```

Ribbon/Hystrix 时代需对齐；OpenFeign + LoadBalancer 以 Feign 为准。

## 2. 重试

```java
@Bean
Retryer feignRetryer() {
 return new Retryer.Default(100, 1000, 2);
}
```

| 接口类型 | 重试 |
|----------|------|
| GET 查询 | 可重试 |
| POST 下单/支付 | **关闭** 或幂等键 |

## 3. 与 Dubbo 对比

内部 **Dubbo** 为主 [[middleware/dubbo-超时链路传递]]；Feign 仅 REST 边界。

## 相关

[[middleware/gateway-超时与重试配置]] · [[middleware/接口幂等性实践]]
