#!/usr/bin/env python3
"""Phase 2 batch #1324: Maven create + 插件/性能优化 + a安装文档挂接."""
from __future__ import annotations

import re
from pathlib import Path

from wujinsen_ingest_lib import (
    WIKI,
    append_log,
    apply_enrich_batch,
    build_slug_sources,
    list_raw_md,
)

TODAY = "2026-07-05"
BATCH = "#1324"
LABEL = "wujinsen Phase2 长尾"

P1324_SINGLE: dict[str, str] = {
    "插件": "ops/maven-多模块与依赖管理",
    "插件/maven": "ops/maven-多模块与依赖管理",
    "插件/PageHelper": "database/mybatis-与-druid持久层",
    "插件/swagger": "middleware/接口幂等性实践",
    "性能优化/DATABASE": "database/mysql-索引",
    "性能优化": "java/jvm-gc调优实战",
    "源码分析/MyCat": "database/sharding-分库分表入门",
}

NOTES: dict[str, str] = {
    "ops/maven-多模块与依赖管理": "合并 `插件/maven/` 与 javaweb Maven Scope raw。",
    "database/mybatis-与-druid持久层": "合并 PageHelper 插件 raw。",
    "database/sharding-分库分表入门": "合并 MyCat 源码阅读 raw。",
    "database/mysql-索引": "合并性能优化 DATABASE raw。",
    "java/jvm-gc调优实战": "合并性能优化 raw。",
}

MAVEN_BODY = """---
title: Maven 多模块与依赖管理
slug: maven-多模块与依赖管理
type: guide
status: active
tags: [Maven, 构建, 依赖, 运维]
sources:
{sources}
related: [jenkins-ci入门, mybatis-与-druid持久层, java-编码规范与CodeReview要点]
created: {today}
updated: {today}
---

# Maven 多模块与依赖管理

## 1. 多模块结构

```
parent (pom packaging)
├── common
├── api
└── server
```

父 POM 统一 `dependencyManagement` 与插件版本；子模块继承。

## 2. Scope 含义（raw 摘要）

| Scope | 说明 |
|-------|------|
| compile | 默认；编译+运行+测试 |
| provided | 容器提供，如 servlet-api |
| runtime | 运行需要，如 JDBC 驱动 |
| test | 仅测试 |

## 3. 依赖冲突

`mvn dependency:tree` 查传递依赖；`<exclusions>` 排除；`<dependencyManagement>` 统一版本。

## 4. 与 CI

打包发布见 [[ops/jenkins-ci入门]]；持久层见 [[database/mybatis-与-druid持久层]]。
"""

# keyword in raw path -> wiki slugs (a安装文档 bulk hook-up)
AINSTALL_RULES: list[tuple[str, list[str]]] = [
    (r"(?i)redis", ["cache/redis-集群与哨兵实践", "cache/redis-面试题"]),
    (r"(?i)mysql", ["database/mysql-索引", "database/mysql-索引面试题"]),
    (r"(?i)hadoop|hdfs|yarn", ["bigdata/hadoop-生态入门"]),
    (r"(?i)hive", ["bigdata/hive-数仓与-sql"]),
    (r"(?i)spark", ["bigdata/spark-核心概念与实践"]),
    (r"(?i)kafka", ["bigdata/kafka-大数据管道"]),
    (r"(?i)flume", ["bigdata/flume-与-数据采集"]),
    (r"(?i)zookeeper|zk", ["middleware/zookeeper-与协调服务"]),
    (r"(?i)elasticsearch|elastic", ["search/elasticsearch-搜索"]),
    (r"(?i)nginx", ["ops/nginx-反向代理与负载", "middleware/nginx-限流与缓冲调优"]),
    (r"(?i)flink", ["bigdata/flink-流批一体入门"]),
    (r"(?i)hbase", ["bigdata/hbase-列式存储入门"]),
    (r"(?i)storm", ["bigdata/flink-流批一体入门"]),
]


def find_ainstall_prefix() -> str:
    from wujinsen_ingest_lib import RAW

    for d in (RAW / "大数据资料-王").iterdir():
        if d.is_dir() and "安装" in d.name:
            return f"大数据资料-王/{d.name}"
    return "大数据资料-王/a安装文档"


def build_ainstall_sources() -> dict[str, set[str]]:
    prefix = find_ainstall_prefix()
    mapping: dict[str, set[str]] = {}
    for src in list_raw_md(prefix):
        rel = src.split("wujinsen_markdown/", 1)[-1]
        for pattern, slugs in AINSTALL_RULES:
            if re.search(pattern, rel):
                for slug in slugs:
                    mapping.setdefault(slug, set()).add(src)
                break
    return mapping


def create_maven_page() -> None:
    path = WIKI / "ops" / "maven-多模块与依赖管理.md"
    if path.exists():
        return
    sources = sorted(
        set(list_raw_md("插件/maven"))
        | set(list_raw_md("javaweb/Maven"))
        | set(list_raw_md("插件"))
    )
    src_yaml = "\n".join(f" - {s}" for s in sources)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        MAVEN_BODY.format(sources=src_yaml, today=TODAY),
        encoding="utf-8",
    )
    print("CREATE ops/maven-多模块与依赖管理", len(sources))


def main() -> None:
    create_maven_page()

    slug_sources = build_slug_sources(P1324_SINGLE)
    ainstall = build_ainstall_sources()
    for slug, srcs in ainstall.items():
        slug_sources.setdefault(slug, set()).update(srcs)

    touched = apply_enrich_batch(slug_sources, TODAY, BATCH, LABEL, NOTES)

    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen Phase2 长尾 → create ops/maven-多模块与依赖管理 + enrich {len(touched)} 页（含 a安装文档关键词挂接）",
    )
    print("Touched", len(touched), "ainstall slugs", len(ainstall))


if __name__ == "__main__":
    main()
