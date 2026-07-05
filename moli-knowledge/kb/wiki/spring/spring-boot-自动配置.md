---
title: Spring Boot 自动配置
slug: spring-boot-自动配置
type: concept
status: active
tags: [spring-boot, 自动配置, starter]
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
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/2.0/Spring Boot 2.0 - WebFlux framework.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/Spring Boot改变JDK版本编译.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/quesion/在@RestController的方法中，如果路径参数带.(点号)会截断，如何配置？.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/server.jsp-servlet.init-parameters.development=true.note.md
- raw/wujinsen_markdown/架构/MicroServer/SpringBoot/无标题笔记.note.md
related: [enableautoconfiguration原理, spring-application启动流程, spring-声明式事务, spring-boot-面试题, spring-ioc与bean生命周期]
created: 2026-06-22
updated: 2026-07-05
---

# Spring Boot 自动配置（概念枢纽）

> 原理 [[spring/enableautoconfiguration原理]]；启动流程 [[spring/spring-application启动流程]]；事务 [[spring/spring-声明式事务]]；面试 [[spring/spring-boot-面试题]]。

各微服务基于 **Spring Boot 2.3.12**（见 ），自动配置是「开箱即用」的核心。

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

## 调试自动配置

```yaml
debug: true # 启动日志打印 Positive/Negative matches
```

或 `--debug` 启动参数，查看哪些 AutoConfiguration 生效/被 `@Conditional` 跳过。

## 扩展阅读

- 装配源码链：[[spring/enableautoconfiguration原理]]
- Bean 创建与循环依赖：Spring 容器启动时处理，Boot 2.3 支持构造器循环依赖（有限）
