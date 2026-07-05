---
title: JVM 内存与 GC
slug: jvm-内存与gc
type: concept
status: active
tags: [jvm, 内存, GC, 调优]
sources:
- raw/wujinsen_markdown/jvm/GC/G1/G1基本概念.note.md
- raw/wujinsen_markdown/jvm/GC/G1/G1字符串去重.note.md
- raw/wujinsen_markdown/jvm/GC/G1/GC Root.note.md
- raw/wujinsen_markdown/jvm/GC/G1/GC回收过程.note.md
- raw/wujinsen_markdown/jvm/GC/G1/无标题笔记.note.md
- raw/wujinsen_markdown/jvm/GC/各个GC收集器对比.note.md
- raw/wujinsen_markdown/jvm/GC日志分析、年轻代、老年代.note.md
- raw/wujinsen_markdown/jvm/GC的触发时间.note.md
- raw/wujinsen_markdown/jvm/HotSpot.note.md
- raw/wujinsen_markdown/jvm/JDK的命令行工具.note.md
- raw/wujinsen_markdown/jvm/JVM内存划分.note.md
- raw/wujinsen_markdown/jvm/JVM调优总结.note.md
- raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md
- raw/wujinsen_markdown/jvm/OutOfMemoryError异常与实战.note.md
- raw/wujinsen_markdown/jvm/cpu 100%.note.md
- raw/wujinsen_markdown/jvm/eclipse memory analyzer 安装使用示例.note.md
- raw/wujinsen_markdown/jvm/eclipse启动时间插件.note.md
- raw/wujinsen_markdown/jvm/eclipse设置jvm.note.md
- raw/wujinsen_markdown/jvm/jdk7、8、9默认垃圾回收器.note.md
- raw/wujinsen_markdown/jvm/jstatd用法详解.note.md
- raw/wujinsen_markdown/jvm/jstatd结合jvisualvm远程监控hot spot JVM.note.md
- raw/wujinsen_markdown/jvm/jvm的参数查询列表.note.md
- raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md
- raw/wujinsen_markdown/jvm/个人笔记/JVM虚拟机笔记.note.md
- raw/wujinsen_markdown/jvm/你假笨.note.md
- raw/wujinsen_markdown/jvm/内存分配与回收策略.note.md
- raw/wujinsen_markdown/jvm/周志明的书.note.attach/Java虚拟机：JVM高级特性与最佳实践（第2版）.md
- raw/wujinsen_markdown/jvm/周志明的书.note.md
- raw/wujinsen_markdown/jvm/垃圾回收器.note.md
- raw/wujinsen_markdown/jvm/垃圾收集算法.note.md
- raw/wujinsen_markdown/jvm/对象的内存布局.note.md
- raw/wujinsen_markdown/jvm/对象的创建.note.md
- raw/wujinsen_markdown/jvm/对象的访问定位.note.md
- raw/wujinsen_markdown/jvm/小白都能看得懂的java虚拟机内存模型.note.md
- raw/wujinsen_markdown/jvm/新生代Eden与两个Survivor区的解释.note.md
- raw/wujinsen_markdown/jvm/理解GC日志.note.md
- raw/wujinsen_markdown/jvm/调优/JVM源码分析之Metaspace解密.note.md
- raw/wujinsen_markdown/jvm/调优/java CPU 100% 排查.note.md
- raw/wujinsen_markdown/jvm/调优/九大工具助你玩转Java性能优化.note.md
- raw/wujinsen_markdown/jvm/调优/假笨说-警惕大量类加载器的创建导致诡异的Full GC.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/GC日志分析、年轻代、老年代.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/GC的触发时间.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/HotSpot.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/JDK的命令行工具.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/JVM内存划分.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/JVM调优总结.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/OutOfMemoryError异常与实战.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/eclipse memory analyzer 安装.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/eclipse启动时间插件.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/eclipse设置jvm.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/jvm的参数查询列表.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/内存分配与回收策略.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/周志明的书.note.attach/Java虚拟机：JVM高级特性与最佳实践（第2版）.md
- raw/wujinsen_markdown/大数据资料-王/jvm/周志明的书.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/垃圾回收器.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/垃圾收集算法.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/对象的内存布局.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/对象的创建.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/对象的访问定位.note.md
- raw/wujinsen_markdown/大数据资料-王/jvm/理解GC日志.note.md
- raw/wujinsen_markdown/源码分析/OpenJDK/openJDK之如何下载各个版本的openJDK源码.note.md
related: [jvm-垃圾收集算法与收集器, jvm-oom与排查入门, jvm-面试题, jmm与happens-before, java-并发]
created: 2026-06-22
updated: 2026-07-05
---

# JVM 内存与 GC（概念枢纽）

> 运行时区域见下表；收集器见 [[java/jvm-垃圾收集算法与收集器]]；OOM 排查 [[java/jvm-oom与排查入门]]；面试 [[java/jvm-面试题]]。Java 并发内存语义见 [[java/jmm与happens-before]]。

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
│ ├── Eden
│ └── Survivor (From / To)
└── 老年代 (Old)
```

- 新对象优先 Eden；Minor GC 存活者复制到 Survivor；年龄达标进老年代。
- 大对象可能直接进老年代。

## 默认 GC（OpenJDK）

| JDK | 默认 |
|-----|------|
| 7 / 8 | Parallel Scavenge + Parallel Old |
| 9+ | **G1** |

目标系统 **JDK 8** → Parallel 组合；可用 `-XX:+PrintCommandLineFlags` 确认。

## 与微服务的关系

- 每个 Spring Boot 进程独立 JVM；Dubbo/HTTP 线程池、连接池占堆外与堆内。
- **Metaspace** 泄漏（动态类加载、CGLIB 过多）→ Full GC 或 OOM。
- 本地 dev 默认堆通常够用；压测需 `-Xms/-Xmx` 对齐并看 GC 日志。

## 常用 JVM 参数（入门）

```
-Xms512m -Xmx512m # 堆固定，避免动态扩缩
-XX:MetaspaceSize=128m
-XX:+PrintGCDetails -XX:+PrintGCDateStamps
-Xloggc:gc.log # JDK8
```

详细排查见 [[java/jvm-oom与排查入门]]、。

原文插图 annex：[[java/annex-小白都能看得懂的java虚拟机内存模型]]

原文插图 annex：[[java/annex-JDK的命令行工具]]

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]
