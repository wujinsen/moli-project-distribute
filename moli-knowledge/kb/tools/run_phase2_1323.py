#!/usr/bin/env python3
"""Phase 2 batch #1323: 数仓/OLAP/ELK/调度/ETL + 大数据面试题 + nginx create."""
from __future__ import annotations

from pathlib import Path

from wujinsen_ingest_lib import (
    WIKI,
    append_log,
    apply_enrich_batch,
    build_slug_sources,
    collect_sources,
    list_raw_md,
    list_raw_md_match,
)

TODAY = "2026-07-05"
BATCH = "#1323"
LABEL = "wujinsen Phase2 P2 数仓调度"

CREATE_PAGES: dict[str, dict] = {
    "bigdata/数仓分层与建模": {
        "title": "数仓分层与建模",
        "type": "concept",
        "tags": "[数仓, ODS, DWD, DWS, ADS, 大数据]",
        "prefixes": ["BigData/架构设计/Daas"],
        "needles": [],
        "related": "[hive-数仓与-sql, olap-与-实时数仓, 数据采集与-etl-工具选型]",
        "body": """# 数仓分层与建模

## 1. 经典分层

| 层 | 含义 | 典型内容 |
|----|------|----------|
| **ODS** | 操作数据层 | 贴源、轻度清洗 |
| **DIM** | 维度层 | 缓慢变化维 SCD |
| **DWD** | 明细层 | 业务过程事实、统一粒度 |
| **DWS** | 汇总层 | 主题宽表、指标聚合 |
| **ADS** | 应用层 | 报表/大屏/API |

## 2. 建模方法

- **维度建模**（Kimball）：星型/雪花；事实表 + 维度表
- **范式建模**（Inmon）：3NF 企业级 EDW，再派生集市

## 3. 规范（raw 摘要）

- 表命名：`dwd_业务_粒度_di/df`（di 增量 df 全量）
- 指标管理：原子/派生/复合指标；口径文档化
- 数据质量：完整性、一致性、及时性门禁

## 4. 与 Hive / OLAP

离线构建见 [[bigdata/hive-数仓与-sql]]；Serving 层 OLAP 见 [[bigdata/olap-与-实时数仓]]。
""",
    },
    "bigdata/olap-与-实时数仓": {
        "title": "OLAP 与实时数仓",
        "type": "article",
        "tags": "[OLAP, ClickHouse, Kylin, Presto, 实时数仓, 大数据]",
        "prefixes": ["BigData/OLAP", "BigData/数据仓库", "源码分析/clickhouse"],
        "needles": [],
        "related": "[数仓分层与建模, flink-流批一体入门, kafka-大数据管道]",
        "body": """# OLAP 与实时数仓

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
""",
    },
    "bigdata/elk-日志分析栈": {
        "title": "ELK 日志分析栈",
        "type": "guide",
        "tags": "[ELK, Elasticsearch, Logstash, Kibana, 大数据]",
        "prefixes": ["BigData/ELK", "BigData/FileBeat"],
        "needles": [],
        "related": "[elasticsearch-搜索, kafka-大数据管道, flume-与-数据采集]",
        "body": """# ELK 日志分析栈

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
""",
    },
    "bigdata/dolphinscheduler-任务调度": {
        "title": "DolphinScheduler 任务调度",
        "type": "guide",
        "tags": "[DolphinScheduler, 调度, 大数据]",
        "prefixes": ["BigData/DolphinScheduler", "BigData/架构设计/任务调度平台架构设计"],
        "needles": [],
        "related": "[数据采集与-etl-工具选型, jenkins-ci入门, hive-数仓与-sql]",
        "body": """# DolphinScheduler 任务调度

## 1. 定位

分布式 **DAG 工作流**调度；可视化编排 Hive/Spark/Sql/Shell 等任务。

## 2. 核心概念

- **Project / Process**：项目与工作流
- **Task**：Shell、SQL、Spark、SubProcess 等
- **依赖**：上下游；失败策略、重试、告警

## 3. 与 Jenkins

Jenkins 偏 **CI/CD** 构建发布；DS 偏 **数据管道** 日批依赖。见 [[ops/jenkins-ci入门]]。

## 4. 选型（raw）

对比 Azkaban/Oozie/Airflow：DS 中文社区、多租户、资源中心较完善。
""",
    },
    "bigdata/数据采集与-etl-工具选型": {
        "title": "数据采集与 ETL 工具选型",
        "type": "article",
        "tags": "[DataX, Sqoop, Flume, ETL, 大数据]",
        "prefixes": [
            "BigData/数据采集",
            "BigData/Sqoop",
            "BigData/技术选型",
            "大数据资料-王/sqoop",
        ],
        "needles": [],
        "related": "[flume-与-数据采集, mysql-binlog与canal同步, kafka-大数据管道]",
        "body": """# 数据采集与 ETL 工具选型

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
""",
    },
    "bigdata/hadoop-面试题": {
        "title": "Hadoop 面试题",
        "type": "interview",
        "tags": "[Hadoop, HDFS, YARN, 面试题, 大数据]",
        "prefixes": ["BigData/Hadoop", "大数据资料-王/hadoop"],
        "needles": ["面试", "面试题"],
        "related": "[hadoop-生态入门, spark-面试题, hive-数仓与-sql]",
        "body": """# Hadoop 面试题

> 概念综述见 [[bigdata/hadoop-生态入门]]。

## Q1. HDFS 读写流程？

**写**：Client → NN 申请块 → DN pipeline 三副本复制 → 确认。**读**：Client → NN 元数据 → 就近 DN 读块。

## Q2. Secondary NameNode 作用？

**不是**热备 NN；定期合并 fsimage+edits，辅助 NN 启动恢复。HA 用 QJM + ZKFC。

## Q3. YARN 调度流程？

Client 提交 → RM 分配 AM Container → AM 向 RM 申请 Task Container → NM 启动 Task。

## Q4. 小文件问题？

NN 内存压力；合并小文件；SequenceFile/Parquet；Har 归档。

## Q5. MapReduce shuffle？

Map 分区排序 spill → 拷贝 → Reduce merge → reduce 聚合。
""",
    },
    "bigdata/spark-面试题": {
        "title": "Spark 面试题",
        "type": "interview",
        "tags": "[Spark, 面试题, 大数据]",
        "prefixes": ["BigData/Spark", "BigData/spark(1)", "大数据资料-王/spark"],
        "needles": ["面试", "面试题"],
        "related": "[spark-核心概念与实践, hadoop-面试题, flink-面试题]",
        "body": """# Spark 面试题

> 实践见 [[bigdata/spark-核心概念与实践]]。

## Q1. Spark 为什么比 MapReduce 快？

内存迭代、DAG 优化、减少落盘；Stage 内 pipeline。

## Q2. RDD 五大属性？

分区列表、计算函数、依赖、分区器（可选）、首选位置。

## Q3. cache 与 persist？

`cache`=MEMORY_ONLY；持久化级别 MEMORY/DISK/序列化/副本。

## Q4. 宽窄依赖？

窄：一对一；宽：shuffle，划 Stage 边界。

## Q5. 数据倾斜怎么处理？

加盐 key、两阶段聚合、广播 join、AQE 倾斜 join。
""",
    },
    "bigdata/flink-面试题": {
        "title": "Flink 面试题",
        "type": "interview",
        "tags": "[Flink, 实时数仓, 面试题, 大数据]",
        "prefixes": ["BigData/Flink", "BigData/数据仓库/实时数仓"],
        "needles": [],
        "related": "[flink-流批一体入门, kafka-大数据管道, olap-与-实时数仓]",
        "body": """# Flink 面试题

> 入门见 [[bigdata/flink-流批一体入门]]。

## Q1. Event Time / Processing Time？

Event Time 用数据自带时间；Watermark 推进窗口；Processing Time 用系统时钟。

## Q2. Checkpoint 原理？

Barrier 对齐（或 unaligned）；状态快照异步；失败从 CK 恢复。

## Q3. Exactly-once？

Source 可重放 + 状态 CK + Sink 幂等/两阶段提交。

## Q4. 反压怎么处理？

定位瓶颈算子；并行度、资源、异步 IO、mini-batch。

## Q5. Flink vs Spark Streaming？

Flink 原生流；Spark SS 微批；低延迟选 Flink。
""",
    },
    "ops/nginx-反向代理与负载": {
        "title": "Nginx 反向代理与负载",
        "type": "guide",
        "tags": "[Nginx, LVS, Keepalived, 负载均衡, 运维]",
        "prefixes": ["大数据资料-王/nginx+ka+lvs"],
        "needles": [],
        "related": "[nginx-限流与缓冲调优, linux-运维基础, 跨域与前后端分离]",
        "body": """# Nginx 反向代理与负载

## 1. 反向代理

```nginx
upstream backend {
    server 127.0.0.1:8081;
    server 127.0.0.1:8082;
}
server {
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 2. 负载策略

`round_robin`（默认）、`ip_hash`、`least_conn`；健康检查配合 `max_fails`。

## 3. LVS + Keepalived（raw 摘要）

四层 DR/TUN/NAT 模式；VIP 漂移；Nginx 七层在其上。限流调参见 [[middleware/nginx-限流与缓冲调优]]。

## 4. 与网关

Spring Cloud Gateway 见 [[spring/spring-cloud-gateway]]；静态资源与 SPA 反代见 [[frontend/前端技术栈]]。
""",
    },
}

