---
title: Spring Boot（面试题系列）
slug: spring-boot-面试题
type: interview
status: active
tags: [spring-boot, 自动配置, 面试题]
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
related: [spring-boot-自动配置, enableautoconfiguration原理, spring-application启动流程, spring-声明式事务]
created: 2026-06-22
updated: 2026-07-05
---

# Spring Boot（面试题系列）

> [[spring/spring-boot-自动配置]] [[spring/enableautoconfiguration原理]] [[spring/spring-application启动流程]]

## Q1. @SpringBootApplication 组成？

@Configuration + @EnableAutoConfiguration + @ComponentScan。

## Q2. 自动配置原理？

@EnableAutoConfiguration → AutoConfigurationImportSelector → 读 `META-INF/spring.factories` → @Conditional 过滤 → 注册 Bean。见 [[spring/enableautoconfiguration原理]]。

## Q3. 如何排除某个 AutoConfiguration？

`@SpringBootApplication(exclude=...)` 或 `spring.autoconfigure.exclude`。

## Q4. @ConditionalOnMissingBean 作用？

容器里已有该 Bean 则跳过，方便用户自定义覆盖。

## Q5. Starter 是什么？

依赖聚合 + 自动配置 + 默认配置；如 `spring-boot-starter-web`。

## Q6. bootstrap.yml 和 application.yml？

bootstrap 优先加载，常用于 Nacos/Spring Cloud；application 放业务配置。

## Q7. Spring Boot 2.x vs 3.x 自动配置注册？

2.x 用 spring.factories；3.x 用 `AutoConfiguration.imports`。

## Q8. 如何调试哪些自动配置生效？

`debug: true` 或 `--debug`。

## Q9. 内嵌 Tomcat 何时启动？

refresh 过程中 `onRefresh` → 创建 WebServer → 发布 Started 事件后监听端口。

## Q10. 与 Spring 事务关系？

Boot 自动 DataSourceTransactionManager；声明式事务见 [[spring/spring-声明式事务]]、[[spring/spring-事务]]。
