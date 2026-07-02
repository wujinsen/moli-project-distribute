---
title: ES 索引与写入流程
slug: es-索引与写入流程
type: article
status: active
tags: [elasticsearch, 写入, refresh, translog]
sources:
 - raw/wujinsen_markdown/面试笔试/面试小结/面试小结之Elasticsearch篇.note.md
related: [elasticsearch-搜索, es-搜索与分片路由, mysql-事务与锁]
created: 2026-06-22
updated: 2026-06-22
---

# ES 索引与写入流程

> 枢纽 [[search/elasticsearch-搜索]]；搜索阶段 [[search/es-搜索与分片路由]]。

## 1. 文档路由

协调节点计算主分片：

```
shard = hash(document_id) % num_primary_shards
```

创建索引后**主分片数不可改**（需 reindex）。可用 `routing` 自定义路由键。

## 2. 写入路径

```
请求 → 协调节点 → 主分片
 → 写入 Memory Buffer
 → 同时写 translog（防丢）
 → refresh（默认约 1s）→ 进入 Filesystem Cache，可搜索（近实时）
 → flush（定时或 translog 过大）→ 落盘 segment，清 translog
```

| 阶段 | 说明 |
|------|------|
| **refresh** | Buffer → 新 segment，**可搜但未持久化到磁盘** |
| **flush** | fsync 提交点，translog 截断 |
| **translog** | 默认 512MB 或 30min 等触发 flush |

因此 ES 搜索是 **near real-time**（默认 1s 级延迟）。

## 3. Segment 与合并

- Lucene 索引由多个 **Segment** 组成，段内倒排索引**不可变**
- 增量写 = 新 segment；段越多搜索越慢
- ES **merge** 小 segment 为大 segment

## 4. 更新与删除（逻辑）

文档**不可变**；更新 = 旧版标记删除（`.del`）+ 新版写入新 segment。删除 = 标记删除，merge 时物理清理。

## 5. 可靠性

- translog 保证 refresh 前宕机可恢复
- 写一致性：`quorum`（默认，多数分片可用才 ack）/ `one` / `all`
- 乐观锁：`_version` / `if_seq_no` + `if_primary_term`

## 6. 批量导入优化

| 手段 | 说明 |
|------|------|
| bulk 5–15MB/批 | 减少网络往返 |
| `index.number_of_replicas: 0` | 导入期关副本 |
| `refresh_interval: 30s` | 降低 refresh 频率 |
| 加大 `translog.flush_threshold_size` | 减少 flush 次数 |
| SSD | 段合并与 IO |

压测导入时勿用默认实时 refresh 期望。

## 7. 与 MySQL 对比

MySQL InnoDB redo/undo 保证事务持久（[[database/mysql-事务与锁]]）；ES 侧重**搜索可见性**与**最终一致**，不适合替代 OLTP 主库。
