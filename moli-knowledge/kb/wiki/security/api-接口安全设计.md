---
title: API 接口安全设计
slug: api-接口安全设计
type: article
status: active
tags: [安全, API, 鉴权, 防刷]
sources:
- raw/wujinsen_markdown/javaweb/jwt/Cookie,Session和Token机制和区别..note.md
- raw/wujinsen_markdown/javaweb/jwt/Cookie和Token.note.md
- raw/wujinsen_markdown/javaweb/jwt/什么是 JWT -- JSON WEB TOKEN.note.md
- raw/wujinsen_markdown/javaweb/jwt/讲真，别再使用JWT了！.note.md
- raw/wujinsen_markdown/架构/安全/top无法查看病毒进程解决方案.note.md
- raw/wujinsen_markdown/架构/安全/安全防护-记录kdevtmpfsi清除过程.note.md
- raw/wujinsen_markdown/架构/安全/挖矿/挖矿病毒处理.note.md
- raw/wujinsen_markdown/架构/安全/挖矿木马自助清理手册.note.md
- raw/wujinsen_markdown/面试笔试/安全性/App开放接口api安全性—Token签名sign的设计与实现.note.md
- raw/wujinsen_markdown/面试笔试/安全性/App开放接口api安全性的设计与实现.note.md
related: [shiro-鉴权体系, sentinel-限流与熔断, 接口幂等性实践, https与-tls基础]
created: 2026-06-22
updated: 2026-07-05
---

# API 接口安全设计

> 鉴权 [[security/shiro-鉴权体系]]；网关 [[spring/spring-cloud-gateway]]；限流 [[middleware/sentinel-限流与熔断]]。

## 1. 身份认证

| 层 | |
|----|------|
| 登录 | Shiro + Redis Session [[security/认证与会话机制]] |
| 调 API | `Authorization` token |
| 开放 API | 可扩展 AppKey + 签名 + 时间戳 |

## 2. 授权

RBAC `@RequiresPermissions` [[security/rbac-权限模型]]；知识库空间 ACL `moli-knowledge/kb/wiki-moli/guides/知识库使用指南.md`。

## 3. 传输安全

生产 HTTPS 终结于 Nginx/Gateway [[security/https与-tls基础]]。

## 4. 防刷与滥用

- **限流**：Sentinel QPS/线程数 [[middleware/sentinel-接入与规则配置]]
- **幂等**：支付/下单 [[middleware/接口幂等性实践]]
- **参数校验**：防 SQL 注入 [[database/mybatis-plus-用法与注入防护]]

## 5. 开放接口签名（通用模式）

```
sign = HMAC(appSecret, sortedParams + timestamp)
```

校验 timestamp 窗口防重放；nonce 防重复。

## 6. 敏感数据

日志脱敏；错误信息不泄露堆栈给外网；MinIO 附件权限 `moli-knowledge/kb/wiki-moli/ops/minio-附件存储指南.md`。

## 相关

 ·
## 开放 API 安全要点（raw 架构/安全）

- **鉴权**：Token/OAuth2、签名校验、时间戳防重放
- **传输**：HTTPS、证书校验
- **限流**：按 appId/IP QPS
- **数据**：敏感字段脱敏；错误响应不泄露堆栈

## 批次#1312 增补（wujinsen P1）

合并 `架构/安全/` + 面试笔试安全性 raw。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 `javaweb/jwt/` JWT 实践 raw。
