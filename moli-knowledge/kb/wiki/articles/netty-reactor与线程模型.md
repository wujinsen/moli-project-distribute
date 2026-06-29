---
title: Netty Reactor 与线程模型
slug: netty-reactor与线程模型
type: article
status: active
tags: [netty, Reactor, EventLoop, 线程模型]
sources:
 - raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
 - raw/wujinsen_markdown/大数据资料-王/netty/netty核心概念.note.md
related: [io模型与-netty, bio-nio-aio对比, netty-pipeline与编解码, dubbo-调用原理与分层]
created: 2026-06-22
updated: 2026-06-22
---

# Netty Reactor 与线程模型

> 概念 [[io模型与-netty]]；Dubbo 传输层 [[dubbo-调用原理与分层]]。

## 1. Reactor 模式

**Reactor** = 事件驱动 + 多路复用：

1. **Reactor** 监听事件（accept/read/write）
2. 就绪事件分发给 **Handler** 处理
3. Handler 可同步处理或丢给线程池

类似生产者-消费者，但**无 Queue 缓冲**——事件到达即 dispatch。

## 2. 三种 Reactor 模型

### 单 Reactor 单线程

Accept + Read + Write 同一线程。简单，无法利用多核；适合连接少。

### 单 Reactor 多线程

Reactor 线程做 accept/read，业务丢 **Worker 线程池**。常见 Web 模型。

### 主从 Reactor（Netty 服务端默认）

| 角色 | 职责 |
|------|------|
| **Boss EventLoopGroup** | 只 accept 新连接 |
| **Worker EventLoopGroup** | 已连接 Channel 的 read/write |

```
Client → Boss(accept) → 注册到 Worker → Worker 处理 IO + Pipeline
```

客户端通常 **一个 EventLoopGroup** 即可。

## 3. Netty 线程映射

| 概念 | 说明 |
|------|------|
| **EventLoop** | 单线程 + Selector 循环，`while(true)` 处理就绪 Channel |
| **EventLoopGroup** | EventLoop 线程池 |
| **一个 Channel 绑定一个 EventLoop** | 生命周期内不换线程，**无锁**处理该连接事件 |

`NioEventLoop` 聚合 Selector，可并发处理**成百上千** Channel（非阻塞）。

## 4. 异步语义

Netty IO 操作返回 **ChannelFuture**，通过 Listener 获知完成。调用线程不必阻塞 wait。

**禁止在 EventLoop 执行**：长时间 DB 查询、sleep、heavy CPU——会阻塞同 Loop 上所有连接。

## 5. 与 BIO 对比

| BIO | Netty |
|-----|-------|
| 一连接一线程 | 少线程多连接 |
| accept/read 阻塞 | 非阻塞 + 事件驱动 |
| 线程数随连接线性涨 | 线程数 ≈ 核数 × 2（可配） |

## 6. Dubbo 中的 Netty

Dubbo 3.x 默认 **Netty4** 作为 Transporter：

- Provider：`NettyServer` 监听 dubbo 端口（如 user-center 20881）
- Consumer：连接池 + 多路复用发 RPC

RPC 超时除业务慢外，可查 Netty 是否 EventLoop 被占满。

## 7. Gateway 中的 Netty

[[spring-cloud-gateway]] 基于 **Reactor Netty**（WebFlux），与 Netty 原生 API 不同层，但同为 Reactor 非阻塞模型。Filter 里阻塞调用 JDBC 会导致性能骤降。

## 8. 调优提示

- `EventLoopGroup` 线程数默认 `CPU×2`，IO 密集可略增
- Boss 通常 1 线程够用
- 业务异步：`executorGroup` 加到 Handler 上 offload
