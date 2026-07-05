---
title: Netty Pipeline 与编解码
slug: netty-pipeline与编解码
type: article
status: active
tags: [netty, Pipeline, 粘包, 编解码]
sources:
- raw/wujinsen_markdown/大数据资料-王/netty/Netty In Action.note.md
- raw/wujinsen_markdown/大数据资料-王/netty/Netty介绍.note.md
- raw/wujinsen_markdown/大数据资料-王/netty/netty hello world.note.md
- raw/wujinsen_markdown/大数据资料-王/netty/netty学习url.note.md
- raw/wujinsen_markdown/大数据资料-王/netty/netty核心概念.note.md
- raw/wujinsen_markdown/并发编程/Netty/1. 下载netty源码，.note.md
- raw/wujinsen_markdown/并发编程/Netty/Netty高性能之Reactor线程模型.note.md
- raw/wujinsen_markdown/并发编程/Netty/netty源码分析之服务端启动全解析.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/Java Netty 4.x 用户指南.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/Netty首页.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/Preface.note.md
- raw/wujinsen_markdown/并发编程/Netty/翻译文章/User guide for 4.x--4.x用户指南.note.md
related: [io模型与-netty, netty-reactor与线程模型, dubbo-调用原理与分层]
created: 2026-06-22
updated: 2026-07-05
---

# Netty Pipeline 与编解码

> 线程模型 [[middleware/netty-reactor与线程模型]]；枢纽 [[middleware/io模型与-netty]]。

## 1. 启动类

| 类 | 场景 |
|----|------|
| **Bootstrap** | 客户端连接远程 |
| **ServerBootstrap** | 服务端 bind 端口 |

服务端需 **两个 EventLoopGroup**（Boss + Worker）；客户端一个。

## 2. ChannelPipeline

每个 Channel 一条 **Pipeline**，双向 **Handler 链表**：

```
入站: Head → InboundHandler₁ → … → Tail
出站: Tail → OutboundHandler₁ → … → Head
```

| 类型 | 方向 | 典型 |
|------|------|------|
| **ChannelInboundHandler** | 读（server→app） | 解码、业务 |
| **ChannelOutboundHandler** | 写（app→server） | 编码、flush |

`ChannelHandlerContext.fireChannelRead()` 传给下一个 Inbound。

## 3. ChannelInitializer

引导阶段向 Pipeline **addLast** 各 Handler，初始化完成后**自动移除自身**。

典型顺序：

```
LoggingHandler → LengthFieldBasedFrameDecoder → Decoder → Encoder → BusinessHandler
```

## 4. 粘包 / 拆包

TCP 流式协议**无消息边界**，一次 read 可能：

- **粘包**：多条小消息一次到达
- **拆包**：一条消息分多次到达

解决：在 Pipeline 加 **帧解码器**：

| 策略 | 说明 |
|------|------|
| 固定长度 | 每帧 N 字节 |
| 分隔符 | 如 `\n` |
| 长度字段 | 头 4 字节表示 body 长（Dubbo/自定义 RPC 常用） |

Dubbo 协议自有编解码，应用层一般不手写。

## 5. 编解码器

| 类 | 方向 |
|----|------|
| `ByteToMessageDecoder` | 字节 → 对象（入站） |
| `MessageToByteEncoder` | 对象 → 字节（出站） |

业务 Handler 继承 `SimpleChannelInboundHandler<T>`，只处理类型 T。

## 6. ByteBuf vs ByteBuffer

- **池化**减少 GC
- **读写索引分离**（readerIndex/writerIndex）
- **引用计数** `retain/release` 防泄漏
- **Direct Buffer** 堆外，配合零拷贝

泄漏 → 直接内存 OOM（[[java/jvm-oom与排查入门]] Direct buffer）。

## 7. 零拷贝（通信优化）

Netty 支持：

- CompositeByteBuf 组合缓冲区
- `FileRegion` 文件发送 sendfile
- Direct Buffer 减少用户态拷贝

Kafka 高吞吐亦依赖 sendfile（[[middleware/kafka-与-mq选型]]）。

## 8. 实践原则

1. 编解码与业务 Handler 分离
2. 业务阻塞操作 **不要** 占 EventLoop
3. 出站 write 后注意 **flush**
4. RPC 框架已封装协议时，改 Dubbo 序列化/协议见文档，勿重复造轮子

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

原文插图 annex：[[middleware/annex-Netty介绍]]

原文插图 annex：[[java/annex-Netty高性能之Reactor线程模型]]

原文插图 annex：[[middleware/annex-Netty-In-Action]]

原文插图 annex：[[middleware/annex-Java-Netty-4.x-用户指南]]
