---
title: Spring AOP 与代理
slug: spring-aop与代理
type: concept
status: active
tags: [Spring, AOP, 代理]
sources:
  - raw/wujinsen_markdown/面试笔试/Spring/69道Spring面试题和答案.note.md
  - raw/wujinsen_markdown/面试笔试/spring框架中的设计模式.note.md
related: [spring-aop执行流程, spring框架中的设计模式, spring-声明式事务, spring-ioc与bean生命周期]
created: 2026-06-22
updated: 2026-06-22
---

# Spring AOP 与代理

> 执行流程 [[spring-aop执行流程]]；事务 [[spring-声明式事务]]；设计模式 [[spring框架中的设计模式]]。

**AOP**（面向切面）：将日志、事务、权限等**横切关注点**从业务代码剥离。

## 1. 核心术语

| 术语 | 说明 |
|------|------|
| Aspect | 切面 = 通知 + 切点 |
| Join Point | 连接点（方法执行） |
| Pointcut | 切点表达式 |
| Advice | Before / After / Around / AfterReturning / AfterThrowing |

## 2. 两种代理

| | JDK 动态代理 | CGLIB |
|---|-------------|-------|
| 条件 | 实现接口 | 类无接口或 `proxyTargetClass=true` |
| 原理 | Proxy + InvocationHandler | 子类继承 |
| 茉莉 | Dubbo/Feign 接口常见 | `@Transactional` 在 class 上 |

## 3. 茉莉触点

- `@Transactional` → AOP 代理 + TransactionInterceptor
- Shiro `@RequiresPermissions` → 权限切面
- 自定义 `@Log` 审计（若扩展）

## 4. 自调用陷阱

同类内 `this.method()` **不走代理** → 事务/权限失效。解决：注入 self、AspectJ 编译织入、拆 Service。

## 相关

[[spring-事务面试题]] · [[shiro-鉴权体系]]
