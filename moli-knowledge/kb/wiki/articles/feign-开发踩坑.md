---
title: Feign 开发踩坑
slug: feign-开发踩坑
type: article
status: active
tags: [Feign, Spring Cloud, 踩坑]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note.md
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/Spring Cloud Feign 上传文件的常见问题.note.md
related: [openfeign-与-http客户端, 跨域与前后端分离, dubbo-与-nacos]
created: 2026-06-22
updated: 2026-06-22
---

# Feign 开发踩坑

> 概念 [[openfeign-与-http客户端]]。茉莉 Dubbo 为主，本文供 Spring Cloud HTTP 栈参考。

## 1. GET 带 @RequestBody

HTTP 语义不推荐；部分网关/代理丢弃 body → 改 POST 或 `@SpringQueryMap`。

## 2. 文件上传

- 勿用 `@RequestBody MultipartFile`
- 用 `@RequestPart` + 正确 `consumes = MULTIPART_FORM_DATA`
- 大文件调超时、内存

## 3. 重试导致重复写

Feign 默认重试 → 写接口需 **幂等** [[接口幂等性实践]] 或关闭重试。

## 4. 404/503 与负载

服务名错误、Nacos 无实例、Ribbon 缓存旧列表 → 查注册中心。

## 5. 与 Dubbo 选型

内部 Java 服务优先 Dubbo；仅当对方只暴露 REST 时用 Feign/RestTemplate。

## 相关

[[故障排查指南]] · [[nacos-注册与配置]]
