#!/usr/bin/env python3
"""P1 batch #1312: enrich enterprise-kb wiki from wujinsen P1 plan rows."""
from __future__ import annotations

import os
import re
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
TODAY = "2026-07-05"
BATCH = "#1312"

# prefix -> slug (merge sources into slug)
P1_MAP: dict[str, str] = {
    "DataBase/canal": "database/mysql-binlog与canal同步",
    "DataBase/mongodb": "database/mongodb与文档库选型",
    "DataBase/mysql/索引": "database/mysql-索引",
    "架构/MicroServer/SpringCloud/SpringCloudGateWay": "spring/spring-cloud-gateway",
    "架构/MicroServer/SpringCloud/采坑记录": "middleware/feign-开发踩坑",
    "架构/项目踩坑": "middleware/feign-开发踩坑",
    "架构/安全": "security/api-接口安全设计",
    "架构/微服务认证": "security/sso与系统门户",
    "架构/性能调优/Arthas": "java/arthas-在线诊断",
    "架构/性能调优/JVM": "java/jvm-gc调优实战",
    "架构/服务注册发现/nacos": "middleware/nacos-注册与配置",
    "架构/消息队列/RabbitMQ": "middleware/rabbitmq-入门与使用场景",
    "架构/消息队列/RocketMQ": "middleware/rocketmq-架构与实战",
    "架构/缓存": "cache/cache-aside与缓存更新模式",
    "架构/运维": "ops/linux-运维基础",
    "面试笔试/Database": "database/mysql-索引面试题",
    "面试笔试/ElasticSearch": "search/elasticsearch-面试题",
    "面试笔试/Java/JVM": "java/jvm-面试题",
    "面试笔试/Java面试题精选": "java/java-并发面试题",
    "面试笔试/kafka": "middleware/kafka-与-mq选型",
    "面试笔试/分布式": "cache/分布式锁面试题",
    "面试笔试/安全性": "security/api-接口安全设计",
    "面试笔试/树": "database/b-plus树与-innodb索引结构",
    "面试笔试/高级java": "java/hashmap-面试题",
}

