#!/usr/bin/env python3
"""P0 batch #1310: enrich enterprise-kb wiki from wujinsen 面试笔试+架构+DataBase."""
from __future__ import annotations

import os
import re
from datetime import date
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
TODAY = "2026-07-05"
BATCH = "#1310"

# raw path prefix (under wujinsen_markdown) -> wiki slug (category/stem)
P0_SOURCES: dict[str, str] = {
    "DataBase/Redis": "cache/redis-面试题",
    "DataBase/mysql": "database/mysql-索引面试题",
    "架构/DevOps": "ops/jenkins-ci入门",
    "架构/容器/Docker": "ops/容器与-docker",
    "架构/容器/k8s": "ops/k8s入门与容器编排",
    "面试笔试/Dubbo": "middleware/dubbo-调用原理与分层",
    "面试笔试/Java": "java/java-并发面试题",
    "架构/中间件": "middleware/消息队列",
    "架构/分布式事务": "middleware/分布式事务",
    "架构/安全框架": "security/shiro-鉴权体系",
    "架构/MicroServer/SpringCloud/Hystrix": "middleware/sentinel-限流与熔断",
}

# multi-target: raw prefix -> list of wiki slugs
P0_MULTI: dict[str, list[str]] = {
    "面试笔试/面试小结/面试小结之并发篇": ["java/java-并发面试题"],
    "面试笔试/面试小结/面试小结之IO篇": ["middleware/netty-与-io面试题"],
    "面试笔试/面试小结/面试小结之Elasticsearch篇": ["search/elasticsearch-面试题"],
    "面试笔试/面试小结/分布式锁原理及实现方式": [
        "cache/分布式锁面试题",
        "cache/分布式锁",
    ],
    "面试笔试/面试题整理/java CPU 100% 排查": ["java/java-cpu-100排查实战"],
    "面试笔试/面试题整理/复合索引的优点和注意事项": ["database/mysql-复合索引与最左前缀"],
    "面试笔试/面试题整理/MySQL数据库MyISAM和InnoDB存储引擎的比较": ["database/mysql-索引面试题"],
    "面试笔试/面试题整理/JVM群面试题": ["java/jvm-面试题"],
    "面试笔试/面试题整理/Java面试通关要点汇总集核心篇": ["java/java-并发面试题"],
    "面试笔试/面试题整理/Java后台面试 常见问题": ["java/java-并发面试题"],
    "面试笔试/Database": ["database/mysql-索引面试题"],
}

# entire folders mapped to multiple pages
P0_FOLDER_MULTI: dict[str, list[str]] = {
    "面试笔试/面试题整理": [
        "database/mysql-索引面试题",
        "database/mysql-复合索引与最左前缀",
        "java/jvm-面试题",
        "java/java-cpu-100排查实战",
        "java/java-并发面试题",
    ],
    "架构/MicroServer/SpringCloud/SpringCloudGateWay": ["spring/spring-cloud-gateway"],
    "架构/MicroServer/SpringCloud/采坑记录": ["middleware/feign-开发踩坑"],
    "架构/MicroServer/SpringCloud/sentinel": ["middleware/sentinel-限流与熔断"],
    "架构/MicroServer/Dubbo": ["middleware/dubbo-调用原理与分层"],
}


def norm(p: str) -> str:
    return p.replace("\\", "/")


def list_raw_md(prefix: str) -> list[str]:
    root = RAW / prefix.replace("/", os.sep)
    if not root.exists():
        return []
    out: list[str] = []
    if root.is_file() and str(root).endswith(".md"):
        rel = norm(os.path.relpath(root, RAW))
        return [f"raw/wujinsen_markdown/{rel}"]
    for dp, _, fns in os.walk(root):
        for fn in fns:
            if not fn.endswith(".md"):
                continue
            if "同步发生冲突" in fn:
                continue
            if fn == "dfsdfa.note.md":
                continue
            rel = norm(os.path.relpath(os.path.join(dp, fn), RAW))
            out.append(f"raw/wujinsen_markdown/{rel}")
    return sorted(out)


def slug_to_path(slug: str) -> Path:
    cat, stem = slug.split("/", 1)
    return WIKI / cat / f"{stem}.md"


