---
title: BCrypt 密码哈希与加盐
slug: bcrypt-密码哈希与加盐
type: article
status: active
tags: [安全, 鉴权]
sources:
- raw/wujinsen_markdown/ (enterprise-kb/security 专题页)
related: [认证与会话机制, bcrypt-密码哈希与加盐, nacos-config动态配置实践]
created: 2026-06-21
updated: 2026-07-05
---

# BCrypt 密码哈希与加盐

> 认证 [[security/认证与会话机制]]；字段加密 [[security/bcrypt-密码哈希与加盐]]；配置加密 [[middleware/nacos-config动态配置实践]]。

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

[[security/认证与会话机制]] ·
