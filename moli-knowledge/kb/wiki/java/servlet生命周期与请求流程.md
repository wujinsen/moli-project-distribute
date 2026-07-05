---
title: Servlet 生命周期与请求流程
slug: servlet生命周期与请求流程
type: article
status: active
tags: [servlet, tomcat, Filter, 生命周期]
sources:
- raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
related: [tomcat与-servlet容器, spring-mvc请求流程, shiro-鉴权体系, http与-servlet面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Servlet 生命周期与请求流程

> 容器概念 [[java/tomcat与-servlet容器]]；Spring MVC [[spring/spring-mvc请求流程]]。

## 1. Servlet 生命周期

```
容器启动 → load-on-startup / 首次请求
 → init()
 → service() 循环（每次请求）
 → destroy()（容器关闭）
```

| 方法 | 次数 |
|------|------|
| `init` | 1 次 |
| `service` / `doGet` / `doPost` | 每请求 |
| `destroy` | 1 次 |

Spring MVC 的 `DispatcherServlet` 在 `init` 中初始化 WebApplicationContext。

## 2. 容器启动（Tomcat 摘要）

1. 解析 `Context`（web.xml / Spring Boot 自动配置）
2. `ContextConfig` 解析 Servlet、Filter、Listener
3. 为每个 Servlet 创建 **Wrapper**
4. 按 `load-on-startup` 顺序 `init` Servlet

Spring Boot 省略 web.xml，用 `@ServletComponentScan` / 自动注册 `DispatcherServlet`。

## 4. Filter vs Servlet

| | Filter | Servlet |
|---|--------|---------|
| 接口 | `javax.servlet.Filter` | `HttpServlet` |
| 链式 | 多个 Filter 串联 | 通常入口为 DispatcherServlet |
| 用途 | 鉴权、日志、编码 | 业务分发 |

`FilterChain.doFilter()` 把请求传给下一个 Filter 或 Servlet。

## 5. 线程模型

Tomcat 从 **线程池** 取线程执行整个 Filter + Servlet 链。长耗时 JDBC/Dubbo 会占住线程 → 高并发时需调 `max-threads` 与连接池（[[database/druid连接池与监控]]）。

与 Netty **EventLoop 非阻塞**对比见 [[middleware/io模型与-netty]]。

## 6. Spring Boot 映射

| Servlet 时代 | Spring Boot |
|--------------|-------------|
| web.xml 配 Servlet | `@SpringBootApplication` 自动配 |
| Servlet | `@RestController` |
| Filter | Shiro `ShiroFilterFactoryBean`、Starter 自动配置 |

## 7. 常见问题

| 问题 | 原因 |
|------|------|
| 404 | Context path / `@RequestMapping` / Gateway StripPrefix |
| 401 无 body | Shiro Filter 拦截 |
| 两次 Filter | 同时配了 ShiroConfig + Starter（勿重复） |

Gateway 404 与 Tomcat 404 排查路径不同，见。

原文插图 annex：[[database/annex-Java后台面试-常见问题]]
