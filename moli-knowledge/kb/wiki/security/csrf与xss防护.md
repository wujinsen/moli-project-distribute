---
title: CSRF 与 XSS 防护
slug: csrf与xss防护
type: article
status: active
tags: [安全, Web, 前端]
sources:
 - raw/wujinsen_markdown/
related: [api-接口安全设计, https与-tls基础, 日志脱敏规范]
created: 2026-06-21
updated: 2026-06-21
---

# CSRF 与 XSS 防护

> API 安全 [[security/api-接口安全设计]]；TLS [[security/https与-tls基础]]；日志 [[日志脱敏规范]]。

## 1. XSS

| 类型 | 防御 |
|------|------|
| 存储型 | 入库 HTML 转义；CSP 头 |
| 反射型 | 参数校验；Vue 默认转义文本插值 |
| DOM 型 | 禁止 `v-html` 不可信内容 |

## 2. CSRF

- 同源 Cookie + **SameSite=Lax/Strict**
- 关键写操作：**CSRF Token** 或 Header 自定义 token
- API 以 `Authorization` SessionId 为主，跨站表单难带 Header → 降低经典 CSRF 面

## 3. 其他

- CORS 白名单 [[middleware/跨域与前后端分离]]
- JSON 接口 `Content-Type: application/json`

## 相关

[[shiro-rememberme-安全]] · [[oauth2-与开放接口]]
