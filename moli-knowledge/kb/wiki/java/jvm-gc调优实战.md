---
title: JVM GC 调优实战
slug: jvm-gc调优实战
type: article
status: active
tags: [JVM, GC, 性能]
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
- raw/wujinsen_markdown/性能优化/DATABASE/mysql left join 慢如何优化.note.md
- raw/wujinsen_markdown/性能优化/DATABASE/总结   慢 SQL 问题经验总结！.note.md
- raw/wujinsen_markdown/性能优化/问题排查/ARTHAS（阿尔萨斯）- 阿里开源的JAVA在线诊断工具（火焰图-实战篇--正则表达式 优化方法）.note.md
- raw/wujinsen_markdown/架构/性能调优/JVM/GC收集器调优.note.md
- raw/wujinsen_markdown/架构/性能调优/JVM/jvm详情——6、堆大小设置简单说明.note.md
related: [jvm-内存与gc, jvm-垃圾收集算法与收集器, production-jvm启动参数]
created: 2026-06-21
updated: 2026-07-05
---

# JVM GC 调优实战

> 内存模型 [[java/jvm-内存与gc]]；收集器 [[java/jvm-垃圾收集算法与收集器]]；启动参数 [[java/production-jvm启动参数]]。

## 1. 目标指标

- **STW** P99 可接受（微服务 < 200ms 常见目标）
- **Full GC** 频率趋近 0（G1/ZGC）
- 堆使用率平稳，无 OOM

## 3. 排查流程

1. `jstat -gcutil` 看 E/O/M/F/Y 频率
2. GC log（`-Xlog:gc*` JDK9+）分析 Pause
3. Heap Dump：MAT 看 Dominator [[java/jvm-oom与排查入门]]
4. 压测前后对比

## 4. 误区

- 堆越大 ≠ 越好（回收负担）
- 盲目调 Survivor/Ratio
- 忽略 metaspace / 类加载泄漏

## 相关

[[java/java-cpu-100排查实战]] · [[java/jvm-面试题]]
## 堆与 GC 调优备忘（raw）

- `-Xms` 与 `-Xms` 设成相同，避免动态扩堆
- 观察 **GC 日志**：`-Xlog:gc*`（JDK9+）或 `-XX:+PrintGCDetails`
- **Full GC 频繁**：老年代不足、Metaspace、大对象；配合 MAT
- 生产默认 G1（JDK9+）；吞吐优先可用 Parallel

见 [[java/jvm-面试题]]、[[java/jvm-oom与排查入门]]。

## 批次#1312 增补（wujinsen P1）

合并 GC 调优/堆设置 raw。

## 批次#1320 增补（wujinsen Phase2 P0）

合并 `jvm/调优/` 与架构性能调优 raw。

## 批次#1324 增补（wujinsen Phase2 长尾）

合并性能优化 raw。

原文插图 annex：[[java/annex-小白都能看得懂的java虚拟机内存模型]]

原文插图 annex：[[java/annex-JDK的命令行工具]]

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 13 组

> 图源 `raw/wujinsen_markdown/jvm/JVM内存划分.note.md` · T22 **B** 档

### 来自：JVM内存划分

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/JVM%E5%86%85%E5%AD%98%E5%88%92%E5%88%86.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/对象的内存布局.note.md` · T22 **B** 档

### 来自：对象的内存布局

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%86%85%E5%AD%98%E5%B8%83%E5%B1%80.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/对象的创建.note.md` · T22 **B** 档

### 来自：对象的创建

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%88%9B%E5%BB%BA.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/新生代Eden与两个Survivor区的解释.note.md` · T22 **B** 档

### 来自：新生代Eden与两个Survivor区的解释

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E6%96%B0%E7%94%9F%E4%BB%A3Eden%E4%B8%8E%E4%B8%A4%E4%B8%AASurvivor%E5%8C%BA%E7%9A%84%E8%A7%A3%E9%87%8A.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/理解GC日志.note.md` · T22 **B** 档

