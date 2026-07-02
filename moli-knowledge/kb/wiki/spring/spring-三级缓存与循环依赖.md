---
title: Spring 三级缓存与循环依赖
slug: spring-三级缓存与循环依赖
type: article
status: active
tags: [spring, 循环依赖, 三级缓存, AOP]
sources:
 - raw/wujinsen_markdown/Spring/Spring循环依赖原理解析.note.md
 - raw/wujinsen_markdown/Spring/什么是循环依赖.note.md
related: [spring-ioc与bean生命周期, spring-容器面试题, spring-boot-自动配置]
created: 2026-06-22
updated: 2026-06-22
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
