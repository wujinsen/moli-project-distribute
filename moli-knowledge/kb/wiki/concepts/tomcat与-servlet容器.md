---
title: Tomcat 与 Servlet 容器
slug: tomcat与-servlet容器
type: concept
status: active
tags: [tomcat, servlet, Spring Boot, HTTP]
sources:
  - raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
  - docs/zh-CN/ARCHITECTURE.md
related: [servlet生命周期与请求流程, http与-servlet面试题, spring-mvc请求流程, spring-cloud-gateway, io模型与-netty]
created: 2026-06-22
updated: 2026-06-22
---

# Tomcat 与 Servlet 容器

> 生命周期 [[servlet生命周期与请求流程]]；MVC 映射 [[spring-mvc请求流程]]；网关对比 [[spring-cloud-gateway]]；面试 [[http与-servlet面试题]]。

茉莉业务服务（user-center、order、bi、knowledge）均为 **Spring Boot 内嵌 Tomcat**（Servlet 容器）处理 HTTP。**Gateway** 则用 **Netty**（[[io模型与-netty]]），二者不要混为一谈。

## 1. Tomcat 容器层次

```
Server → Service → Engine → Host → Context → Wrapper(Servlet)
```

| 层级 | 含义 |
|------|------|
| **Context** | 一个 Web 应用（一个 WAR / 一个 Spring Boot 应用） |
| **Wrapper** | 包装单个 Servlet 实例 |

一个 Spring Boot 应用 ≈ 一个 Context；`DispatcherServlet` 是核心 Servlet。

## 2. Servlet 规范角色

| 组件 | 作用 |
|------|------|
| **Servlet** | 处理请求/响应 |
| **Filter** | 请求链前置/后置（Shiro Filter 在此） |
| **Listener** | 容器生命周期事件 |
| **ServletContext** | 应用级上下文 |

Shiro `AuthenticationFilter`、Spring `CharacterEncodingFilter` 都在 **Filter 链** 上，早于 Servlet。

## 3. Spring Boot 与 Tomcat

| 项 | 茉莉默认 |
|----|----------|
| 容器 | 内嵌 Tomcat（spring-boot-starter-web） |
| 端口 | 各服务独立（8888/8087/1128/8090） |
| 线程 | Tomcat 工作线程池处理请求（**BIO/NIO2 容器线程模型**） |
| 最大连接 | `server.tomcat.max-threads` 等可配 |

**阻塞模型**：业务线程在 JDBC、Dubbo 同步调用上阻塞，与 Gateway Netty 非阻塞不同。

## 4. Gateway vs Tomcat 服务

| | Gateway (Netty) | 业务服务 (Tomcat) |
|---|-----------------|-------------------|
| 模型 | WebFlux 响应式 | Servlet 阻塞 |
| 职责 | 路由转发 | 业务 + Shiro + Dubbo |
| 茉莉端口 | 21000 | 8888… |

见 [[spring-cloud-gateway]]。

## 5. 茉莉触点

- **Shiro 过滤器链**在 Tomcat 到达 `DispatcherServlet` 之前 [[shiro-鉴权体系]]
- **Swagger UI** 静态资源由 Tomcat 提供 [[swagger接口调试指南]]
- 压测瓶颈常在 **Tomcat 线程 + Druid 池**，非 Netty 层

## 6. 学习路径

1. Servlet 生命周期与一次 HTTP 请求 [[servlet生命周期与请求流程]]
2. Spring MVC 如何挂到 DispatcherServlet [[spring-mvc请求流程]]
3. 面试题 [[http与-servlet面试题]]
