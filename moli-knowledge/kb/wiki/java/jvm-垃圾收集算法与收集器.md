---
title: JVM 垃圾收集算法与收集器
slug: jvm-垃圾收集算法与收集器
type: article
status: active
tags: [jvm, GC, G1, CMS]
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
related: [jvm-内存与gc, jvm-面试题]
created: 2026-06-22
updated: 2026-07-05
---

# JVM 垃圾收集算法与收集器

> 枢纽 [[java/jvm-内存与gc]]。

## 三种基础算法

| 算法 | 思路 | 缺点 |
|------|------|------|
| **标记-清除** | 标记存活 → 清除未标记 | 碎片 |
| **复制** | 半区复制存活对象 | 浪费一半空间；存活率高时复制多 |
| **标记-整理** | 标记 → 存活向一端移动 | STW 移动成本 |

**分代收集**：新生代存活率低 → **复制**（Eden+Survivor）；老年代 → **标记-清除/整理**。

## 常见收集器

| 收集器 | 区域 | 特点 |
|--------|------|------|
| Serial | 新/老 | 单线程 STW，Client 模式 |
| ParNew | 新生代 | 多线程，配合 CMS |
| **Parallel Scavenge** | 新生代 | 吞吐量优先，JDK8 默认 |
| **Parallel Old** | 老年代 | 配合 PS，JDK8 默认 |
| CMS | 老年代 | 并发标记清除，低延迟，碎片+浮动垃圾 |
| **G1** | 整堆分区 | 可预测停顿，JDK9+ 默认 |
| ZGC / Shenandoah | 整堆 | 超低延迟，大堆 |

## Minor GC vs Full GC

- **Minor GC**：新生代，较频繁，通常较短。
- **Full GC**：老年代/Metaspace/显式 `System.gc()`，STW 长，需警惕。

## 选型简记

- **JDK8 微服务默认**：Parallel，调 `-Xmx` 即可应对多数 dev。
- **低延迟要求**：G1（`-XX:+UseG1GC`），设 `-XX:MaxGCPauseMillis`。
- **堆 > 6G 且 JDK11+**：评估 ZGC。

## 面试一句话

> 分代 + 复制(年轻) + 标记整理(老)；JDK8 默认 Parallel，JDK9+ 默认 G1。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 `jvm/GC/` 收集器对比 raw。
