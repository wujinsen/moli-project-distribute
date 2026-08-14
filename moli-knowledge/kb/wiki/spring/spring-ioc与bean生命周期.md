---
title: Spring IoC 与 Bean 生命周期
slug: spring-ioc与bean生命周期
type: concept
status: active
tags: [spring, IoC, Bean, 生命周期]
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
- raw/wujinsen_markdown/源码分析/spring/Spring 是如何解决循环依赖的？.note.md
related: [spring-三级缓存与循环依赖, spring-mvc请求流程, spring-容器面试题, spring-boot-自动配置, spring-声明式事务]
created: 2026-06-22
updated: 2026-07-05
---

# Spring IoC 与 Bean 生命周期（概念枢纽）

> 循环依赖 [[spring/spring-三级缓存与循环依赖]]；Web 请求 [[spring/spring-mvc请求流程]]；面试 [[spring/spring-容器面试题]]；Boot 装配 [[spring/spring-boot-自动配置]]。

**IoC**：对象创建与依赖关系由 Spring 容器管理，开发者声明 `@Component/@Service` 与 `@Autowired`，而非 `new`。

## Bean 生命周期（单例，简化）

```
扫描 → BeanDefinition
 → 实例化（构造器）
 → 属性填充（依赖注入）
 → Aware / BeanPostProcessor 前置
 → 初始化（@PostConstruct、InitializingBean）
 → BeanPostProcessor 后置（含 AOP 代理）
 → 放入 singletonObjects
 → 销毁（容器关闭）
```

循环依赖发生在 **实例化之后、属性填充** 阶段，见 [[spring/spring-三级缓存与循环依赖]]。

## 作用域

| 作用域 | 说明 |
|--------|------|
| **singleton**（默认） | 容器内唯一，三级缓存仅解决单例循环依赖 |
| prototype | 每次 getBean 新建，**不**走三级缓存 |
| request/session | Web 环境 |

## 常见扩展点

- **BeanPostProcessor**：AOP、属性校验
- **BeanFactoryPostProcessor**：修改 BeanDefinition（如配置中心刷新）
- **ApplicationListener**：启动事件（见 [[spring/spring-application启动流程]]）

## 设计原则

- 优先构造器注入（不可变、易测）；字段注入简洁但难测
- 避免 prototype 注入 singleton 的反向依赖等复杂 scope 组合
- 循环依赖应 **从设计上消除**；三级缓存是兜底而非最佳实践

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

原文插图 annex：[[spring/annex-Spring-是如何解决循环依赖的？]]

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
