#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Ingest kb/raw/school/certify/*.md → kb/wiki-jp-exam/certify/.

用法：
  python kb/tools/ingest_certify_wiki.py
  python kb/tools/ingest_certify_wiki.py --dry-run
"""
from __future__ import annotations

import argparse
import re
from datetime import date
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "school" / "certify"
WIKI = KB / "wiki-jp-exam" / "certify"
HUB_SLUG = "Certifyサーティファイ"

SKIP = {"片假名词汇表_预览10.md", "certify_analyze_zh.py"}
FM_RE = re.compile(r"^---\n.*?\n---\n", re.S)


def _stem(name: str) -> str:
    return Path(name).stem


def _slug_for(name: str) -> str:
    stem = _stem(name)
    if stem == "片假名词汇表":
        return "certify-katakana-vocab"
    if stem.endswith("_中文解析"):
        return stem.replace("_中文解析", "-中文解析")
    return stem


def _html_sources(stem: str) -> list[str]:
    out: list[str] = []
    for path in sorted(RAW.glob("*.html")):
        if path.name.startswith(stem) and "Attempt review" in path.name:
            out.append(f"raw/school/certify/{path.name}")
    return out


def _exam_stem_from_zh(stem: str) -> str | None:
    if not stem.endswith("_中文解析"):
        return None
    return stem[: -len("_中文解析")]


def _body(path: Path, *, title: str) -> str:
    text = path.read_text(encoding="utf-8")
    body = FM_RE.sub("", text, count=1).lstrip("\n")
    if body.startswith("# "):
        lines = body.splitlines()
        lines[0] = f"# {title}"
        body = "\n".join(lines)
    return body


def _frontmatter(
    *,
    title: str,
    slug: str,
    kb_type: str,
    tags: list[str],
    sources: list[str],
    related: list[str],
) -> str:
    today = date.today().isoformat()
    tags_s = ", ".join(tags)
    src_lines = "\n".join(f"  - {s}" for s in sources)
    rel_lines = "\n".join(f"  - {r}" for r in related) if related else "  []"
    return (
        "---\n"
        f"title: {title}\n"
        f"slug: {slug}\n"
        f"type: {kb_type}\n"
        "status: active\n"
        f"tags: [{tags_s}]\n"
        "sources:\n"
        f"{src_lines}\n"
        "related:\n"
        f"{rel_lines}\n"
        f"created: {today}\n"
        f"updated: {today}\n"
        "---\n\n"
    )


def _ingest_file(path: Path, *, dry_run: bool) -> tuple[str, str]:
    name = path.name
    stem = _stem(name)
    slug = _slug_for(name)

    if stem.endswith("_中文解析"):
        exam_stem = _exam_stem_from_zh(stem)
        title = f"Certify · {exam_stem} · 中文解析"
        kb_type = "article"
        tags = ["certify", "サーティファイ", "中文解析"]
        sources = [f"raw/school/certify/{name}"]
        if exam_stem:
            exam_md = RAW / f"{exam_stem}.md"
            if exam_md.is_file():
                sources.append(f"raw/school/certify/{exam_md.name}")
            sources.extend(_html_sources(exam_stem))
        related = [exam_stem, HUB_SLUG] if exam_stem else [HUB_SLUG]
    elif stem == "片假名词汇表":
        title = "Certify · 片假名词汇表"
        kb_type = "article"
        tags = ["certify", "サーティファイ", "片假名", "词汇"]
        sources = [
            "raw/school/certify/片假名词汇表.md",
            "kb/tools/certify_katakana_translations.json",
        ]
        related = [HUB_SLUG]
    else:
        title = f"Certify · {stem}"
        kb_type = "article"
        tags = ["certify", "サーティファイ", "exam", "moodle"]
        sources = [f"raw/school/certify/{name}"]
        sources.extend(_html_sources(stem))
        zh_slug = f"{stem}-中文解析"
        related = [HUB_SLUG]
        if (RAW / f"{stem}_中文解析.md").is_file():
            related.insert(0, zh_slug)

    body = _body(path, title=title)
    content = _frontmatter(
        title=title,
        slug=slug,
        kb_type=kb_type,
        tags=tags,
        sources=sources,
        related=related,
    ) + body

    out = WIKI / f"{slug}.md"
    action = "update" if out.is_file() else "create"
    if not dry_run:
        WIKI.mkdir(parents=True, exist_ok=True)
        out.write_text(content, encoding="utf-8")
    return action, slug


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    actions: list[tuple[str, str]] = []
    for path in sorted(RAW.glob("*.md")):
        if path.name in SKIP or path.name.startswith("片假名词汇表_预览"):
            continue
        actions.append(_ingest_file(path, dry_run=args.dry_run))

    print(f"[ok] {'would ingest' if args.dry_run else 'ingested'} {len(actions)} pages → wiki-jp-exam/certify/")
    for action, slug in actions:
        print(f"  {action}: {slug}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
