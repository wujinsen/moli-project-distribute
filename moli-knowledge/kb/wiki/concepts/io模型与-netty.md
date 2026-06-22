---
title: IO 模型与 Netty
slug: io模型与-netty
type: concept
status: active
tags: [netty, NIO, IO, 高并发]
sources:
  - raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
  - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之IO篇.note.md
related: [bio-nio-aio对比, netty-reactor与线程模型, netty-pipeline与编解码, netty-与-io面试题, dubbo-与-nacos, spring-cloud-gateway, kafka-与-mq选型]
created: 2026-06-22
updated: 2026-06-22
---

# IO 模型与 Netty

> BIO/NIO/AIO [[bio-nio-aio对比]]；Reactor [[netty-reactor与线程模型]]；Pipeline [[netty-pipeline与编解码]]；面试 [[netty-与-io面试题]]。

**Netty** 是基于 Java NIO 的**异步事件驱动**网络框架，广泛用于 RPC、MQ、网关。茉莉栈中 **Dubbo 默认 Netty 传输**、**Spring Cloud Gateway 基于 WebFlux/Netty**，理解 IO 模型有助于读压测超时与 RPC 性能。

## 1. 三代 IO 模型（速览）

| 模型 | 线程 | 阻塞 | 典型 |
|------|------|------|------|
| **BIO** | 一连接一线程 | 读/写阻塞 | 老式 Socket |
| **NIO** | 少线程 + Selector 多路复用 | 非阻塞 + 事件就绪 | Netty、Redis 单线程 |
| **AIO** | 异步回调 | 操作系统完成通知 | Java 7+，服务端用得少 |

详见 [[bio-nio-aio对比]]。

## 2. 为什么 Netty 快

| 手段 | 说明 |
|------|------|
| **Reactor** | 事件分发，单线程处理大量连接 [[netty-reactor与线程模型]] |
| **零拷贝** | `DirectBuffer`、sendfile（Kafka 亦用，[[kafka-与-mq选型]]） |
| **无同步阻塞** | IO 线程不 wait 单连接 |
| **Pipeline** | 编解码/业务 Handler 链式 [[netty-pipeline与编解码]] |

## 3. 与茉莉技术栈

| 组件 | IO 层 |
|------|--------|
| [[网关]] | Spring Cloud Gateway → Reactor Netty |
| [[dubbo-与-nacos]] | Dubbo 协议默认 Netty Server/Client |
| [[redis-缓存]] | Redis 单线程 epoll 多路复用（类比 NIO 思想） |
| Tomcat（各 Spring Boot 服务） | 传统 BIO/NIO2 容器，HTTP 入口 |

秒杀 HTTP 经 Gateway → order-server（Tomcat），Dubbo 调 user-center 走 Netty。

## 4. 核心概念映射

| Netty | NIO | 作用 |
|-------|-----|------|
| EventLoop | 线程 + Selector 循环 | 处理 IO 事件 |
| Channel | SocketChannel | 连接抽象 |
| ByteBuf | ByteBuffer | 缓冲区（池化、引用计数） |
| ChannelPipeline | — | Handler 链 |

## 5. 排查触点

| 现象 | 可能 IO 层原因 |
|------|----------------|
| 连接数暴涨线程耗尽 | BIO 式一连接一线程（非 Netty 侧） |
| Dubbo 超时 | 网络阻塞、EventLoop 被业务阻塞 |
| Gateway 502/慢 | 下游慢 + 背压；Reactor 线程阻塞（勿在 filter 里 sync 重活） |

业务逻辑应 offload 到业务线程池，**勿阻塞 EventLoop**。

## 6. 学习路径

1. Buffer/Channel/Selector → [[bio-nio-aio对比]]
2. Reactor 单/多/主从 → [[netty-reactor与线程模型]]
3. Bootstrap/Pipeline/粘包 → [[netty-pipeline与编解码]]
4. 面试题 → [[netty-与-io面试题]]
