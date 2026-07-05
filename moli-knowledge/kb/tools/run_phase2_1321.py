#!/usr/bin/env python3
"""Phase 2 batch #1321: BigData core create + enrich (Hadoop/Spark/Flink/Hive/Kafka/HBase/Flume)."""
from __future__ import annotations

from pathlib import Path

from wujinsen_ingest_lib import (
    WIKI,
    append_log,
    apply_enrich_batch,
    build_slug_sources,
    get_sources,
    list_raw_md,
    set_sources,
    set_updated,
    split_frontmatter,
)

TODAY = "2026-07-05"
BATCH = "#1321"
LABEL = "wujinsen Phase2 P1 BigData"

CREATE_PAGES: dict[str, dict] = {
    "bigdata/hadoop-生态入门": {
        "title": "Hadoop 生态入门",
        "type": "concept",
        "tags": "[Hadoop, HDFS, MapReduce, YARN, 大数据]",
        "prefixes": ["BigData/Hadoop", "大数据资料-王/hadoop"],
        "related": "[spark-核心概念与实践, hive-数仓与-sql, kafka-大数据管道]",
        "body": """# Hadoop 生态入门

> HDFS + YARN + MapReduce 构成经典离线大数据底座；上层 Hive/Spark 见同分类互链。

## 1. 组件

| 组件 | 作用 |
|------|------|
| **HDFS** | 分布式文件系统；NameNode 元数据 + DataNode 块存储 |
| **YARN** | 资源调度；ResourceManager / NodeManager |
| **MapReduce** | 批处理编程模型（现多被 Spark/Flink 取代） |

## 2. HDFS 要点

- 块默认 **128MB**（可配）；副本数默认 3
- 写：客户端 → NN 分配块 → DN pipeline 复制
- 读：就近 DN；NN 不参与数据传输
- **Secondary NN / HA**：元数据备份与主备切换

## 3. YARN 调度

ApplicationMaster 向 RM 申请 Container；队列容量/公平调度（Capacity/Fair Scheduler）。

## 4. 与 Hive/Spark

- **Hive**：SQL on HDFS，Metastore 存表结构
- **Spark**：内存计算，可读 HDFS/Hive 表

见 [[bigdata/spark-核心概念与实践]]、[[bigdata/hive-数仓与-sql]]。
""",
    },
    "bigdata/spark-核心概念与实践": {
        "title": "Spark 核心概念与实践",
        "type": "article",
        "tags": "[Spark, RDD, DataFrame, 大数据]",
        "prefixes": ["BigData/Spark", "BigData/spark(1)", "大数据资料-王/spark"],
        "related": "[hadoop-生态入门, hive-数仓与-sql, flink-流批一体入门]",
        "body": """# Spark 核心概念与实践

## 1. 架构

Driver + Executors；DAGScheduler 切 Stage，TaskScheduler 发 Task。比 MapReduce **内存迭代**快。

## 2. RDD / DataFrame / Dataset

| API | 特点 |
|-----|------|
| **RDD** | 弹性分布式数据集；血缘 lineage 容错 |
| **DataFrame** | 结构化；Catalyst 优化 |
| **Dataset** | 类型安全 DataFrame（Scala/Java） |

## 3. 宽窄依赖与 Stage

宽依赖（shuffle）划新 Stage；常见 shuffle：groupBy、join、repartition。

## 4. 调优备忘

- `spark.default.parallelism` 与分区数
- 广播大变量避免 shuffle
- 序列化 Kryo；避免 UDF 装箱

## 5. 与 Flink

Spark 偏 **批+微批**；低延迟流见 [[bigdata/flink-流批一体入门]]。
""",
    },
    "bigdata/flink-流批一体入门": {
        "title": "Flink 流批一体入门",
        "type": "article",
        "tags": "[Flink, 流计算, 实时数仓, 大数据]",
        "prefixes": ["BigData/Flink"],
        "related": "[kafka-大数据管道, spark-核心概念与实践, olap-与-实时数仓]",
        "body": """# Flink 流批一体入门

## 1. 定位

**事件驱动**流引擎；同一套 DataStream API 可跑批（有界流）。延迟毫秒~秒级。

## 2. 核心概念

| 概念 | 说明 |
|------|------|
| **Event Time** | 事件自带时间戳 |
| **Watermark** | 衡量事件时间进度，触发窗口 |
| **Checkpoint** | 分布式快照容错；Barrier 对齐 |
| **State** | Keyed State / Operator State |

## 3. 窗口

滚动 Tumbling、滑动 Sliding、会话 Session；与 Kafka 源见 [[bigdata/kafka-大数据管道]]。

## 4. 与 Spark Streaming

Flink **原生流**；Spark Structured Streaming 微批模型。实时数仓分层见 Phase2 `olap-与-实时数仓`（#1323）。
""",
    },
    "bigdata/hive-数仓与-sql": {
        "title": "Hive 数仓与 SQL",
        "type": "guide",
        "tags": "[Hive, 数仓, SQL, 大数据]",
        "prefixes": ["BigData/Hive", "大数据资料-王/hive"],
        "related": "[hadoop-生态入门, 数仓分层与建模, spark-核心概念与实践]",
        "body": """# Hive 数仓与 SQL

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
""",
    },
    "bigdata/kafka-大数据管道": {
        "title": "Kafka 大数据管道",
        "type": "concept",
        "tags": "[Kafka, 日志, 管道, 大数据]",
        "prefixes": ["BigData/Kafka", "大数据资料-王/kafka"],
        "related": "[kafka-与-mq选型, flink-流批一体入门, elk-日志分析栈]",
        "body": """# Kafka 大数据管道

> 业务 MQ 选型与面试题见 [[middleware/kafka-与-mq选型]]；本文侧重 **日志/埋点/大数据管道**。

## 1. 架构

Topic 分区 + 副本；Producer → Broker → Consumer Group。Zookeeper/KRaft 存元数据。

## 2. 管道场景

- 日志采集 → Kafka → Flink/Spark → Hive/OLAP
- 埋点 → Kafka → 实时大屏 / 离线数仓

## 3. 可靠性

| 环节 | 配置 |
|------|------|
| Producer | `acks=all`，幂等 + 事务（EOS） |
| Broker | `min.insync.replicas`，禁止 unclean leader |
| Consumer | 处理完再 commit；幂等写入 |

## 4. 与 MQ 面试页

延迟、顺序、事务对比见 [[middleware/kafka-与-mq选型]]。
""",
    },
    "bigdata/hbase-列式存储入门": {
        "title": "HBase 列式存储入门",
        "type": "concept",
        "tags": "[HBase, NoSQL, 大数据]",
        "prefixes": ["大数据资料-王/hbase"],
        "related": "[hadoop-生态入门, hive-数仓与-sql]",
        "body": """# HBase 列式存储入门

## 1. 模型

列族 Column Family；RowKey 字典序；稀疏宽表。基于 HDFS，Region 水平切分。

## 2. 读写

- 写：WAL + MemStore flush 成 HFile
- 读：BlockCache + BloomFilter 减少 IO

## 3. RowKey 设计

避免热点（单调递增前缀）；散列/反转/预分区。

## 4. 与 Hive

Hive 离线分析；HBase 低延迟点查/列存。可 Hive 外部表映射 HBase。
""",
    },
    "bigdata/flume-与-数据采集": {
        "title": "Flume 与数据采集",
        "type": "guide",
        "tags": "[Flume, 采集, 大数据]",
        "prefixes": ["BigData/Flume", "大数据资料-王/flume"],
        "related": "[kafka-大数据管道, 数据采集与-etl-工具选型]",
        "body": """# Flume 与数据采集

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
""",
    },
}