P1323_ENRICH_MULTI: dict[str, list[str]] = {
    "BigData/Storm": ["bigdata/flink-流批一体入门"],
    "大数据资料-王/storm": ["bigdata/flink-流批一体入门"],
}

NOTES: dict[str, str] = {
    "bigdata/flink-流批一体入门": "合并 Storm raw 作历史对照；Storm 不单独建页。",
}

STORM_EXTRA = """
## Storm 历史对照（raw #1323）

Apache Storm 为 **纯流** 早期方案（Spout/Bolt、acker 机制）。维护活跃度低，新项目优先 **Flink** 或 Spark Structured Streaming。面试可答：Storm at-least-once + ack；与 Flink Checkpoint 精确一次对比。
"""


def write_create_page(slug: str, spec: dict) -> None:
    cat, stem = slug.split("/", 1)
    path = WIKI / cat / f"{stem}.md"
    if path.exists():
        print("EXISTS", slug)
        return
    needles = spec.get("needles")
    if needles:
        sources = collect_sources(spec["prefixes"], *needles)
        if not sources and spec.get("type") != "interview":
            sources = collect_sources(spec["prefixes"])
    else:
        sources = collect_sources(spec["prefixes"])
    src_yaml = "\n".join(f" - {s}" for s in sources)
    text = f"""---
title: {spec["title"]}
slug: {stem}
type: {spec["type"]}
status: active
tags: {spec["tags"]}
sources:
{src_yaml}
related: {spec["related"]}
created: {TODAY}
updated: {TODAY}
---

{spec["body"].strip()}

## 批次{BATCH} 增补（{LABEL}）

本页 Phase 2 #1323 创建；sources **{len(sources)}** 篇。
"""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
    print("CREATE", slug, "sources", len(sources))


def main() -> None:
    created: list[str] = []
    for slug, spec in CREATE_PAGES.items():
        cat, stem = slug.split("/", 1)
        path = WIKI / cat / f"{stem}.md"
        existed = path.exists()
        write_create_page(slug, spec)
        if path.exists() and not existed:
            created.append(slug)

    slug_sources = build_slug_sources({}, P1323_ENRICH_MULTI)
    touched = apply_enrich_batch(
        slug_sources, TODAY, BATCH, LABEL, NOTES, {"bigdata/flink-流批一体入门": STORM_EXTRA}
    )

    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen Phase2 P2 → create {len(created)} 页 "
        f"({', '.join(created[:5])}{'…' if len(created) > 5 else ''}) + enrich {len(touched)} 页",
    )
    print("Created", len(created), "enriched", len(touched))


if __name__ == "__main__":
    main()
