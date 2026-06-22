---
title: Spring Boot（面试题系列）
slug: spring-boot-面试题
type: interview
status: active
tags: [spring-boot, 自动配置, 面试题]
sources:
  - raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguraton自动装配原理.note.md
  - raw/wujinsen_markdown/Spring/SpringBoot源码解析/SpringApplication初始化阶段.note.md
related: [spring-boot-自动配置, enableautoconfiguration原理, spring-application启动流程, spring-声明式事务]
created: 2026-06-22
updated: 2026-06-22
---

# Spring Boot（面试题系列）

> [[spring-boot-自动配置]] [[enableautoconfiguration原理]] [[spring-application启动流程]]

## Q1. @SpringBootApplication 组成？

@Configuration + @EnableAutoConfiguration + @ComponentScan。

## Q2. 自动配置原理？

@EnableAutoConfiguration → AutoConfigurationImportSelector → 读 `META-INF/spring.factories` → @Conditional 过滤 → 注册 Bean。见 [[enableautoconfiguration原理]]。

## Q3. 如何排除某个 AutoConfiguration？

`@SpringBootApplication(exclude=...)` 或 `spring.autoconfigure.exclude`。

## Q4. @ConditionalOnMissingBean 作用？

容器里已有该 Bean 则跳过，方便用户自定义覆盖。

## Q5. Starter 是什么？

依赖聚合 + 自动配置 + 默认配置；如 `spring-boot-starter-web`。

## Q6. bootstrap.yml 和 application.yml？

bootstrap 优先加载，常用于 Nacos/Spring Cloud；application 放业务配置。

## Q7. Spring Boot 2.x vs 3.x 自动配置注册？

2.x 用 spring.factories；3.x 用 `AutoConfiguration.imports`。

## Q8. 如何调试哪些自动配置生效？

`debug: true` 或 `--debug`。

## Q9. 内嵌 Tomcat 何时启动？

refresh 过程中 `onRefresh` → 创建 WebServer → 发布 Started 事件后监听端口。

## Q10. 与 Spring 事务关系？

Boot 自动 DataSourceTransactionManager；声明式事务见 [[spring-声明式事务]]、[[spring-事务]]。
