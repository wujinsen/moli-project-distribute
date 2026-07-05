#!/usr/bin/env python3
"""Repair wiki frontmatter + lint warnings (#1333)."""
from __future__ import annotations

import re
from pathlib import Path

from fix_enterprise_broken_links import PATH_REF, WIKI_LINK, replace_body
from sync_to_db import parse_frontmatter
from wujinsen_ingest_lib import append_log

KB = Path(__file__).resolve().parent.parent
WIKI = KB / "wiki"
TODAY = "2026-07-05"
BATCH = "#1333"

CONCEPT_PATH: dict[str, str] = {
    **PATH_REF,
    "docker部署指南": "moli-knowledge/kb/wiki-moli/ops/docker部署指南.md",
    "git-分支与发布策略": "moli-knowledge/kb/wiki-moli/develop/茉莉-规范-git分支.md",
    "minio-附件存储指南": "moli-knowledge/kb/wiki-moli/ops/minio-附件存储指南.md",
    "nginx反向代理与前端部署指南": "moli-knowledge/kb/wiki-moli/ops/nginx反向代理与前端部署指南.md",
    "前端开发与联调指南": "moli-knowledge/kb/wiki-moli/guides/前端开发与联调指南.md",
    "多级缓存架构": "moli-knowledge/kb/wiki-moli/develop/茉莉-缓存-多级.md",
    "技术栈与版本": "moli-knowledge/kb/wiki-moli/develop/技术栈与版本.md",
    "故障排查指南": "moli-knowledge/kb/wiki-moli/ops/故障排查指南.md",
    "服务调用与架构": "moli-knowledge/kb/wiki-moli/develop/服务调用与架构.md",
    "本地启动指南": "moli-knowledge/kb/wiki-moli/guides/本地启动指南.md",
    "用户中心": "moli-knowledge/kb/wiki-moli/develop/用户中心.md",
    "登录与鉴权指南": "moli-knowledge/kb/wiki-moli/guides/登录与鉴权指南.md",
    "知识库服务": "moli-knowledge/kb/wiki-moli/develop/知识库服务.md",
    "秒杀压测指南": "moli-knowledge/kb/wiki-moli/test/秒杀压测指南.md",
    "秒杀设计": "moli-knowledge/kb/wiki-moli/develop/秒杀设计.md",
    "网关": "moli-knowledge/kb/wiki-moli/develop/网关.md",
    "订单-状态机设计": "moli-knowledge/kb/wiki-moli/develop/茉莉-订单-状态机.md",
}

CONCEPT_LINK: dict[str, str] = {
    **WIKI_LINK,
    "micrometer-与指标暴露": "middleware/压测监控与prometheus",
    "mysql-主从读写分离": "database/mysql-binlog与canal同步",
    "skywalking-链路追踪": "ops/skywalking-安装与链路追踪",
    "spring-boot-actuator监控": "ops/prometheus-告警规则设计",
    "webflux-响应式入门": "middleware/webclient-与-resttemplate",
    "线程池-实战调优": "spring/spring-async与线程池",
}

ORPHAN_SLUGS = [
    "bigdata/dolphinscheduler-任务调度",
    "bigdata/elk-日志分析栈",
    "bigdata/hbase-列式存储入门",
    "database/flyway-数据库版本迁移",
    "database/mongodb与文档库选型",
    "database/mysql-slow-log慢查询分析",
    "middleware/bi报表服务演进路线",
    "middleware/rocketmq-消息堆积排查",
    "ops/k8s-健康检查探针",
    "ops/maven-多模块与依赖管理",
    "ops/nginx-反向代理与负载",
    "ops/shell-脚本入门",
    "ops/skywalking-安装与链路追踪",
    "patterns/算法面试题精选",
    "search/elasticsearch-ik分词与分析器",
    "security/bcrypt-密码哈希与加盐",
    "security/csrf与xss防护",
    "spring/spring-boot-启动优化",
]


def fix_related_extended(line: str) -> str:
    """Extend related fix for concept path slugs."""
    m = re.match(r"(related:\s*\[)([^\]]*)(\])", line)
    if not m:
        return line
    prefix, inner, suffix = m.groups()
    skip_paths = set(PATH_REF) | set(CONCEPT_PATH)
    out: list[str] = []
    for item in [x.strip() for x in inner.split(",") if x.strip()]:
        if item in skip_paths:
            continue
        if item in WIKI_LINK:
            out.append(item.split("/")[-1] if "/" in WIKI_LINK[item] else WIKI_LINK[item].split("/")[-1])
        elif item in CONCEPT_LINK:
            out.append(CONCEPT_LINK[item].split("/")[-1])
        elif item in skip_paths or item in CONCEPT_PATH or item in CONCEPT_LINK:
            continue
        else:
            out.append(item)
    seen: set[str] = set()
    deduped: list[str] = []
    for item in out:
        if item in seen:
            continue
        seen.add(item)
        deduped.append(item)
    return f"{prefix}{', '.join(deduped)}{suffix}"


