---
title: HTTPS 与 TLS 基础
slug: https与-tls基础
type: article
status: active
tags: [HTTPS, TLS, 安全, 网络]
sources:
 - raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集之微服务篇参考答案.note.md
related: [http与-servlet面试题, 认证与会话机制, spring-cloud-gateway, 服务调用与架构]
created: 2026-06-22
updated: 2026-06-22
---

# HTTPS 与 TLS 基础

> HTTP 面试 [[http与-servlet面试题]]； token 机制 [[认证与会话机制]]。

## 1. HTTP vs HTTPS

| | HTTP | HTTPS |
|---|------|-------|
| 端口 | 80 | 443 |
| 加密 | 明文 | **TLS** 加密 |
| 身份 | 无默认校验 | 证书校验服务器身份 |
| 开发环境 | ✅ 默认（8888/21000） | 一般未配 |

HTTPS = HTTP + **TLS**（原 SSL 演进）。

## 2. TLS 握手（简化）

1. Client Hello（支持的 cipher、随机数）
2. Server Hello + **证书**（公钥）
3. 客户端验证证书链（CA）
4. 协商对称密钥（非对称交换）
5. 后续 HTTP 用对称密钥加密

保证：**机密性、完整性、服务器身份**（防中间人需可信 CA）。

## 3. 证书

| 项 | 说明 |
|----|------|
| 自签名 | dev 可用，浏览器不信任 |
| CA 签发 | 生产公网 |
| 通配符 | `*.example.com` |

Tomcat/ Gateway 配错证书 → `No Certificate file specified` 等启动失败（运维 raw 笔记）。

## 4. 微服务场景

| 层 | 实践 |
|----|------|
| 对外 | 网关或 LB 终结 TLS（443→内网 HTTP） |
| 内网 | 常明文 HTTP + 隔离网络；金融场景 mTLS |
| 前后端分离 | 浏览器↔网关 HTTPS；API 仍 JSON |

 21000 本地 HTTP；生产建议在 **Nginx/云 LB** 配 TLS，转发到 Gateway。

## 5. 与鉴权关系

- **TLS** 解决传输层窃听/篡改
- **Shiro Session token** 解决应用层身份

二者正交：HTTPS 不替代登录；登录 token 仍应走 HTTPS 防截获。

## 6. REST 相关（微服务篇）

- **幂等**：GET/PUT/DELETE 幂等；POST 创建非幂等
- **API 设计**：资源名词、状态码语义
- 与 HTTPS 无冲突，HTTPS 是传输保障

## 8. 常见面试点

- 为什么需要 CA？→ 公钥信任链
- HTTPS slower？→ 握手有 RTT，可 session 复用
- 双向 TLS？→ 客户端也交证书（服务网格）