P1321_ENRICH_MULTI: dict[str, list[str]] = {
    "BigData/ElasticSearch": [
        "search/elasticsearch-搜索",
        "search/elasticsearch-面试题",
        "search/es-match与bool查询",
    ],
    "BigData/Zookeeper": [
        "middleware/zookeeper-与协调服务",
        "middleware/zookeeper-面试题",
    ],
}

NOTES: dict[str, str] = {
    "search/elasticsearch-搜索": "合并 `BigData/ElasticSearch/` 教程 raw。",
    "search/elasticsearch-面试题": "合并 BigData ES 面试向 raw。",
    "middleware/zookeeper-与协调服务": "合并 BigData/王 ZK 原理 raw。",
    "middleware/kafka-与-mq选型": "与 [[bigdata/kafka-大数据管道]] 互链；BigData Kafka raw 挂 bigdata 页。",
}

KAFKA_EXTRA = """
## 与大数据管道页

日志/埋点管道架构见 [[bigdata/kafka-大数据管道]]（批次 #1321）。
"""


def collect_sources(prefixes: list[str]) -> list[str]:
    out: list[str] = []
    for p in prefixes:
        out.extend(list_raw_md(p))
    return sorted(set(out))


def write_create_page(slug: str, spec: dict) -> None:
    cat, stem = slug.split("/", 1)
    path = WIKI / cat / f"{stem}.md"
    if path.exists():
        print("EXISTS", slug)
        return
    sources = collect_sources(spec["prefixes"])
    src_yaml = "\n".join(f" - {s}" for s in sources)
    related = spec["related"]
    text = f"""---
title: {spec["title"]}
slug: {stem}
type: {spec["type"]}
status: active
tags: {spec["tags"]}
sources:
{src_yaml}
related: {related}
created: {TODAY}
updated: {TODAY}
---

{spec["body"].strip()}

## 批次{BATCH} 增补（{LABEL}）

本页由 Phase 2 #1321 从 wujinsen `BigData/` 与 `大数据资料-王/` 合并创建；sources **{len(sources)}** 篇。
"""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
    print("CREATE", slug, "sources", len(sources))


def link_kafka_pages() -> None:
    slug = "middleware/kafka-与-mq选型"
    path = WIKI / "middleware" / "kafka-与-mq选型.md"
    if not path.exists():
        return
    text = path.read_text(encoding="utf-8")
    fm, body = split_frontmatter(text)
    if KAFKA_EXTRA.strip() in body:
        return
    fm = set_updated(fm, TODAY)
    body = body.rstrip() + KAFKA_EXTRA + "\n"
    path.write_text(fm + body, encoding="utf-8")
    print("OK link", slug)


def main() -> None:
    (WIKI / "bigdata").mkdir(parents=True, exist_ok=True)

    created: list[str] = []
    for slug, spec in CREATE_PAGES.items():
        write_create_page(slug, spec)
        if slug_to_path_exists(slug):
            created.append(slug)

    slug_sources = build_slug_sources({}, P1321_ENRICH_MULTI)
    touched = apply_enrich_batch(slug_sources, TODAY, BATCH, LABEL, NOTES)
    link_kafka_pages()

    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen Phase2 P1 → create {len(created)} 页 "
        f"({', '.join(created[:4])}{'…' if len(created) > 4 else ''}) + enrich {len(touched)} 页",
    )
    print("Created", len(created), "enriched", len(touched))


def slug_to_path_exists(slug: str) -> bool:
    cat, stem = slug.split("/", 1)
    return (WIKI / cat / f"{stem}.md").exists()


if __name__ == "__main__":
    main()
