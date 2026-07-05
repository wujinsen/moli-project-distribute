#!/usr/bin/env python3
"""Generate enrich/create plan for wujinsen 面试笔试 + 架构 + DataBase → enterprise-kb only."""
from __future__ import annotations

import os
import re
from collections import defaultdict
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW_ROOT = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
OUT = Path(__file__).resolve().parent / "WUJINSEN_INGEST_PLAN_面试架构DB.md"
PREFIXES = ("面试笔试", "架构", "DataBase")

THEME_PLAN: list[tuple[str, str, str | None, str, str | None, str | None]] = [
    ("DataBase/mysql", "database", "mysql-索引面试题", "enrich", "interview", "B+树/ROW_FORMAT/20道经典题等"),
    ("DataBase/mysql/事务", "database", "mysql-事务面试题", "enrich", "interview", "隔离/MVCC/幻读"),
    ("DataBase/mysql/索引", "database", "mysql-索引", "enrich", "concept", "索引原理"),
    ("DataBase/mysql/锁", "database", "mysql-innodb锁机制", "enrich", "article", "InnoDB 锁"),
    ("DataBase/Oracle", "database", None, "skip", None, "Oracle 非主栈"),
    ("DataBase/Redis", "cache", "redis-面试题", "enrich", "interview", "Redis 夺命16问等"),
    ("DataBase/Redis/Jedis", "cache", "redis-数据结构与使用场景", "enrich", "concept", "Jedis/Redisson 选型"),
    ("DataBase/mongodb", "database", "mongodb与文档库选型", "enrich", "article", "文档库选型"),
    ("DataBase/mysql/分库分表", "database", "sharding-分库分表入门", "create", "article", "分库分表 raw 合并新建"),
    ("DataBase/canal", "database", "mysql-binlog与canal同步", "enrich", "article", "Canal 同步"),
    ("架构/MicroServer", "middleware", None, "enrich", "article", "见下表：Gateway/Sentinel/Feign 等多 slug"),
    ("架构/MicroServer/SpringCloud/SpringCloudGateWay", "spring", "spring-cloud-gateway", "enrich", "concept", "Gateway 原理/跨域"),
    ("架构/MicroServer/SpringCloud/sentinel", "middleware", "sentinel-限流与熔断", "enrich", "concept", "Sentinel 滑动窗口/动态规则"),
    ("架构/MicroServer/SpringCloud/采坑记录", "middleware", "feign-开发踩坑", "enrich", "article", "Feign 上传/配置踩坑"),
    ("架构/MicroServer/SpringCloud/Hystrix", "middleware", "sentinel-限流与熔断", "enrich", "article", "Hystrix 限流降级（历史对照 enrich）"),
    ("架构/分布式事务", "middleware", "分布式事务", "enrich", "concept", "Seata/TCC 等"),
    ("架构/Git", "ops", None, "skip", None, "Git 踩坑 → 已在 wiki-moli/git协作指南 有架构/Git sources；本批不进 enterprise 新建"),
    ("架构/安全框架", "security", "shiro-鉴权体系", "enrich", "concept", "安全框架选型/配置"),
    ("架构/安全", "security", "api-接口安全设计", "enrich", "article", "安全实践"),
    ("架构/中间件", "middleware", "消息队列", "enrich", "concept", "中间件总览 raw"),
    ("架构/区块链", "middleware", None, "skip", None, "区块链非主栈"),
    ("架构/SAML", "security", None, "skip", None, "SAML/SSO 旧方案，按需下批"),
    ("架构/开发工具", "ops", None, "skip", None, "IDE 踩坑，非 KB 核心"),
    ("面试笔试/2020面试题整理", "java", None, "skip", None, "与 面试题整理 重复倾向"),
    ("面试笔试/Java", "java", "java-并发面试题", "enrich", "interview", "Java 基础/并发包"),
    ("面试笔试/Java/JVM", "java", "jvm-面试题", "enrich", "interview", "JVM/GC"),
    ("面试笔试/Java面试题精选", "java", "java-并发面试题", "enrich", "interview", "【67-70期】系列"),
    ("面试笔试/高级java", "java", "hashmap-面试题", "enrich", "interview", "HashMap/高级面试"),
    ("面试笔试/精尽面试题/JVM", "java", "jvm-面试题", "enrich", "interview", "精尽 JVM"),
    ("面试笔试/精尽面试题/dubbo", "middleware", "dubbo-面试题", "enrich", "interview", None),
    ("面试笔试/Spring", "spring", "spring-事务", "enrich", "interview", "Spring 事务多篇"),
    ("面试笔试/Database", "database", "mysql-索引面试题", "enrich", "interview", "MySQL 20 道等"),
    ("面试笔试/树", "database", "b-plus树与-innodb索引结构", "enrich", "concept", "B/B+ 树"),
    ("面试笔试/kafka", "middleware", "kafka-与-mq选型", "enrich", "interview", "Kafka 面试+丢消息"),
    ("面试笔试/redis", "cache", "redis分布式锁实现", "enrich", "article", "Redis 分布式锁"),
    ("面试笔试/Dubbo", "middleware", "dubbo-调用原理与分层", "enrich", "article", "Dubbo 剖析"),
    ("面试笔试/ElasticSearch", "search", "elasticsearch-面试题", "enrich", "interview", "ES 小结"),
    ("面试笔试/分布式", "cache", "分布式锁面试题", "enrich", "interview", "分布式锁"),
    ("面试笔试/安全性", "security", "api-接口安全设计", "enrich", "article", "API 安全"),
    ("面试笔试/面试小结", "java", None, "enrich", "interview", "按主题拆 enrich，不建汇总页"),
    ("面试笔试/面试题整理", "java", None, "enrich", "interview", "按题 merge 到已有 interview"),
    ("面试笔试/2020程序员内推", "middleware", None, "skip", None, "内推/offer 营销"),
    ("面试笔试/面试公司", "middleware", None, "skip", None, "个人面试记录"),
    ("面试笔试/面试要求", "middleware", None, "skip", None, "JD 剪藏"),
    ("面试笔试/算法", "patterns", "算法面试题精选", "create", "interview", "动态规划等"),
    ("面试笔试/大数据", "middleware", None, "skip", None, "大数据岗真题"),
    ("面试笔试/框架/zookeeper", "middleware", "zookeeper-面试题", "enrich", "interview", None),
    ("架构/缓存", "cache", "cache-aside与缓存更新模式", "enrich", "article", "穿透/击穿/雪崩"),
    ("架构/高并发", "middleware", "高并发券系统实战", "create", "article", "通用券系统/QPS，不进 wiki-moli"),
    ("架构/消息队列/RocketMQ", "middleware", "rocketmq-架构与实战", "enrich", "article", "安装+实战"),
    ("架构/消息队列/RabbitMQ", "middleware", "rabbitmq-入门与使用场景", "enrich", "guide", "安装教程"),
    ("架构/消息队列/ActiveMQ", "middleware", None, "skip", None, "ActiveMQ 非主栈"),
    ("架构/服务注册发现/nacos", "middleware", "nacos-注册与配置", "enrich", "concept", "Nacos 架构"),
    ("架构/容器/Docker", "ops", "容器与-docker", "enrich", "guide", "Docker 安装/命令"),
    ("架构/容器/k8s", "ops", "k8s入门与容器编排", "enrich", "guide", "K8s 笔记"),
    ("架构/性能调优/JVM", "java", "jvm-gc调优实战", "enrich", "article", "GC/堆设置"),
    ("架构/性能调优/Arthas", "java", "arthas-在线诊断", "enrich", "guide", None),
    ("架构/性能监控/skywalking", "ops", "skywalking-安装与链路追踪", "create", "guide", "SkyWalking 安装"),
    ("架构/文件存储/minio", "middleware", "minio-对象存储实践", "create", "guide", "MinIO 安装/迁移"),
    ("架构/微服务认证", "security", "sso与系统门户", "enrich", "article", "Spring Cloud Security+CAS"),
    ("架构/运维", "ops", "linux-运维基础", "enrich", "guide", "组件/防火墙"),
    ("架构/DevOps", "ops", "jenkins-ci入门", "enrich", "guide", "CI/CD"),
    ("架构/腾讯云", "ops", None, "skip", None, "无标题空壳"),
    ("架构/项目踩坑", "middleware", "feign-开发踩坑", "enrich", "article", "版本兼容"),
    ("架构/编码规范", "java", "java-编码规范与CodeReview要点", "create", "guide", "Java 规范/CR"),
    ("架构/通信协议/Thrift", "middleware", None, "skip", None, "Thrift 非主栈"),
]


