---
title: Gateway 路由与过滤器
slug: gateway-路由与过滤器
type: article
status: active
tags: [gateway, 路由, Filter, Sentinel]
sources:
 - moli-gateway/src/main/resources/application-dev.yml
 - moli-gateway/src/main/resources/application-loadtest.yml
related: [spring-cloud-gateway, 网关, 服务调用与架构, 秒杀设计, 故障排查指南, sentinel-限流与熔断, sentinel-接入与规则配置]
created: 2026-06-22
updated: 2026-06-22
---

# Gateway 路由与过滤器

> 概念 [[spring-cloud-gateway]]；服务页。

## Discovery 配置

```yaml
spring.cloud.gateway.discovery.locator.lower-case-service-id: true
```

支持小写服务 id；路由仍以显式 `routes` 为主。

## 常用 Filter 类型（扩展）

| Filter | 作用 |
|--------|------|
| StripPrefix | 去路径前缀 |
| AddRequestHeader | 加请求头（如 X-Trace-Id） |
| RequestRateLimiter | 限流（需 Redis + 配置） |
| Retry | 失败重试 |

**现状**：仅 StripPrefix；限流未启用。

## loadtest profile

压测配置可能**不含** knowledge 路由，仅 UserCenter/Order/Bi。压秒杀时注意 profile，见项目 `load-test/` 与。

## Sentinel 接入（规划）

架构建议网关 + 热点参数限流。详细步骤与规则类型见 [[sentinel-接入与规则配置]]；概念 [[sentinel-限流与熔断]]。

接入步骤概要：

1. 引 `spring-cloud-starter-alibaba-sentinel` + gateway adapter
2. 配 Sentinel Dashboard / Nacos 规则源
3. 对 `/UserCenter/login`、`/OrderServer/seckill/**` 等配 QPS/并发线程数

当前排查「网关没限流」属预期行为，见。

## 调试

- `GET http://localhost:21000/actuator/gateway/routes`（若暴露 actuator）
- 404：检查 Path 前缀、StripPrefix、下游 context-path
- 502：下游未注册 Nacos 或未启动
