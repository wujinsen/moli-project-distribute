---
title: Spring Boot 自动配置
slug: spring-boot-自动配置
type: concept
status: active
tags: [spring-boot, 自动配置, starter]
sources:
  - raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguration自动装配.note.md
  - raw/wujinsen_markdown/Spring/SpringBoot源码解析/@EnableAutoConfiguraton自动装配原理.note.md
  - raw/wujinsen_markdown/Spring/SpringBoot源码解析/SpringApplication初始化阶段.note.md
related: [enableautoconfiguration原理, spring-application启动流程, spring-声明式事务, spring-boot-面试题, 技术栈与版本]
created: 2026-06-22
updated: 2026-06-22
---

# Spring Boot 自动配置（概念枢纽）

> 原理 [[enableautoconfiguration原理]]；启动流程 [[spring-application启动流程]]；事务 [[spring-声明式事务]]；面试 [[spring-boot-面试题]]。

茉莉各微服务基于 **Spring Boot 2.3.12**（见 [[技术栈与版本]]），自动配置是「开箱即用」的核心。

## @SpringBootApplication 三合一

```java
@SpringBootApplication
// = @Configuration + @EnableAutoConfiguration + @ComponentScan
```

| 注解 | 作用 |
|------|------|
| `@Configuration` | 当前类为配置源，可 `@Bean` |
| `@EnableAutoConfiguration` | **自动装配**第三方 Starter |
| `@ComponentScan` | 扫描 `@Component`/`@Service`/`@Controller` 等 |

## 自动配置做了什么？

根据 **classpath 上的 jar** 和 **配置项**，条件性地注册 Bean。例如：

- 有 `DataSource` + JDBC → 配 DataSourceAutoConfiguration
- 有 Redis → RedisAutoConfiguration
- 有 Dubbo → Dubbo 相关 AutoConfiguration

业务模块只需引 starter + 写 `application.yml`，不必手写大量 `@Bean`。

## 与茉莉模块的关系

| 模块 | 典型自动配置触点 |
|------|------------------|
| user-center | Shiro、Druid、MyBatis-Plus、Redis |
| order | 同上 + 秒杀 Scheduling |
| knowledge | 同上 + kb 自定义 `@Configuration` |
| gateway | Gateway、Nacos Discovery |

自定义配置类（如 `SeckillSchedulingConfig`）与自动配置 **并存**：AutoConfig 打底，业务 `@Configuration` 扩展。

## 调试自动配置

```yaml
debug: true   # 启动日志打印 Positive/Negative matches
```

或 `--debug` 启动参数，查看哪些 AutoConfiguration 生效/被 `@Conditional` 跳过。

## 扩展阅读

- 装配源码链：[[enableautoconfiguration原理]]
- Bean 创建与循环依赖：Spring 容器启动时处理，Boot 2.3 支持构造器循环依赖（有限）
