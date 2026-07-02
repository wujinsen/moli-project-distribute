---
title: Spring 框架中的设计模式
slug: spring框架中的设计模式
type: article
status: active
tags: [Spring, 设计模式, IoC, AOP]
sources:
 - raw/wujinsen_markdown/Spring/SpringMVC/Spring 中经典的 9 种设计模式，打死也要记住啊！.note.md
related: [设计模式, spring-ioc与bean生命周期, spring-mvc请求流程, spring-三级缓存与循环依赖, enableautoconfiguration原理]
created: 2026-06-22
updated: 2026-06-22
---

# Spring 框架中的设计模式

> 枢纽 [[patterns/设计模式]]；MVC [[spring/spring-mvc请求流程]]；单例缓存 [[spring/spring-三级缓存与循环依赖]]。

## 1. 简单工厂（非 GoF 23）

**BeanFactory** 根据 name/type 返回 Bean：`getBean("userService")`。

- 启动：XML/注解 → `BeanDefinition` → 注册 `BeanFactory`
- 扩展点：`BeanFactoryPostProcessor`（如 `PropertyPlaceholderConfigurer` 占位符）

意义：**松耦合**依赖注入；生命周期钩子（Aware、`*PostProcessor`）。

## 2. 工厂方法

**FactoryBean**：容器 getBean 时实际调用 `getObject()`。

典型：**MyBatis** `SqlSessionFactoryBean` → 得到 `SqlSessionFactory`。各服务 MyBatis-Plus 底层同类思路。

## 3. 单例

默认 scope **singleton**。`AbstractBeanFactory.getBean` → `getSingleton()`，配合三级缓存解决循环依赖 + **双重检查锁**。

> Spring 管的是 Bean 单例，不是禁止你用 `new` 造多个类实例。

## 4. 适配器

**HandlerAdapter**：不同 Controller（注解/旧接口）统一执行入口。

`DispatcherServlet` → `HandlerMapping` 找 handler → **HandlerAdapter** 执行 → `ModelAndView`/Response。

扩展新 Controller 类型 = 新 Adapter，符合开闭原则。

## 5. 装饰器

类名含 **Wrapper** / **Decorator**：包装 InputStream、HttpServletRequest 等，动态加职责。

## 6. 代理（AOP）

**动态代理**织入切面：JDK 接口代理 / **CGLIB** 子类。

运行时创建代理对象，事务 `@Transactional`、Shiro、日志切面均依赖此模式。

## 7. 观察者

| 角色 | Spring API |
|------|------------|
| 事件 | `ApplicationEvent` |
| 监听器 | `ApplicationListener` |
| 发布 | `ApplicationContext.publishEvent()` |

用于启动后初始化、配置刷新等；与 MQ 观察者不同（[[middleware/消息队列]]）。

## 8. 策略

**Resource** 接口：`ClassPathResource`、`UrlResource`、`FileSystemResource`… 按策略加载配置。

Dubbo **LoadBalance**（random、roundrobin…）也是策略族。

## 9. 模板方法

**JdbcTemplate**：固定获取连接→执行→异常转换→释放，子步骤回调。

`RestTemplate`、许多 `*Template` 同理。 JDBC 现以 **MyBatis-Plus** 为主，模板方法更多在 Spring 基础设施层。

## 10. 对照表（面试用）

| # | 模式 | Spring 落点 |
|---|------|-------------|
| 1 | 简单工厂 | BeanFactory |
| 2 | 工厂方法 | FactoryBean |
| 3 | 单例 | singleton + getSingleton |
| 4 | 适配器 | HandlerAdapter |
| 5 | 装饰器 | Wrapper 类 |
| 6 | 代理 | AOP |
| 7 | 观察者 | ApplicationEvent |
| 8 | 策略 | Resource / LoadBalance |
| 9 | 模板方法 | JdbcTemplate 等 |
