---
title: Spring 声明式事务
slug: spring-声明式事务
type: concept
status: active
tags: [spring, 事务, transactional]
sources:
 - raw/wujinsen_markdown/面试笔试/Spring/关于Spring事务的面试题.note.md
 - raw/wujinsen_markdown/Spring/事务/@Transactional失效的几种场景.note.md
related: [spring-事务, spring-boot-自动配置, 秒杀设计]
created: 2026-06-22
updated: 2026-06-22
---

# Spring 声明式事务（概念枢纽）

> 面试题系列 [[spring-事务]]（interview 页）；自动配置 [[spring-boot-自动配置]]。

Spring 声明式事务 = **AOP + PlatformTransactionManager**，底层是数据库事务（MySQL InnoDB）。订单/用户写操作依赖 `@Transactional`。

## 核心组件

| 组件 | 作用 |
|------|------|
| `@Transactional` | 声明边界、传播、隔离、回滚规则 |
| `TransactionManager` | 如 `DataSourceTransactionManager` |
| AOP 代理 | 方法前后 commit/rollback |

## 传播行为（常用）

| 传播 | 含义 |
|------|------|
| **REQUIRED**（默认） | 有事务加入，无则新建 |
| **REQUIRES_NEW** | 总是新建，挂起外层 |
| **NESTED** | 嵌套保存点 |

其余 SUPPORTS / NOT_SUPPORTED / MANDATORY / NEVER 见 [[spring-事务]] Q3。

## 失效场景（必记）

1. 非 public 方法
2. **同类自调用**（绕过代理）
3. 异常被 catch 未抛出
4. rollbackFor 未含 checked 异常
5. MyISAM 等不支持事务的引擎

详情见 [[spring-事务]] interview 页 Q5～Q7。

## 与 Boot 自动配置

`DataSourceTransactionManagerAutoConfiguration` 在存在 DataSource 时注册 TM；Boot 自动 `@EnableTransactionManagement`。
