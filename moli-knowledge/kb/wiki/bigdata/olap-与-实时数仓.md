---
title: OLAP 与实时数仓
slug: olap-与-实时数仓
type: article
status: active
tags: [OLAP, ClickHouse, Kylin, Presto, 实时数仓, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Hudi/方案/百信银行基于ApacheHudi实时数据湖演进方案.note.md
- raw/wujinsen_markdown/BigData/Kylin/Kylin简介.note.md
- raw/wujinsen_markdown/BigData/OLAP/ApacheDoris/安装/ApacheDoris安装部署.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/clickhouse.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/从 ClickHouse 到 ByteHouse：实时数据分析场景下的优化实践.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/ClickHouse主键索引最佳实践.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clichouse索引.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse sql语法注意.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouseji建表规范.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse使用一些优化和经验.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse引擎.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse精确去重.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/优化查询性能-深入理解ClickHouse跳数索引.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/安装部署/linux安装clickhouse.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/安装部署/mac 安装clickhouse.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/安装部署/mac安装clickhouse.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/安装部署/可视化界面工具.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/性能调优/clickhouse慢查询调优.note.md
- raw/wujinsen_markdown/BigData/OLAP/ClickHouse/老司机教你如何调教Presto和ClickHouse，应对业务难题！.note.md
- raw/wujinsen_markdown/BigData/OLAP/HBase/安装部署/hbase安装部署.note.md
- raw/wujinsen_markdown/BigData/OLAP/OLAP基础知识梳理.note.md
- raw/wujinsen_markdown/BigData/OLAP/无标题笔记.note.md
- raw/wujinsen_markdown/BigData/数据仓库/实时数仓/Flink SQL 在美团实时数仓中的增强与实践.note.md
- raw/wujinsen_markdown/BigData/数据仓库/实时数仓实战项目：架构、分层、设计、场景、框架、以及流批一体....note.md
- raw/wujinsen_markdown/BigData/数据仓库/数据湖/Flink实战之Flink CDC + Hudi + Hive + Presto 构建实时数据湖.note.md
- raw/wujinsen_markdown/源码分析/clickhouse/ClickHouse源码阅读计划（三）物化视图的概念、场景、用法和源码实现.note.md
related: [数仓分层与建模, flink-流批一体入门, kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

# OLAP 与实时数仓

## 1. OLAP 引擎选型

| 引擎 | 特点 |
|------|------|
| **ClickHouse** | 列存 MPP；明细/聚合查询快 |
| **Kylin** | Cube 预计算；固定维度组合 |
| **Presto/Trino** | 联邦查询；跨 Hive/MySQL |
| **Doris/StarRocks** | 实时导入 + 高并发查询 |

## 2. 离线 vs 实时数仓

- **离线**：T+1 批处理；Hive/Spark → DWS → OLAP
- **实时**：Kafka + Flink → 明细/汇总 → OLAP/Redis 大屏

## 3. 实时数仓分层（raw）

ODS（Kafka 原始）→ DWD（清洗关联）→ DWS（窗口聚合）→ ADS（指标 API）。

## 4. ClickHouse 备忘

MergeTree 引擎；分区 + 排序键；物化视图预聚合。源码阅读见 raw `源码分析/clickhouse/` sources。

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **24** 篇。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse精确去重.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/OLAP/ClickHouse/基础教程/clickhouse精确去重.note.md` · T22 **D** 档

### 来自：clickhouse精确去重

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/OLAP/ClickHouse/%E5%9F%BA%E7%A1%80%E6%95%99%E7%A8%8B/clickhouse%E7%B2%BE%E7%A1%AE%E5%8E%BB%E9%87%8D.note_images/imageFile1.png)

原文插图 annex：[[bigdata/annex-老司机教你如何调教Presto和ClickHouse，应对业务难题！]]

原文插图 annex：[[bigdata/annex-百信银行基于ApacheHudi实时数据湖演进方案]]

原文插图 annex：[[bigdata/annex-ClickHouse主键索引最佳实践]]

原文插图 annex：[[bigdata/annex-实时数仓实战项目：架构、分层、设计、场景、框架、以及流批一体...]]

原文插图 annex：[[bigdata/annex-优化查询性能-深入理解ClickHouse跳数索引]]

原文插图 annex：[[bigdata/annex-Flink实战之Flink-CDC-+-Hudi-+-Hive-+-Presto-构建实时数据湖]]

原文插图 annex：[[bigdata/annex-从-ClickHouse-到-ByteHouse：实时数据分析场景下的优化实践]]