def replace_concepts(text: str) -> str:
    for old, new in CONCEPT_LINK.items():
        text = text.replace(f"[[{old}]]", f"[[{new}]]")
    for old, path in CONCEPT_PATH.items():
        text = text.replace(f"[[{old}]]", f"`{path}`")
    return text


def normalize_list_line(line: str) -> str:
    """sync_to_db parser requires list lines to start with '- ' or '  - '."""
    m = re.match(r"^\s+-\s+(.*)$", line)
    if m:
        return f"- {m.group(1)}"
    return line


def dedupe_sources_block(fm_lines: list[str]) -> list[str]:
    """Merge multiple sources: blocks; keep other fields in order."""
    sources_items: list[str] = []
    out: list[str] = []
    for ln in fm_lines:
        if ln.strip() == "sources:":
            continue
        if re.match(r"^\s*-\s+", ln):
            item = normalize_list_line(ln)
            if item not in sources_items:
                sources_items.append(item)
            continue
        out.append(ln)
    if not sources_items:
        return out
    # insert sources before related/created/updated if possible
    insert_at = len(out)
    for i, ln in enumerate(out):
        if ln.strip().startswith(("related:", "created:", "updated:")):
            insert_at = i
            break
    return out[:insert_at] + ["sources:"] + sources_items + out[insert_at:]


def repair_frontmatter_block(fm: str) -> str:
    lines = fm.splitlines()
    out: list[str] = []
    i = 0
    while i < len(lines):
        line = lines[i]
        # drop dash-only separator lines inside sources
        if re.fullmatch(r"-{3,}", line.strip()):
            if out and re.match(r"^\s*-\s+", out[-1]):
                cont = ""
                j = i + 1
                while j < len(lines) and lines[j].startswith(" - "):
                    cont = lines[j][3:].strip()
                    j += 1
                    break
                if not cont and j < len(lines):
                    cont = lines[j].strip()
                    j += 1
                out[-1] = out[-1].rstrip() + cont
                i = j
                continue
            i += 1
            continue
        out.append(line)
        i += 1
    return "\n".join(out)


def normalize_file(text: str) -> str:
    if text.startswith("---\n\n"):
        text = "---\n" + text[5:]
    if not text.startswith("---"):
        return replace_concepts(replace_body(text))
    end = text.find("\n---", 3)
    if end == -1:
        return replace_concepts(replace_body(text))
    fm = repair_frontmatter_block(text[3:end].strip("\n"))
    body = text[end + 4 :].lstrip("\n")
    fm_lines = dedupe_sources_block(fm.splitlines())
    new_fm: list[str] = []
    for ln in fm_lines:
        if re.match(r"^\s*-\s+", ln):
            new_fm.append(normalize_list_line(ln))
        elif ln.strip().startswith("related:"):
            new_fm.append(fix_related_extended(ln))
        else:
            new_fm.append(ln)
    meta, _ = parse_frontmatter("---\n" + "\n".join(new_fm) + "\n---\n")
    if not meta.get("created"):
        new_fm.append(f"created: {TODAY}")
    if not meta.get("updated"):
        new_fm.append(f"updated: {TODAY}")
    meta2, _ = parse_frontmatter("---\n" + "\n".join(new_fm) + "\n---\n")
    if not meta2.get("sources"):
        slug = meta2.get("slug") or "unknown"
        cat = slug.split("/")[0] if "/" in slug else "wiki"
        new_fm.append("sources:")
        new_fm.append(f"- enterprise-kb/{cat} scaffold")
    fm = "\n".join(new_fm)
    body = replace_concepts(replace_body(body))
    return f"---\n{fm}\n---\n\n{body}"


def patch_index() -> None:
    idx = WIKI / "index.md"
    text = idx.read_text(encoding="utf-8")
    block = "\n## 专题页索引（lint orphan 收口）\n\n"
    for slug in ORPHAN_SLUGS:
        stem = slug.split("/")[-1]
        block += f"- [[{stem}]]（`wiki/{slug}.md`）\n"
    if "专题页索引" not in text:
        text = text.rstrip() + block + "\n"
        idx.write_text(text, encoding="utf-8")
        print("patched index.md")


def main() -> None:
    changed = 0
    for p in sorted(WIKI.rglob("*.md")):
        if p.name in ("index.md", "log.md"):
            continue
        old = p.read_text(encoding="utf-8")
        new = normalize_file(old)
        if new != old:
            p.write_text(new, encoding="utf-8")
            changed += 1
    patch_index()
    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} enterprise-kb lint 收口 → repair fm {changed} 页 + concept 链接 + index orphan {len(ORPHAN_SLUGS)}",
    )
    print("changed", changed)


if __name__ == "__main__":
    main()
