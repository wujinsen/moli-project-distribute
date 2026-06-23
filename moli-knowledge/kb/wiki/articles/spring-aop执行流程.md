---
title: Spring AOP 执行流程
slug: spring-aop执行流程
type: article
status: active
tags: [Spring, AOP, 源码]
sources:
  - raw/wujinsen_markdown/面试笔试/Spring/69道Spring面试题和答案.note.md
related: [spring-aop与代理, spring-mvc请求流程, spring-声明式事务, spring-容器面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Spring AOP 执行流程

> 概念 [[spring-aop与代理]]。

## 1. Bean 创建时织入

```
BeanDefinition → instantiate → populate
→ AbstractAutoProxyCreator.postProcessAfterInitialization
→ 匹配 Advisor → 创建 Proxy 包装原始 Bean
```

容器 getBean 拿到的是 **代理对象**。

## 2. 一次方法调用

```
Client → Proxy.invoke
→ ReflectiveMethodInvocation.proceed()
→ 按 Order 执行拦截器链（含 TransactionInterceptor）
→ targetMethod.invoke（真实 Bean）
```

## 3. 与 MVC / 事务顺序

| 层次 | 组件 |
|------|------|
| Web | Filter → Shiro → DispatcherServlet [[spring-mvc请求流程]] |
| Service | AOP 事务、权限 |
| DAO | MyBatis 非 Spring AOP 默认切点 |

`@Transactional` 仅对 **public** 方法、**外部调用** 生效。

## 4. 排查事务不生效

1. 是否代理 Bean（注入接口实现）
2. 是否自调用
3. 异常是否被吞（默认 rollbackFor=RuntimeException）
4. 多数据源是否绑错 TM

见 [[spring-事务面试题]]。

## 相关

[[spring-ioc与bean生命周期]] · [[mybatis-与-druid持久层]]
