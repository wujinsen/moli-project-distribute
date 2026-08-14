#!/usr/bin/env python3
"""Phase 3 batch #1330: delete empty raw + enrich sources + skip manifest."""
from __future__ import annotations

import os
import re
from collections import defaultdict
from pathlib import Path

from gen_phase3_wujinsen_plan import (
    INSTALL_KEYWORDS,
    RAW,
    classify,
    load_cited,
    norm,
)
from wujinsen_ingest_lib import (
    WIKI,
    append_batch,
    append_log,
    apply_enrich_batch,
    enrich_wiki_page,
    get_sources,
    set_sources,
    set_updated,
    split_frontmatter,
)

TODAY = "2026-07-05"
BATCH = "#1330"
LABEL = "wujinsen Phase3 收口"
MANIFEST = Path(__file__).resolve().parent / "WUJINSEN_SKIP_MANIFEST.md"

BIGDATA_NEARBY: list[tuple[str, str]] = [
    (r"(?i)ambari|cloudera|hadoop|hdfs|yarn|mapreduce|griffin", "bigdata/hadoop-生态入门"),
    (r"(?i)hudi|kylin|olap|presto|clickhouse|doris", "bigdata/olap-与-实时数仓"),
    (r"(?i)mongo", "database/mongodb与文档库选型"),
    (r"(?i)pulsar|kafka", "bigdata/kafka-大数据管道"),
    (r"(?i)spark", "bigdata/spark-核心概念与实践"),
    (r"(?i)flink", "bigdata/flink-流批一体入门"),
    (r"(?i)hive", "bigdata/hive-数仓与-sql"),
    (r"(?i)elastic|solr|lucene", "search/elasticsearch-搜索"),
    (r"(?i)docker|k8s|kubernetes", "ops/k8s入门与容器编排"),
]

PHASE1_NEARBY: list[tuple[str, str]] = [
    (r"(?i)mysql|sql|索引|innodb", "database/mysql-索引面试题"),
    (r"(?i)redis|缓存", "cache/redis-面试题"),
    (r"(?i)jvm|gc|内存", "java/jvm-面试题"),
    (r"(?i)spring", "spring/spring-容器面试题"),
    (r"(?i)dubbo|rpc", "middleware/dubbo-调用原理与分层"),
    (r"(?i)kafka|mq|消息", "middleware/kafka-与-mq选型"),
    (r"(?i)算法|海量|数据处理|秒杀", "patterns/算法面试题精选"),
    (r"(?i)docker|k8s|jenkins|nginx|linux", "ops/linux-运维基础"),
]

SOURCE_CODE_MAP: list[tuple[str, str]] = [
    ("dubbo", "middleware/dubbo-调用原理与分层"),
    ("feign", "middleware/openfeign-与-http客户端"),
    ("rocketmq", "middleware/rocketmq-事务消息实践"),
    ("kafka", "middleware/kafka-与-mq选型"),
    ("mycat", "database/sharding-分库分表入门"),
    ("nacos", "middleware/nacos-注册与配置"),
    ("clickhouse", "bigdata/olap-与-实时数仓"),
    ("openjdk", "java/jvm-内存与gc"),
    ("spring", "spring/spring-ioc与bean生命周期"),
]

NOTES: dict[str, str] = {
    "java/java-并发面试题": "Phase3：王树 x线程 簇 sources。",
    "middleware/dubbo-调用原理与分层": "Phase3：RPC/webservice raw。",
    "bigdata/hadoop-生态入门": "Phase3：BigData 零散 Ambari/Cloudera 等。",
    "patterns/算法面试题精选": "Phase3：海量数据处理面试向 raw。",
}


def resolve_slugs(rel: str, target: str) -> list[str]:
    if target == "bigdata/* 就近":
        for pat, slug in BIGDATA_NEARBY:
            if re.search(pat, rel):
                return [slug]
        return ["bigdata/hadoop-生态入门"]
    if target == "邻近 Phase1 slug":
        for pat, slug in PHASE1_NEARBY:
            if re.search(pat, rel):
                return [slug]
        return ["patterns/算法面试题精选"]
    if target == "见 Phase2 源码映射":
        low = rel.lower()
        for key, slug in SOURCE_CODE_MAP:
            if key in low:
                return [slug]
        return ["middleware/dubbo-调用原理与分层"]
    return [s.strip() for s in target.split("|") if s.strip() and s != "—"]


