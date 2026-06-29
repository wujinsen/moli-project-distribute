---
title: BCrypt 密码哈希与加盐
slug: bcrypt-密码哈希与加盐
type: article
status: active
tags: [安全, 鉴权]
sources:
 - raw/wujinsen_markdown/
related: [认证与会话机制, 字段级加密存储, 配置-敏感信息与加密]
created: 2026-06-21
updated: 2026-06-21
---

# BCrypt 密码哈希与加盐

> 认证 [[认证与会话机制]]；字段加密 [[字段级加密存储]]；配置加密 [[配置-敏感信息与加密]]。

## 1. 为何不用 MD5/SHA1

彩虹表 + 无盐可逆查；BCrypt **自适应 cost** 抗暴力破解。

## 2. 用法（Spring Security Crypto / Shiro）

```java
String hash = new BCryptPasswordEncoder(10).encode(rawPassword);
boolean ok = encoder.matches(raw, hash);
```

- **盐内置**于 hash 字符串，无需单独列
- cost 10–12 按 CPU 调整

## 相关

[[ldap-与企业账号]] ·
