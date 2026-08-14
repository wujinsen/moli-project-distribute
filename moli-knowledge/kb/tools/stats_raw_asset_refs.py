#!/usr/bin/env python3
"""Stats /kb/raw/asset references in wiki/ and minimum deployable asset bundle size.

Usage:
    python kb/tools/stats_raw_asset_refs.py
    python kb/tools/stats_raw_asset_refs.py --manifest-out paths.txt

paths.txt lines are relative to ``kb/raw/`` (for ``rsync --files-from`` / ``tar -C raw -T``).
"""
from __future__ import annotations

import argparse
import re
import sys
import urllib.parse
from collections import defaultdict
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
WIKI = KB / "wiki"
RAW = KB / "raw"

PATTERNS = (
    re.compile(r"/kb/raw/asset\?[^)\s\"']*?path=([^)&\s\"']+)", re.I),
    re.compile(r"/KnowledgeServer/kb/raw/asset\?[^)\s\"']*?path=([^)&\s\"']+)", re.I),
)


def collect_raw_asset_paths(wiki_dir: Path) -> tuple[set[str], set[str], int]:
    paths: set[str] = set()
    pages_with_raw: set[str] = set()
    line_hits = 0

    for md in wiki_dir.rglob("*.md"):
        if md.name in ("index.md", "log.md"):
            continue
        text = md.read_text(encoding="utf-8", errors="replace")
        slug = str(md.relative_to(wiki_dir).with_suffix(""))
        found: set[str] = set()
        for line in text.splitlines():
            if "/kb/raw/asset" in line or "/KnowledgeServer/kb/raw/asset" in line:
                line_hits += 1
        for pat in PATTERNS:
            for m in pat.finditer(text):
                p = urllib.parse.unquote(m.group(1))
                paths.add(p)
                found.add(p)
        if found:
            pages_with_raw.add(slug)

    return paths, pages_with_raw, line_hits


def resolve_files(paths: set[str]) -> tuple[list[tuple[str, int]], list[str], set[str], dict[str, int], int]:
    missing: list[str] = []
    found_files: list[tuple[str, int]] = []
    dirs_needed: set[str] = set()
    by_ext: dict[str, int] = defaultdict(int)
    total_bytes = 0

    for rel in sorted(paths):
        fp = RAW / rel
        if fp.is_file():
            sz = fp.stat().st_size
            found_files.append((rel, sz))
            total_bytes += sz
            by_ext[fp.suffix.lower()] += sz
            dirs_needed.add(str(Path(rel).parent))
        else:
            missing.append(rel)

    return found_files, missing, dirs_needed, by_ext, total_bytes


def write_manifest(out_path: Path, found_files: list[tuple[str, int]]) -> None:
    lines = [rel.replace("\\", "/") for rel, _ in found_files]
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text("\n".join(lines) + ("\n" if lines else ""), encoding="utf-8")


def main() -> int:
    ap = argparse.ArgumentParser(description="Count /kb/raw/asset refs and minimal deploy bundle size")
    ap.add_argument(
        "--wiki-dir",
        default="wiki",
        help="wiki subtree under kb/ (default: wiki = enterprise-kb)",
    )
    ap.add_argument(
        "--manifest-out",
        metavar="PATH",
        help="write deployable raw paths (relative to kb/raw/) one per line, for rsync --files-from",
    )
    args = ap.parse_args()

    wiki_dir = KB / args.wiki_dir
    if not wiki_dir.is_dir():
        print(f"[error] wiki dir not found: {wiki_dir}", file=sys.stderr)
        return 1

    paths, pages_with_raw, line_hits = collect_raw_asset_paths(wiki_dir)
    found_files, missing, dirs_needed, by_ext, total_bytes = resolve_files(paths)

    print(f"=== /kb/raw/asset stats ({args.wiki_dir}/) ===")
    print(f"wiki pages with raw/asset: {len(pages_with_raw)}")
    print(f"markdown lines with raw/asset: {line_hits}")
    print(f"unique path= targets: {len(paths)}")
    print(f"files exist on disk: {len(found_files)}")
    print(f"files missing on disk: {len(missing)}")
    print(f"note_images dirs involved: {len(dirs_needed)}")
    print()
    print("=== minimum asset bundle (referenced files that exist) ===")
    mib = total_bytes / 1024 / 1024
    print(f"total: {total_bytes:,} bytes ({mib:.2f} MiB)")
    for ext, sz in sorted(by_ext.items(), key=lambda x: -x[1]):
        print(f"  {ext or '(none)'}: {sz / 1024 / 1024:.2f} MiB")
    print()
    if missing:
        print(f"missing samples ({min(15, len(missing))} of {len(missing)}):")
        for m in missing[:15]:
            print(f"  - {m}")
    print()
    print("largest files (top 10):")
    for rel, sz in sorted(found_files, key=lambda x: -x[1])[:10]:
        print(f"  {sz / 1024 / 1024:.2f} MiB  {rel}")

    wujinsen = RAW / "wujinsen_markdown"
    if wujinsen.is_dir():
        full_bytes = 0
        full_files = 0
        for fp in wujinsen.rglob("*"):
            if fp.is_file():
                full_bytes += fp.stat().st_size
                full_files += 1
        print()
        print("=== contrast: full kb/raw/wujinsen_markdown/ ===")
        print(f"all files: {full_files:,} ({full_bytes / 1024 / 1024 / 1024:.2f} GiB)")
        if full_bytes:
            pct = 100.0 * total_bytes / full_bytes
            print(f"minimal bundle vs full wujinsen: {pct:.1f}% of size")

    if args.manifest_out:
        out_path = Path(args.manifest_out)
        if not out_path.is_absolute():
            out_path = Path.cwd() / out_path
        write_manifest(out_path, found_files)
        print()
        print(f"[manifest] wrote {len(found_files)} paths -> {out_path}")
        print("  rsync:  cd moli-knowledge/kb && rsync -av --files-from=paths.txt raw/ user@host:.../kb/raw/")
        print("  tar:    cd moli-knowledge/kb/raw && tar -czf ../kb-raw-assets-min.tar.gz -T ../paths.txt")

    return 0


if __name__ == "__main__":
    sys.exit(main())
