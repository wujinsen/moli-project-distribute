---
title: Flink 面试题
slug: flink-面试题
type: interview
status: active
tags: [Flink, 实时数仓, 面试题, 大数据]
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
- raw/wujinsen_markdown/BigData/数据仓库/实时数仓/Flink SQL 在美团实时数仓中的增强与实践.note.md
related: [flink-流批一体入门, kafka-大数据管道, olap-与-实时数仓]
created: 2026-07-05
updated: 2026-07-05
---

# Flink 面试题

> 入门见 [[bigdata/flink-流批一体入门]]。

## Q1. Event Time / Processing Time？

Event Time 用数据自带时间；Watermark 推进窗口；Processing Time 用系统时钟。

## Q2. Checkpoint 原理？

Barrier 对齐（或 unaligned）；状态快照异步；失败从 CK 恢复。

## Q3. Exactly-once？

Source 可重放 + 状态 CK + Sink 幂等/两阶段提交。

## Q4. 反压怎么处理？

定位瓶颈算子；并行度、资源、异步 IO、mini-batch。

## Q5. Flink vs Spark Streaming？

Flink 原生流；Spark SS 微批；低延迟选 Flink。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **12** 篇。

原文插图 annex：[[bigdata/annex-Flink从入门到入土（详细教程）]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/Flink/基础/flink-table-planner-blink.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/Flink/基础/flink-table-planner-blink.note.md` · T22 **B** 档

### 来自：flink-table-planner-blink

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Flink/%E5%9F%BA%E7%A1%80/flink-table-planner-blink.note_images/imageFile1.png)

原文插图 annex：[[bigdata/annex-Flink-原理与实现：Window-机制]]

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/Flink/aboutyun/深入解析 Flink 细粒度资源管理.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/Flink/aboutyun/深入解析 Flink 细粒度资源管理.note.md` · T22 **B** 档

### 来自：深入解析 Flink 细粒度资源管理

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Flink/aboutyun/%E6%B7%B1%E5%85%A5%E8%A7%A3%E6%9E%90%20Flink%20%E7%BB%86%E7%B2%92%E5%BA%A6%E8%B5%84%E6%BA%90%E7%AE%A1%E7%90%86.note_images/imageFile1.png)

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/Flink/flinkcdc/flink cdc mysql到clickhouse.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/Flink/flinkcdc/flink cdc mysql到clickhouse.note.md` · T22 **B** 档

### 来自：flink cdc mysql到clickhouse

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Flink/flinkcdc/flink%20cdc%20mysql%E5%88%B0clickhouse.note_images/imageFile1.png)
