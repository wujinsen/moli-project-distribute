---
title: SpringApplication 启动流程
slug: spring-application启动流程
type: article
status: active
tags: [spring-boot, 启动, 生命周期]
sources:
  - raw/wujinsen_markdown/Spring/SpringBoot源码解析/SpringApplication初始化阶段.note.md
  - raw/wujinsen_markdown/Spring/Spring、SpringMVC和SpringBoot看这一篇就够了！.note.md
related: [spring-boot-自动配置, enableautoconfiguration原理, 服务调用与架构]
created: 2026-06-22
updated: 2026-06-22
---

# SpringApplication 启动流程

> 自动配置 [[enableautoconfiguration原理]]；茉莉架构 [[服务调用与架构]]。

## 启动入口

```java
SpringApplication.run(UserCenterApplication.class, args);
```

## 主要阶段（简化）

1. **推断应用类型** — Web / Reactive / None（是否 Servlet、DispatcherServlet）
2. **加载 ApplicationContextInitializer** — `META-INF/spring.factories`
3. **加载 ApplicationListener** — 早期事件监听
4. **推断主类** — 含 main 的启动类
5. **创建 ApplicationContext** — Servlet 环境用 `AnnotationConfigServletWebServerApplicationContext`
6. **prepareContext** — 打印 Banner、加载配置源（application.yml、bootstrap.yml）
7. **refreshContext** — **Spring 容器核心 refresh**（Bean 定义加载、Bean 实例化、AutoConfiguration 导入）
8. **callRunners** — `ApplicationRunner` / `CommandLineRunner`
9. **发布 Started 事件** — 内嵌 Tomcat/Jetty 开始监听端口

## bootstrap vs application

茉莉各服务通常：

- `bootstrap.yml` — Nacos 地址、应用名（Spring Cloud 上下文优先加载）
- `application-dev.yml` — 数据源、Redis、Dubbo、端口

## refresh 核心（与自动配置交汇）

`AbstractApplicationContext.refresh()` → `invokeBeanFactoryPostProcessors` → 处理 `@Configuration` 与 **ConfigurationClassPostProcessor** → 解析 `@Import(AutoConfigurationImportSelector)` → 自动配置 Bean 注册。

## 多服务启动顺序（茉莉）

Nacos → Redis → MySQL → **user-center** → order/bi/knowledge → **gateway**。见 [[本地启动指南]]。

## 常见问题

- **端口占用** — 改 `server.port` 或杀占用进程
- **Bean 循环依赖** — 构造器注入循环在 Boot 2.6+ 更严格；改 `@Lazy` 或 setter 注入
- **AutoConfiguration 未生效** — 查 `@ConditionalOnClass`、exclude 配置
