#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""T22 R3: verify wiki ![]( refs resolve to disk or raw/asset paths.

Usage:
  python moli-knowledge/kb/tools/verify_wujinsen_images.py
  python moli-knowledge/kb/tools/verify_wujinsen_images.py --wiki-dir moli-knowledge/kb/wiki
  python moli-knowledge/kb/tools/verify_wujinsen_images.py --report
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from datetime import date
from pathlib import Path
from urllib.parse import parse_qs, unquote, urlparse

HERE = Path(__file__).resolve().parent
KB = HERE.parent
RAW_ROOT = KB / "raw"
WIKI = KB / "wiki"
MANIFEST = HERE / "WUJINSEN_IMAGE_REMEDIATION.json"
REPORT = HERE / "WUJINSEN_R3_REPORT.md"

IMG_MD = re.compile(r"!\[[^\]]*\]\(([^)]+)\)")


def is_t22_page(text: str) -> bool:
    return (
        "<!-- t22-wujinsen-images:" in text
        or "/kb/raw/asset" in text
        or "](assets/" in text
    )


def check_ref(ref: str, wiki_file: Path, wiki_dir: Path) -> tuple[bool, str]:
    ref = ref.strip()
    if ref.startswith("http") and "/kb/raw/asset" in ref:
        parsed = urlparse(ref)
        qs = parse_qs(parsed.query)
        paths = qs.get("path", [])
        if not paths:
            return False, "raw/asset missing path query"
        p = RAW_ROOT / unquote(paths[0])
        return (p.is_file(), str(p))
    if ref.startswith("/KnowledgeServer/kb/raw/asset") or ref.startswith("/kb/raw/asset"):
        parsed = urlparse(ref if ref.startswith("http") else f"http://x{ref}")
        qs = parse_qs(parsed.query)
        paths = qs.get("path", [])
        if paths:
            p = RAW_ROOT / unquote(paths[0])
            return (p.is_file(), str(p))
        return False, ref
    if ref.startswith("assets/") or ref.startswith("./assets/"):
        rel = ref.replace("./", "")
        slug_stem = wiki_file.relative_to(wiki_dir).with_suffix("")
        asset_dir = wiki_dir / f"{slug_stem}.assets"
        target = asset_dir / Path(rel).name
        return (target.is_file(), str(target))
    return True, "external or relative (skipped)"


def scan_wiki(wiki_dir: Path, *, t22_only: bool) -> tuple[int, list[str], Counter]:
    errors: list[str] = []
    checked = 0
    kinds: Counter = Counter()
    for md in sorted(wiki_dir.rglob("*.md")):
        if md.name in ("index.md", "log.md"):
            continue
        text = md.read_text(encoding="utf-8", errors="ignore")
        if t22_only and not is_t22_page(text):
            continue
        for m in IMG_MD.finditer(text):
            ref = m.group(1).strip()
            if ref.startswith("assets/") or ref.startswith("./assets/"):
                kinds["wiki.assets"] += 1
            elif "/kb/raw/asset" in ref:
                kinds["raw.asset"] += 1
            elif ref.startswith("http"):
                kinds["external"] += 1
            else:
                kinds["other"] += 1
            ok, detail = check_ref(ref, md, wiki_dir)
            if ok and detail == "external or relative (skipped)":
                continue
            checked += 1
            if not ok:
                errors.append(f"{md.relative_to(wiki_dir).as_posix()}: {ref} -> {detail}")
    return checked, errors, kinds


def manifest_stats() -> dict:
    if not MANIFEST.exists():
        return {}
    rows = json.loads(MANIFEST.read_text(encoding="utf-8"))
    done = sum(1 for r in rows if r.get("status") == "done")
    pending = sum(1 for r in rows if r.get("status") != "done" and r.get("strategy") != "defer")
    defer = sum(1 for r in rows if r.get("strategy") == "defer")
    by_strategy = Counter(r.get("applied_strategy") or r.get("strategy") for r in rows if r.get("status") == "done")
    annex_pages = len(list(WIKI.rglob("annex-*.md")))
    return {
        "manifest_total": len(rows),
        "done": done,
        "pending_cited": pending,
        "defer": defer,
        "by_strategy": dict(by_strategy),
        "annex_pages": annex_pages,
    }


def write_report(wiki_dir: Path, checked: int, errors: list[str], kinds: Counter) -> None:
    stats = manifest_stats()
    lines = [
        "# wujinsen 图片回迁 R3 验收报告",
        "",
        f"> 生成：`verify_wujinsen_images.py --report` · {date.today().isoformat()}",
        "",
        "## 汇总",
        "",
        "| 指标 | 值 |",
        "|------|-----|",
        f"| wiki 插图引用校验 | {checked} 条 |",
        f"| 断链 | **{len(errors)}** |",
        f"| annex 页数 | {stats.get('annex_pages', '—')} |",
        f"| manifest 已完成 | {stats.get('done', '—')} / {stats.get('manifest_total', '—')} |",
        f"| manifest 待回迁（已 cite） | {stats.get('pending_cited', '—')} |",
        f"| defer（未 cite） | {stats.get('defer', '—')} |",
        "",
        "## 引用类型",
        "",
    ]
    for k, v in sorted(kinds.items()):
        lines.append(f"- `{k}`: {v}")
    lines.extend(["", "## 结论", ""])
    if errors:
        lines.append(f"- **FAIL**：{len(errors)} 处插图无法解析到磁盘")
        lines.extend(["", "### 样例（前 20）", ""])
        for e in errors[:20]:
            lines.append(f"- `{e}`")
    else:
        lines.append("- **PASS**：T22 插图引用均可解析（raw 文件或 wiki `.assets/`）")
    lines.append("")
    REPORT.write_text("\n".join(lines), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--wiki-dir", default=str(WIKI))
    parser.add_argument("--report", action="store_true", help="write WUJINSEN_R3_REPORT.md")
    parser.add_argument("--all", action="store_true", help="scan all pages with ![](, not only T22-tagged")
    args = parser.parse_args()
    wiki_dir = Path(args.wiki_dir)

    checked, errors, kinds = scan_wiki(wiki_dir, t22_only=not args.all)
    print(f"Checked {checked} image refs in {wiki_dir}")
    if args.report:
        write_report(wiki_dir, checked, errors, kinds)
        print(f"Report -> {REPORT}")

    if errors:
        print(f"FAIL {len(errors)} broken refs:")
        for e in errors[:50]:
            print(" ", e)
        if len(errors) > 50:
            print(f"  ... and {len(errors) - 50} more")
        return 1
    print("OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
