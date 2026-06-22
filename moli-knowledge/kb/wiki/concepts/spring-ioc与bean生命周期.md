---
title: Spring IoC 与 Bean 生命周期
slug: spring-ioc与bean生命周期
type: concept
status: active
tags: [spring, IoC, Bean, 生命周期]
sources:
  - raw/wujinsen_markdown/Spring/Spring循环依赖原理解析.note.md
  - raw/wujinsen_markdown/Spring/什么是循环依赖.note.md
  - raw/wujinsen_markdown/Spring/Spring解析，加载及实例化Bean的顺序（零配置）.note.md
related: [spring-三级缓存与循环依赖, spring-mvc请求流程, spring-容器面试题, spring-boot-自动配置, spring-声明式事务]
created: 2026-06-22
updated: 2026-06-22
---

# Spring IoC 与 Bean 生命周期（概念枢纽）

> 循环依赖 [[spring-三级缓存与循环依赖]]；Web 请求 [[spring-mvc请求流程]]；面试 [[spring-容器面试题]]；Boot 装配 [[spring-boot-自动配置]]。

**IoC**：对象创建与依赖关系由 Spring 容器管理，开发者声明 `@Component/@Service` 与 `@Autowired`，而非 `new`。

## Bean 生命周期（单例，简化）

```
扫描 → BeanDefinition
  → 实例化（构造器）
  → 属性填充（依赖注入）
  → Aware / BeanPostProcessor 前置
  → 初始化（@PostConstruct、InitializingBean）
  → BeanPostProcessor 后置（含 AOP 代理）
  → 放入 singletonObjects
  → 销毁（容器关闭）
```

循环依赖发生在 **实例化之后、属性填充** 阶段，见 [[spring-三级缓存与循环依赖]]。

## 作用域

| 作用域 | 说明 |
|--------|------|
| **singleton**（默认） | 容器内唯一，三级缓存仅解决单例循环依赖 |
| prototype | 每次 getBean 新建，**不**走三级缓存 |
| request/session | Web 环境 |

## 与茉莉项目

- 各 `*Application` 启动后容器加载 Controller、Service、Mapper、Dubbo `@Service` 等
- `user-center-shiro-starter` 通过 `spring.factories` 自动注册 Shiro 相关 Bean，见 [[spring-boot-自动配置]]
- `@Transactional` 依赖 AOP 代理 Bean，与循环依赖 + 三级缓存中的 **earlyReference** 相关

## 常见扩展点

- **BeanPostProcessor**：AOP、属性校验
- **BeanFactoryPostProcessor**：修改 BeanDefinition（如配置中心刷新）
- **ApplicationListener**：启动事件（见 [[spring-application启动流程]]）

## 设计原则

- 优先构造器注入（不可变、易测）；字段注入简洁但难测
- 避免 prototype 注入 singleton 的反向依赖等复杂 scope 组合
- 循环依赖应 **从设计上消除**；三级缓存是兜底而非最佳实践
