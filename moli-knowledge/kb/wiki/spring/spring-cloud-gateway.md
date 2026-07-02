---
title: Spring Cloud Gateway
slug: spring-cloud-gateway
type: concept
status: active
tags: [gateway, Spring Cloud, 微服务, WebFlux]
sources:
 - moli-gateway/src/main/resources/application-dev.yml
 - docs/zh-CN/ARCHITECTURE.md
related: [网关, gateway-路由与过滤器, spring-mvc请求流程, 服务调用与架构, 认证与会话机制, io模型与-netty, netty-reactor与线程模型, 跨域与前后端分离]
created: 2026-06-22
updated: 2026-06-22
---

# Spring Cloud Gateway（概念枢纽）

> 实例页 ；路由配置 [[middleware/gateway-路由与过滤器]]；全链路。

**Spring Cloud Gateway** 是 Spring 官方 API 网关：基于 **WebFlux + Netty**（Reactor 模型见 [[middleware/io模型与-netty]]），非 Tomcat Servlet。 `moli-gateway` 端口 **21000**，统一对外 HTTP 入口。

## 与 Spring MVC 对比

| | Gateway | 业务服务 MVC |
|---|---------|--------------|
| 模型 | 响应式 WebFlux | Servlet 阻塞 |
| 职责 | 路由、过滤、聚合 | 业务 REST |
| 端口 | 21000 | 8888/8087/… |

见 [[spring/spring-mvc请求流程]]。

## 核心概念

| 概念 | 说明 |
|------|------|
| **Route** | id + URI + Predicate + Filter |
| **Predicate** | 匹配条件，如 `Path=/UserCenter/**` |
| **Filter** | 改写请求/响应，如 `StripPrefix=1` |
| **URI** | `lb://service-name` 经 Nacos + Ribbon 负载均衡 |

## 典型访问

```
http://localhost:21000/UserCenter/login
 → StripPrefix → user-center:8888/login
```

## 演进方向

- 网关统一鉴权 / JWT 校验（可选）
- Sentinel 限流（秒杀/登录防刷）
- 全局 CORS、请求日志、TraceId — 前端跨域见 [[middleware/跨域与前后端分离]]

配置细节 [[middleware/gateway-路由与过滤器]]。
