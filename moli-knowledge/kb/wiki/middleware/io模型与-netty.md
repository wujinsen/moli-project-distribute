---
title: IO 模型与 Netty
slug: io模型与-netty
type: concept
status: active
tags: [netty, NIO, IO, 高并发]
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
- raw/wujinsen_markdown/并发编程/Netty/1. 下载netty源码，.note.md
- raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
- raw/wujinsen_markdown/并发编程/Netty/netty源码分析之服务端启动全解析.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/Java Netty 4.x 用户指南.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/Netty首页.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/Preface.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/User guide for 4.x--4.x用户指南.note.md
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之IO篇.note.md
related: [bio-nio-aio对比, netty-reactor与线程模型, netty-pipeline与编解码, netty-与-io面试题, dubbo-与-nacos, spring-cloud-gateway, kafka-与-mq选型]
created: 2026-06-22
updated: 2026-07-05
---

# IO 模型与 Netty

> BIO/NIO/AIO [[java/bio-nio-aio对比]]；Reactor [[middleware/netty-reactor与线程模型]]；Pipeline [[middleware/netty-pipeline与编解码]]；面试 [[middleware/netty-与-io面试题]]。

**Netty** 是基于 Java NIO 的**异步事件驱动**网络框架，广泛用于 RPC、MQ、网关。栈中 **Dubbo 默认 Netty 传输**、**Spring Cloud Gateway 基于 WebFlux/Netty**，理解 IO 模型有助于读压测超时与 RPC 性能。

## 1. 三代 IO 模型（速览）

| 模型 | 线程 | 阻塞 | 典型 |
|------|------|------|------|
| **BIO** | 一连接一线程 | 读/写阻塞 | 老式 Socket |
| **NIO** | 少线程 + Selector 多路复用 | 非阻塞 + 事件就绪 | Netty、Redis 单线程 |
| **AIO** | 异步回调 | 操作系统完成通知 | Java 7+，服务端用得少 |

详见 [[java/bio-nio-aio对比]]。

## 2. 为什么 Netty 快

| 手段 | 说明 |
|------|------|
| **Reactor** | 事件分发，单线程处理大量连接 [[middleware/netty-reactor与线程模型]] |
| **零拷贝** | `DirectBuffer`、sendfile（Kafka 亦用，[[middleware/kafka-与-mq选型]]） |
| **无同步阻塞** | IO 线程不 wait 单连接 |
| **Pipeline** | 编解码/业务 Handler 链式 [[middleware/netty-pipeline与编解码]] |

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

1. Buffer/Channel/Selector → [[java/bio-nio-aio对比]]
2. Reactor 单/多/主从 → [[middleware/netty-reactor与线程模型]]
3. Bootstrap/Pipeline/粘包 → [[middleware/netty-pipeline与编解码]]
4. 面试题 → [[middleware/netty-与-io面试题]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 2 组

> 图源 `raw/wujinsen_markdown/并发编程/Netty/netty源码分析之服务端启动全解析.note.md` · T22 **B** 档

### 来自：netty源码分析之服务端启动全解析

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/Netty/netty%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%E4%B9%8B%E6%9C%8D%E5%8A%A1%E7%AB%AF%E5%90%AF%E5%8A%A8%E5%85%A8%E8%A7%A3%E6%9E%90.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/并发编程/Netty/翻译文章/Preface.note.md` · T22 **B** 档

### 来自：Preface

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/Netty/%E7%BF%BB%E8%AF%91%E6%96%87%E7%AB%A0/Preface.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/Netty/%E7%BF%BB%E8%AF%91%E6%96%87%E7%AB%A0/Preface.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B/Netty/%E7%BF%BB%E8%AF%91%E6%96%87%E7%AB%A0/Preface.note_images/imageFile3.png)

原文插图 annex：[[java/annex-Netty高性能之Reactor线程模型]]

原文插图 annex：[[middleware/annex-Java-Netty-4.x-用户指南]]

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
