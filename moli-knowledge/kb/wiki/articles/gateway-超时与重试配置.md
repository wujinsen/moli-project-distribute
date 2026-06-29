---
title: Gateway 超时与重试配置
slug: gateway-超时与重试配置
type: article
status: active
tags: [Gateway, Spring Cloud, 超时]
sources:
 - raw/wujinsen_markdown/
related: [spring-cloud-gateway, rpc-超时重试与链路, sentinel-限流与熔断]
created: 2026-06-21
updated: 2026-06-21
---

# Gateway 超时与重试配置

> Gateway 概念 [[spring-cloud-gateway]]；RPC 超时 [[rpc-超时重试与链路]]；限流 [[sentinel-限流与熔断]]。

## 1. 超时层级

```
Client → Gateway → 下游 HTTP/Dubbo
 ↑ 此处需小于客户端超时，并大于最慢下游 P99
```

## 2. 配置示例

```yaml
spring.cloud.gateway:
 httpclient:
 connect-timeout: 3000
 response-timeout: 10s
 routes:
 - id: user-center
 filters:
 - name: Retry
 args:
 retries: 2
 statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
```

**写接口慎用 Retry**，配合幂等 [[接口幂等性实践]]。

## 3. 与 Sentinel

Gateway 集成 Sentinel 规则 [[gateway-接入-sentinel规划]]：慢调用比例、异常比例熔断。

## 相关

[[gateway-断言与请求改写]] · [[feign-超时重试配置]]
