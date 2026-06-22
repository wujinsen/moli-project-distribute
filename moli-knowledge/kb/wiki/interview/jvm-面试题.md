---
title: JVM（面试题系列）
slug: jvm-面试题
type: interview
status: active
tags: [jvm, 面试题, GC, 内存]
sources:
  - raw/wujinsen_markdown/面试笔试/精尽面试题/JVM/精尽 Java【虚拟机】面试题.note.md
  - raw/wujinsen_markdown/jvm/JVM内存划分.note.md
related: [jvm-内存与gc, jvm-垃圾收集算法与收集器, jvm-oom与排查入门, jmm与happens-before]
created: 2026-06-22
updated: 2026-06-22
---

# JVM（面试题系列）

> [[jvm-内存与gc]] [[jvm-垃圾收集算法与收集器]] [[jvm-oom与排查入门]]

## Q1. JVM 组成部分？

类加载器、运行时数据区、执行引擎、本地方法接口。见 [[jvm-内存与gc]]。

## Q2. 堆和栈区别？

堆存对象，线程共享；栈存栈帧/局部变量，线程私有。对象引用在栈，实例在堆。

## Q3. 类加载过程？

加载 → 验证 → 准备 → 解析 → 初始化（双亲委派）。

## Q4. 三种 GC 算法？

标记清除、复制、标记整理。分代收集组合使用。见 [[jvm-垃圾收集算法与收集器]]。

## Q5. JDK8 默认 GC？

Parallel Scavenge + Parallel Old。

## Q6. G1 特点？

Region 分区、可预测停顿、整堆收集，JDK9+ 默认。

## Q7. Minor GC 和 Full GC？

Minor 清新生代；Full 常含老年代+Metaspace，STW 更长。

## Q8. 内存泄漏 vs 内存溢出？

泄漏：对象无用仍被引用无法回收；溢出：需要的内存超过上限。泄漏可导致 OOM。

## Q9. 如何 dump 堆？

`jmap -dump` + MAT 分析 Dominator Tree。

## Q10. JVM 与 JMM 关系？

JVM 实现内存布局；JMM 是并发可见性规范，见 [[jmm与happens-before]]。