def prune_deleted_sources(deleted: set[str]) -> int:
    """Remove deleted raw paths from wiki sources if present."""
    n = 0
    deleted_full = {f"raw/wujinsen_markdown/{d}" for d in deleted}
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        text = p.read_text(encoding="utf-8")
        if "---" not in text:
            continue
        fm, body = split_frontmatter(text)
        old = get_sources(fm)
        merged = [s for s in old if s not in deleted_full]
        if len(merged) == len(old):
            continue
        fm = set_sources(fm, merged)
        fm = set_updated(fm, TODAY)
        p.write_text(fm + body, encoding="utf-8")
        n += 1
        print("PRUNE sources", p.relative_to(WIKI))
    return n


def write_skip_manifest(skipped: list[tuple[str, str]]) -> None:
    by_cluster: dict[str, list[str]] = defaultdict(list)
    for rel, note in skipped:
        top = rel.split("/")[0]
        by_cluster[top].append(rel)
    lines = [
        "# wujinsen_markdown · Skip Manifest（Phase 3 定案）",
        "",
        f"> 生成：{TODAY} · 批次 {BATCH} · **不再 ingest** 至 enterprise-kb",
        f"> 共 **{len(skipped)}** 篇 raw 路径（逻辑 skip；raw 文件仍保留 unless 另行删除）",
        "",
        "## Prefix 级 skip（见 gen_phase3_wujinsen_plan.py SKIP_PREFIXES）",
        "",
        "AI · QA · 产品 · Oracle · 区块链 · Git · SAML · loadrunner · selenium 等。",
        "",
        "## 本篇级 skip 样例（按 top-level）",
        "",
    ]
    for top in sorted(by_cluster, key=lambda k: -len(by_cluster[k])):
        rels = by_cluster[top]
        lines.append(f"### `{top}/`（{len(rels)}）")
        for r in rels[:5]:
            lines.append(f"- `{r}`")
        if len(rels) > 5:
            lines.append(f"- … +{len(rels) - 5}")
        lines.append("")
    MANIFEST.write_text("\n".join(lines), encoding="utf-8")
    print("Wrote", MANIFEST.name)


def main() -> None:
    cited = load_cited()
    raw_all = sorted(norm(os.path.relpath(str(f), str(RAW))) for f in RAW.rglob("*.md"))
    uncited = [r for r in raw_all if r not in cited]

    deleted: set[str] = set()
    skipped: list[tuple[str, str]] = []
    slug_sources: dict[str, set[str]] = defaultdict(set)

    for rel in uncited:
        info = classify(rel)
        action = info["action"]
        src = f"raw/wujinsen_markdown/{rel}"
        if action == "delete":
            path = RAW / rel.replace("/", os.sep)
            if path.is_file():
                path.unlink()
                deleted.add(rel)
                print("DELETE", rel)
            else:
                print("MISSING delete", rel)
        elif action == "enrich":
            for slug in resolve_slugs(rel, info["target"]):
                slug_sources[slug].add(src)
        else:
            skipped.append((rel, info.get("note", "")))

    pruned = prune_deleted_sources(deleted)

    touched = apply_enrich_batch(dict(slug_sources), TODAY, BATCH, LABEL, NOTES)

    # One batch note on primary pages
    note = (
        f"Phase 3 收口：补挂 **{sum(len(v) for v in slug_sources.values())}** 条 sources；"
        f"删除 raw **{len(deleted)}**；skip 定案 **{len(skipped)}**。"
    )
    if touched:
        path = WIKI / "java" / "java-并发面试题.md"
        if path.exists():
            text = path.read_text(encoding="utf-8")
            fm, body = split_frontmatter(text)
            body = append_batch(body, BATCH, LABEL, note)
            path.write_text(fm + body, encoding="utf-8")

    write_skip_manifest(skipped)

    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen Phase3 → delete {len(deleted)} + enrich {len(touched)} 页 "
        f"+ skip manifest {len(skipped)} + prune sources {pruned}",
    )

    cited2 = load_cited()
    print("Done delete", len(deleted), "enrich pages", len(touched), "skip", len(skipped))
    print("cited", len(cited), "->", len(cited2), "raw left", len(list(RAW.rglob('*.md'))))


if __name__ == "__main__":
    main()
