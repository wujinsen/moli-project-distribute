#!/usr/bin/env python3
"""Phase 2 batch #1322: 大数据资料-王 ↔ existing wiki dedupe hook-up."""
from __future__ import annotations

from wujinsen_ingest_lib import (
    append_log,
    apply_enrich_batch,
    build_slug_sources,
)

TODAY = "2026-07-05"
BATCH = "#1322"
LABEL = "wujinsen Phase2 王树挂接"

P1322_MULTI: dict[str, list[str]] = {
    "大数据资料-王/redis": [
        "cache/redis-集群与哨兵实践",
        "cache/redis-面试题",
        "cache/redis-数据结构与使用场景",
    ],
    "大数据资料-王/mysql": [
        "database/mysql-索引",
        "database/mysql-索引面试题",
    ],
    "大数据资料-王/netty": [
        "middleware/netty-reactor与线程模型",
        "middleware/netty-pipeline与编解码",
    ],
    "大数据资料-王/nio": ["java/bio-nio-aio对比", "middleware/io模型与-netty"],
    "大数据资料-王/linux": ["ops/linux-运维基础"],
    "大数据资料-王/jvm": [
        "java/jvm-面试题",
        "java/jvm-内存与gc",
        "java/jvm-gc调优实战",
    ],
    "大数据资料-王/nginx+ka+lvs": [
        "middleware/nginx-限流与缓冲调优",
    ],
}

NOTES: dict[str, str] = {
    "cache/redis-集群与哨兵实践": "合并 `大数据资料-王/redis/` 与 a安装文档 Redis 集群 raw。",
    "database/mysql-索引面试题": "合并 `大数据资料-王/mysql/` 安装与调优 raw。",
    "ops/linux-运维基础": "合并 `大数据资料-王/linux/` 与 `Linux/` 双树。",
    "java/jvm-面试题": "合并 `大数据资料-王/jvm/` 双树。",
}


def main() -> None:
    slug_sources = build_slug_sources({}, P1322_MULTI)
    touched = apply_enrich_batch(slug_sources, TODAY, BATCH, LABEL, NOTES)
    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen Phase2 → enrich {len(touched)} 页（王树挂接）",
    )
    print("Touched", len(touched))


if __name__ == "__main__":
    main()
