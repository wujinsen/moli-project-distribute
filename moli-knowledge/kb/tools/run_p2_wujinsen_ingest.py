#!/usr/bin/env python3
"""P2 batch #1313: enrich enterprise-kb wiki from wujinsen P2 plan rows."""
from __future__ import annotations

import os
import re
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
TODAY = "2026-07-05"
BATCH = "#1313"

P2_MAP: dict[str, str] = {
    "DataBase/Redis/Jedis": "cache/redis-数据结构与使用场景",
    "DataBase/Redis/Jedis_tedis_redisson": "cache/redis-数据结构与使用场景",
    "架构/MicroServer/SpringCloud/sentinel": "middleware/sentinel-限流与熔断",
    "面试笔试/Spring": "spring/spring-事务",
    "面试笔试/redis": "cache/redis分布式锁实现",
    "面试笔试/框架/zookeeper": "middleware/zookeeper-面试题",
    "面试笔试/精尽面试题/JVM": "java/jvm-面试题",
    "面试笔试/精尽面试题/dubbo": "middleware/dubbo-面试题",
}

# mysql 无 事务/锁 子目录，按文件名挂到对应 slug
P2_MYSQL_FILES: dict[str, list[str]] = {
    "database/mysql-事务面试题": [
        "DataBase/mysql/正确的理解MySQL的MVCC及实现原理.note.md",
        "DataBase/mysql/数据库事务的四大特性以及事务的隔离级别.note.md",
        "DataBase/mysql/事务隔离级别中的可重复读能防幻读吗.note.md",
    ],
    "database/mysql-innodb锁机制": [
        "DataBase/mysql/全局锁和表锁 ：给表加个字段怎么有这么多阻碍？.note.md",
        "DataBase/mysql/MySQL死锁案例，我一口气说了6个.note.md",
    ],
}

ENRICH_NOTES: dict[str, str] = {
    "cache/redis-数据结构与使用场景": "合并 Jedis/Redisson 选型 raw。",
    "database/mysql-事务面试题": "补充 MVCC 原理 raw（`DataBase/mysql/` 根目录）。",
    "database/mysql-innodb锁机制": "补充表锁/全局锁 raw。",
    "middleware/sentinel-限流与熔断": "补充 Sentinel 动态规则源 raw。",
    "spring/spring-事务": "确认 `面试笔试/Spring/` 五篇 raw 已挂接。",
    "cache/redis分布式锁实现": "合并 `面试笔试/redis/` raw。",
    "middleware/zookeeper-面试题": "合并精尽 ZK 面试题 raw。",
    "java/jvm-面试题": "合并精尽 JVM 面试题 raw。",
    "middleware/dubbo-面试题": "合并精尽 Dubbo 面试题 raw。",
}


def norm(p: str) -> str:
    return p.replace("\\", "/")


def list_raw_md(prefix: str) -> list[str]:
    root = RAW / prefix.replace("/", os.sep)
    if not root.exists():
        return []
    out: list[str] = []
    for dp, _, fns in os.walk(root):
        for fn in fns:
            if not fn.endswith(".md"):
                continue
            if "同步发生冲突" in fn or fn == "dfsdfa.note.md":
                continue
            rel = norm(os.path.relpath(os.path.join(dp, fn), RAW))
            out.append(f"raw/wujinsen_markdown/{rel}")
    return sorted(out)


def slug_to_path(slug: str) -> Path:
    cat, stem = slug.split("/", 1)
    return WIKI / cat / f"{stem}.md"


def get_sources(fm: str) -> list[str]:
    block = re.search(r"^sources:\n((?:[ \t]+-[^\n]+\n?)*)", fm, re.M)
    if not block:
        return []
    return [ln.strip()[2:].strip() for ln in block.group(1).splitlines() if ln.strip().startswith("-")]


def set_sources(fm: str, sources: list[str]) -> str:
    lines = ["sources:"] + [f" - {s}" for s in sources]
    return re.sub(
        r"^sources:\n(?:[ \t]+-[^\n]+\n?)*",
        "\n".join(lines) + "\n",
        fm,
        count=1,
        flags=re.M,
    )


def set_updated(fm: str) -> str:
    if re.search(r"^updated:", fm, re.M):
        return re.sub(r"^updated:.*$", f"updated: {TODAY}", fm, flags=re.M)
    return fm


def append_batch(body: str, note: str) -> str:
    marker = f"## 批次{BATCH} 增补（wujinsen P2）"
    if marker in body:
        return body
    return body.rstrip() + f"\n\n{marker}\n\n{note}\n"