def norm(p: str) -> str:
    return p.replace("\\", "/")


def walk_raw(prefix: str) -> list[str]:
    root = RAW_ROOT / prefix
    out: list[str] = []
    if not root.is_dir():
        return out
    for dp, _, fns in os.walk(root):
        for fn in fns:
            if fn.endswith(".md"):
                out.append(norm(os.path.relpath(os.path.join(dp, fn), RAW_ROOT)))
    return out


def match_cluster(raw: str, cluster: str) -> bool:
    return raw == cluster or raw.startswith(cluster + "/")


def priority(row: dict) -> str:
    if row["action"] == "skip":
        return "skip"
    if row["action"] == "create":
        return "P1" if row["raw_count"] >= 3 else "P2"
    if row["uncovered_count"] >= 5 or row["raw_count"] >= 8:
        return "P0"
    if row["uncovered_count"] >= 1:
        return "P1"
    return "P2"


def main() -> None:
    raw_all: list[str] = []
    for p in PREFIXES:
        raw_all.extend(walk_raw(p))
    raw_all = sorted(set(raw_all))

    pages: dict[str, dict] = {}
    raw_to_wiki: dict[str, set[str]] = defaultdict(set)
    for dp, _, fns in os.walk(WIKI):
        for fn in fns:
            if not fn.endswith(".md") or fn in ("index.md", "log.md"):
                continue
            path = os.path.join(dp, fn)
            text = Path(path).read_text(encoding="utf-8")
            slug_dir = norm(os.path.relpath(dp, WIKI))
            slug = f"{slug_dir}/{fn[:-3]}" if slug_dir != "." else fn[:-3]
            m = re.search(r"^---\n(.*?)\n---", text, re.S)
            meta = m.group(1) if m else ""

            def g(key: str) -> str:
                mm = re.search(rf"^{key}:\s*(.+)$", meta, re.M)
                return mm.group(1).strip() if mm else ""

            sources = [
                norm(s)
                for s in re.findall(r"^\s*-\s*(raw/wujinsen_markdown/[^\n]+)", meta, re.M)
            ]
            pages[slug] = {"title": g("title") or slug, "type": g("type") or "?", "sources": sources}
            for s in sources:
                if s.startswith("raw/wujinsen_markdown/"):
                    rel = norm(s[len("raw/wujinsen_markdown/") :])
                    raw_to_wiki[rel].add(slug)

    covered = set(raw_to_wiki.keys())
    uncovered = [r for r in raw_all if r not in covered]

    rows: list[dict] = []
    assigned: set[str] = set()
    for cluster_prefix, cat, slug, action, kb_type, note in THEME_PLAN:
        matched = [r for r in raw_all if match_cluster(r, cluster_prefix)]
        for r in matched:
            assigned.add(r)
        linked = sorted({s for r in matched for s in raw_to_wiki.get(r, set())})
        rows.append(
            {
                "raw_cluster": cluster_prefix,
                "raw_count": len(matched),
                "uncovered_count": sum(1 for r in matched if r not in covered),
                "action": action,
                "space": "enterprise-kb",
                "category": cat,
                "target_slug": slug or "—",
                "kb_type": kb_type or "—",
                "linked_wiki": linked,
                "note": note or "",
                "priority": "",
            }
        )

    for row in rows:
        row["priority"] = priority(row)

    unassigned = [r for r in raw_all if r not in assigned]

    lines: list[str] = [
        "# wujinsen_markdown Ingest 规划 · 面试笔试 + 架构 + DataBase",
        "",
        "> **空间**：仅 `enterprise-kb`（`kb/wiki/{category}/`）· **禁止**写入 `wiki-moli/`",
        "> **策略**：默认 A（enrich 已有 slug）· 建议批次 `#1310`",
        f"> **统计**：raw `.md` **{len(raw_all)}** 篇 · 已被 wiki `sources` 引用 **{len(covered)}** · 待挂接 **{len(uncovered)}**",
        "",
        "## 执行顺序",
        "",
        "1. 按 P0 → P1 → P2 执行 enrich/create",
        "2. `python kb/tools/lint.py --strict`",
        "3. `python kb/tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb`",
        "4. Web 单空间浏览验证分类/体裁 facet",
        "",
        "## 规划表",
        "",
        "| 优先级 | 动作 | raw 簇 | raw≈ | 未挂接≈ | 分类 | 目标 slug | 体裁 | 已有 wiki | 说明 |",
        "|--------|------|--------|------|---------|------|-----------|------|-----------|------|",
    ]

    for row in sorted(rows, key=lambda r: (r["priority"] == "skip", r["priority"], r["raw_cluster"])):
        linked = ", ".join(f"`{s}`" for s in row["linked_wiki"][:2])
        if len(row["linked_wiki"]) > 2:
            linked += f" +{len(row['linked_wiki']) - 2}"
        if not linked:
            linked = "—"
        slug_display = row["target_slug"]
        if slug_display != "—":
            slug_display = f"{row['category']}/{slug_display}"
        lines.append(
            f"| {row['priority']} | **{row['action']}** | `{row['raw_cluster']}/` | {row['raw_count']} | "
            f"{row['uncovered_count']} | `{row['category']}` | `{slug_display}` | `{row['kb_type']}` | "
            f"{linked} | {row['note']} |"
        )

    creates = [r for r in rows if r["action"] == "create"]
    lines.extend(["", "## create 新建页（6）", ""])
    for c in creates:
        lines.append(
            f"- **`{c['category']}/{c['target_slug']}`** · `{c['kb_type']}` · "
            f"raw `{c['raw_cluster']}/`（{c['raw_count']} 篇）"
        )

    lines.extend(["", "## skip（不 ingest）", ""])
    for s in [r for r in rows if r["action"] == "skip"]:
        lines.append(f"- `{s['raw_cluster']}/`（{s['raw_count']} 篇）— {s['note']}")

    lines.extend(["", "## enrich · P0 优先", ""])
    for r in [x for x in rows if x["action"] == "enrich" and x["priority"] == "P0"]:
        slug = f"{r['category']}/{r['target_slug']}"
        lines.append(f"- `{slug}` ← `{r['raw_cluster']}/`（+{r['uncovered_count']} sources）")

    lines.extend(["", "## 未纳入主题的 raw（需人工或下批）", ""])
    if unassigned:
        by_u = defaultdict(list)
        for r in unassigned:
            parts = r.split("/")
            key = "/".join(parts[:2]) if len(parts) >= 2 else parts[0]
            by_u[key].append(r)
        for k, vs in sorted(by_u.items(), key=lambda x: -len(x[1]))[:15]:
            lines.append(f"- `{k}/` — {len(vs)} 篇（建议 skip 或并入邻近 enrich）")
    else:
        lines.append("- （无）")

    lines.extend(
        [
            "",
            "## 面试小结 / 面试题整理 → 多 slug enrich 映射",
            "",
            "| raw 子篇 | 目标 slug | 体裁 |",
            "|----------|-----------|------|",
            "| 面试小结之并发篇 | `java/java-并发面试题` | interview |",
            "| 面试小结之 IO 篇 | `middleware/netty-与-io面试题` | interview |",
            "| 面试小结之 Elasticsearch 篇 | `search/elasticsearch-面试题` | interview |",
            "| 面试小结之综合篇 | 按题拆到 database/cache/java | interview |",
            "| 面试题整理 · MySQL/索引 | `database/mysql-索引面试题` | interview |",
            "| 面试题整理 · JVM | `java/jvm-面试题` | interview |",
            "| 面试题整理 · CPU100% | `java/java-cpu-100排查实战` | article |",
            "",
            "## conflicts / 人工确认",
            "",
            "- **架构/高并发/优惠券**：落 `middleware/高并发券系统实战`（create），**不**写 `wiki-moli`",
            "- **Redis 双路径**：`DataBase/Redis` 与 `架构/缓存` 统一 enrich 到 `cache/*`",
            "- **冲突副本**：如 `Redis夺命16问(同步发生冲突)` — delete 或 skip 后再 ingest",
            "- **面试小结/面试题整理**：拆到各主题 interview 页，不建「汇总 output」",
        ]
    )

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT}")
    print(f"raw={len(raw_all)} covered={len(covered)} unassigned={len(unassigned)}")


if __name__ == "__main__":
    main()
