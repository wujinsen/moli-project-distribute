#!/usr/bin/env python3
"""Generate enterprise-kb directory migration CSV draft (方案 B)."""
from __future__ import annotations

import csv
import re
from collections import Counter
from pathlib import Path

WIKI = Path(__file__).resolve().parent.parent / "wiki"
OUT = Path(__file__).resolve().parent / "enterprise_kb_migration_draft.csv"

CATEGORIES = {
    "database": "数据库",
    "cache": "缓存与 Redis",
    "java": "Java 与 JVM",
    "middleware": "微服务与中间件",
    "spring": "Spring 生态",
    "search": "搜索与 ES",
    "security": "网络与安全",
    "frontend": "前端",
    "ops": "运维与 Linux",
    "patterns": "设计模式",
    "uncategorized": "未分类",
}

RULES: list[tuple[str, str]] = [
    ("search", r"elasticsearch|\bes\b|es-|ik分词|索引与写入流程"),
    (
        "cache",
        r"redis|缓存|cache-aside|cache aside|双写|延迟队列|看门狗|redisson|分布式锁",
    ),
    (
        "database",
        r"mysql|mongodb|flyway|innodb|b\+tree|b-plus|事务与锁|mvcc|死锁|索引|"
        r"druid|mybatis|持久层|雪花算法|深分页|慢sql|复合索引|覆盖索引|回表|隔离级别",
    ),
    (
        "spring",
        r"^spring|spring-|springboot|spring boot|springcache|spring cache|"
        r"spring-async|async与线程池|容器面试|spring-事务",
    ),
    (
        "middleware",
        r"dubbo|nacos|kafka|rabbitmq|rocketmq|sentinel|gateway|feign|openfeign|"
        r"zookeeper|zk-|netty|bio-nio|reactor|rpc|限流|熔断|令牌桶|接口幂等|"
        r"seckill|秒杀|mq选型|注册与配置|动态配置|pipeline|编解码|webclient|"
        r"resttemplate|okhttp|http客户端|sse|服务端推送|跨域|前后端分离|"
        r"loadtest|压测|bi报表|openapi|swagger|api-版本|接口文档|微服务|消息队列|"
        r"分布式事务|分布式理论|分布式id|seata|cap|base|一致性理论",
    ),
    (
        "security",
        r"shiro|rbac|认证|会话|bcrypt|密码|csrf|xss|https|tls|"
        r"api-接口安全|api-安全|授权|鉴权|字段级加密|加密存储|登录|sso|门户|sys_system",
    ),
    ("frontend", r"前端|vue|react|javascript|css|html|webpack|vite|node\.?js|浏览器"),
    (
        "ops",
        r"linux|nginx|运维|网络-端口|连通性|arthas|cpu-100|oom|排查|"
        r"gc调优|启动优化|jvm-gc|jvm-oom|监控|日志|k8s|docker|容量规划|"
        r"jenkins|ci|devops|蓝绿|滚动发布|部署",
    ),
    ("patterns", r"设计模式|单例|工厂|观察者|代理模式|spring框架中的设计模式"),
    (
        "java",
        r"jvm|java-|juc|concurrenthashmap|synchronized|并发|happens-before|jmm|"
        r"集合|completablefuture|bio-nio|aio|selector|io模型|classloader|"
        r"类加载|泛型|反射|stream|lambda|多线程|threadlocal|volatile|aqs|"
        r"锁原理|内存与gc|垃圾收集|垃圾回收|tomcat|servlet容器|hashmap",
    ),
]

STEM_OVERRIDES: dict[str, str] = {
    "分布式锁": "cache",
    "接口幂等性实践": "middleware",
    "限流算法与令牌桶": "middleware",
    "容量规划与水平扩展": "ops",
    "消息队列": "middleware",
    "openfeign-与-http客户端": "middleware",
    "mybatis-与-druid持久层": "database",
    "mybatis-plus-用法与注入防护": "database",
    "tomcat与-servlet容器": "java",
    "认证与会话机制": "security",
    "rbac-权限模型": "security",
    "设计模式": "patterns",
    "spring框架中的设计模式": "patterns",
    "bio-nio-aio对比": "java",
    "网络-端口与连通性排查": "ops",
    "跨域与前后端分离": "middleware",
    "前端技术栈": "frontend",
    "前端基础面试题": "frontend",
    "mongodb与文档库选型": "database",
    "b-plus树与-innodb索引结构": "database",
    "mysql-索引": "database",
    "mysql-事务与锁": "database",
    "elasticsearch-搜索": "search",
    "jenkins-ci入门": "ops",
    "分布式事务": "middleware",
    "分布式理论基础": "middleware",
    "蓝绿与滚动发布": "ops",
    "sso与系统门户": "security",
    "hashmap-面试题": "java",
    "分布式id面试题": "middleware",
    "分布式理论面试题": "middleware",
}

# Topic hubs: align sibling pages by shared mysql-/redis-/spring- prefix
TOPIC_PREFIX_TO_DIR: list[tuple[str, str]] = [
    ("mysql-", "database"),
    ("redis", "cache"),
    ("spring-", "spring"),
    ("spring", "spring"),
    ("dubbo", "middleware"),
    ("nacos", "middleware"),
    ("kafka", "middleware"),
    ("rocketmq", "middleware"),
    ("rabbitmq", "middleware"),
    ("sentinel", "middleware"),
    ("gateway", "middleware"),
    ("feign", "middleware"),
    ("openfeign", "middleware"),
    ("zookeeper", "middleware"),
    ("netty", "middleware"),
    ("elasticsearch", "search"),
    ("es-", "search"),
    ("shiro", "security"),
    ("jvm", "java"),
    ("java-", "java"),
    ("juc", "java"),
    ("concurrenthashmap", "java"),
    ("synchronized", "java"),
    ("completablefuture", "java"),
    ("设计模式", "patterns"),
    ("前端", "frontend"),
]


