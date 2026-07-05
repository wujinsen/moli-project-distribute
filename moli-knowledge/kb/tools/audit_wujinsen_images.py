#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Audit wujinsen raw images vs wiki; emit WUJINSEN_IMAGE_REMEDIATION.md (T22 R1).

Usage:
  python moli-knowledge/kb/tools/audit_wujinsen_images.py
  python moli-knowledge/kb/tools/audit_wujinsen_images.py --json out.json
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
OUT = HERE / "WUJINSEN_IMAGE_REMEDIATION.md"
SKIP_DELETED = HERE / "WUJINSEN_SKIP_DELETED.md"

IMG_REF = re.compile(r"!\[[^\]]*\]\([^)]*(?:note_images|_images/)[^)]*\)", re.I)
RAW_SRC = re.compile(r"raw/wujinsen_markdown/([^\s\]]+\.md)")


def norm(p: str) -> str:
    return p.replace("\\", "/")


def companion_image_dirs(md_path: Path) -> list[Path]:
    out: list[Path] = []
    name = md_path.name
    parent = md_path.parent
    if not name.endswith(".note.md"):
        return out
    stem = name[: -len(".note.md")]
    for suffix in (".note_images", "_note_images"):
        p = parent / f"{stem}{suffix}"
        if p.is_dir():
            out.append(p)
    alt = parent / f"{name}.note_images"
    if alt.is_dir():
        out.append(alt)
    return out


def count_images(md_path: Path) -> tuple[int, int]:
    """Return (markdown image refs, png files on disk)."""
    try:
        text = md_path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return 0, 0
    refs = len(IMG_REF.findall(text)) + text.count(".note_images/")
    png = 0
    for d in companion_image_dirs(md_path):
        png += sum(1 for _ in d.rglob("*") if _.suffix.lower() in {".png", ".jpg", ".jpeg", ".gif", ".webp"})
    return refs, png


def load_wiki_citations() -> dict[str, list[str]]:
    """raw rel path -> wiki slugs citing it."""
    m: dict[str, list[str]] = defaultdict(list)
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        text = p.read_text(encoding="utf-8", errors="ignore")
        fm = text.split("---", 2)
        body = text
        slug = p.stem
        if len(fm) >= 3 and fm[1].strip():
            sm = re.search(r"^slug:\s*(.+)$", fm[1], re.M)
            if sm:
                slug = sm.group(1).strip()
            cat = p.parent.name
            slug = f"{cat}/{slug}" if "/" not in slug else slug
        for rel in RAW_SRC.findall(text):
            m[norm(rel)].append(slug)
    for k in m:
        m[k] = sorted(set(m[k]))
    return m


def load_skip_deleted_raw() -> set[str]:
    if not SKIP_DELETED.exists():
        return set()
    text = SKIP_DELETED.read_text(encoding="utf-8", errors="ignore")
    out: set[str] = set()
    for line in text.splitlines():
        line = line.strip()
        if line.startswith("raw/wujinsen_markdown/") and line.endswith(".md"):
            out.add(line.split("wujinsen_markdown/", 1)[-1])
    return out


def suggest_strategy(cited_slugs: list[str], img_refs: int, png_files: int) -> str:
    if not cited_slugs:
        return "defer"  # not in wiki sources
    if img_refs >= 8 or png_files >= 8:
        return "A"  # annex page
    if len(cited_slugs) == 1:
        return "C-or-A"  # single cite: annex or careful replace
    return "B"  # enrich illustration section on hub page(s)


def main() -> int:
    parser = argparse.ArgumentParser(description="Audit wujinsen images for T22 remediation")
    parser.add_argument("--json", help="optional JSON output path")
    args = parser.parse_args()

    citations = load_wiki_citations()
    skip_deleted = load_skip_deleted_raw()
    rows: list[dict] = []

    for md in sorted(RAW.rglob("*.md")):
        rel = norm(md.relative_to(RAW).as_posix())
        refs, png = count_images(md)
        if refs == 0 and png == 0:
            continue
        raw_key = f"raw/wujinsen_markdown/{rel}"
        cited = citations.get(rel, [])
        if rel in skip_deleted:
            strategy = "skip-deleted"
            status = "waived"
        elif not cited:
            strategy = "defer"
            status = "pending"
        else:
            strategy = suggest_strategy(cited, refs, png)
            status = "pending"

        rows.append(
            {
                "raw_path": raw_key,
                "image_refs": refs,
                "png_files": png,
                "cited_by": cited,
                "cited_count": len(cited),
                "strategy": strategy,
                "status": status,
            }
        )

    cited_rows = [r for r in rows if r["cited_by"]]
    priority = [r for r in cited_rows if r["strategy"] != "skip-deleted"]

    lines = [
        "# wujinsen 图片回迁 manifest（T22 · R1 审计）",
        "",
        "> 由 `audit_wujinsen_images.py` 生成。策略说明见 `docs/product/wujinsen-wiki-image-remediation-prd.md`。",
        "",
        "## 汇总",
        "",
        f"| 指标 | 值 |",
        f"|------|-----|",
        f"| raw 含图 md | {len(rows)} |",
        f"| 已被 wiki cite 的含图 raw | {len(cited_rows)} |",
        f"| 优先回迁（非 skip-deleted） | {len(priority)} |",
        f"| png 文件合计（抽样统计） | {sum(r['png_files'] for r in rows)} |",
        "",
        "## 策略档",
        "",
        "| 档 | 含义 |",
        "|----|------|",
        "| **A** | 新建 annex 页 + `.assets` |",
        "| **B** | 枢纽页追加 `## 原文插图` |",
        "| **C-or-A** | 单 cite：annex 或谨慎全文替换 |",
        "| **D** | 过渡：仅 `/kb/raw/asset` 直链 |",
        "| **defer** | 未 cite，暂不回迁 |",
        "| **skip-deleted** | #1331 已删 raw，不可恢复 |",
        "",
        "## 优先集（已 cite · 待回迁）",
        "",
        "| raw | 图(ref/png) | cite 数 | wiki slugs | 建议 |",
        "|-----|-------------|---------|------------|------|",
    ]

    for r in sorted(priority, key=lambda x: (-x["png_files"], x["raw_path"]))[:120]:
        slugs = ", ".join(r["cited_by"][:3])
        if len(r["cited_by"]) > 3:
            slugs += f" (+{len(r['cited_by']) - 3})"
        lines.append(
            f"| `{r['raw_path']}` | {r['image_refs']}/{r['png_files']} | {r['cited_count']} | {slugs} | **{r['strategy']}** |"
        )

    if len(priority) > 120:
        lines.append("")
        lines.append(f"> 仅展示 png 最多的前 120 行；全量 {len(priority)} 行见 `--json`。")

    lines.extend(["", "## 全量", ""])
    lines.append("<details><summary>展开全量 {} 行</summary>".format(len(rows)))
    lines.append("")
    lines.append("| raw | ref/png | cited | strategy | status |")
    lines.append("|-----|---------|-------|----------|--------|")
    for r in rows:
        lines.append(
            f"| `{r['raw_path']}` | {r['image_refs']}/{r['png_files']} | {r['cited_count']} | {r['strategy']} | {r['status']} |"
        )
    lines.append("")
    lines.append("</details>")
    lines.append("")

    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {OUT} ({len(rows)} rows, {len(priority)} priority)")

    if args.json:
        Path(args.json).write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"Wrote {args.json}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
