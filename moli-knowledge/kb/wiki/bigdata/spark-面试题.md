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