def parse_frontmatter(text: str) -> tuple[str, str, str]:
    m = re.match(r"^(---\n.*?\n---\n)([\s\S]*)$", text, re.S)
    if not m:
        raise ValueError("no frontmatter")
    return m.group(1), m.group(2), text


def get_sources(fm: str) -> list[str]:
    block = re.search(r"^sources:\n((?:[ \t]+-[^\n]+\n?)*)", fm, re.M)
    if not block:
        return []
    return [ln.strip()[2:].strip() for ln in block.group(1).splitlines() if ln.strip().startswith("-")]


def set_sources(fm: str, sources: list[str]) -> str:
    lines = ["sources:"] + [f" - {s}" for s in sources]
    if re.search(r"^sources:\n", fm, re.M):
        return re.sub(r"^sources:\n(?:[ \t]+-[^\n]+\n?)*", "\n".join(lines) + "\n", fm, count=1, flags=re.M)
    return fm.replace("---\n", "---\n" + "\n".join(lines) + "\n", 1)


def set_updated(fm: str) -> str:
    if re.search(r"^updated:", fm, re.M):
        return re.sub(r"^updated:.*$", f"updated: {TODAY}", fm, flags=re.M)
    return fm.replace("---\n", f"---\nupdated: {TODAY}\n", 1)


def append_batch_section(body: str, slug: str, note: str) -> str:
    marker = f"## 批次{BATCH} 增补（wujinsen P0）"
    if marker in body:
        return body
    section = f"\n{marker}\n\n{note}\n"
    return body.rstrip() + section + "\n"


def build_slug_sources() -> dict[str, set[str]]:
    mapping: dict[str, set[str]] = {}

    def add(slug: str, sources: list[str]) -> None:
        mapping.setdefault(slug, set()).update(sources)

    for prefix, slug in P0_SOURCES.items():
        add(slug, list_raw_md(prefix))

    for prefix, slugs in P0_MULTI.items():
        for slug in slugs:
            add(slug, list_raw_md(prefix))

    for prefix, slugs in P0_FOLDER_MULTI.items():
        srcs = list_raw_md(prefix)
        for slug in slugs:
            add(slug, srcs)

    return mapping


ENRICH_NOTES: dict[str, str] = {
    "cache/redis-面试题": (
        "本批合并 `DataBase/Redis/` 下 **Redis 夺命16问**、16 场景、线程模型、Codis 架构等 raw。"
        "新增 Q8（过期策略）、Q11（主从/哨兵）、Q12（Cluster 槽位）见上节；细节链 [[cache/redis-持久化与高可用]]、"
        "[[cache/redis-集群与哨兵实践]]。"
    ),
    "database/mysql-索引面试题": (
        "合并 `DataBase/mysql/` 索引子目录 + 面试笔试 Database/树/B+树 raw。"
        "索引命中规则、900W 优化案例、ROW_FORMAT 见 [[database/mysql-索引失效场景]]。"
    ),
    "java/java-并发面试题": (
        "合并 `面试笔试/Java/` 并发包（volatile/CHM/多线程）及面试小结并发篇、"
        "Java面试题精选 67-70 期 raw sources。"
    ),
    "middleware/dubbo-调用原理与分层": (
        "合并 Dubbo 剖析（集群容错/负载均衡/线程模型）与精尽 Dubbo 面试题 raw。"
    ),
    "ops/jenkins-ci入门": (
        "合并 `架构/DevOps/` Jenkins 安装、Pipeline、自动部署 jar 等 raw。"
    ),
    "ops/容器与-docker": "合并 Docker 安装/命令/挂载/Java 部署 raw。",
    "ops/k8s入门与容器编排": "合并 K8s 学习笔记、故障锦囊、Kuboard raw。",
    "middleware/消息队列": "合并 `架构/中间件/` MQ 选型与对比 raw。",
    "middleware/分布式事务": "合并 Seata/TCC/RocketMQ 事务消息完整示例 raw。",
    "security/shiro-鉴权体系": (
        "合并 `架构/安全框架/` Shiro/Spring Security 配置类 raw（通用概念，非茉莉手册）。"
    ),
    "middleware/sentinel-限流与熔断": (
        "合并 Hystrix 限流/降级/队列术与 Sentinel 滑动窗口 raw，作历史对照。"
    ),
}


