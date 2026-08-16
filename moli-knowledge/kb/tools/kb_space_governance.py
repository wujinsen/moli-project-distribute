#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""知识库空间治理（安全版）：项目 ↔ 通用分离，清理 enterprise-kb 误加「茉莉」。

不破坏 Markdown 表格换行；逐行处理正文。

用法：
  python kb_space_governance.py [--dry-run]
"""
from __future__ import annotations

import argparse
import re
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
WIKI = KB / "wiki"
WIKI_MOLI = KB / "wiki-moli"
SKIP = {"index.md", "log.md"}

MOVE_TO_MOLI: dict[str, str] = {
    "concepts/服务调用与架构.md": "develop/服务调用与架构.md",
    "concepts/秒杀设计.md": "develop/秒杀设计.md",
    "concepts/技术栈与版本.md": "develop/技术栈与版本.md",
    "concepts/知识库三操作.md": "develop/知识库三操作.md",
    "articles/moli生产部署拓扑备忘.md": "ops/生产部署拓扑备忘.md",
}

KEEP_IN_MOLI_ARTICLES = {
    "docker-compose-茉莉依赖栈.md",
    "kb-wiki到es同步流水线.md",
    "wiki-ingest-质量规范.md",
}

SLUG_RENAME: dict[str, str] = {
    "k8s入门与茉莉关系": "k8s入门与容器编排",
    "moli生产部署拓扑备忘": "生产部署拓扑备忘",
}

STRIP_WIKILINKS = {
    "项目文档总览", "本地启动指南", "登录与鉴权指南", "权限管理操作指南",
    "数据库初始化指南", "故障排查指南", "docker部署指南", "秒杀压测指南",
    "压测报告解读指南", "前端开发与联调指南", "swagger接口调试指南",
    "git协作指南", "增量ingest与raw投喂指南", "AI自我进化与MD审校流程",
    "Wiki治理工作台产品方案", "Ingest工作台产品方案", "wiki同步指南",
    "服务调用与架构", "秒杀设计", "技术栈与版本", "知识库三操作",
    "用户中心", "网关", "订单服务", "bi服务", "知识库服务",
    "茉莉微服务全链路一张图", "茉莉稳定性与故障排查要点汇总",
    "茉莉新人上手checklist", "生产部署拓扑备忘",
}

MOLI_SECTION = re.compile(r"^##\s+.*茉莉")
WIKILINK = re.compile(r"\[\[([^\]|]+)(?:\|[^\]]+)?\]\]")

LINE_REPLACEMENTS: list[tuple[str, str]] = [
    (r"茉莉触点", "实践要点"),
    (r"茉莉现状", "典型现状"),
    (r"茉莉项目", "目标系统"),
    (r"茉莉微服务栈", "典型微服务栈"),
    (r"茉莉当前", "当前常见"),
    (r"茉莉 dev", "开发环境"),
    (r"茉莉未", "常见未"),
    (r"与茉莉关系", "与容器编排"),
    (r"与茉莉场景", "与典型场景"),
    (r"在茉莉中的", "在典型栈中的"),
    (r"在茉莉栈中", "在典型栈中"),
    (r"茉莉本地联调", "本地联调"),
    (r"茉莉架构", "系统架构"),
    (r"茉莉推荐", "常见推荐"),
    (r"茉莉", ""),
]


def is_moli_article(name: str) -> bool:
    stem = Path(name).stem
    return stem.startswith("茉莉") or stem.startswith("茉莉实践")


def neutralize_line(line: str, *, strip_links: bool) -> str:
    if strip_links:
        def _repl(m: re.Match[str]) -> str:
            slug = m.group(1).strip()
            if slug in STRIP_WIKILINKS or slug.startswith("茉莉"):
                return ""
            return m.group(0)

        line = WIKILINK.sub(_repl, line)
    for pat, repl in LINE_REPLACEMENTS:
        line = re.sub(pat, repl, line)
    line = re.sub(r"  +", " ", line)
    line = re.sub(r" ，", "，", line)
    line = re.sub(r" 。", "。", line)
    return line.rstrip()


def neutralize_body(text: str, *, strip_links: bool) -> str:
    lines = text.splitlines()
    out: list[str] = []
    skip = False
    for line in lines:
        if line.startswith("## "):
            if MOLI_SECTION.match(line):
                skip = True
                continue
            skip = False
        if skip:
            continue
        out.append(neutralize_line(line, strip_links=strip_links))
    result = "\n".join(out)
    result = re.sub(r"\n{3,}", "\n\n", result)
    return result.rstrip() + "\n"


def apply_slug_renames(text: str) -> str:
    for old, new in SLUG_RENAME.items():
        text = text.replace(f"[[{old}]]", f"[[{new}]]")
        if f"slug: {old}" in text:
            text = text.replace(f"slug: {old}", f"slug: {new}")
        if old in text and "k8s" in old:
            text = text.replace("title: K8s 入门与茉莉关系", "title: K8s 入门与容器编排")
            text = text.replace("# K8s 入门与茉莉关系", "# K8s 入门与容器编排")
    return text


def move_path(src: Path, tgt: Path, dry_run: bool, transform=None) -> bool:
    if not src.exists():
        return False
    if dry_run:
        print(f"MOVE {src.relative_to(KB)} -> {tgt.relative_to(KB)}")
        return True
    tgt.parent.mkdir(parents=True, exist_ok=True)
    body = src.read_text(encoding="utf-8")
    if transform:
        body = transform(body)
    tgt.write_text(body, encoding="utf-8")
    src.unlink()
    return True


def move_project_pages(dry_run: bool) -> int:
    n = 0
    for src_rel, tgt_rel in MOVE_TO_MOLI.items():
        src = WIKI / src_rel
        tgt = WIKI_MOLI / tgt_rel
        tf = None
        if "moli生产" in src_rel:
            def tf(t):
                return (t.replace("slug: moli生产部署拓扑备忘", "slug: 生产部署拓扑备忘")
                        .replace("title: moli 生产部署拓扑备忘", "title: 生产部署拓扑备忘")
                        .replace("# moli 生产部署拓扑备忘", "# 生产部署拓扑备忘"))
        if move_path(src, tgt, dry_run, transform=tf):
            n += 1
    return n


def move_moli_articles(dry_run: bool) -> int:
    """wiki/articles 中项目专文（茉莉*）→ wiki-moli/develop/"""
    n = 0
    src_dir = WIKI / "articles"
    if not src_dir.is_dir():
        return 0
    for f in sorted(src_dir.glob("*.md")):
        if f.name in KEEP_IN_MOLI_ARTICLES:
            tgt = WIKI_MOLI / "develop" / f.name
            if move_path(f, tgt, dry_run):
                n += 1
            continue
        if is_moli_article(f.name):
            tgt = WIKI_MOLI / "develop" / f.name
            if move_path(f, tgt, dry_run):
                n += 1
    return n


def rename_k8s(dry_run: bool) -> bool:
    old = WIKI / "concepts" / "k8s入门与茉莉关系.md"
    new = WIKI / "concepts" / "k8s入门与容器编排.md"
    if not old.exists():
        return False
    if dry_run:
        print("RENAME k8s入门与茉莉关系 -> k8s入门与容器编排")
        return True
    text = apply_slug_renames(old.read_text(encoding="utf-8"))
    text = neutralize_body(text, strip_links=True)
    new.write_text(text, encoding="utf-8")
    old.unlink()
    return True


def patch_enterprise_corpus(dry_run: bool) -> int:
    n = 0
    for sub in ("articles", "concepts", "interview"):
        root = WIKI / sub
        if not root.is_dir():
            continue
        for md in root.rglob("*.md"):
            if md.name in SKIP:
                continue
            raw = md.read_text(encoding="utf-8")
            new = apply_slug_renames(raw)
            new = neutralize_body(new, strip_links=True)
            if new != raw:
                n += 1
                if not dry_run:
                    md.write_text(new, encoding="utf-8")
    return n


def patch_all_slug_renames(dry_run: bool) -> int:
    n = 0
    for root in (WIKI, WIKI_MOLI):
        if not root.is_dir():
            continue
        for md in root.rglob("*.md"):
            if md.name in SKIP:
                continue
            raw = md.read_text(encoding="utf-8")
            new = apply_slug_renames(raw)
            if new != raw:
                n += 1
                if not dry_run:
                    md.write_text(new, encoding="utf-8")
    return n


def fix_wiki_moli(dry_run: bool) -> int:
    fixes = [
        (WIKI_MOLI / "develop" / "茉莉微服务演进路线-2026.md", "## 2. 茉莉触点", "## 2. 落地对照"),
        (WIKI_MOLI / "ops" / "docker部署指南.md", "## 6. 与茉莉启动方式选择", "## 6. 启动方式选择"),
        (WIKI_MOLI / "guides" / "事故复盘-postmortem.md", "## 2. 茉莉示例场景", "## 2. 示例场景"),
    ]
    n = 0
    for path, old, new in fixes:
        if path.exists() and old in path.read_text(encoding="utf-8"):
            n += 1
            if not dry_run:
                t = path.read_text(encoding="utf-8").replace(old, new)
                path.write_text(t, encoding="utf-8")
    return n


def dedupe_moli_after_move(dry_run: bool) -> int:
    """wiki-moli 已有副本时删 wiki 侧重复（MOVE_TO_MOLI 已处理）；删 wiki-moli/develop/articles 若存在。"""
    n = 0
    stray = WIKI_MOLI / "develop" / "articles"
    if stray.is_dir():
        for f in stray.rglob("*.md"):
            n += 1
            if not dry_run:
                f.unlink()
        if not dry_run:
            import shutil
            shutil.rmtree(stray, ignore_errors=True)
    return n


def remove_project_dirs_from_enterprise(dry_run: bool) -> int:
    """enterprise-kb 只保留 articles/concepts/interview；删 guides/services/outputs 副本。"""
    n = 0
    for sub in ("guides", "services", "outputs"):
        d = WIKI / sub
        if not d.is_dir():
            continue
        for f in sorted(d.rglob("*.md")):
            n += 1
            if dry_run:
                print(f"DELETE {f.relative_to(KB)}")
            else:
                f.unlink()
        if not dry_run:
            import shutil
            shutil.rmtree(d, ignore_errors=True)
    return n


def append_log(dry_run: bool) -> None:
    if dry_run:
        return
    line = (
        f"\n## [{date.today().isoformat()}] governance | "
        "空间分离 + enterprise-kb 去茉莉 branding\n"
    )
    with (WIKI_MOLI / "log.md").open("a", encoding="utf-8") as f:
        f.write(line)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    n1 = move_project_pages(args.dry_run)
    n2 = move_moli_articles(args.dry_run)
    k8s = rename_k8s(args.dry_run)
    n3 = patch_enterprise_corpus(args.dry_run)
    n4 = patch_all_slug_renames(args.dry_run)
    n5 = fix_wiki_moli(args.dry_run)
    n6 = dedupe_moli_after_move(args.dry_run)
    n7 = remove_project_dirs_from_enterprise(args.dry_run)
    append_log(args.dry_run)

    print(
        f"{'[dry-run] ' if args.dry_run else ''}"
        f"project_moves={n1} moli_articles={n2} k8s={k8s} "
        f"corpus_patched={n3} slug_patched={n4} moli_fixes={n5} stray_articles={n6} "
        f"enterprise_purged={n7}"
    )


if __name__ == "__main__":
    main()
