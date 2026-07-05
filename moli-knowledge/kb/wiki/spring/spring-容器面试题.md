---
title: Spring 容器（面试题系列）
slug: spring-容器面试题
type: interview
status: active
tags: [spring, IoC, 循环依赖, 面试题]
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
related: [spring-ioc与bean生命周期, spring-三级缓存与循环依赖, spring-mvc请求流程, spring-boot-自动配置]
created: 2026-06-22
updated: 2026-07-05
---

# Spring 容器（面试题系列）

> [[spring/spring-ioc与bean生命周期]] [[spring/spring-三级缓存与循环依赖]] [[spring/spring-mvc请求流程]] [[spring/spring-boot-自动配置]]

## Q1. IoC 和 DI？

控制反转：对象由容器创建管理。依赖注入：容器注入依赖（@Autowired/构造器）。

## Q2. Bean 生命周期主要步骤？

实例化 → 属性注入 → 初始化 → AOP 代理 → 放入单例池。见 [[spring/spring-ioc与bean生命周期]]。

## Q3. 循环依赖怎么解？

单例 + 字段/setter：三级缓存提前暴露；构造器循环依赖无法解。见 [[spring/spring-三级缓存与循环依赖]]。

## Q4. 三级缓存分别存什么？

singletonObjects / earlySingletonObjects / singletonFactories。

## Q5. 为什么需要 singletonFactories？

支持循环依赖场景下 **AOP 早期代理**（getEarlyBeanReference）。

## Q6. prototype 能循环依赖吗？

不能，无缓存 early expose。

## Q7. @Autowired 原理？

AutowiredAnnotationBeanPostProcessor 在属性填充阶段从容器 getBean。

## Q8. Spring MVC 流程？

DispatcherServlet → HandlerMapping → HandlerAdapter → Controller → View/JSON。见 [[spring/spring-mvc请求流程]]。

## Q9. Gateway 和 MVC 区别？

Gateway WebFlux 网关路由；MVC Servlet 业务处理。见 [[spring/spring-cloud-gateway]]。

## Q10. Boot 和 Spring 容器关系？

Boot 启动 SpringApplication，自动配置向容器注册大量 AutoConfiguration Bean。
