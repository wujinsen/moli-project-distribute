---
title: SpringApplication 启动流程
slug: spring-application启动流程
type: article
status: active
tags: [spring-boot, 启动, 生命周期]
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
related: [spring-boot-自动配置, enableautoconfiguration原理]
created: 2026-06-22
updated: 2026-07-05
---

# SpringApplication 启动流程

> 自动配置 [[spring/enableautoconfiguration原理]]；系统架构。

## 启动入口

```java
SpringApplication.run(UserCenterApplication.class, args);
```

## 主要阶段（简化）

1. **推断应用类型** — Web / Reactive / None（是否 Servlet、DispatcherServlet）
2. **加载 ApplicationContextInitializer** — `META-INF/spring.factories`
3. **加载 ApplicationListener** — 早期事件监听
4. **推断主类** — 含 main 的启动类
5. **创建 ApplicationContext** — Servlet 环境用 `AnnotationConfigServletWebServerApplicationContext`
6. **prepareContext** — 打印 Banner、加载配置源（application.yml、bootstrap.yml）
7. **refreshContext** — **Spring 容器核心 refresh**（Bean 定义加载、Bean 实例化、AutoConfiguration 导入）
8. **callRunners** — `ApplicationRunner` / `CommandLineRunner`
9. **发布 Started 事件** — 内嵌 Tomcat/Jetty 开始监听端口

## bootstrap vs application

各服务通常：

- `bootstrap.yml` — Nacos 地址、应用名（Spring Cloud 上下文优先加载）
- `application-dev.yml` — 数据源、Redis、Dubbo、端口

## refresh 核心（与自动配置交汇）

`AbstractApplicationContext.refresh()` → `invokeBeanFactoryPostProcessors` → 处理 `@Configuration` 与 **ConfigurationClassPostProcessor** → 解析 `@Import(AutoConfigurationImportSelector)` → 自动配置 Bean 注册。

## 常见问题

- **端口占用** — 改 `server.port` 或杀占用进程
- **Bean 循环依赖** — 构造器注入循环在 Boot 2.6+ 更严格；改 `@Lazy` 或 setter 注入
- **AutoConfiguration 未生效** — 查 `@ConditionalOnClass`、exclude 配置

原文插图 annex：[[patterns/annex-SpringMVC接收复杂集合参数]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 2 组

> 图源 `raw/wujinsen_markdown/Spring/Spring解析，加载及实例化Bean的顺序（零配置）.note.md` · T22 **B** 档

### 来自：Spring解析，加载及实例化Bean的顺序（零配置）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/Spring%E8%A7%A3%E6%9E%90%EF%BC%8C%E5%8A%A0%E8%BD%BD%E5%8F%8A%E5%AE%9E%E4%BE%8B%E5%8C%96Bean%E7%9A%84%E9%A1%BA%E5%BA%8F%EF%BC%88%E9%9B%B6%E9%85%8D%E7%BD%AE%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/Spring%E8%A7%A3%E6%9E%90%EF%BC%8C%E5%8A%A0%E8%BD%BD%E5%8F%8A%E5%AE%9E%E4%BE%8B%E5%8C%96Bean%E7%9A%84%E9%A1%BA%E5%BA%8F%EF%BC%88%E9%9B%B6%E9%85%8D%E7%BD%AE%EF%BC%89.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC工作原理.note.md` · T22 **B** 档

### 来自：SpringMVC工作原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/SpringMVC/SpringMVC%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/SpringMVC/SpringMVC%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/SpringMVC/SpringMVC%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.note_images/imageFile3.png)

原文插图 annex：[[patterns/annex-SpringMVC接收复杂集合参数]]

原文插图 annex：[[patterns/annex-ModelMap、ModelAndView和@Modelattribute的区别]]

原文插图 annex：[[patterns/annex-Spring、SpringMVC和SpringBoot看这一篇就够了！]]

原文插图 annex：[[patterns/annex-@RequestParam-@RequestBody-@PathVariable-等参数绑定注解详解(转)]]

原文插图 annex：[[patterns/annex-Spring-中经典的-9-种设计模式，打死也要记住啊！]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/Spring/Spring 事务管理探究.note.md` · T22 **B** 档

### 来自：Spring 事务管理探究

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/Spring%20%E4%BA%8B%E5%8A%A1%E7%AE%A1%E7%90%86%E6%8E%A2%E7%A9%B6.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/Spring/事务/深入理解 Spring 事务原理.note.md` · T22 **B** 档

### 来自：深入理解 Spring 事务原理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/%E4%BA%8B%E5%8A%A1/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Spring%20%E4%BA%8B%E5%8A%A1%E5%8E%9F%E7%90%86.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/Spring/@Autowired注解实现原理（Spring Bean的自动装配）.note.md` · T22 **B** 档

### 来自：@Autowired注解实现原理（Spring Bean的自动装配）

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/%40Autowired%E6%B3%A8%E8%A7%A3%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86%EF%BC%88Spring%20Bean%E7%9A%84%E8%87%AA%E5%8A%A8%E8%A3%85%E9%85%8D%EF%BC%89.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/Spring/%40Autowired%E6%B3%A8%E8%A7%A3%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86%EF%BC%88Spring%20Bean%E7%9A%84%E8%87%AA%E5%8A%A8%E8%A3%85%E9%85%8D%EF%BC%89.note_images/imageFile2.png)
