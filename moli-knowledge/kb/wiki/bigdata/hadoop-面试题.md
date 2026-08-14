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

<!-- t22-wujinsen-images:raw/wujinsen_markdown/面试笔试/教你如何迅速秒杀掉：99%的海量数据处理面试题.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/面试笔试/教你如何迅速秒杀掉：99%的海量数据处理面试题.note.md` · T22 **B** 档

### 来自：教你如何迅速秒杀掉：99%的海量数据处理面试题

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E6%95%99%E4%BD%A0%E5%A6%82%E4%BD%95%E8%BF%85%E9%80%9F%E7%A7%92%E6%9D%80%E6%8E%89%EF%BC%9A99%25%E7%9A%84%E6%B5%B7%E9%87%8F%E6%95%B0%E6%8D%AE%E5%A4%84%E7%90%86%E9%9D%A2%E8%AF%95%E9%A2%98.note_images/imageFile1.png)

![image 2](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E6%95%99%E4%BD%A0%E5%A6%82%E4%BD%95%E8%BF%85%E9%80%9F%E7%A7%92%E6%9D%80%E6%8E%89%EF%BC%9A99%25%E7%9A%84%E6%B5%B7%E9%87%8F%E6%95%B0%E6%8D%AE%E5%A4%84%E7%90%86%E9%9D%A2%E8%AF%95%E9%A2%98.note_images/imageFile2.png)

![image 3](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/%E9%9D%A2%E8%AF%95%E7%AC%94%E8%AF%95/%E6%95%99%E4%BD%A0%E5%A6%82%E4%BD%95%E8%BF%85%E9%80%9F%E7%A7%92%E6%9D%80%E6%8E%89%EF%BC%9A99%25%E7%9A%84%E6%B5%B7%E9%87%8F%E6%95%B0%E6%8D%AE%E5%A4%84%E7%90%86%E9%9D%A2%E8%AF%95%E9%A2%98.note_images/imageFile3.png)

原文插图 annex：[[bigdata/annex-Hadoop大数据面试-Hadoop篇-复制链接]]
