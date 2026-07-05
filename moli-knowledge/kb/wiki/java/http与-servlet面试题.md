---
title: HTTP 与 Servlet 面试题
slug: http与-servlet面试题
type: interview
status: active
tags: [HTTP, Servlet, Tomcat, 面试]
sources:
- raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集基础篇之参考答案.note.md
related: [tomcat与-servlet容器, servlet生命周期与请求流程, spring-mvc请求流程, 认证与会话机制]
created: 2026-06-22
updated: 2026-07-05
---

# HTTP 与 Servlet 面试题

> Tomcat 枢纽 [[java/tomcat与-servlet容器]]；请求链 [[java/servlet生命周期与请求流程]]。

## Q1. GET 和 POST 区别？

GET 幂等、取资源，参数在 URL；POST 可能改服务器状态，body 传参。GET 长度受 URL/浏览器限制；POST 理论无规范上限。登录用 **POST** JSON body。

## Q2. HTTP 和 HTTPS？

HTTPS = HTTP + TLS，加密与身份校验。微服务前后端分离常全链路 HTTPS；本地 dev 多用 HTTP。

## Q3. Servlet 生命周期？

init → 多次 service → destroy。见 [[java/servlet生命周期与请求流程]]。

## Q4. Filter 和 Servlet 区别？

Filter 链式拦截，Servlet 处理请求。Shiro 鉴权在 Filter 层 [[security/shiro-鉴权体系]]。

## Q5. Tomcat 容器四层？

Engine → Host → **Context**（Web 应用）→ **Wrapper**（Servlet）。一个 Spring Boot 应用一个 Context。

## Q6. DispatcherServlet 作用？

Spring MVC 前端控制器，分发到 `@Controller`。见 [[spring/spring-mvc请求流程]]。

## Q8. Cookie 和 Session？

Cookie 客户端；Session 服务端。 token 走 **Authorization 头**，非 Cookie。

## Q9. 转发 forward vs 重定向 redirect？

forward 服务器内部跳转一次请求；redirect 302 客户端再发请求。Gateway **转发**到下游是 HTTP 代理，不是 Servlet forward。

## Q10. Gateway 和业务 Tomcat 区别？

Gateway 用 **Netty/WebFlux**；业务用 **Tomcat Servlet 阻塞模型** [[spring/spring-cloud-gateway]]、[[middleware/io模型与-netty]]。

## Q11. 404 可能在哪一层？

Gateway 路由错、StripPrefix 错、Tomcat context-path、Controller 映射错。分层排查。

## Q12. load-on-startup 作用？

控制 Servlet 启动顺序与提前 init；数值越小越先加载。
