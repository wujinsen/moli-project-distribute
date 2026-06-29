---
title: Dubbo 与 Nacos
slug: dubbo-与-nacos
type: concept
status: active
tags: [dubbo, nacos, 微服务, 注册发现]
sources:
 - raw/wujinsen_markdown/面试笔试/精尽面试题/dubbo/精尽 Dubbo 面试题.note.md
 - raw/wujinsen_markdown/moli项目/使用Nacos作为配置中心和服务注册发现.note.md
related: [dubbo-调用原理与分层, nacos-注册与配置, 服务调用与架构, dubbo-面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Dubbo 与 Nacos（概念枢纽）

> Dubbo 原理 [[dubbo-调用原理与分层]]；Nacos 用法 [[nacos-注册与配置]]；全链路 ；面试 [[dubbo-面试题]]。

典型微服务栈：**Nacos** 做注册发现（+ 可选配置中心），**Spring Cloud Dubbo** 做服务间 RPC。

## 分工

| 组件 | 角色 |
|------|------|
| **Nacos Discovery** | 服务注册/发现，健康检查 |
| **Nacos Config** | 集中配置（开发环境 多为本地 yml，Nacos Config 可注释） |
| **Dubbo** | 业务服务 → 等 Provider 的 **RPC** |
| **Gateway** | 外部 HTTP 统一入口 |

## 配置要点

- `spring.application.name` — 服务名
- `dubbo.registry.address` — 通常 `nacos://127.0.0.1:8848`
- `dubbo.scan.base-packages` — `@DubboService` 扫描包
- Nacos 未启动 → 服务注册失败，Dubbo 找不到 Provider

## 与 HTTP 的边界

| 场景 | 方式 |
|------|------|
| 前端/外部 | HTTP → Gateway |
| 服务间内部 | **Dubbo RPC**（已移除 OpenFeign） |

见。