### 来自：理解GC日志

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E7%90%86%E8%A7%A3GC%E6%97%A5%E5%BF%97.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/jvm/JVM内存划分.note.md` · T22 **B** 档

### 来自：JVM内存划分

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/JVM%E5%86%85%E5%AD%98%E5%88%92%E5%88%86.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/jvm/对象的内存布局.note.md` · T22 **B** 档

### 来自：对象的内存布局

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%86%85%E5%AD%98%E5%B8%83%E5%B1%80.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/jvm/对象的创建.note.md` · T22 **B** 档

### 来自：对象的创建

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E5%88%9B%E5%BB%BA.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/jvm/理解GC日志.note.md` · T22 **B** 档

### 来自：理解GC日志

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E7%90%86%E8%A7%A3GC%E6%97%A5%E5%BF%97.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/jvm/对象的访问定位.note.md` · T22 **B** 档

### 来自：对象的访问定位

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E8%AE%BF%E9%97%AE%E5%AE%9A%E4%BD%8D.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E8%AE%BF%E9%97%AE%E5%AE%9A%E4%BD%8D.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/jvm/对象的访问定位.note.md` · T22 **B** 档

### 来自：对象的访问定位

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E8%AE%BF%E9%97%AE%E5%AE%9A%E4%BD%8D.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%AF%B9%E8%B1%A1%E7%9A%84%E8%AE%BF%E9%97%AE%E5%AE%9A%E4%BD%8D.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/jvm/垃圾收集算法.note.md` · T22 **B** 档

### 来自：垃圾收集算法

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile3.png)

> 图源 `raw/wujinsen_markdown/大数据资料-王/jvm/垃圾收集算法.note.md` · T22 **B** 档

### 来自：垃圾收集算法

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E5%A4%A7%E6%95%B0%E6%8D%AE%E8%B5%84%E6%96%99-%E7%8E%8B/jvm/%E5%9E%83%E5%9C%BE%E6%94%B6%E9%9B%86%E7%AE%97%E6%B3%95.note_images/imageFile3.png)

原文插图 annex：[[java/annex-小白都能看得懂的java虚拟机内存模型]]

原文插图 annex：[[java/annex-JDK的命令行工具]]

原文插图 annex：[[java/annex-九大工具助你玩转Java性能优化]]

原文插图 annex：[[java/annex-垃圾回收器]]

原文插图 annex：[[java/annex-JVM虚拟机笔记]]

原文插图 annex：[[java/annex-Java虚拟机：JVM高级特性与最佳实践（第2版）]]

原文插图 annex：[[java/annex-《深入理解-Java-内存模型》读书笔记]]

原文插图 annex：[[java/annex-eclipse-memory-analyzer-安装使用示例]]

原文插图 annex：[[java/annex-假笨说-警惕大量类加载器的创建导致诡异的Full-GC]]

原文插图 annex：[[java/annex-cpu-100%]]

原文插图 annex：[[java/annex-java-CPU-100%-排查]]

原文插图 annex：[[database/annex-mysql-left-join-慢如何优化]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md` · T22 **B** 档

### 来自：Minor GC和Full GC区别

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/jvm/Minor%20GC%E5%92%8CFull%20GC%E5%8C%BA%E5%88%AB.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/性能优化/DATABASE/总结   慢 SQL 问题经验总结！.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/性能优化/DATABASE/总结   慢 SQL 问题经验总结！.note.md` · T22 **B** 档

### 来自：总结   慢 SQL 问题经验总结！

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96/DATABASE/%E6%80%BB%E7%BB%93%20%20%20%E6%85%A2%20SQL%20%E9%97%AE%E9%A2%98%E7%BB%8F%E9%AA%8C%E6%80%BB%E7%BB%93%EF%BC%81.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E6%80%A7%E8%83%BD%E4%BC%98%E5%8C%96/DATABASE/%E6%80%BB%E7%BB%93%20%20%20%E6%85%A2%20SQL%20%E9%97%AE%E9%A2%98%E7%BB%8F%E9%AA%8C%E6%80%BB%E7%BB%93%EF%BC%81.note_images/imageFile2.png)
