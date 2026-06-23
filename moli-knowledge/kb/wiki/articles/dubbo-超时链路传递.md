---
title: Dubbo 超时与链路传递
slug: dubbo-超时链路传递
type: article
status: active
tags: [Dubbo, RPC, 超时]
sources:
  - raw/wujinsen_markdown/
related: [dubbo-调用原理与分层, rpc-超时重试与链路, feign-超时重试配置]
created: 2026-06-21
updated: 2026-06-21
---

# Dubbo 超时与链路传递

> 原理 [[dubbo-调用原理与分层]]；全链路 [[rpc-超时重试与链路]]；Feign [[feign-超时重试配置]]。

## 1. 超时语义

- **consumer timeout**：单次调用等待上限
- **provider timeout**：服务端执行上限（通常 ≥ consumer）
- 链式 A→B→C：A 超时须 **> B+C 预算** 或分设

## 2. 配置

```yaml
dubbo.consumer.timeout: 5000
dubbo.provider.timeout: 10000
```

方法级：`@DubboReference(methods=@Method(name="getUser", timeout=3000))`

## 3. 重试

`retries=0` 写操作默认；读可适当 `retries=2`（幂等前提）。

## 4. 茉莉

- 用户中心鉴权接口短超时 + 快速失败
- 订单调用户中心：避免外层长事务占连接 [[druid-连接池泄漏排查]]

## 相关

[[dubbo-分组版本与环境]] · [[sentinel-限流与熔断]]
