---
title: Elasticsearch 面试题
slug: elasticsearch-面试题
type: interview
status: active
tags: [elasticsearch, 面试, Lucene]
sources:
 - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之Elasticsearch篇.note.md
related: [elasticsearch-搜索, es-索引与写入流程, es-搜索与分片路由, mysql-索引面试题]
created: 2026-06-22
updated: 2026-06-22
---

# Elasticsearch 面试题

> 枢纽 [[search/elasticsearch-搜索]]；写入 [[search/es-索引与写入流程]]；搜索 [[search/es-搜索与分片路由]]。

## Q1. ES 集群如何选 master？

ZenDiscovery：master 候选节点按 nodeId 排序投票，得票 ≥ n/2+1 且自投则当选。防脑裂：`minimum_master_nodes > 候选/2`；两节点仅 1 个 master 候选。

## Q2. 索引文档过程？

协调节点算 shard → 主分片写 Memory Buffer + translog → refresh 可搜 → flush 落盘清 translog。见 [[search/es-索引与写入流程]]。

## Q3. 更新/删除如何实现？

文档不可变；删除标记 `.del`；更新 = 删旧 + 写新 segment。merge 时物理清理。

## Q4. 搜索过程？

**Query Then Fetch**：各分片 query 得 id+sort → 协调节点合并 → fetch `_source`。近实时因 refresh。见 [[search/es-搜索与分片路由]]。

## Q5. 倒排索引如何查词？

分词得 term → 倒排表得 posting list（doc id）。Lucene Segment 不可变，增量新 segment。

## Q6. refresh 和 flush 区别？

refresh：buffer → segment，可搜索。flush：fsync 持久化，清 translog。

## Q7. 如何保证读写一致？

写：quorum/one/all；读：replication=sync 或 `_preference=primary`。乐观锁 version/seq_no。

## Q8. 大数据量 cardinality 聚合？

HyperLogLog++ 近似去重，内存与精度可配置，适合上亿 distinct。

## Q9. ES 部署优化？

堆 ≤ 32GB（压缩指针）；留一半 RAM 给 Lucene OS cache；SSD；避免 swap；足够 file descriptor；bulk 导入调 refresh/replicas。

## Q10. ES vs MySQL 索引？

MySQL B+Tree 适合范围/精确（[[database/mysql-索引]]）；ES 倒排适合全文与相关性。知识库现用 MySQL LIKE，扩展见 [[search/elasticsearch-搜索]]。
