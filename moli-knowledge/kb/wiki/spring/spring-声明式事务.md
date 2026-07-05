---
title: Spring 声明式事务
slug: spring-声明式事务
type: concept
status: active
tags: [spring, 事务, transactional]
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
- raw/wujinsen_markdown/面试笔试/Spring/关于Spring事务的面试题.note.md
related: [spring-事务, spring-boot-自动配置]
created: 2026-06-22
updated: 2026-07-05
---

# Spring 声明式事务（概念枢纽）

> 面试题系列 [[spring/spring-事务]]（interview 页）；自动配置 [[spring/spring-boot-自动配置]]。

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

其余 SUPPORTS / NOT_SUPPORTED / MANDATORY / NEVER 见 [[spring/spring-事务]] Q3。

## 失效场景（必记）

1. 非 public 方法
2. **同类自调用**（绕过代理）
3. 异常被 catch 未抛出
4. rollbackFor 未含 checked 异常
5. MyISAM 等不支持事务的引擎

详情见 [[spring/spring-事务]] interview 页 Q5～Q7。

## 与 Boot 自动配置

`DataSourceTransactionManagerAutoConfiguration` 在存在 DataSource 时注册 TM；Boot 自动 `@EnableTransactionManagement`。
