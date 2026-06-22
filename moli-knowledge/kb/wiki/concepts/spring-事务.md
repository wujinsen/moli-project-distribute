---
title: Spring 事务
slug: spring-事务
type: concept
status: active
tags: [spring, 事务, transactional]
sources:
  - raw/wujinsen_markdown/面试笔试/Spring/关于Spring事务的面试题.note.md
  - raw/wujinsen_markdown/Spring/事务/@Transactional失效的几种场景.note.md
related: [spring-事务, spring-boot-自动配置, mysql-索引]
created: 2026-06-22
updated: 2026-06-22
---

# Spring 事务（概念枢纽）

> 面试题详解见同 slug 的 **interview** 页：路径 `interview/spring-事务`（[[spring-事务]] 在 index 的 interview 区）。本文是概念枢纽，与面试页互链。

> ⚠️ 命名说明：interview 页 slug 也为 `spring-事务`，type 不同（concept vs interview）。Query 时按 index 分区区分。

Spring 声明式事务 = **AOP + PlatformTransactionManager**，底层仍是数据库事务（MySQL InnoDB）。茉莉订单/用户写操作依赖 `@Transactional`。

## 核心组件

| 组件 | 作用 |
|------|------|
| `@Transactional` | 声明边界、传播、隔离、回滚规则 |
| `TransactionManager` | 如 `DataSourceTransactionManager` |
| AOP 代理 | 方法前后 commit/rollback |

## ACID 与隔离级别（复习）

ACID：原子性、一致性、隔离性、持久性。  
MySQL 默认 **REPEATABLE READ**；Spring 默认隔离级别 = 数据源默认。

## 7 种传播行为（常用 3 个）

| 传播 | 含义 |
|------|------|
| **REQUIRED**（默认） | 有事务加入，无则新建 |
| **REQUIRES_NEW** | 总是新建，挂起外层 |
| **NESTED** | 嵌套保存点（需 JDBC 支持） |
| SUPPORTS / NOT_SUPPORTED / MANDATORY / NEVER | 按需选用 |

## 失效场景（必记）

1. **非 public 方法**
2. **同类自调用**（绕过代理）
3. **异常被 catch 未抛出**（默认只回滚 RuntimeException/Error）
4. **rollbackFor 未指定 checked 异常**
5. **数据库/引擎不支持**（MyISAM）

详细 Q&A 见 interview 区 [[spring-事务]] 面试页（index → interview）。

## 与自动配置

`DataSourceTransactionManagerAutoConfiguration` 在存在 DataSource 时自动注册 TM，配合 `@EnableTransactionManagement`（Boot 自动开启）。

## 茉莉实践

- 订单落库、用户写操作：Service 层 `@Transactional`
- 秒杀：**Redis 预减 + 异步 MQ 落库**，同步事务边界在消费者侧，见 [[秒杀设计]]
