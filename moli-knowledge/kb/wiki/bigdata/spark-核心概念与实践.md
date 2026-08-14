---
title: Spark 核心概念与实践
slug: spark-核心概念与实践
type: article
status: active
tags: [Spark, RDD, DataFrame, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Spark/Kafka+Spark Streaming+Redis实时系统实践.note.md
- raw/wujinsen_markdown/BigData/Spark/Preview of Apache Spark 2.0 now on Databricks Community Edition Easier, Faster,.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark RDD的分区.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark性能优化指南——基础篇.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark性能优化指南——高级篇.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark源码阅读笔记.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark的Master和Worker集群启动的源码分析.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark计算过程分析.note.md
- raw/wujinsen_markdown/BigData/Spark/Spark集群安装.note.md
- raw/wujinsen_markdown/BigData/Spark/spark sql 工程项目.note.md
- raw/wujinsen_markdown/BigData/Spark/spark yarn 模式安装.note.md
- raw/wujinsen_markdown/BigData/Spark/【Kafka二】Kafka工作原理详解.note.md
- raw/wujinsen_markdown/BigData/Spark/各模式下运行spark自带实例SparkPi.note.md
- raw/wujinsen_markdown/BigData/Spark/异常问题/spark on yarn提交任务时报ClosedChannelException解决方案.note.md
- raw/wujinsen_markdown/BigData/Spark/异常问题/避免在Spark 2.x版本中使用sparkSQL，关于CTAS bug的发现过程.note.md
- raw/wujinsen_markdown/BigData/Spark/消息队列设计精要.note.md
- raw/wujinsen_markdown/BigData/Spark/算子/Spark常用算子详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/DataFrame-api手册.note.md
- raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践(1).note.md
- raw/wujinsen_markdown/BigData/spark(1)/GC调优在Spark应用中的实践.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Parquet格式详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/RDD-API整理.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Sharethrough使用Spark Streaming优化实时竞价.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Checkpoint机制.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark GraphX基本操作.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Streaming 与 Kafka 集成原理.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Streaming作业提交源码分析接收数据篇.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Streaming作业提交源码分析数据处理篇.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark Streaming原理简析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn调度模式.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark 集群安装.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark1.0.0 的一些小经验.note.md
- raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming原理详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming编程指南.note.md
- raw/wujinsen_markdown/BigData/spark(1)/SparkStreaming调优总结1.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark中参数设置总结 JVM - 动态调整executor数.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark共享变量的使用.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结1.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结2.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结3.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark性能调优.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕_ Shuffle详解（一）.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕_ Shuffle详解（三）.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕_ Shuffle详解（二）.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕_ Task向Executor提交的源码解析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕： 如何解决Shuffle Write一定要落盘的问题？.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle Map Task运算结果的处理.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle Read的整体流程.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Shuffle的性能调优.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Sort Based Shuffle实现解析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Stage划分及提交源码分析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Spark技术内幕：Storage 模块整体架构.note.md
- raw/wujinsen_markdown/BigData/spark(1)/Storm与Spark Streaming比较 .note.md
- raw/wujinsen_markdown/BigData/spark(1)/kafka--streaming.note.md
- raw/wujinsen_markdown/BigData/spark(1)/kafka参数配置详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/kafka集群原理介绍(2).note.md
- raw/wujinsen_markdown/BigData/spark(1)/shuffle相关.note.md
- raw/wujinsen_markdown/BigData/spark(1)/spark-1.3.0作业提交的几种方式.note.md
- raw/wujinsen_markdown/BigData/spark(1)/spark-submit工具参数说明.note.md
- raw/wujinsen_markdown/BigData/spark(1)/sparkstreaming整合kafka的两种机制比较分析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/spark性能调优总结1.note.md
- raw/wujinsen_markdown/BigData/spark(1)/spark配置参数详解.note.md
- raw/wujinsen_markdown/BigData/spark(1)/使用Spark MLlib给豆瓣用户推荐电影.note.md
- raw/wujinsen_markdown/BigData/spark(1)/开发自己的Shuffle Service？.note.md
- raw/wujinsen_markdown/BigData/spark(1)/操作DataFrame.note.md
- raw/wujinsen_markdown/BigData/spark(1)/整合kafka和spark的深层解析.note.md
- raw/wujinsen_markdown/BigData/spark(1)/理解Spark的核心RDD.note.md
- raw/wujinsen_markdown/BigData/spark(1)/论SparkStreaming的数据可靠性和一致性.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/spark安装文档(ok).note.md
- raw/wujinsen_markdown/大数据资料-王/spark/Kafka+Spark Streaming+Redis实时系统实践.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/Spark RDD详解.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/Spark算子系列文章.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/ip转数字.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/mapPartitions和mapPartitionsWithIndex.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/mapValues(function) .note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark RDD函数详解.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark-sql.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark-streaming.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark公开课.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark基础.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/executor启动和任务处理流程.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/master启动流程.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/worker启动流程.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/任务启动流程submit.note.md
- raw/wujinsen_markdown/大数据资料-王/spark/spark源码分析/任务提交流程.note.md
- raw/wujinsen_markdown/面试笔试/海量数据处理：十道面试题与十个海量数据处理方法总结.note.md
related: [hadoop-生态入门, hive-数仓与-sql, flink-流批一体入门]
created: 2026-07-05
updated: 2026-07-05
---

# Spark 核心概念与实践

## 1. 架构

Driver + Executors；DAGScheduler 切 Stage，TaskScheduler 发 Task。比 MapReduce **内存迭代**快。

## 2. RDD / DataFrame / Dataset

| API | 特点 |
|-----|------|
| **RDD** | 弹性分布式数据集；血缘 lineage 容错 |
| **DataFrame** | 结构化；Catalyst 优化 |
| **Dataset** | 类型安全 DataFrame（Scala/Java） |

## 3. 宽窄依赖与 Stage

宽依赖（shuffle）划新 Stage；常见 shuffle：groupBy、join、repartition。

## 4. 调优备忘

- `spark.default.parallelism` 与分区数
- 广播大变量避免 shuffle
- 序列化 Kryo；避免 UDF 装箱

## 5. 与 Flink

Spark 偏 **批+微批**；低延迟流见 [[bigdata/flink-流批一体入门]]。

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **85** 篇。

## 原文插图（wujinsen）

> wujinsen 原文插图回迁（T22）· 共 6 组

> 图源 `raw/wujinsen_markdown/BigData/Spark/各模式下运行spark自带实例SparkPi.note.md` · T22 **D** 档

### 来自：各模式下运行spark自带实例SparkPi

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Spark/%E5%90%84%E6%A8%A1%E5%BC%8F%E4%B8%8B%E8%BF%90%E8%A1%8Cspark%E8%87%AA%E5%B8%A6%E5%AE%9E%E4%BE%8BSparkPi.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/BigData/Spark/消息队列设计精要.note.md` · T22 **D** 档

### 来自：消息队列设计精要

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Spark/%E6%B6%88%E6%81%AF%E9%98%9F%E5%88%97%E8%AE%BE%E8%AE%A1%E7%B2%BE%E8%A6%81.note_images/imageFile1.png)

> 图源 `raw/wujinsen_markdown/BigData/spark(1)/Spark常见问题总结2.note.md` · T22 **D** 档

### 来自：Spark常见问题总结2

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/Spark%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98%E6%80%BB%E7%BB%932.note_images/imageFile1.png)

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

原文插图 annex：[[bigdata/annex-Spark常用算子详解]]

原文插图 annex：[[bigdata/annex-spark基础]]

原文插图 annex：[[bigdata/annex-GC调优在Spark应用中的实践(1)]]

原文插图 annex：[[bigdata/annex-GC调优在Spark应用中的实践]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Stage划分及提交源码分析]]

原文插图 annex：[[bigdata/annex-Spark性能优化指南——高级篇]]

原文插图 annex：[[bigdata/annex-Parquet格式详解]]

原文插图 annex：[[bigdata/annex-Spark计算过程分析]]

原文插图 annex：[[bigdata/annex-论SparkStreaming的数据可靠性和一致性]]

原文插图 annex：[[bigdata/annex-Spark性能调优]]

原文插图 annex：[[bigdata/annex-SparkStreaming编程指南]]

原文插图 annex：[[bigdata/annex-Spark常见问题总结3]]

原文插图 annex：[[bigdata/annex-spark-sql]]

原文插图 annex：[[bigdata/annex-Spark性能优化指南——基础篇]]

原文插图 annex：[[bigdata/annex-executor启动和任务处理流程]]

原文插图 annex：[[bigdata/annex-master启动流程]]

原文插图 annex：[[bigdata/annex-worker启动流程]]

原文插图 annex：[[bigdata/annex-任务启动流程submit]]

原文插图 annex：[[bigdata/annex-任务提交流程]]

原文插图 annex：[[bigdata/annex-spark-yarn-模式安装]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Shuffle-Map-Task运算结果的处理]]

原文插图 annex：[[bigdata/annex-使用Spark-MLlib给豆瓣用户推荐电影]]

原文插图 annex：[[bigdata/annex-spark-on-yarn提交任务时报ClosedChannelException解决方案]]

原文插图 annex：[[bigdata/annex-Preview-of-Apache-Spark-2.0-now-on-Databricks-Community-Edition-Easier,-Fa]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Sort-Based-Shuffle实现解析]]

原文插图 annex：[[bigdata/annex-Sharethrough使用Spark-Streaming优化实时竞价]]

原文插图 annex：[[bigdata/annex-Spark1.0.0-的一些小经验]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Shuffle-Read的整体流程]]

原文插图 annex：[[bigdata/annex-Spark技术内幕：Storage-模块整体架构]]

原文插图 annex：[[bigdata/annex-开发自己的Shuffle-Service？]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/spark(1)/Spark on yarn中的内存溢出案例.note.md` · T22 **B** 档

### 来自：Spark on yarn中的内存溢出案例

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/spark%281%29/Spark%20on%20yarn%E4%B8%AD%E7%9A%84%E5%86%85%E5%AD%98%E6%BA%A2%E5%87%BA%E6%A1%88%E4%BE%8B.note_images/imageFile1.png)
