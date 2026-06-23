---
title: API 接口安全设计
slug: api-接口安全设计
type: article
status: active
tags: [安全, API, 鉴权, 防刷]
sources:
  - raw/wujinsen_markdown/面试笔试/安全性/App开放接口api安全性的设计与实现.note.md
related: [登录与鉴权指南, shiro-鉴权体系, sentinel-限流与熔断, 接口幂等性实践, https与-tls基础]
created: 2026-06-22
updated: 2026-06-22
---

# API 接口安全设计

> 茉莉鉴权 [[shiro-鉴权体系]]；网关 [[spring-cloud-gateway]]；限流 [[sentinel-限流与熔断]]。

## 1. 身份认证

| 层 | 茉莉 |
|----|------|
| 登录 | Shiro + Redis Session [[认证与会话机制]] |
| 调 API | `Authorization` token |
| 开放 API | 可扩展 AppKey + 签名 + 时间戳 |

## 2. 授权

RBAC `@RequiresPermissions` [[rbac-权限模型]]；知识库空间 ACL [[知识库使用指南]]。

## 3. 传输安全

生产 HTTPS 终结于 Nginx/Gateway [[https与-tls基础]]。

## 4. 防刷与滥用

- **限流**：Sentinel QPS/线程数 [[sentinel-接入与规则配置]]
- **幂等**：支付/下单 [[接口幂等性实践]]
- **参数校验**：防 SQL 注入 [[mybatis-plus-用法与注入防护]]

## 5. 开放接口签名（通用模式）

```
sign = HMAC(appSecret, sortedParams + timestamp)
```

校验 timestamp 窗口防重放；nonce 防重复。

## 6. 敏感数据

日志脱敏；错误信息不泄露堆栈给外网；MinIO 附件权限 [[minio-附件存储指南]]。

## 相关

[[故障排查指南]] · [[茉莉登录与鉴权故障根因汇总]]
