---
title: Java 类加载与双亲委派
slug: java-类加载与双亲委派
type: concept
status: active
tags: ['JVM', '类加载']
sources:
 - raw/wujinsen_markdown/
related: [jvm-内存与gc, jvm-面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Java 类加载与双亲委派

## 双亲委派

- Bootstrap → Extension → Application
- 防核心类被篡改
- 自定义 ClassLoader：热部署、插件
