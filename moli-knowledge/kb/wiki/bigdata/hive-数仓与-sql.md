---
title: Hive 数仓与 SQL
slug: hive-数仓与-sql
type: guide
status: active
tags: [Hive, 数仓, SQL, 大数据]
sources:
- raw/wujinsen_markdown/BigData/Hive/Hive SQL/计算次留.note.md
- raw/wujinsen_markdown/BigData/Hive/Hive内部表和外部表的区别详解.note.md
- raw/wujinsen_markdown/BigData/Hive/Hive命令/hive常用命令.note.md
- raw/wujinsen_markdown/BigData/Hive/Hive安装与配置详解.note.md
- raw/wujinsen_markdown/BigData/Hive/Hive怎样加入第三方JAR.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/Datax抽取mongo数据到hdfs.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/Hortonwork Ambari配置Hive集成Hbase的java开发maven配置.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/MongoDB数据增量同步到Hive（方案一通过BSON文件映射）.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/datax导入hive脚本.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/dolphinscheduler脚本.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/hdfs操作命令.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/mongodb脚本文件- 同步json到hive.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/sqoop系列-sqoop MongoDB导入Hive方案.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/脚本文件.note.md
- raw/wujinsen_markdown/BigData/Hive/MongoDBToHive/采坑记录.note.md
- raw/wujinsen_markdown/BigData/Hive/hive基础命令.note.md
- raw/wujinsen_markdown/BigData/Hive/基础教程/hive sql相关.note.md
- raw/wujinsen_markdown/BigData/Hive/基础教程/hive基础教程.note.md
- raw/wujinsen_markdown/BigData/Hive/安装部署/Hive2.3.8安装配置.note.md
- raw/wujinsen_markdown/BigData/Hive/安装部署/Hive如何添加第三方JAR.note.md
- raw/wujinsen_markdown/BigData/Hive/安装部署/hive2.3.2安装使用.note.md
- raw/wujinsen_markdown/BigData/Hive/安装部署/hive安装.note.md
- raw/wujinsen_markdown/BigData/Hive/采坑记录/hive启动报错.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/hive web页面的搭建  .note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/hive安装文档.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/修改hive元数据库.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/Hive MapJoin 优化.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/Hive配置项的含义详解.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/OLAP.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/OLTP.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/hive mapjoin使用.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/hive.note.md
- raw/wujinsen_markdown/大数据资料-王/hive/hive操作base.note.md
related: [hadoop-生态入门, 数仓分层与建模, spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

# Hive 数仓与 SQL

## 1. 定位

**SQL on Hadoop**；Metastore（MySQL）存库表元数据；执行引擎 MapReduce/Tez/Spark。

## 2. 表类型

| 类型 | 说明 |
|------|------|
| **内部表** | 删表删 HDFS 数据 |
| **外部表** | EXTERNAL；删表保留数据路径 |
| **分区表** | PARTITION；剪枝加速 |
| **分桶表** | CLUSTERED BY；采样 join |

## 3. 常用优化

- 分区字段过滤；避免全表 scan
- 小文件合并；ORC/Parquet 列存
- 谓词下推、列裁剪

数仓分层 ODS/DWD/DWS 见 #1323 [[bigdata/数仓分层与建模]]（待 ingest）。

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **30** 篇。

<!-- t22-wujinsen-images:raw/wujinsen_markdown/BigData/Hive/Hive命令/hive常用命令.note.md -->
## 原文插图（wujinsen）

> 图源 `raw/wujinsen_markdown/BigData/Hive/Hive命令/hive常用命令.note.md` · T22 **D** 档

### 来自：hive常用命令

![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Hive/Hive%E5%91%BD%E4%BB%A4/hive%E5%B8%B8%E7%94%A8%E5%91%BD%E4%BB%A4.note_images/imageFile1.png)

原文插图 annex：[[bigdata/annex-Hive安装与配置详解]]

原文插图 annex：[[bigdata/annex-hive2.3.2安装使用]]

原文插图 annex：[[bigdata/annex-hive]]

原文插图 annex：[[bigdata/annex-采坑记录]]

原文插图 annex：[[bigdata/annex-修改hive元数据库]]

原文插图 annex：[[bigdata/annex-Hortonwork-Ambari配置Hive集成Hbase的java开发maven配置]]

原文插图 annex：[[bigdata/annex-hive-web页面的搭建]]
