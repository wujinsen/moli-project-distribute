---
title: Spring Boot 启动优化
slug: spring-boot-启动优化
type: article
status: active
tags: [Spring Boot, 性能, JVM]
sources:
- raw/wujinsen_markdown/ (enterprise-kb/spring 专题页)
related: [spring-boot-自动配置, jvm-内存与gc, production-jvm启动参数]
created: 2026-06-21
updated: 2026-07-05
---

# Spring Boot 启动优化

> 自动配置 [[spring/spring-boot-自动配置]]；类加载 [[java/jvm-内存与gc]]；JVM 参数 [[java/production-jvm启动参数]]。

## 1. 耗时构成

组件扫描 → 自动配置评估 → Bean 初始化 → Dubbo/Nacos 注册。

## 2. 常用手段

| 手段 | 说明 |
|------|------|
| lazy-init | `@Lazy` / `spring.main.lazy-initialization`（慎用首请求延迟） |
| 缩小扫描 | `@SpringBootApplication(scanBasePackages=...)` |
| 排除无用自动配置 | `@EnableAutoConfiguration(exclude=...)` |
| 索引 cache | Spring 2.7+ `spring-context-index` |
| 延迟注册 | Dubbo `register=false` 本地 dev |

## 4. 测量

`SpringApplicationRunListeners` / Actuator `startup` endpoint [[ops/prometheus-告警规则设计]]。

## 相关

[[java/jvm-gc调优实战]] · [[java/arthas-在线诊断]]
