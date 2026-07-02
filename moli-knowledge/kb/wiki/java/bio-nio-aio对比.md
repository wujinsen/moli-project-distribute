---
title: BIO NIO AIO 对比
slug: bio-nio-aio对比
type: article
status: active
tags: [BIO, NIO, AIO, Selector]
sources:
 - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之IO篇.note.md
 - raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
related: [io模型与-netty, netty-reactor与线程模型, java-并发, netty-与-io面试题]
created: 2026-06-22
updated: 2026-06-22
---

# BIO NIO AIO 对比

> 枢纽 [[middleware/io模型与-netty]]；Reactor 实现 [[middleware/netty-reactor与线程模型]]。

## 1. BIO（Blocking IO）

- `InputStream.read()` **阻塞**直到有数据
- 服务端：`ServerSocket.accept()` 阻塞；每 accept 一个连接 **new Thread**
- 问题：并发连接数 ≈ 线程数，线程是昂贵资源（[[java/java-并发]]）

```
Client ──► Accept Thread ──► Thread-1 (连接 A)
 └──► Thread-2 (连接 B)
```

C10K 问题：连接上千后上下文切换、栈内存、句柄溢出。

## 2. NIO（Non-blocking IO）

三大件：

| 组件 | 作用 |
|------|------|
| **Buffer** | capacity / limit / position；`flip()` 读模式 |
| **Channel** | 双向通道，读写用 Buffer |
| **Selector** | 多路复用：一个线程监听多 Channel 就绪事件 |

```java
channel.configureBlocking(false);
channel.register(selector, SelectionKey.OP_READ | OP_CONNECT);
selector.select(); // 阻塞直到有就绪事件
```

**多路复用**：把多个 IO 阻塞合并到一个 `select/epoll` 等待，**单线程可处理多连接**。

Linux 上 JDK 1.6+ 用 **epoll** 替代 select/poll，提升性能。

## 3. AIO（Asynchronous IO）

Java 7 `AsynchronousChannel`：操作提交后由 OS 回调 `CompletionHandler`，线程可去做别的事。

```java
AsynchronousFileChannel.write(buffer, 0, null, completionHandler);
```

服务端 AIO 在 Linux 上 **实际仍可能基于 epoll 模拟**，Netty 等主流框架选 **NIO + Reactor** 而非 AIO。

## 4. 对比表

| | BIO | NIO | AIO |
|---|-----|-----|-----|
| 阻塞 | 读写阻塞 | 非阻塞 + Selector | 异步回调 |
| 线程模型 | 一连接一线程 | 少线程多连接 | 线程池 + 回调 |
| 复杂度 | 低 | 中 | 高 |
| 典型框架 | 老 Servlet | **Netty**、Mina | 较少 |

## 5. NIO.2（Java 7+）

- **Path / Files** 替代部分 File API
- **WatchService** 目录监听
- **AsynchronousSocketChannel** 属 AIO 范畴

面试知道「Path、Files、异步通道」即可（[[middleware/netty-与-io面试题]]）。

## 6. 与 Netty 关系

Netty 封装 NIO：

- 不用手写 Selector 循环
- ByteBuf 优于 ByteBuffer（池化、双索引）
- Reactor 线程模型开箱即用 [[middleware/netty-reactor与线程模型]]

## 7. 选型口诀

- 少量连接、逻辑简单 → BIO 够用
- 高并发 RPC/网关/MQ → **NIO + Netty**
- 文件异步读写 → AIO 或线程池 + NIO
