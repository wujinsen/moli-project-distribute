---
title: Dubbo 调用原理与分层
slug: dubbo-调用原理与分层
type: article
status: active
tags: [dubbo, RPC, 微服务]
sources:
  - raw/wujinsen_markdown/面试笔试/精尽面试题/dubbo/精尽 Dubbo 面试题.note.md
related: [dubbo-与-nacos, 服务调用与架构, dubbo-面试题, netty-reactor与线程模型, io模型与-netty]
created: 2026-06-22
updated: 2026-06-22
---

# Dubbo 调用原理与分层

> 枢纽 [[dubbo-与-nacos]]；茉莉架构 [[服务调用与架构]]。

## 一次 RPC 调用（Consumer → Provider）

1. Consumer 代理调用接口方法
2. **Cluster** 负载均衡选 Invoker（从注册中心 Directory）
3. **Protocol** 封装 Invocation
4. **Serialization** 序列化
5. **Transport** Netty 发送（线程模型 [[netty-reactor与线程模型]]）
6. Provider 反序列化 → 执行实现 → 返回 Result
7. Consumer 收到响应

## 十层架构（简化为三层）

```
Business   — Service 接口与实现（@DubboService / @DubboReference）
RPC        — config / proxy / registry / cluster / monitor
Remoting   — protocol / exchange / transport / serialize
```

- **Proxy**：生成 Stub/Skeleton 透明代理
- **Registry**：Nacos/ZK 注册与订阅 URL
- **Cluster + LoadBalance**：失败转移、随机/轮询/最少活跃等
- **Protocol**：默认 dubbo 协议（长连接、hessian2/kryo 等）

## Dubbo SPI

Dubbo **自研 SPI**（非 JDK SPI）：`META-INF/dubbo/` 扩展点，支持 Adaptive、Wrapper。

## 茉莉配置示例

```yaml
dubbo:
  application:
    name: order-server
  registry:
    address: nacos://127.0.0.1:8848
  protocol:
    name: dubbo
    port: 20882
  scan:
    base-packages: com.moli.order.server.provider
```

Provider 在用户中心暴露 `UserCenterServer`；Consumer 在 order/bi/knowledge 引用。

## 常见问题

- **No provider** — Provider 未启动、group/version 不一致、Nacos 命名空间错误
- **超时** — `timeout`、线程池满、下游慢 SQL
- **序列化** — 接口 DTO 需 Serializable，版本兼容
