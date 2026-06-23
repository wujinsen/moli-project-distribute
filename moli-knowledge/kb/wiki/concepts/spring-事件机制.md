---
title: Spring 事件机制
slug: spring-事件机制
type: concept
status: active
tags: ['Spring', '事件']
sources:
  - raw/wujinsen_markdown/
related: [spring-ioc与bean生命周期, spring-aop与代理]
created: 2026-06-22
updated: 2026-06-22
---

# Spring 事件机制

## 要点

- `ApplicationEvent` + `ApplicationListener` / `@EventListener`
- 同步默认在同线程；`@Async` 异步需线程池 [[线程池-实战调优]]
- 解耦：登录成功发事件刷新缓存、审计日志
