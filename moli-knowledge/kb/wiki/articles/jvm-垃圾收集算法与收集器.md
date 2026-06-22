---
title: JVM 垃圾收集算法与收集器
slug: jvm-垃圾收集算法与收集器
type: article
status: active
tags: [jvm, GC, G1, CMS]
sources:
  - raw/wujinsen_markdown/jvm/垃圾收集算法.note.md
  - raw/wujinsen_markdown/jvm/GC/各个GC收集器对比.note.md
  - raw/wujinsen_markdown/jvm/jdk7、8、9默认垃圾回收器.note.md
related: [jvm-内存与gc, jvm-面试题]
created: 2026-06-22
updated: 2026-06-22
---

# JVM 垃圾收集算法与收集器

> 枢纽 [[jvm-内存与gc]]。

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
