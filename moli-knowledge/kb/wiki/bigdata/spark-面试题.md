---
title: Spark 面试题
slug: spark-面试题
type: interview
status: active
tags: [Spark, 面试题, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Spark/Spark RDD的分区.note.md
- raw/wujinsen_markdown/BigData/Spark/【Kafka二】Kafka工作原理详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践(1).note.md
- raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践.note.md
- raw/wujinsen_markdown/BigData/spark(1)/RDD-API整理.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Streaming 与 Kafka 集成原理.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Streaming原理简析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md
- raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming原理详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming调优总结1.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark性能调优.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle的性能调优.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Stage划分及提交源码分析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/kafka集群原理介绍(2).note.md
- raw/wujinsen_markdown/BigData/spark(1)/shuffle相关.note.md
- raw/wujinsen_markdown/BigData/spark(1)/spark性能调优总结1.note.md
- raw/wujinsen_markdown/BigData/spark(1)/理解Spark的核心RDD.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/Spark RDD详解.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark RDD函数详解.note.md
related: [spark-核心概念与实践, hadoop-面试题, flink-面试题]
created: 2026-07-05
updated: 2026-07-05
---

# Spark 面试题

> 实践见 [[bigdata/spark-核心概念与实践]]。

## Q1. Spark 为什么比 MapReduce 快？

内存迭代、DAG 优化、减少落盘；Stage 内 pipeline。

## Q2. RDD 五大属性？

分区列表、计算函数、依赖、分区器（可选）、首选位置。

## Q3. cache 与 persist？

`cache`=MEMORY_ONLY；持久化级别 MEMORY/DISK/序列化/副本。

## Q4. 宽窄依赖？

窄：一对一；宽：shuffle，划 Stage 边界。

## Q5. 数据倾斜怎么处理？

加盐 key、两阶段聚合、广播 join、AQE 倾斜 join。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **85** 篇。

原文插图 annex：[[bigdata/annex-GC调优在Spark应用中的实践(1)]]

原文插图 annex：[[bigdata/annex-GC调优在Spark应用中的实践]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Stage划分及提交源码分析]]

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 3 组

> 图源 `raw/wujinsen_markdown/BigData/Spark/【Kafka二】Kafka工作原理详解.note.md` · T22 **B** 档

### 来自：【Kafka二】Kafka工作原理详解

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Spark/%E3%80%90Kafka%E4%BA%8C%E3%80%91Kafka%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86%E8%AF%A6%E8%A7%A3.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/BigData/spark(1)/spark性能调优总结1.note.md` · T22 **B** 档

### 来自：spark性能调优总结1

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/spark%E6%80%A7%E8%83%BD%E8%B0%83%E4%BC%98%E6%80%BB%E7%BB%931.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/spark%E6%80%A7%E8%83%BD%E8%B0%83%E4%BC%98%E6%80%BB%E7%BB%931.note_images/imageFile2.png)

> 图源 `raw/wujinsen_markdown/BigData/spark(1)/理解Spark的核心RDD.note.md` · T22 **B** 档

### 来自：理解Spark的核心RDD

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/%E7%90%86%E8%A7%A3Spark%E7%9A%84%E6%A0%B8%E5%BF%83RDD.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/%E7%90%86%E8%A7%A3Spark%E7%9A%84%E6%A0%B8%E5%BF%83RDD.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/%E7%90%86%E8%A7%A3Spark%E7%9A%84%E6%A0%B8%E5%BF%83RDD.note_images/imageFile3.png)

原文插图 annex：[[bigdata/annex-GC调优在Spark应用中的实践(1)]]

原文插图 annex：[[bigdata/annex-GC调优在Spark应用中的实践]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Stage划分及提交源码分析]]

原文插图 annex：[[bigdata/annex-Spark性能调优]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md` · T22 **B** 档

### 来自：Spark on yarn中的内存溢出案例

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/Spark%20on%20yarn%E4%B8%AD%E7%9A%84%E5%86%85%E5%AD%98%E6%BA%A2%E5%87%BA%E6%A1%88%E4%BE%8B.note_images/imageFile1.png)
