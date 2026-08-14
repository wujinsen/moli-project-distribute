---
title: 数据采集与 ETL 工具选型
slug: 数据采集与-etl-工具选型
type: article
status: active
tags: [DataX, Sqoop, Flume, ETL, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Sqoop/Sqoop 数据导出：全量、增量、更新.note.md
- raw/wujinsen_markdown/BigData/Sqoop/sqoop安装.note.md
- raw/wujinsen_markdown/BigData/Sqoop/安装部署/安装部署.note.md
- raw/wujinsen_markdown/BigData/Sqoop/导入数据.note.md
- raw/wujinsen_markdown/BigData/技术选型/Flinkx Datax Flink-CDC 优劣势对比.note.md
- raw/wujinsen_markdown/BigData/技术选型/大数据工具对比.note.md
- raw/wujinsen_markdown/BigData/技术选型/实时数仓技术选型.note.md
- raw/wujinsen_markdown/BigData/技术选型/离线数仓技术选项.note.md
- raw/wujinsen_markdown/BigData/数据采集/Datax/datax二次开发，非常详细[源码下载-项目搭建-打包发布].note.md
- raw/wujinsen_markdown/BigData/数据采集/Datax/datax数据采集.note.md
- raw/wujinsen_markdown/BigData/数据采集/Datax/教程/datax使用教程.note.md
- raw/wujinsen_markdown/BigData/数据采集/利用 Log-Pilot + Kafka + Elasticsearch + Kibana 搭建 kubernetes日志解决方案.note.md
- raw/wujinsen_markdown/BigData/数据采集/数据采集工具对比.note.md
- raw/wujinsen_markdown/BigData/数据采集/日志采集框架.note.md
- raw/wujinsen_markdown/大数据资料-王/sqoop/kettle.note.md
- raw/wujinsen_markdown/大数据资料-王/sqoop/利用SQOOP将数据从数据库导入到HDFS.note.md
related: [flume-与-数据采集, mysql-binlog与canal同步, kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

# 数据采集与 ETL 工具选型

## 1. 工具对比

| 工具 | 场景 |
|------|------|
| **Sqoop** | RDBMS ↔ HDFS/Hive 批量导入导出 |
| **DataX** | 阿里开源；多源异构离线同步 |
| **Flink CDC / Canal** | 增量实时；见 [[database/mysql-binlog与canal同步]] |
| **Flume** | 日志/文件流式采集；见 [[bigdata/flume-与-数据采集]] |

## 2. 选型要点

- 批量 vs 实时；全量 vs 增量
- 一致性（至少一次/精确一次）
- 运维成本与监控

## 3. FlinkX / DataX / CDC（raw 技术选型）

批同步优先 DataX；实时入湖 Flink CDC；与 Kafka 管道组合见 [[bigdata/kafka-大数据管道]]。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **16** 篇。

原文插图 annex：[[bigdata/annex-Sqoop-数据导出：全量、增量、更新]]

原文插图 annex：[[bigdata/annex-利用-Log-Pilot-+-Kafka-+-Elasticsearch-+-Kibana-搭建-kubernetes日志解决方案]]
