---
title: BIO NIO AIO 对比
slug: bio-nio-aio对比
type: article
status: active
tags: [BIO, NIO, AIO, Selector]
sources:
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO  transfer 通道之间的数据传输.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Buffer.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Channel.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO FileChannel.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Java NIO DatagramChannel.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Pipe.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Scatter Gather.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Selector.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO ServerSocketChannel.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO SocketChannel.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO 按行读写大文件.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO 概述.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO 系列教程.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO与IO 区别.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Java NIO写大文件比较.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/RandomAccessFile.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/RandomAccessFile案例.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Socket NIO原理和实现.note.md
- raw/wujinsen_markdown/大数据资料-王/nio/Socket NIO演示代码.note.md
- raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
- raw/wujinsen_markdown/并发编程/java/Atomic原子类.note.md
- raw/wujinsen_markdown/并发编程/java/Java并发编程：CountDownLatch、CyclicBarrier和 Semaphore.note.md
- raw/wujinsen_markdown/并发编程/java/ReentrantLock.note.md
- raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md
- raw/wujinsen_markdown/并发编程/java/volatile.note.md
- raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之IO篇.note.md
related: [io模型与-netty, netty-reactor与线程模型, java-并发, netty-与-io面试题]
created: 2026-06-22
updated: 2026-07-05
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

原文插图 annex：[[java/annex-volatile]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/并发编程/java/深入理解并发之CompareAndSet(CAS).note.md` · T22 **B** 档

### 来自：深入理解并发之CompareAndSet(CAS)

![imageFile1.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile1.png)

![imageFile2.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile2.png)

![imageFile3.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile3.png)

![imageFile4.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile4.png)

![imageFile5.png](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%E5%B9%B6%E5%8F%91%E4%B9%8BCompareAndSet%28CAS%29.note_images/imageFile5.png)

原文插图 annex：[[java/annex-volatile]]

原文插图 annex：[[java/annex-Netty高性能之Reactor线程模型]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Buffer.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Buffer.note.md` · T22 **B** 档

### 来自：Java NIO Buffer

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20Buffer.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Channel.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Channel.note.md` · T22 **B** 档

### 来自：Java NIO Channel

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20Channel.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Pipe.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Pipe.note.md` · T22 **B** 档

### 来自：Java NIO Pipe

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20Pipe.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/并发编程/java/synchronized与static synchronized 的区别.note.md` · T22 **B** 档

### 来自：synchronized与static synchronized 的区别

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/java/synchronized%E4%B8%8Estatic%20synchronized%20%E7%9A%84%E5%8C%BA%E5%88%AB.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Scatter Gather.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO Scatter Gather.note.md` · T22 **B** 档

### 来自：Java NIO Scatter Gather

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20Scatter%20Gather.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20Scatter%20Gather.note_images/imageFile2.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/大数据资料-王/nio/Java NIO 概述.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/大数据资料-王/nio/Java NIO 概述.note.md` · T22 **B** 档

### 来自：Java NIO 概述

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20%E6%A6%82%E8%BF%B0.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/nio/Java%20NIO%20%E6%A6%82%E8%BF%B0.note_images/imageFile2.png)
