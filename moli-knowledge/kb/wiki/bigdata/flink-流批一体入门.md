---
title: Flink 流批一体入门
slug: flink-流批一体入门
type: article
status: active
tags: [Flink, 流计算, 实时数仓, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Flink/Flink 原理与实现：Window 机制.note.md
- raw/wujinsen_markdown/BigData/Flink/aboutyun/深入解析 Flink 细粒度资源管理.note.md
- raw/wujinsen_markdown/BigData/Flink/flinkcdc/flink cdc mysql到clickhouse.note.md
- raw/wujinsen_markdown/BigData/Flink/基础/Flink从入门到入土（详细教程）.note.md
- raw/wujinsen_markdown/BigData/Flink/基础/Flink官方中文文档.note.md
- raw/wujinsen_markdown/BigData/Flink/基础/flink-table-planner-blink.note.md
- raw/wujinsen_markdown/BigData/Flink/数据同步/一文讲解从Flink、Spark、Kafka、MySQL、Hive导入数据到ClickHouse.note.md
- raw/wujinsen_markdown/BigData/Flink/源码分析/《Flink 源码解析》—— 源码编译运行.note.md
- raw/wujinsen_markdown/BigData/Flink/调优/flink实时写hive 产生的小文件处理方式.note.md
- raw/wujinsen_markdown/BigData/Flink/问题/java.lang.NoClassDefFoundError.note.md
- raw/wujinsen_markdown/BigData/Flink/问题/无标题笔记.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm UI界面参数含义.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm-UI-Restful.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm-源码分析汇总/Storm-源码分析汇总.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm命令详解.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm安装启动.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm概念讲解和工作原理介绍.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm的坑/Storm 集群空闲 CPU 飙高问题排查.note.md
- raw/wujinsen_markdown/BigData/Storm/Storm的基本概念.note.md
- raw/wujinsen_markdown/BigData/Storm/storm 入门原理介绍.note.md
- raw/wujinsen_markdown/BigData/Storm/storm不同版本storm.yaml.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/storm.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/storm安装文档.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/Storm入门.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：一致性事务.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：前言.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：安装部署步骤详解(1).note.md
- raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：构建Topology.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/Storm入门教程：消息的可靠处理.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/bloom filter 的Java 版.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/storm入门(1)(08-48-52).note.md
- raw/wujinsen_markdown/大数据资料-王/storm/storm实战入门一.note.md
- raw/wujinsen_markdown/大数据资料-王/storm/storm开发文档.note.md
related: [kafka-大数据管道, spark-核心概念与实践, olap-与-实时数仓]
created: 2026-07-05
updated: 2026-07-05
---

# Flink 流批一体入门

## 1. 定位

**事件驱动**流引擎；同一套 DataStream API 可跑批（有界流）。延迟毫秒~秒级。

## 2. 核心概念

| 概念 | 说明 |
|------|------|
| **Event Time** | 事件自带时间戳 |
| **Watermark** | 衡量事件时间进度，触发窗口 |
| **Checkpoint** | 分布式快照容错；Barrier 对齐 |
| **State** | Keyed State / Operator State |

## 3. 窗口

滚动 Tumbling、滑动 Sliding、会话 Session；与 Kafka 源见 [[bigdata/kafka-大数据管道]]。

## 4. 与 Spark Streaming

Flink **原生流**；Spark Structured Streaming 微批模型。实时数仓分层见 Phase2 `olap-与-实时数仓`（#1323）。

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **11** 篇。
## Storm 历史对照（raw #1323）

Apache Storm 为 **纯流** 早期方案（Spout/Bolt、acker 机制）。维护活跃度低，新项目优先 **Flink** 或 Spark Structured Streaming。面试可答：Storm at-least-once + ack；与 Flink Checkpoint 精确一次对比。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

合并 Storm raw 作历史对照；Storm 不单独建页。

原文插图 annex：[[bigdata/annex-Flink从入门到入土（详细教程）]]

原文插图 annex：[[bigdata/annex-Storm安装启动]]
