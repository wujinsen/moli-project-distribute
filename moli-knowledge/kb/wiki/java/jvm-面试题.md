---
title: JVM（面试题系列）
slug: jvm-面试题
type: interview
status: active
tags: [jvm, 面试题, GC, 内存]
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
- raw/wujinsen_markdown/面试笔试/JVM/jdk1.8——jvm分析与调优.note.md
- raw/wujinsen_markdown/面试笔试/Java/JVM/JVM面试题.note.md
- raw/wujinsen_markdown/面试笔试/Java/JVM/垃圾收集器.note.md
- raw/wujinsen_markdown/面试笔试/精尽面试题/JVM/精尽 Java【虚拟机】面试题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/JVM群面试题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集之微服务篇参考答案.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集基础篇之参考答案.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/Java面试通关要点汇总集核心篇.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/MySQL数据库MyISAM和InnoDB存储引擎的比较.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/java CPU 100% 排查.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/复合索引的优点和注意事项.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/游戏排行榜算法设计实现比较.note.md
- raw/wujinsen_markdown/面试笔试/面试题整理/面试题整理.note.md
related: [jvm-内存与gc, jvm-垃圾收集算法与收集器, jvm-oom与排查入门, jmm与happens-before]
created: 2026-06-22
updated: 2026-07-05
---

# JVM（面试题系列）

> [[java/jvm-内存与gc]] [[java/jvm-垃圾收集算法与收集器]] [[java/jvm-oom与排查入门]]

## Q1. JVM 组成部分？

类加载器、运行时数据区、执行引擎、本地方法接口。见 [[java/jvm-内存与gc]]。

## Q2. 堆和栈区别？

堆存对象，线程共享；栈存栈帧/局部变量，线程私有。对象引用在栈，实例在堆。

## Q3. 类加载过程？

加载 → 验证 → 准备 → 解析 → 初始化（双亲委派）。

## Q4. 三种 GC 算法？

标记清除、复制、标记整理。分代收集组合使用。见 [[java/jvm-垃圾收集算法与收集器]]。

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

JVM 实现内存布局；JMM 是并发可见性规范，见 [[java/jmm与happens-before]]。

## 批次#1313 增补（wujinsen P2）

合并精尽 JVM 面试题 raw。

## 批次#1322 增补（wujinsen Phase2 王树挂接）

合并 `大数据资料-王/jvm/` 双树。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 `jvm/` 全树及 GC/调优 raw sources。

原文插图 annex：[[java/annex-小白都能看得懂的java虚拟机内存模型]]

原文插图 annex：[[java/annex-JDK的命令行工具]]

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]
