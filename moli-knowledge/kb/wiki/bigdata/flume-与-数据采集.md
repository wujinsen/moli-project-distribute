---
title: Flume 与数据采集
slug: flume-与-数据采集
type: guide
status: active
tags: [Flume, 采集, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Flume/Flume之agent基本配置使用.note.md
- raw/wujinsen_markdown/BigData/Flume/日志采集框架Flume.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/flume安装文档.note.md
- raw/wujinsen_markdown/大数据资料-王/flume/Flume-ng+Kafka+storm的学习笔记.note.md
- raw/wujinsen_markdown/大数据资料-王/flume/Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据).note.md
- raw/wujinsen_markdown/大数据资料-王/flume/flume_log4j.note.md
- raw/wujinsen_markdown/大数据资料-王/flume/什么是Flume.note.md
- raw/wujinsen_markdown/大数据资料-王/flume/让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2.note.md
related: [kafka-大数据管道, 数据采集与-etl-工具选型]
created: 2026-07-05
updated: 2026-07-05
---

# Flume 与数据采集

## 1. 架构

Agent = **Source** + **Channel** + **Sink**。多 Agent 串联 Fan-out。

## 2. 常见 Source/Sink

| Source | Sink |
|--------|------|
| exec/taildir/spooldir | HDFS |
| avro/thrift | Kafka |
| kafka | HBase |

## 3. 可靠性

Channel 类型：Memory（快）/ File（持久）。事务提交保证 At-least-once。

与 DataX/Sqoop 选型见 #1323 `数据采集与-etl-工具选型`。

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **7** 篇。

原文插图 annex：[[bigdata/annex-让你快速认识flume及安装和使用flume1.5传输数据(日志)到hadoop2.2]]

原文插图 annex：[[bigdata/annex-Flume的体系结构介绍以及Flume入门案例(往HDFS上传数据)]]

原文插图 annex：[[bigdata/annex-什么是Flume]]
