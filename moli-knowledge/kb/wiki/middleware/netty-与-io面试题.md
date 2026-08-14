---
title: Netty 与 IO 面试题
slug: netty-与-io面试题
type: interview
status: active
tags: [netty, NIO, 面试, IO]
sources:
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之IO篇.note.md
related: [io模型与-netty, bio-nio-aio对比, netty-reactor与线程模型, netty-pipeline与编解码, dubbo-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# Netty 与 IO 面试题

> 枢纽 [[middleware/io模型与-netty]]；BIO/NIO [[java/bio-nio-aio对比]]；Reactor [[middleware/netty-reactor与线程模型]]。

## Q1. BIO 和 NIO 区别？

BIO 阻塞 IO，一连接一线程。NIO 非阻塞 + Channel/Buffer/Selector 多路复用，少线程处理多连接。见 [[java/bio-nio-aio对比]]。

## Q2. Buffer 三个核心属性？

capacity、limit、position。`flip()` 切读模式；`clear()` 复写。

## Q3. Selector 作用？

单线程监听多 Channel 就绪事件（CONNECT/READ/WRITE），底层 epoll/kqueue，避免每连接一线程。

## Q4. 为什么 Netty 比原生 NIO 好用？

封装 Reactor、ByteBuf 池化、Pipeline、粘包处理、Future-Listener；生产级线程模型与内存管理。

## Q5. Reactor 三种模型？

单 Reactor 单线程；单 Reactor 多线程；**主从 Reactor**（Boss accept + Worker IO）。Netty 服务端默认主从。见 [[middleware/netty-reactor与线程模型]]。

## Q6. EventLoop 和线程关系？

一个 EventLoop 一个线程，绑定 Selector；一个 Channel 生命周期内固定在一个 EventLoop，保证无锁。

## Q7. 粘包拆包怎么解决？

定长、分隔符、**长度字段**帧协议；Pipeline 加 FrameDecoder。见 [[middleware/netty-pipeline与编解码]]。

## Q8. 为什么不能阻塞 EventLoop？

EventLoop 上跑该线程负责的所有 Channel；阻塞一个连接会拖死同 Loop 上其他连接。

## Q9. Netty 零拷贝手段？

Direct Buffer、CompositeByteBuf、FileRegion/sendfile。Kafka 也用 sendfile。

## Q10. Dubbo 和 Netty 关系？

Dubbo 默认 **Netty** 作为 RPC 传输层，Provider/Consumer 间 TCP 长连接多路复用。见 [[middleware/dubbo-调用原理与分层]]。

## Q11. AIO 为什么用得少？

Linux 上 AIO 对 socket 支持有限，Netty 等选 NIO+Reactor 更成熟。

## Q12. Java 7 NIO.2 增强？

Path/Files、WatchService、AsynchronousFileChannel 等。见 [[java/bio-nio-aio对比]]。
