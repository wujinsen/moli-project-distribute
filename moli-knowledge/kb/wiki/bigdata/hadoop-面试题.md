---
title: Hadoop 面试题
slug: hadoop-面试题
type: interview
status: active
tags: [Hadoop, HDFS, YARN, 面试题, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Hadoop/大数据面试题.note.md
- raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop大数据面试--Hadoop篇 [复制链接].note.md
- raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop面试题.note.md
- raw/wujinsen_markdown/面试笔试/教你如何迅速秒杀掉：99%的海量数据处理面试题.note.md
related: [hadoop-生态入门, spark-面试题, hive-数仓与-sql]
created: 2026-07-05
updated: 2026-07-05
---

# Hadoop 面试题

> 概念综述见 [[bigdata/hadoop-生态入门]]。

## Q1. HDFS 读写流程？

**写**：Client → NN 申请块 → DN pipeline 三副本复制 → 确认。**读**：Client → NN 元数据 → 就近 DN 读块。

## Q2. Secondary NameNode 作用？

**不是**热备 NN；定期合并 fsimage+edits，辅助 NN 启动恢复。HA 用 QJM + ZKFC。

## Q3. YARN 调度流程？

Client 提交 → RM 分配 AM Container → AM 向 RM 申请 Task Container → NM 启动 Task。

## Q4. 小文件问题？

NN 内存压力；合并小文件；SequenceFile/Parquet；Har 归档。

## Q5. MapReduce shuffle？

Map 分区排序 spill → 拷贝 → Reduce merge → reduce 聚合。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **3** 篇。
