---
title: ES 搜索与分片路由
slug: es-搜索与分片路由
type: article
status: active
tags: [elasticsearch, 搜索, 分片, Query-Then-Fetch]
sources:
- raw/wujinsen_markdown/面试笔试/面试小结/面试小结之Elasticsearch篇.note.md
related: [elasticsearch-搜索, es-索引与写入流程, es-match与bool查询]
created: 2026-06-22
updated: 2026-07-05
---

# ES 搜索与分片路由

> 概念 [[search/elasticsearch-搜索]]；DSL [[search/es-match与bool查询]]。

## 1. Query Then Fetch（默认）

两阶段：

**阶段 1 — Query**

1. 协调节点将查询广播到各分片（主或副本）
2. 每分片本地搜索，构建 **priority queue**（size = from + size）
3. 返回 **doc id + 排序值** 给协调节点
4. 协调节点合并为全局 TopN

**阶段 2 — Fetch**

1. 协调节点对全局结果 doc id 向各分片 **GET** 取 `_source`
2. 聚合后返回客户端

搜索会查 **Filesystem Cache**；部分数据仍在 Memory Buffer → **近实时**。

## 2. DFS Query Then Fetch

默认按**本分片**统计词频，词少时分词相关性可能偏。DFS 先**预查全局词频**再打分，更准但更慢。一般默认模式即可。

## 3. 倒排索引如何命中

分词 → term → 倒排表（posting list）→ 文档 id 集合。Lucene 底层 FST/Timeline 等结构见官方「索引文件格式」；面试知道 **term → 文档列表** 即可（[[search/elasticsearch-面试题]]）。

## 4. 分片与副本

| 角色 | 职责 |
|------|------|
| Primary | 写入口 |
| Replica | 读扩展、故障转移 |

查询可打副本分担读；写只进 primary 再同步副本。

## 5. 深分页问题

`from + size` 很大时，协调节点需每分片收集 from+size 条再排序，**内存与延迟爆炸**。

替代：

- **`search_after`**：游标，适合实时翻页
- **`scroll`**：批量导出（非用户翻页）
- **限制 from+size**（如 max_result_window 10000）

对比 MySQL 深分页：[[database/mysql-深分页与慢sql优化]]。

## 6. Master 选举（集群）

- `node.master: true` 的节点参与选举
- 按 nodeId 排序投票，得票 ≥ `n/2+1` 且自投成为 master
- **脑裂**：设置 `discovery.zen.minimum_master_nodes` > 候选数一半；两节点集群应仅 1 个 master 候选

Master 管集群元数据，**不处理文档级** CRUD。

## 7. 客户端路由

TransportClient（旧）/ REST：轮询连接节点，由协调节点分发。生产用 **同版本 JVM**（ES 本地序列化敏感）。

原文插图 annex：[[search/annex-面试小结之Elasticsearch篇]]