def enrich_body(slug: str, body: str) -> str:
    """Add substantive Q&A blocks for key interview pages."""
    if slug == "cache/redis-面试题":
        extra = """
## Q8. 过期键删除策略？

惰性删除（访问时检查）+ 定期抽样删除。内存满时按 maxmemory-policy（volatile-lru/allkeys-lru 等）淘汰。

## Q11. 主从复制原理？

全量 RDB + 增量 repl_backlog；从库只读。见 [[cache/redis-集群与哨兵实践]]。

## Q12. Cluster 如何分片？

16384 slot；MOVED/ASK 重定向；客户端/smart 路由。多 master 水平扩展。
"""
        if "## Q8. 过期键" not in body:
            body = body.rstrip() + extra + "\n"
    elif slug == "database/mysql-索引面试题":
        extra = """
## Q11. InnoDB 行格式 ROW_FORMAT 影响什么？

COMPACT/DYNAMIC 影响溢出列存储与索引记录大小；大 VARCHAR 可能 off-page 存储，影响二级索引叶子大小。

## Q12. 如何判断索引是否被使用？

`EXPLAIN` 的 `key`/`rows`/`Extra`；`SHOW INDEX FROM t` 看 Cardinality；慢日志 + `pt-query-digest` 验证。
"""
        if "## Q11. InnoDB 行格式" not in body:
            body = body.rstrip() + extra + "\n"
    elif slug == "java/java-并发面试题":
        extra = """
## Q10. run() 与 start() 区别？

`start()` 新建线程并进入 RUNNABLE 执行 `run()`；直接调 `run()` 只是普通方法调用，不启动新线程。

## Q11. 如何优雅停止线程？

协作式：volatile 标志 + 中断 `interrupt()`；线程池用 `shutdown()`/`awaitTermination()`，避免 `stop()`。
"""
        if "## Q10. run()" not in body:
            body = body.rstrip() + extra + "\n"
    elif slug == "middleware/dubbo-调用原理与分层":
        extra = """
## Dubbo 剖析补充（raw 架构/面试笔试）

| 主题 | 要点 |
|------|------|
| **集群容错** | Failover（默认重试）、Failfast、Failsafe、Failback、Forking、Broadcast |
| **负载均衡** | Random、RoundRobin、LeastActive、ConsistentHash |
| **线程模型** | Netty boss/worker；Provider 业务线程池与 `threads`/`queues` 配置 |

见 [[middleware/dubbo-面试题]]。
"""
        if "## Dubbo 剖析补充" not in body:
            body = body.rstrip() + extra + "\n"
    return body


def main() -> None:
    slug_sources = build_slug_sources()
    touched: list[str] = []

    for slug, new_sources in sorted(slug_sources.items()):
        path = slug_to_path(slug)
        if not path.exists():
            print("SKIP missing wiki:", slug)
            continue
        text = path.read_text(encoding="utf-8")
        fm_end = text.index("---", 3)
        fm = text[: fm_end + 4]
        body = text[fm_end + 4 :]

        old_sources = get_sources(fm)
        merged = sorted(set(old_sources) | new_sources)
        if merged == old_sources and slug not in ENRICH_NOTES:
            continue

        fm = set_sources(fm, merged)
        fm = set_updated(fm)
        body = enrich_body(slug, body)
        if slug in ENRICH_NOTES:
            body = append_batch_section(body, slug, ENRICH_NOTES[slug])
        path.write_text(fm + body, encoding="utf-8")
        touched.append(slug)
        print(f"OK {slug}: sources {len(old_sources)} -> {len(merged)}")

    log_path = WIKI / "log.md"
    if log_path.exists():
        line = f"\n## [{TODAY}] ingest | 批次{BATCH} wujinsen P0 → enrich {len(touched)} 页 ({', '.join(touched[:5])}{'…' if len(touched)>5 else ''})\n"
        content = log_path.read_text(encoding="utf-8")
        if BATCH not in content:
            log_path.write_text(content.rstrip() + line, encoding="utf-8")

    print("Touched", len(touched), "pages")


if __name__ == "__main__":
    main()
