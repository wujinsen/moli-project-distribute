---
title: Dubbo（面试题系列）
slug: dubbo-面试题
type: interview
status: active
tags: [dubbo, nacos, 面试题, RPC]
sources:
 - raw/wujinsen_markdown/面试笔试/精尽面试题/dubbo/精尽 Dubbo 面试题.note.md
related: [dubbo-与-nacos, dubbo-调用原理与分层, nacos-注册与配置, 服务调用与架构]
created: 2026-06-22
updated: 2026-06-22
---

# Dubbo（面试题系列）

> [[middleware/dubbo-与-nacos]] [[middleware/dubbo-调用原理与分层]] [[middleware/nacos-注册与配置]]

## Q1. Dubbo 是什么？

Java RPC 框架：注册发现、负载均衡、容错、序列化、监控。

## Q2. 分层架构？

Business / RPC(config,proxy,registry,cluster) / Remoting(protocol,transport,serialize)。见 [[middleware/dubbo-调用原理与分层]]。

## Q3. 调用流程？

Proxy → Cluster 负载 → Protocol 封装 → 序列化 → Netty 传输 → Provider 执行。

## Q4. 负载均衡策略？

Random、RoundRobin、LeastActive、ConsistentHash 等。

## Q5. 集群容错？

Failover（默认重试）、Failfast、Failsafe、Failback、Forking、Broadcast。

## Q6. Dubbo SPI vs JDK SPI？

Dubbo 自研：按需加载、Adaptive 自适应、Wrapper AOP。

## Q7. 注册中心作用？

Provider 注册 URL，Consumer 订阅并缓存列表，下线感知。

## Q8. Nacos 与 Dubbo 关系？

Nacos 作 Registry；Dubbo 2.7+ 原生集成 Nacos。用 Nacos + Dubbo。

## Q9. 与 Spring Cloud OpenFeign 区别？

Dubbo RPC 二进制、性能高、接口契约；Feign HTTP REST。已选 Dubbo。

## Q10. No provider 怎么排？

Provider 是否起、group/version、namespace、防火墙、Nacos 是否 8848。见。
