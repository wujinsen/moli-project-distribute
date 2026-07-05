---
title: Spring 三级缓存与循环依赖
slug: spring-三级缓存与循环依赖
type: article
status: active
tags: [spring, 循环依赖, 三级缓存, AOP]
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
related: [spring-ioc与bean生命周期, spring-容器面试题, spring-boot-自动配置]
created: 2026-06-22
updated: 2026-07-05
---

# Spring 三级缓存与循环依赖

> 生命周期背景 [[spring/spring-ioc与bean生命周期]]。

## 什么是循环依赖？

A 的属性需要 B，B 的属性需要 A（或 A→B→C→A）。**Setter/字段注入** 在 Spring 单例下可解；**构造器循环依赖** 无法解（对象都实例化不出来）。

## 三级缓存

| 缓存 | 变量名 | 内容 |
|------|--------|------|
| 一级 | `singletonObjects` | 完整生命周期的单例 Bean |
| 二级 | `earlySingletonObjects` | 提前暴露的早期引用（可能是 AOP 代理） |
| 三级 | `singletonFactories` | `ObjectFactory`，用于生成早期引用 |

## 解决流程（A↔B 字段注入）

1. 创建 A：实例化 → 将 **ObjectFactory** 放入三级缓存 → 填充属性需要 B
2. 创建 B：实例化 → 三级缓存 → 填充属性需要 A
3. 从三级/二级拿到 A 的早期引用（若需 AOP，Factory 里 `getEarlyBeanReference` 生成代理）
4. B 完成 → 回到 A 完成 → A 最终代理放入一级缓存

**关键**：提前暴露的是「早期引用」，保证全局只有一个 A（或同一代理）。

## 为什么需要三级，不是二级？

若 A 需要 **AOP**，早期暴露的必须是**最终代理对象**；`singletonFactories` 延迟执行 Factory，在循环依赖点按需生成代理并放入二级，避免 B 注入到「非代理的 A」。

`earlyProxyReferences` 记录是否已 AOP，避免 `postProcessAfterInitialization` 重复代理。

## 不能解决的情况

| 场景 | 原因 |
|------|------|
| **构造器循环依赖** | 未实例化无法提前暴露 |
| **prototype 循环** | 不缓存，无法 early expose |
| **@Async 等自注入** | 特殊代理场景需单独分析 |

Boot 2.6+ 默认禁止部分循环依赖（可配置 `spring.main.allow-circular-references=true`）。

## 面试一句话

> 单例 + 字段注入：三级缓存提前暴露 Factory/早期引用；构造器循环依赖无解；AOP 靠 getEarlyBeanReference 保证注入的是同一代理。

详见 [[spring/spring-容器面试题]]。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 Spring 循环依赖与 IOC 解析 raw。

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