def parse_frontmatter(text: str) -> tuple[dict, str]:
    if not text.startswith("---"):
        return {}, text
    parts = text.split("---", 2)
    if len(parts) < 3:
        return {}, text
    meta: dict = {}
    for line in parts[1].splitlines():
        if ":" not in line:
            continue
        key, val = line.split(":", 1)
        key = key.strip()
        val = val.strip()
        if key == "tags" and val.startswith("["):
            meta[key] = re.findall(r"[\w\u4e00-\u9fff\-\+]+", val)
        else:
            meta[key] = val.strip("\"'")
    return meta, parts[2]


def classify(stem: str, title: str, tags: list[str], old_dir: str) -> tuple[str, str]:
    if stem in STEM_OVERRIDES:
        return STEM_OVERRIDES[stem], "override:stem"

    blob = " ".join([stem, title, " ".join(tags or [])]).lower()
    scores = {k: 0 for k in CATEGORIES}
    matched_rules: list[str] = []
    for cat, pat in RULES:
        if re.search(pat, blob, re.I):
            scores[cat] += 2
            matched_rules.append(cat)

    for prefix, cat in TOPIC_PREFIX_TO_DIR:
        if stem.lower().startswith(prefix.lower()):
            scores[cat] += 4

    best_score = max(scores.values())
    if best_score == 0:
        return "uncategorized", "fallback:none"

    priority = [
        "database",
        "cache",
        "spring",
        "middleware",
        "search",
        "security",
        "java",
        "ops",
        "frontend",
        "patterns",
        "uncategorized",
    ]
    tied = [c for c, s in scores.items() if s == best_score]
    for p in priority:
        if p in tied:
            reason = f"rule:{','.join(matched_rules[:3])}" if matched_rules else "rule:prefix"
            return p, reason
    return tied[0], "rule:score"


def align_topic_families(rows: list[dict]) -> None:
    """If a concept hub stem is X, push articles/interviews with same topic prefix."""
    hub_dirs: dict[str, str] = {}
    for r in rows:
        if r["kb_type"] == "concept":
            hub_dirs[r["stem"]] = r["new_dir_slug"]

    for r in rows:
        stem = r["stem"]
        for hub_stem, hub_dir in hub_dirs.items():
            if stem == hub_stem:
                continue
            if stem.startswith(hub_stem.split("-")[0] + "-") or hub_stem in stem:
                if r["confidence"] == "low" or r["new_dir_slug"] != hub_dir:
                    # only align when hub shares mysql-/redis- style prefix
                    prefix = hub_stem.split("-")[0]
                    if stem.startswith(prefix + "-") or stem.startswith(prefix):
                        r["new_dir_slug"] = hub_dir
                        r["new_category_name"] = CATEGORIES[hub_dir]
                        r["new_slug"] = f"{hub_dir}/{stem}"
                        r["assign_reason"] = f"align:hub:{hub_stem}"
                        r["confidence"] = "medium"


def main() -> None:
    rows: list[dict] = []
    type_by_old_dir = {
        "concepts": "concept",
        "articles": "article",
        "interview": "interview",
    }

    for old_dir in ("concepts", "articles", "interview"):
        d = WIKI / old_dir
        if not d.exists():
            continue
        for path in sorted(d.glob("*.md")):
            text = path.read_text(encoding="utf-8")
            meta, _ = parse_frontmatter(text)
            stem = path.stem
            title = str(meta.get("title") or stem)
            kb_type = str(meta.get("type") or type_by_old_dir.get(old_dir, "article"))
            tags = meta.get("tags") or []
            if isinstance(tags, str):
                tags = [tags]

            old_slug = f"{old_dir}/{stem}"
            new_dir, reason = classify(stem, title, tags, old_dir)
            confidence = "low" if reason == "fallback:none" else "high"
            if reason.startswith("align:"):
                confidence = "medium"

            rows.append(
                {
                    "stem": stem,
                    "old_dir": old_dir,
                    "old_slug": old_slug,
                    "new_dir_slug": new_dir,
                    "new_category_name": CATEGORIES[new_dir],
                    "new_slug": f"{new_dir}/{stem}",
                    "kb_type": kb_type,
                    "title": title,
                    "tags": "|".join(tags) if tags else "",
                    "confidence": confidence,
                    "assign_reason": reason,
                    "review_note": "",
                }
            )

    align_topic_families(rows)

    # refresh new_slug after align
    for r in rows:
        r["new_slug"] = f"{r['new_dir_slug']}/{r['stem']}"
        r["new_category_name"] = CATEGORIES[r["new_dir_slug"]]

    fieldnames = [
        "old_dir",
        "old_slug",
        "new_dir_slug",
        "new_category_name",
        "new_slug",
        "kb_type",
        "title",
        "tags",
        "confidence",
        "assign_reason",
        "review_note",
    ]
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)

    counts = Counter(r["new_dir_slug"] for r in rows)
    print(f"Total: {len(rows)}")
    for k, v in counts.most_common():
        print(f"  {k} ({CATEGORIES[k]}): {v}")
    low = sum(1 for r in rows if r["confidence"] == "low")
    print(f"Low confidence (need review): {low}")
    print(f"Wrote: {OUT}")


if __name__ == "__main__":
    main()
