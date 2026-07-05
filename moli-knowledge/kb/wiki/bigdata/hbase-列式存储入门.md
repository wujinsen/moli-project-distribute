---
title: HBase 列式存储入门
slug: hbase-列式存储入门
type: concept
status: active
tags: [HBase, NoSQL, 大数据]
sources:
- raw/wujinsen_markdown/大数据资料-王/a安装文档/HBase多MASTER配置.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase增加backup master  .note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase多master.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase安装手顺(ok).note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase管理工具 phpHBaseAdmin.note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/nutch + hbase 安装部署(1).note.md
- raw/wujinsen_markdown/大数据资料-王/a安装文档/nutch + hbase 安装部署.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase基本数据操作详解.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase建表函数createTable的几点说明.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase开发代码---------------1遍.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase性能优化---------------1遍.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase性能优化方法总结（三）：读表操作.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase查询一条数据的过程. .note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase连接池 -- HTablePool被Deprecated以及可能原因是什么 .note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HBase随机读写性能测试.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/HTablePool的实现分析.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/Hbase 源码分析之 Regionserver上的 Get 全流程.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/hbase HTable之Put、delete、get等源码分析.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/hbase shell.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/hbase0.9.22全新api.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/hbase很有价值的读写性能提升.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/hbase源码系列（十二）Get、Scan在服务端是如何处理.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/hbase调优.note.md
- raw/wujinsen_markdown/大数据资料-王/hbase/nutch + hbase 安装部署.note.md
related: [hadoop-生态入门, hive-数仓与-sql]
created: 2026-07-05
updated: 2026-07-05
---

# HBase 列式存储入门

## 1. 模型

列族 Column Family；RowKey 字典序；稀疏宽表。基于 HDFS，Region 水平切分。

## 2. 读写

- 写：WAL + MemStore flush 成 HFile
- 读：BlockCache + BloomFilter 减少 IO

## 3. RowKey 设计

避免热点（单调递增前缀）；散列/反转/预分区。

## 4. 与 Hive

Hive 离线分析；HBase 低延迟点查/列存。可 Hive 外部表映射 HBase。

## 批次#1321 增补（wujinsen Phase2 P1 BigData）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **18** 篇。

原文插图 annex：[[bigdata/annex-hbase源码系列（十二）Get、Scan在服务端是如何处理]]

原文插图 annex：[[bigdata/annex-hbase多master]]

原文插图 annex：[[bigdata/annex-hbase安装手顺(ok)]]

原文插图 annex：[[bigdata/annex-hbase管理工具-phpHBaseAdmin]]

原文插图 annex：[[bigdata/annex-hbase-HTable之Put、delete、get等源码分析]]

原文插图 annex：[[bigdata/annex-HBase查询一条数据的过程.]]

原文插图 annex：[[bigdata/annex-HBase连接池-HTablePool被Deprecated以及可能原因是什么]]

原文插图 annex：[[bigdata/annex-Hbase-源码分析之-Regionserver上的-Get-全流程]]