def enrich_body(slug: str, body: str) -> str:
    extras: dict[str, str] = {
        "cache/redis-数据结构与使用场景": """
## Jedis vs Redisson（raw）

| | Jedis | Redisson |
|---|-------|----------|
| 模型 | 轻量客户端 | 封装分布式对象/锁 |
| 分布式锁 | 需自写 Lua | `RLock` + 看门狗 [[cache/redisson-看门狗与分布式锁]] |
| 连接池 | `JedisPool`；高版本配置项更名需注意 | 开箱即用 |

选型：简单 KV 用 Jedis/Lettuce；锁/队列/对象语义用 Redisson。
""",
        "database/mysql-事务面试题": """
## Q8. MVCC 简述（raw）

InnoDB 通过 **undo log 版本链** + **Read View** 实现可重复读；快照读不加锁，当前读用 next-key lock。见 [[database/mysql-隔离级别与mvcc]]。
""",
        "spring/spring-事务": """
## @Transactional 失效场景速查（raw 汇总）

1. **非 public** 方法
2. **同类自调用**（绕过代理）
3. **异常被吞**或未配置 `rollbackFor=Exception.class`
4. **传播行为**误用（如 `NOT_SUPPORTED`）
5. **数据库引擎**非 InnoDB

原理页 [[spring/spring-声明式事务]]。
""",
        "middleware/sentinel-限流与熔断": """
## Sentinel 动态规则（raw）

- 规则可推送到 **Nacos/Apollo** 等数据源，OAP 热更新
- `SentinelRuleManager.loadRules` 与控制台联动
- 与 Hystrix 对比：Sentinel 滑动窗口更轻、控制台统一 [[middleware/sentinel-接入与规则配置]]
""",
        "middleware/zookeeper-面试题": """
## ZK 面试速记补充

- **ZAB** 协议：崩溃恢复 + 消息广播
- **临时节点**：Session 断开自动删，做服务发现
- **watch**：一次性触发，需重新注册
- **脑裂**：过半写成功原则

见 [[middleware/zookeeper-与协调服务]]。
""",
        "middleware/dubbo-面试题": """
## Dubbo 面试补充

- **SPI** 与 Adaptive 扩展点
- **集群容错**：Failover / Failfast 等
- 注册中心挂掉：Consumer 本地缓存 Provider 列表仍可调用（短期）

原理 [[middleware/dubbo-调用原理与分层]]。
""",
    }
    extra = extras.get(slug, "")
    if extra and extra.strip() not in body:
        body = body.rstrip() + extra + "\n"
    return body


def main() -> None:
    slug_sources: dict[str, set[str]] = {}
    for prefix, slug in P2_MAP.items():
        slug_sources.setdefault(slug, set()).update(list_raw_md(prefix))
    for slug, rel_paths in P2_MYSQL_FILES.items():
        for rel in rel_paths:
            p = RAW / rel.replace("/", os.sep)
            if p.is_file():
                slug_sources.setdefault(slug, set()).add(f"raw/wujinsen_markdown/{norm(rel)}")

    touched: list[str] = []
    for slug in sorted(slug_sources):
        path = slug_to_path(slug)
        if not path.exists():
            print("SKIP missing:", slug)
            continue
        text = path.read_text(encoding="utf-8")
        fm_end = text.index("---", 3)
        fm, body = text[: fm_end + 4], text[fm_end + 4 :]
        old = get_sources(fm)
        merged = sorted(set(old) | slug_sources[slug])
        fm = set_sources(fm, merged)
        fm = set_updated(fm)
        body = enrich_body(slug, body)
        if slug in ENRICH_NOTES:
            body = append_batch(body, ENRICH_NOTES[slug])
        path.write_text(fm + body, encoding="utf-8")
        touched.append(slug)
        print(f"OK {slug}: {len(old)} -> {len(merged)} sources")

    log_path = WIKI / "log.md"
    content = log_path.read_text(encoding="utf-8") if log_path.exists() else ""
    if BATCH not in content:
        line = (
            f"\n## [{TODAY}] ingest | 批次{BATCH} wujinsen P2 → enrich {len(touched)} 页 "
            f"({', '.join(touched[:5])}{'…' if len(touched) > 5 else ''})\n"
        )
        log_path.write_text(content.rstrip() + line, encoding="utf-8")
    print("Touched", len(touched))


if __name__ == "__main__":
    main()
