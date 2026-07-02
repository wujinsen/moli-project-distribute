#!/usr/bin/env python3
"""
Rewrite wikilinks / paths after enterprise-kb directory migration.

Reads the same CSV as apply_enterprise_kb_migration.py and updates:
  - [[wikilink]] targets (full old slug -> new slug)
  - frontmatter related: entries
  - sources: file paths containing wiki/{old_slug}

Default dry-run. Use --execute to write files.

Examples:
  python fix_enterprise_kb_wikilinks.py
  python fix_enterprise_kb_wikilinks.py --execute
"""
from __future__ import annotations

import argparse
import csv
import re
import sys
from pathlib import Path

TOOLS = Path(__file__).resolve().parent
KB_ROOT = TOOLS.parent
WIKI_ROOT = KB_ROOT / "wiki"
DEFAULT_CSV = TOOLS / "enterprise_kb_migration_draft.csv"

WIKILINK_RE = re.compile(r"\[\[([^\]]+)\]\]")
OLD_DIRS = ("concepts", "articles", "interview")


def load_slug_map(csv_path: Path) -> dict[str, str]:
    mapping: dict[str, str] = {}
    with csv_path.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            old_slug = (row.get("old_slug") or "").strip()
            new_slug = (row.get("new_slug") or "").strip()
            if old_slug and new_slug and old_slug != new_slug:
                mapping[old_slug] = new_slug
    return mapping


def resolve_link(target: str, slug_map: dict[str, str]) -> str:
    t = target.strip()
    if not t:
        return target
    if t in slug_map:
        return slug_map[t]
    for prefix in OLD_DIRS:
        p = prefix + "/"
        if t.startswith(p):
            old = t
            if old in slug_map:
                return slug_map[old]
            stem = t[len(p) :]
            for old_slug, new_slug in slug_map.items():
                if old_slug.endswith("/" + stem) or old_slug.split("/")[-1] == stem:
                    return new_slug
    # bare stem: unique match by filename
    matches = [new for old, new in slug_map.items() if old.split("/")[-1] == t]
    if len(matches) == 1:
        return matches[0]
    return t


def replace_paths_in_text(text: str, slug_map: dict[str, str]) -> str:
    """Replace plain path occurrences (sources, prose), longest old slug first."""
    out = text
    for old_slug in sorted(slug_map.keys(), key=len, reverse=True):
        new_slug = slug_map[old_slug]
        patterns = [
            f"wiki/{old_slug}",
            f"kb/wiki/{old_slug}",
            f"moli-knowledge/kb/wiki/{old_slug}",
            old_slug,
        ]
        replacements = [
            f"wiki/{new_slug}",
            f"kb/wiki/{new_slug}",
            f"moli-knowledge/kb/wiki/{new_slug}",
            new_slug,
        ]
        for pat, rep in zip(patterns, replacements):
            if pat in out:
                out = out.replace(pat, rep)
    return out


def rewrite_content(text: str, slug_map: dict[str, str]) -> tuple[str, int]:
    changes = 0

    def repl_wikilink(m: re.Match[str]) -> str:
        nonlocal changes
        inner = m.group(1)
        new_inner = resolve_link(inner, slug_map)
        if new_inner != inner:
            changes += 1
        return f"[[{new_inner}]]"

    out = WIKILINK_RE.sub(repl_wikilink, text)
    new_out = replace_paths_in_text(out, slug_map)
    if new_out != out:
        changes += 1
        out = new_out
    return out, changes


def iter_wiki_md() -> list[Path]:
    if not WIKI_ROOT.is_dir():
        return []
    return sorted(WIKI_ROOT.rglob("*.md"))


def main() -> int:
    parser = argparse.ArgumentParser(description="Fix wikilinks after enterprise-kb migration")
    parser.add_argument("--csv", type=Path, default=DEFAULT_CSV)
    parser.add_argument("--execute", action="store_true")
    args = parser.parse_args()

    if not args.csv.is_file():
        print(f"[ERROR] CSV not found: {args.csv}", file=sys.stderr)
        return 1

    slug_map = load_slug_map(args.csv)
    if not slug_map:
        print("[WARN] Empty slug map")
        return 0

    mode = "EXECUTE" if args.execute else "DRY-RUN"
    files = iter_wiki_md()
    print(f"[{mode}] wiki md files: {len(files)}")
    print(f"[{mode}] slug mappings: {len(slug_map)}")
    print()

    touched = 0
    total_changes = 0
    for path in files:
        try:
            original = path.read_text(encoding="utf-8")
        except OSError as e:
            print(f"  [FAIL] read {path}: {e}")
            continue
        updated, n = rewrite_content(original, slug_map)
        if n <= 0 and updated == original:
            continue
        rel = path.relative_to(KB_ROOT)
        touched += 1
        total_changes += max(n, 1)
        if args.execute:
            path.write_text(updated, encoding="utf-8", newline="\n")
            print(f"  [ OK ] {rel} ({n} change(s))")
        else:
            print(f"  [PLAN] {rel} ({n} change(s))")

    print()
    print(f"Summary: files_touched={touched} change_ops≈{total_changes}")
    if not args.execute and touched:
        print("Dry-run only. Re-run with --execute to write.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
