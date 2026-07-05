---
title: Kafka 大数据管道
slug: kafka-大数据管道
type: concept
status: active
tags: [Kafka, 日志, 管道, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Kafka/Kafka Consumer开发的一些关键点.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka Tools.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka 之 中级.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka 配置参数.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka入门经典教程.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka学习之consumer端部署及API.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka实战－KafkaOffsetMonitor.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka深度解析(1).note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka深度解析.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka管理工具介绍.note.md
- raw/wujinsen_markdown/BigData/Kafka/Kafka部署与代码实例.note.md
- raw/wujinsen_markdown/BigData/Kafka/apache kafka系列之在zookeeper中存储结构.note.md
- raw/wujinsen_markdown/BigData/Kafka/kafka offset迁移.note.md
- raw/wujinsen_markdown/BigData/Kafka/kafka的监控与告警.note.md
- raw/wujinsen_markdown/BigData/Kafka/shibing/免费资料.note.md
- raw/wujinsen_markdown/BigData/Kafka/为什么要在Kubernetes上运行Kafka，有哪些问题？.note.md
- raw/wujinsen_markdown/BigData/Kafka/其实kafka提供了一系列的工具可以查看offset..note.md
- raw/wujinsen_markdown/BigData/Kafka/如何手动更新Kafka中某个Topic的偏移量.note.md
- raw/wujinsen_markdown/BigData/Kafka/安装部署/kafka集群资源评估.note.md
- raw/wujinsen_markdown/BigData/Kafka/安装部署/个人kafka配置.note.md
- raw/wujinsen_markdown/BigData/Kafka/教程/Kafka rebalance机制.note.md
- raw/wujinsen_markdown/BigData/Kafka/教程/Kafka入门经典教程.note.md
- raw/wujinsen_markdown/BigData/Kafka/教程/【kafka】Kafka 之 Group 状态变化分析及 Rebalance 过程.note.md
- raw/wujinsen_markdown/BigData/Kafka/教程/什么是kafka的Rebalance.note.md
- raw/wujinsen_markdown/BigData/Kafka/无标题笔记.note.md
- raw/wujinsen_markdown/BigData/Kafka/源码分析/Kafka源码环境搭建.note.md
- raw/wujinsen_markdown/BigData/Kafka/踩坑记录/kafka rebalance.note.md
- raw/wujinsen_markdown/BigData/Pulsar/为什么已有Kafka，我们最终却选择了Apache Pulsar？.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/kafka集群安装文档.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/ActiveMQ-readme-王森丰.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/ActiveMQ_2.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/ActiveMQ_3.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/Kafka Producer同步模式发送message源码分析.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/Kafka 之 async producer (2) kafka.producer.async.DefaultEventHandler.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/Kafka命令.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka java示例.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka-users mailing list archives.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka分布式消息系统 .note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka可靠性.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka系列介绍 — 核心API介绍及实例.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka详细配置1.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/kafka详细配置2.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/分布式发布订阅消息系统 Kafka 架构设计.note.md
- raw/wujinsen_markdown/大数据资料-王/kafka/快速理解Kafka分布式消息队列框架.note.md
- raw/wujinsen_markdown/架构/埋点/西瓜客户端埋点实践：基于责任链的埋点框架.note.md
related: [kafka-与-mq选型, flink-流批一体入门, elk-日志分析栈]
created: 2026-07-05
updated: 2026-07-05
---

# Kafka 大数据管道

> 业务 MQ 选型与面试题见 [[middleware/kafka-与-mq选型]]；本文侧重 **日志/埋点/大数据管道**。

## 1. 架构

Topic 分区 + 副本；Producer → Broker → Consumer Group。Zookeeper/KRaft 存元数据。

## 2. 管道场景

- 日志采集 → Kafka → Flink/Spark → Hive/OLAP
- 埋点 → Kafka → 实时大屏 / 离线数仓

## 3. 可靠性

| 环节 | 配置 |
|------|------|
| Producer | `acks=all`，幂等 + 事务（EOS） |
| Broker | `min.insync.replicas`，禁止 unclean leader |
| Consumer | 处理完再 commit；幂等写入 |

## 4. 与 MQ 面试页

延迟、顺序、事务对比见 [[middleware/kafka-与-mq选型]]。

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **44** 篇。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/Kafka/为什么要在Kubernetes上运行Kafka，有哪些问题？.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/Kafka/为什么要在Kubernetes上运行Kafka，有哪些问题？.note.md` · T22 **D** 档

### 来自：为什么要在Kubernetes上运行Kafka，有哪些问题？

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Kafka/%E4%B8%BA%E4%BB%80%E4%B9%88%E8%A6%81%E5%9C%A8Kubernetes%E4%B8%8A%E8%BF%90%E8%A1%8CKafka%EF%BC%8C%E6%9C%89%E5%93%AA%E4%BA%9B%E9%97%AE%E9%A2%98%EF%BC%9F.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/Kafka/安装部署/kafka集群资源评估.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/Kafka/安装部署/kafka集群资源评估.note.md` · T22 **D** 档

### 来自：kafka集群资源评估

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Kafka/%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2/kafka%E9%9B%86%E7%BE%A4%E8%B5%84%E6%BA%90%E8%AF%84%E4%BC%B0.note_images/imageFile1.png)
