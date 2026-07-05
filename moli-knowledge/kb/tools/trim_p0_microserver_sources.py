#!/usr/bin/env python3
"""Trim over-broad MicroServer sources after P0 ingest fix."""
import re
from pathlib import Path

WIKI = Path(__file__).resolve().parent.parent / "wiki"

RULES: dict[str, list[str]] = {
    "middleware/dubbo-调用原理与分层": [
        "面试笔试/Dubbo",
        "面试笔试/精尽面试题/dubbo",
        "架构/MicroServer/Dubbo",
    ],
    "middleware/feign-开发踩坑": [
        "架构/MicroServer/SpringCloud/采坑记录",
        "架构/项目踩坑",
    ],
    "spring/spring-cloud-gateway": [
        "架构/MicroServer/SpringCloud/SpringCloudGateWay",
    ],
    "middleware/sentinel-限流与熔断": [
        "架构/MicroServer/SpringCloud/sentinel",
        "架构/MicroServer/SpringCloud/Hystrix",
    ],
    "java/java-并发面试题": [
        "面试笔试/Java",
        "面试笔试/Java面试题精选",
        "面试笔试/面试小结/面试小结之并发篇",
        "面试笔试/面试题整理/Java面试通关要点汇总集核心篇",
        "面试笔试/面试题整理/Java后台面试 常见问题",
    ],
    "database/mysql-索引面试题": [
        "DataBase/mysql",
        "面试笔试/Database",
        "面试笔试/树",
        "面试笔试/面试题整理/复合索引",
        "面试笔试/面试题整理/MySQL数据库MyISAM",
    ],
    "middleware/netty-与-io面试题": [
        "面试笔试/面试小结/面试小结之IO篇",
    ],
    "search/elasticsearch-面试题": [
        "面试笔试/面试小结/面试小结之Elasticsearch篇",
        "面试笔试/ElasticSearch",
    ],
    "cache/分布式锁面试题": [
        "面试笔试/面试小结/分布式锁",
        "面试笔试/分布式",
    ],
}


def keep_source(src: str, prefixes: list[str]) -> bool:
    if not src.startswith("raw/wujinsen_markdown/"):
        return True
    rel = src[len("raw/wujinsen_markdown/") :]
    return any(rel.startswith(p) or rel == p for p in prefixes)


def main() -> None:
    for slug, prefixes in RULES.items():
        cat, stem = slug.split("/", 1)
        path = WIKI / cat / f"{stem}.md"
        text = path.read_text(encoding="utf-8")
        m = re.match(r"^(---\n.*?\n---\n)([\s\S]*)$", text, re.S)
        fm, body = m.group(1), m.group(2)
        srcs = re.findall(r"^\s*-\s*(raw/wujinsen_markdown/[^\n]+)", fm, re.M)
        other = [s for s in re.findall(r"^\s*-\s*([^\n]+)", re.search(r"^sources:\n((?:[ \t]+-[^\n]+\n?)*)", fm, re.M).group(1)) if s.strip()]
        all_src = []
        block = re.search(r"^sources:\n((?:[ \t]+-[^\n]+\n?)*)", fm, re.M).group(1)
        for ln in block.splitlines():
            s = ln.strip()[2:].strip()
            all_src.append(s)
        kept = [s for s in all_src if keep_source(s, prefixes)]
        if len(kept) == len(all_src):
            print(slug, "unchanged", len(all_src))
            continue
        new_block = "sources:\n" + "\n".join(f" - {s}" for s in kept) + "\n"
        fm = re.sub(r"^sources:\n(?:[ \t]+-[^\n]+\n?)*", new_block, fm, count=1, flags=re.M)
        path.write_text(fm + body, encoding="utf-8")
        print(slug, len(all_src), "->", len(kept))


if __name__ == "__main__":
    main()
