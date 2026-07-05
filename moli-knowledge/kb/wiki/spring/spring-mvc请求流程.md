---
title: Spring MVC 请求流程
slug: spring-mvc请求流程
type: article
status: active
tags: [spring, SpringMVC, DispatcherServlet]
sources:
- raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md
- raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md
- raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguration自动装配.note.md
- raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguraton自动装配原理.note.md
- raw/wujinsen_markdown/Spring/SpringBoot源码解析/SpringApplication初始化阶段.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Java 必须掌握的 12 种 Spring 常用注解！.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/ModelMap、ModelAndView和@Modelattribute的区别.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Spring 中经典的 9 种设计模式，打死也要记住啊！.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Spring 事务管理探究.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/Spring 最常用的 7 个注解，你用哪几个？.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC工作原理.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC接收复杂集合参数.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/defaultServlet.note.md
- raw/wujinsen_markdown/Spring/SpringMVC/spring service事务传播.note.md
- raw/wujinsen_markdown/Spring/Spring、SpringMVC和SpringBoot看这一篇就够了！.note.md
- raw/wujinsen_markdown/Spring/Spring循环依赖原理解析.note.md
- raw/wujinsen_markdown/Spring/Spring源码分析：@Autowired注解原理分析.note.md
- raw/wujinsen_markdown/Spring/Spring解析，加载及实例化Bean的顺序（零配置）.note.md
- raw/wujinsen_markdown/Spring/事务/@Transactional失效的几种场景.note.md
- raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md
- raw/wujinsen_markdown/Spring/什么是循环依赖.note.md
- raw/wujinsen_markdown/Spring/深入理解 Spring 事务原理 传播属性.note.md
- raw/wujinsen_markdown/Spring/真实项目中 ThreadLocal 的妙用.note.md
- raw/wujinsen_markdown/Spring/采坑记录.note.md
- raw/wujinsen_markdown/Spring/采坑记录/springboot与web前端的下划线与驼峰的json转换配置.note.md
related: [spring-ioc与bean生命周期, spring-cloud-gateway, tomcat与-servlet容器, servlet生命周期与请求流程]
created: 2026-06-22
updated: 2026-07-05
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

## 批次#1320 增补（wujinsen Phase2 P0）

合并 `Spring/SpringMVC/` 原理与设计模式 raw。