ENRICH_NOTES: dict[str, str] = {
    "database/mysql-binlog与canal同步": "合并 `DataBase/canal/` Canal 同步 raw。",
    "database/mongodb与文档库选型": "合并 `DataBase/mongodb/` 选型 raw。",
    "database/mysql-索引": "合并 `DataBase/mysql/索引/` 七篇索引原理 raw。",
    "spring/spring-cloud-gateway": "合并 Gateway 原理/跨域 raw。",
    "middleware/feign-开发踩坑": "合并 Feign 采坑记录 + 项目版本兼容 raw。",
    "security/api-接口安全设计": "合并 `架构/安全/` + 面试笔试安全性 raw。",
    "security/sso与系统门户": "合并 Spring Cloud Security + CAS raw。",
    "java/arthas-在线诊断": "合并 Arthas 安装使用 raw。",
    "java/jvm-gc调优实战": "合并 GC 调优/堆设置 raw。",
    "middleware/nacos-注册与配置": "合并 Nacos 架构与踩坑 raw。",
    "middleware/rabbitmq-入门与使用场景": "合并 RabbitMQ 安装/私信队列 raw。",
    "middleware/rocketmq-架构与实战": "合并 RocketMQ 安装/事务/命令 raw。",
    "cache/cache-aside与缓存更新模式": "合并缓存穿透/击穿/雪崩 raw。",
    "ops/linux-运维基础": "合并架构运维组件/防火墙 raw。",
    "search/elasticsearch-面试题": "合并 ES 面试小结 raw。",
    "middleware/kafka-与-mq选型": "合并 Kafka 面试与丢消息处理 raw。",
    "database/b-plus树与-innodb索引结构": "合并 B/B+ 树面试 raw。",
    "java/hashmap-面试题": "合并高级 Java/HashMap raw。",
    "cache/分布式锁面试题": "合并分布式锁实现 raw。",
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
    return re.sub(r"^updated:.*$", f"updated: {TODAY}", fm, flags=re.M)


def append_batch(body: str, note: str) -> str:
    marker = f"## 批次{BATCH} 增补（wujinsen P1）"
    if marker in body:
        return body
    return body.rstrip() + f"\n\n{marker}\n\n{note}\n"


def enrich_body(slug: str, body: str) -> str:
    extras: dict[str, str] = {
        "cache/cache-aside与缓存更新模式": """
## 穿透 / 击穿 / 雪崩（raw 架构/缓存）

| 问题 | 现象 | 常见方案 |
|------|------|----------|
| **穿透** | 查不存在的数据，缓存与 DB 都没有 | 布隆过滤器；缓存空值短 TTL |
| **击穿** | 热点 key 过期，并发打穿 DB | 互斥锁重建；热点永不过期 |
| **雪崩** | 大量 key 同时过期 | TTL 加随机；多级缓存；限流 |

与 [[cache/redis-面试题]] Q3 一致。
""",
        "middleware/kafka-与-mq选型": """
## Kafka 丢消息怎么处理？（raw 面试笔试）

1. **生产者**：`acks=all`、重试、`min.insync.replicas`
2. **Broker**：副本同步、禁止 unclean leader 选举
3. **消费者**：先处理再 commit offset；幂等消费
4. **监控**：滞后 lag、ISR 收缩告警

见 [[middleware/rocketmq-架构与实战]] 对比选型。
""",
        "security/api-接口安全设计": """
## 开放 API 安全要点（raw 架构/安全）

- **鉴权**：Token/OAuth2、签名校验、时间戳防重放
- **传输**：HTTPS、证书校验
- **限流**：按 appId/IP QPS
- **数据**：敏感字段脱敏；错误响应不泄露堆栈
""",
        "middleware/feign-开发踩坑": """
## Feign 常见问题（raw 采坑）

| 问题 | 处理 |
|------|------|
| **Multipart 上传** | Feign 默认不支持文件；改 `SpringFormEncoder` 或换 RestTemplate |
| **bootstrap vs application** | 配置加载顺序导致注册失败；统一 Spring Cloud 版本 |
| **Dalston/SR4 踩坑** | 与 Hystrix/Ribbon 版本对齐 |

Gateway 跨域见 [[spring/spring-cloud-gateway]]。
""",
        "spring/spring-cloud-gateway": """
## Gateway 要点（raw）

- **路由**：Predicate + Filter 链；动态路由可接 Nacos
- **跨域**：全局 `CorsWebFilter` 或 YAML `globalcors`
- **与 Sentinel**：网关层 QPS/热点参数限流 [[middleware/sentinel-限流与熔断]]
""",
        "database/b-plus树与-innodb索引结构": """
## B 树 vs B+ 树（raw 面试笔试/树）

| | B 树 | B+ 树 |
|---|------|-------|
| 数据存储 | 内部节点也可存数据 | **只在叶子存数据** |
| 叶子链表 | 无 | **有**，范围扫描友好 |
| InnoDB | — | **聚簇索引默认 B+** |

见 [[database/mysql-索引面试题]] Q1。
""",
        "java/jvm-gc调优实战": """
## 堆与 GC 调优备忘（raw）

- `-Xms` 与 `-Xms` 设成相同，避免动态扩堆
- 观察 **GC 日志**：`-Xlog:gc*`（JDK9+）或 `-XX:+PrintGCDetails`
- **Full GC 频繁**：老年代不足、Metaspace、大对象；配合 MAT
- 生产默认 G1（JDK9+）；吞吐优先可用 Parallel

见 [[java/jvm-面试题]]、[[java/jvm-oom与排查入门]]。
""",
        "middleware/nacos-注册与配置": """
## Nacos 备忘（raw）

- **注册**：服务名 + group + namespace 与 Consumer 一致
- **配置**：`shared-configs` / `extension-configs`；动态刷新 `@RefreshScope`
- **War 部署未注册**：检查 `spring.cloud.nacos.discovery` 与网络

见 [[middleware/nacos-config动态配置实践]]、[[middleware/dubbo-与-nacos]]。
""",
    }
    extra = extras.get(slug, "")
    if extra and extra.strip() not in body:
        body = body.rstrip() + extra + "\n"
    return body


def main() -> None:
    slug_sources: dict[str, set[str]] = {}
    for prefix, slug in P1_MAP.items():
        slug_sources.setdefault(slug, set()).update(list_raw_md(prefix))

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
        if merged == old and slug not in ENRICH_NOTES:
            continue
        fm = set_sources(fm, merged)
        fm = set_updated(fm)
        body = enrich_body(slug, body)
        if slug in ENRICH_NOTES:
            body = append_batch(body, ENRICH_NOTES[slug])
        path.write_text(fm + body, encoding="utf-8")
        touched.append(slug)
        print(f"OK {slug}: {len(old)} -> {len(merged)} sources")

    log_path = WIKI / "log.md"
    if log_path.exists() and BATCH not in log_path.read_text(encoding="utf-8"):
        line = (
            f"\n## [{TODAY}] ingest | 批次{BATCH} wujinsen P1 → enrich {len(touched)} 页 "
            f"({', '.join(touched[:4])}{'…' if len(touched) > 4 else ''})\n"
        )
        log_path.write_text(log_path.read_text(encoding="utf-8").rstrip() + line, encoding="utf-8")
    print("Touched", len(touched))


if __name__ == "__main__":
    main()
