---
title: @EnableAutoConfiguration 原理
slug: enableautoconfiguration原理
type: article
status: active
tags: [spring-boot, 自动配置, spring.factories]
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
related: [spring-boot-自动配置, spring-application启动流程, spring-boot-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# @EnableAutoConfiguration 原理

> 枢纽 [[spring/spring-boot-自动配置]]；启动总流程 [[spring/spring-application启动流程]]。

## 核心链路（Spring Boot 2.x）

```
@SpringBootApplication
 └─ @EnableAutoConfiguration
 └─ @Import(AutoConfigurationImportSelector.class)
 └─ 读取 META-INF/spring.factories
 key: org.springframework.boot.autoconfigure.EnableAutoConfiguration
 value: 各 XxxAutoConfiguration 全限定名列表
 └─ 去重、排除 @SpringBootApplication(exclude=...)
 └─ 按 @AutoConfigureOrder / @Order 排序
 └─ 过滤 @ConditionalOnXxx 不满足的配置类
 └─ 导入生效的 AutoConfiguration → 注册 Bean
```

## spring.factories 示例

```properties
# my-starter/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.MyAutoConfiguration
```

 `moli-distribute-common`、`user-center-shiro-starter` 可用同一机制做 **Starter 化**（`META-INF/spring.factories` 已用于 MoliCommonAutoConfiguration）。

## 常用 @Conditional

| 注解 | 条件 |
|------|------|
| `@ConditionalOnClass` | classpath 有某类 |
| `@ConditionalOnMissingBean` | 容器无该 Bean 才配 |
| `@ConditionalOnProperty` | 配置项匹配 |
| `@ConditionalOnWebApplication` | Web 应用 |

## 排除自动配置

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

或 `spring.autoconfigure.exclude` 配置项。

## @Configuration 下 @Bean 的 CGLIB

`@SpringBootApplication` 作为 `@Configuration` 时，类被 CGLIB 增强，`@Bean` 方法调用走容器单例；普通 `@Component` 类中 `@Bean` 无此增强。

## Boot 3 变更（了解）

`spring.factories` 的 AutoConfiguration 改为 **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**。当前常见 2.3 仍用 spring.factories。

## 面试一句话

> EnableAutoConfiguration → ImportSelector 读 spring.factories → Conditional 过滤 → 注册自动 Bean。
