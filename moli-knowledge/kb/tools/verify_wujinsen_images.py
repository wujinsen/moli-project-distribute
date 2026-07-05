#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""T22 R3: verify wiki ![]( refs resolve to disk or raw/asset paths.

Usage:
  python moli-knowledge/kb/tools/verify_wujinsen_images.py
  python moli-knowledge/kb/tools/verify_wujinsen_images.py --wiki-dir moli-knowledge/kb/wiki
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
from urllib.parse import parse_qs, urlparse

HERE = Path(__file__).resolve().parent
KB = HERE.parent
RAW_ROOT = KB / "raw"
WIKI = KB / "wiki"

IMG_MD = re.compile(r"!\[[^\]]*\]\(([^)]+)\)")
RAW_ASSET = re.compile(r"/kb/raw/asset\?.*")


def check_ref(ref: str, wiki_file: Path, wiki_dir: Path) -> tuple[bool, str]:
    ref = ref.strip()
    if ref.startswith("http") and "/kb/raw/asset" in ref:
        parsed = urlparse(ref)
        qs = parse_qs(parsed.query)
        paths = qs.get("path") or qs.get("path", [])
        if not paths:
            return False, "raw/asset missing path query"
        p = RAW_ROOT / paths[0]
        return (p.is_file(), str(p))
    if ref.startswith("/KnowledgeServer/kb/raw/asset") or ref.startswith("/kb/raw/asset"):
        parsed = urlparse(ref if ref.startswith("http") else f"http://x{ref}")
        qs = parse_qs(parsed.query)
        paths = qs.get("path", [])
        if paths:
            p = RAW_ROOT / paths[0]
            return (p.is_file(), str(p))
        return False, ref
    if ref.startswith("assets/") or ref.startswith("./assets/"):
        rel = ref.replace("./", "")
        slug_stem = wiki_file.relative_to(wiki_dir).with_suffix("")
        asset_dir = wiki_dir / f"{slug_stem}.assets"
        target = asset_dir / Path(rel).name
        return (target.is_file(), str(target))
    return True, "external or relative (skipped)"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--wiki-dir", default=str(WIKI))
    args = parser.parse_args()
    wiki_dir = Path(args.wiki_dir)
    errors: list[str] = []
    checked = 0
    for md in sorted(wiki_dir.rglob("*.md")):
        if md.name in ("index.md", "log.md"):
            continue
        text = md.read_text(encoding="utf-8", errors="ignore")
        if "<!-- t22-wujinsen-images:" not in text and "/kb/raw/asset" not in text and "](assets/" not in text:
            continue
        for m in IMG_MD.finditer(text):
            ref = m.group(1)
            ok, detail = check_ref(ref, md, wiki_dir)
            checked += 1
            if not ok:
                errors.append(f"{md.relative_to(wiki_dir)}: {ref} -> {detail}")
    print(f"Checked {checked} image refs in {wiki_dir}")
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
