---
title: ELK 日志分析栈
slug: elk-日志分析栈
type: guide
status: active
tags: [ELK, Elasticsearch, Logstash, Kibana, 大数据]
sources:
- raw/wujinsen_markdown/BigData/ELK/Kibana安装.note.md
- raw/wujinsen_markdown/BigData/ELK/elasticsearch-head 安装.note.md
- raw/wujinsen_markdown/BigData/ELK/filebeat和logstash.note.md
- raw/wujinsen_markdown/BigData/ELK/安装/Docker安装ELK.note.md
- raw/wujinsen_markdown/BigData/FileBeat/filebeat-kafka日志收集.note.md
- raw/wujinsen_markdown/面试笔试/使用logstash收集日志的可靠性验证.note.md
related: [elasticsearch-搜索, kafka-大数据管道, flume-与-数据采集]
created: 2026-07-05
updated: 2026-07-05
---

# ELK 日志分析栈

## 1. 组件

| 组件 | 作用 |
|------|------|
| **Filebeat/Logstash** | 采集与解析 |
| **Elasticsearch** | 存储与检索 |
| **Kibana** | 可视化与 Dashboard |

## 2. 典型链路

App/File → Filebeat → Kafka（可选）→ Logstash → ES → Kibana。

ES 查询语法见 [[search/elasticsearch-搜索]]、[[search/elasticsearch-面试题]]。

## 3. 运维要点

- 索引按天滚动；ILM 冷热分层
- mapping 与 dynamic 模板；避免 field 爆炸
- 集群监控：shard 数、GC、写入 reject

## 批次#1323 增补（wujinsen Phase2 P2 数仓调度）

本页 Phase 2 #1323 创建；sources **5** 篇。
