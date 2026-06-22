---
title: JVM 内存与 GC
slug: jvm-内存与gc
type: concept
status: active
tags: [jvm, 内存, GC, 调优]
sources:
  - raw/wujinsen_markdown/jvm/JVM内存划分.note.md
  - raw/wujinsen_markdown/jvm/垃圾收集算法.note.md
  - raw/wujinsen_markdown/jvm/jdk7、8、9默认垃圾回收器.note.md
related: [jvm-垃圾收集算法与收集器, jvm-oom与排查入门, jvm-面试题, jmm与happens-before, java-并发]
created: 2026-06-22
updated: 2026-06-22
---

# JVM 内存与 GC（概念枢纽）

> 运行时区域见下表；收集器见 [[jvm-垃圾收集算法与收集器]]；OOM 排查 [[jvm-oom与排查入门]]；面试 [[jvm-面试题]]。Java 并发内存语义见 [[jmm与happens-before]]。

## 运行时数据区（JDK 8 HotSpot）

| 区域 | 线程 | 作用 | OOM |
|------|------|------|-----|
| **程序计数器** | 私有 | 字节码行号 | 无 |
| **虚拟机栈** | 私有 | 方法栈帧、局部变量 | StackOverflow / OOM |
| **本地方法栈** | 私有 | Native 方法 | 同栈 |
| **堆 Heap** | 共享 | **对象实例**（GC 主战场） | OOM |
| **方法区 / Metaspace** | 共享 | 类元数据、常量、静态变量 | OOM（元空间） |
| **直接内存** | — | NIO DirectBuffer，堆外 | OOM |

> 堆 vs 栈：对象在**堆**；局部变量、引用在**栈帧**。口语「栈内存」常指虚拟机栈局部变量表。

## 堆分代（经典模型）

```
堆
├── 新生代 (Young)
│   ├── Eden
│   └── Survivor (From / To)
└── 老年代 (Old)
```

- 新对象优先 Eden；Minor GC 存活者复制到 Survivor；年龄达标进老年代。
- 大对象可能直接进老年代。

## 默认 GC（OpenJDK）

| JDK | 默认 |
|-----|------|
| 7 / 8 | Parallel Scavenge + Parallel Old |
| 9+ | **G1** |

茉莉项目 **JDK 8** → Parallel 组合；可用 `-XX:+PrintCommandLineFlags` 确认。

## 与微服务的关系

- 每个 Spring Boot 进程独立 JVM；Dubbo/HTTP 线程池、连接池占堆外与堆内。
- **Metaspace** 泄漏（动态类加载、CGLIB 过多）→ Full GC 或 OOM。
- 本地 dev 默认堆通常够用；压测需 `-Xms/-Xmx` 对齐并看 GC 日志。

## 常用 JVM 参数（入门）

```
-Xms512m -Xmx512m          # 堆固定，避免动态扩缩
-XX:MetaspaceSize=128m
-XX:+PrintGCDetails -XX:+PrintGCDateStamps
-Xloggc:gc.log             # JDK8
```

详细排查见 [[jvm-oom与排查入门]]、[[故障排查指南]]。
