---
title: RPC 超时重试与链路
slug: rpc-超时重试与链路
type: article
status: active
tags: [Dubbo, RPC, 超时, 重试]
sources:
 - raw/wujinsen_markdown/面试笔试/Dubbo/精尽 Dubbo 面试题.note.md
related: [dubbo-负载均衡与集群容错, dubbo-调用原理与分层, 接口幂等性实践, 故障排查指南]
created: 2026-06-22
updated: 2026-06-22
---

# RPC 超时重试与链路

> Dubbo 容错 [[dubbo-负载均衡与集群容错]]；幂等 [[middleware/接口幂等性实践]]。

## 1. 超时层级

| 层 | 配置 |
|----|------|
| Consumer | `timeout=3000` |
| Provider | 服务端限制 |
| 网关 | Spring Cloud Gateway `response-timeout` |

**规则**：Consumer timeout ≥ Provider 处理时间 + 网络。

## 2. 重试

Dubbo `retries=2`（默认）：失败换节点重调。

| 方法类型 | 重试 |
|----------|------|
| 读 | 可开 |
| **写** | 应 **0** 或幂等 [[middleware/接口幂等性实践]] |

## 3. 链路放大

A→B→C 每层 3s 超时，用户感知 9s+。治理：

- 设置合理超时、快速失败
- Sentinel 熔断 [[middleware/sentinel-限流与熔断]]
- 异步化非关键路径

## 相关

[[skywalking-链路追踪]] ·
