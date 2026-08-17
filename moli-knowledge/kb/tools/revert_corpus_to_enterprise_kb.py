#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""将通用技术语料从 wiki-moli 迁回 enterprise-kb（wiki/），并去掉 ingest 模板里的「茉莉」章节。

茉莉系统手册（wiki-moli）只保留 moli-project-distribute **项目文档**：
  guides/ product/ develop/（实体页、索引、outputs） ops/ test/（项目测试，非 interview 语料）

迁回 enterprise-kb：
  develop/articles/*  → wiki/articles/*（项目专文 → develop/ 根目录）
  develop/concepts/*  → wiki/concepts/*
  test/interview/*    → wiki/interview/*

用法：
  python revert_corpus_to_enterprise_kb.py --dry-run
  python revert_corpus_to_enterprise_kb.py
"""
from __future__ import annotations

import argparse
import re
import shutil
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
WIKI = KB / "wiki"
WIKI_MOLI = KB / "wiki-moli"
SKIP_NAMES = {"index.md", "log.md"}

# 留在 wiki-moli/develop/ 根目录（非 articles/）的项目专文
KEEP_IN_DEVELOP = {
    "茉莉微服务演进路线-2026.md",
    "docker-compose-茉莉依赖栈.md",
    "kb-wiki到es同步流水线.md",
    "wiki-ingest-质量规范.md",
}

# 链修：wiki-moli 前缀 → enterprise-kb 前缀（最长优先）
REVERSE_SLUG_PREFIXES: list[tuple[str, str]] = [
    ("develop/articles/", "articles/"),
    ("test/interview/", "interview/"),
    ("develop/concepts/", "concepts/"),
]

MOLI_SECTION = re.compile(r"^##\s+.*茉莉")


def strip_moli_touchpoint_sections(text: str) -> str:
    """删除批量 ingest 插入的「## …茉莉…」整节（至下一同级 ##）。"""
    lines = text.splitlines(keepends=True)
    out: list[str] = []
    skip = False
    for line in lines:
        if line.startswith("## "):
            if MOLI_SECTION.match(line.rstrip("\r\n")):
                skip = True
                continue
            skip = False
        if not skip:
            out.append(line)
    result = "".join(out)
    result = re.sub(r"\n{3,}", "\n\n", result)
    return result.rstrip() + "\n"


def rewrite_links(text: str, pairs: list[tuple[str, str]]) -> str:
    for old, new in pairs:
        text = text.replace(old, new)
        text = text.replace(old.replace("/", "\\"), new.replace("/", "\\"))
    return text


def move_corpus(dry_run: bool) -> dict[str, int]:
    stats = {"articles": 0, "concepts": 0, "interview": 0, "kept_develop": 0}

    src_articles = WIKI_MOLI / "develop" / "articles"
    if src_articles.is_dir():
        for f in sorted(src_articles.rglob("*.md")):
            rel = f.relative_to(src_articles)
            name = f.name
            if name in KEEP_IN_DEVELOP:
                tgt = WIKI_MOLI / "develop" / name
                stats["kept_develop"] += 1
                if dry_run:
                    print(f"KEEP develop/{name}")
                else:
                    text = f.read_text(encoding="utf-8")
                    tgt.write_text(text, encoding="utf-8")
                    f.unlink()
                continue
            tgt = WIKI / "articles" / rel
            stats["articles"] += 1
            if dry_run:
                print(f"MOVE articles/{rel.as_posix()}")
            else:
                tgt.parent.mkdir(parents=True, exist_ok=True)
                text = strip_moli_touchpoint_sections(f.read_text(encoding="utf-8"))
                tgt.write_text(text, encoding="utf-8")
                f.unlink()

    src_concepts = WIKI_MOLI / "develop" / "concepts"
    if src_concepts.is_dir():
        for f in sorted(src_concepts.rglob("*.md")):
            rel = f.relative_to(src_concepts)
            tgt = WIKI / "concepts" / rel
            stats["concepts"] += 1
            if dry_run:
                print(f"MOVE concepts/{rel.as_posix()}")
            else:
                tgt.parent.mkdir(parents=True, exist_ok=True)
                text = strip_moli_touchpoint_sections(f.read_text(encoding="utf-8"))
                tgt.write_text(text, encoding="utf-8")
                f.unlink()

    src_interview = WIKI_MOLI / "test" / "interview"
    if src_interview.is_dir():
        for f in sorted(src_interview.rglob("*.md")):
            rel = f.relative_to(src_interview)
            tgt = WIKI / "interview" / rel
            stats["interview"] += 1
            if dry_run:
                print(f"MOVE interview/{rel.as_posix()}")
            else:
                tgt.parent.mkdir(parents=True, exist_ok=True)
                text = strip_moli_touchpoint_sections(f.read_text(encoding="utf-8"))
                tgt.write_text(text, encoding="utf-8")
                f.unlink()

    # 清理空目录
    if not dry_run:
        for d in (src_articles, src_concepts, src_interview):
            if d.is_dir() and not any(d.rglob("*.md")):
                shutil.rmtree(d, ignore_errors=True)

    return stats


def fix_all_wikilinks(dry_run: bool) -> int:
    pairs = sorted(REVERSE_SLUG_PREFIXES, key=lambda x: len(x[0]), reverse=True)
    count = 0
    for root in (KB / "wiki", KB / "wiki-moli"):
        if not root.is_dir():
            continue
        for f in root.rglob("*.md"):
            if f.name in SKIP_NAMES:
                continue
            text = f.read_text(encoding="utf-8")
            new = rewrite_links(text, pairs)
            if new != text:
                count += 1
                if not dry_run:
                    f.write_text(new, encoding="utf-8")
    return count


def write_wiki_index(dry_run: bool) -> None:
    """enterprise-kb 入口：通用技术文库，非茉莉项目手册。"""
    content = """# 企业知识库（enterprise-kb）

> **通用技术文库**（articles / concepts / interview 等 ingest 语料）。  
> **茉莉项目文档**（产品、架构、运维、测试 Runbook 等）在 **`moli-ops-manual`（茉莉系统手册）** · `kb/wiki-moli/` → [[项目文档总览]]（在 moli-ops-manual 空间打开）。

## 目录

| 目录 | 说明 |
|------|------|
| `articles/` | 技术文章沉淀（Dubbo、MySQL、Redis、Vue 等 **通用** 主题） |
| `concepts/` | 跨文档概念枢纽 |
| `interview/` | 面试题 / 八股语料 |

> 本库正文 **不含** ingest 模板里的「茉莉触点」节；与项目相关的实践请写在 wiki-moli 对应服务页或 Runbook。

## 其它空间

| 空间 | 目录 | 定位 |
|------|------|------|
| **moli-ops-manual** | `wiki-moli/` | moli-project-distribute **项目手册** |
| **moli-ops-manual** | `wiki-moli/` | 茉莉系统手册 |

## 同步

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all
```

详见 wiki-moli 内 [[wiki同步指南]]（moli-ops-manual 空间）。
"""
    if dry_run:
        print("WRITE wiki/index.md")
    else:
        WIKI.mkdir(parents=True, exist_ok=True)
        (WIKI / "index.md").write_text(content, encoding="utf-8")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    stats = move_corpus(args.dry_run)
    links = fix_all_wikilinks(args.dry_run)
    write_wiki_index(args.dry_run)

    print(
        f"{'[dry-run] ' if args.dry_run else ''}"
        f"articles={stats['articles']} concepts={stats['concepts']} "
        f"interview={stats['interview']} kept_in_develop={stats['kept_develop']} "
        f"link_files={links}"
    )


if __name__ == "__main__":
    main()
