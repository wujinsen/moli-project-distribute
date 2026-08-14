#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""T22 R2c: merge duplicate `## 原文插图（wujinsen）` blocks on hub wiki pages.

Usage:
  python moli-knowledge/kb/tools/merge_wujinsen_image_sections.py --dry-run
  python moli-knowledge/kb/tools/merge_wujinsen_image_sections.py --apply
"""
from __future__ import annotations

import argparse
import re
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
WIKI = KB / "wiki"
LOG = WIKI / "log.md"

MARKER_PREFIX = "<!-- t22-wujinsen-images:"
SECTION_HEADER = "## 原文插图（wujinsen）"
ANNEX_LINE = re.compile(r"^原文插图 annex：\[\[([^\]]+)\]\]\s*$", re.M)

BLOCK_RE = re.compile(
    rf"(?:{re.escape(MARKER_PREFIX)}[^\n]*\n)?"
    rf"{re.escape(SECTION_HEADER)}\n"
    rf"(.*?)"
    rf"(?=(?:{re.escape(MARKER_PREFIX)}|{re.escape(SECTION_HEADER)}|原文插图 annex：|\Z))",
    re.S,
)


def extract_annex_links(text: str) -> list[str]:
    seen: set[str] = set()
    out: list[str] = []
    for m in ANNEX_LINE.finditer(text):
        slug = m.group(1).strip()
        if slug and slug not in seen:
            seen.add(slug)
            out.append(slug)
    return out


def strip_trailing_annex_block(text: str) -> str:
    lines = text.splitlines()
    while lines and (not lines[-1].strip() or ANNEX_LINE.match(lines[-1].strip())):
        if lines and ANNEX_LINE.match(lines[-1].strip()):
            lines.pop()
            while lines and not lines[-1].strip():
                lines.pop()
            continue
        if lines and not lines[-1].strip():
            lines.pop()
        else:
            break
    return "\n".join(lines).rstrip()


def merge_file(path: Path, dry_run: bool) -> bool:
    text = path.read_text(encoding="utf-8")
    if text.count(SECTION_HEADER) < 2:
        annex_only = extract_annex_links(text)
        if len(annex_only) <= 1:
            return False
        # dedupe annex lines even when only one section header
        base = strip_trailing_annex_block(text)
        annex_lines = [f"原文插图 annex：[[{s}]]" for s in annex_only]
        merged = base.rstrip() + "\n\n" + "\n\n".join(annex_lines) + "\n"
        if merged == text:
            return False
        if not dry_run:
            path.write_text(merged, encoding="utf-8")
        return True

    body = strip_trailing_annex_block(text)
    blocks = list(BLOCK_RE.finditer(body))
    if len(blocks) < 2:
        return False

    prefix_end = blocks[0].start()
    prefix = body[:prefix_end].rstrip()

    subsections: list[str] = []
    seen_sub: set[str] = set()
    for m in blocks:
        chunk = m.group(1).strip()
        if not chunk:
            continue
        key = chunk.split("\n", 1)[0].strip()
        if key in seen_sub:
            continue
        seen_sub.add(key)
        subsections.append(chunk)

    annex_slugs = extract_annex_links(text)
    merged_section = (
        f"{SECTION_HEADER}\n\n"
        f"> wujinsen 原文插图回迁（T22）· 共 {len(subsections)} 组\n\n"
        + "\n\n".join(subsections)
    )
    annex_block = ""
    if annex_slugs:
        annex_block = "\n\n" + "\n\n".join(f"原文插图 annex：[[{s}]]" for s in annex_slugs)

    merged = prefix + "\n\n" + merged_section + annex_block + "\n"
    if merged == text:
        return False
    if not dry_run:
        path.write_text(merged, encoding="utf-8")
    return True


def append_log(count: int, dry_run: bool) -> None:
    if dry_run or count <= 0:
        return
    line = f"## [{date.today().isoformat()}] remediate | T22 R2c merge {count} hub pages\n"
    if LOG.exists():
        LOG.write_text(LOG.read_text(encoding="utf-8").rstrip() + "\n" + line, encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Merge duplicate wujinsen image sections")
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--slug", help="only merge one wiki slug, e.g. bigdata/hadoop-生态入门")
    args = parser.parse_args()
    dry_run = not args.apply or args.dry_run

    touched = 0
    for md in sorted(WIKI.rglob("*.md")):
        if md.name in ("index.md", "log.md"):
            continue
        rel = md.relative_to(WIKI).as_posix()[:-3]
        if args.slug and rel != args.slug and md.stem != args.slug:
            continue
        if SECTION_HEADER not in md.read_text(encoding="utf-8", errors="ignore"):
            continue
        if merge_file(md, dry_run):
            touched += 1
            print(f"{'would merge' if dry_run else 'merged'}: {rel}")

    append_log(touched, dry_run)
    print(f"Done: {touched} file(s) {'(dry-run)' if dry_run else '(applied)'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
