---
title: Spring MVC 请求流程
slug: spring-mvc请求流程
type: article
status: active
tags: [spring, SpringMVC, DispatcherServlet]
sources:
 - raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC工作原理.note.md
related: [spring-ioc与bean生命周期, spring-cloud-gateway, 用户中心, 网关, tomcat与-servlet容器, servlet生命周期与请求流程]
created: 2026-06-22
updated: 2026-06-22
---

# Spring MVC 请求流程

> IoC 容器 [[spring/spring-ioc与bean生命周期]]；网关层 [[spring/spring-cloud-gateway]]；Servlet 容器 [[java/tomcat与-servlet容器]]。

各业务服务（、 等）内嵌 **Tomcat + Spring MVC**（Boot 2.3 `spring-boot-starter-web`），处理 REST API。

## 核心组件

| 组件 | 作用 |
|------|------|
| **DispatcherServlet** | 前端控制器，统一入口 |
| **HandlerMapping** | URL → Controller 方法 |
| **HandlerAdapter** | 调用 Controller |
| **ViewResolver** | 逻辑视图 → 视图（REST 常直接 `@ResponseBody`） |

## 请求链路（11 步简记）

```
Client HTTP
 → DispatcherServlet
 → HandlerMapping 找 Handler
 → HandlerAdapter 执行 Controller
 → 返回 ModelAndView 或 @ResponseBody 体
 →（视图则 ViewResolver 渲染）
 → 响应
```

## 与 Gateway 的关系

浏览器 → **Spring Cloud Gateway**（WebFlux）→ HTTP 转发 → 下游 **DispatcherServlet**（Servlet 栈）。两层「分发器」，不要混淆，见 [[spring/spring-cloud-gateway]]。

## 与 Shiro 过滤器

Shiro `Filter` 链在 DispatcherServlet **之前**拦截，未认证请求到不了 Controller，见 [[security/认证与会话机制]]。

## 面试一句话

> DispatcherServlet → Mapping → Adapter → Controller → View/JSON；Gateway 是网关侧响应式路由，MVC 是服务内 Servlet 模型。
