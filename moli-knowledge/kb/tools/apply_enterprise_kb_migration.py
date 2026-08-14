#!/usr/bin/env python3
"""
Apply enterprise-kb wiki directory migration (方案 B) via git mv.

Reads enterprise_kb_migration_draft.csv and moves:
  kb/wiki/{old_slug}.md  ->  kb/wiki/{new_slug}.md

Default is dry-run. Pass --execute to perform git mv.

Examples:
  cd moli-knowledge/kb/tools
  python apply_enterprise_kb_migration.py
  python apply_enterprise_kb_migration.py --execute
  python apply_enterprise_kb_migration.py --execute --only database,cache
"""
from __future__ import annotations

import argparse
import csv
import subprocess
import sys
from pathlib import Path

TOOLS = Path(__file__).resolve().parent
KB_ROOT = TOOLS.parent
WIKI_ROOT = KB_ROOT / "wiki"
DEFAULT_CSV = TOOLS / "enterprise_kb_migration_draft.csv"


def find_git_root(start: Path) -> Path | None:
    p = start.resolve()
    for _ in range(12):
        if (p / ".git").exists():
            return p
        if p.parent == p:
            break
        p = p.parent
    return None


def load_plan(csv_path: Path, only: set[str] | None) -> list[dict]:
    rows: list[dict] = []
    with csv_path.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            if only and row.get("new_dir_slug") not in only:
                continue
            old_slug = (row.get("old_slug") or "").strip()
            new_slug = (row.get("new_slug") or "").strip()
            if not old_slug or not new_slug:
                continue
            if old_slug == new_slug:
                continue
            rows.append(row)
    return rows


def rel_wiki_path(slug: str) -> Path:
    return Path("moli-knowledge") / "kb" / "wiki" / f"{slug}.md"


def run_git_mv(git_root: Path, src_rel: Path, dst_rel: Path, execute: bool) -> tuple[str, str]:
    src_abs = git_root / src_rel
    dst_abs = git_root / dst_rel
    if not src_abs.exists():
        return "skip", f"missing source: {src_rel}"
    if dst_abs.exists():
        return "skip", f"dest exists: {dst_rel}"
    dst_abs.parent.mkdir(parents=True, exist_ok=True)
    if not execute:
        return "plan", f"{src_rel} -> {dst_rel}"
    try:
        subprocess.run(
            ["git", "mv", str(src_abs), str(dst_abs)],
            cwd=str(git_root),
            check=True,
            capture_output=True,
            text=True,
        )
        return "ok", f"{src_rel} -> {dst_rel}"
    except subprocess.CalledProcessError as e:
        err = (e.stderr or e.stdout or str(e)).strip()
        return "fail", f"{src_rel} -> {dst_rel}: {err}"


def main() -> int:
    parser = argparse.ArgumentParser(description="Batch git mv enterprise-kb wiki per CSV")
    parser.add_argument(
        "--csv",
        type=Path,
        default=DEFAULT_CSV,
        help=f"Migration CSV (default: {DEFAULT_CSV.name})",
    )
    parser.add_argument(
        "--execute",
        action="store_true",
        help="Actually run git mv (default: dry-run only)",
    )
    parser.add_argument(
        "--only",
        type=str,
        default="",
        help="Comma-separated new_dir_slug filter, e.g. database,cache",
    )
    args = parser.parse_args()

    if not args.csv.is_file():
        print(f"[ERROR] CSV not found: {args.csv}", file=sys.stderr)
        return 1

    git_root = find_git_root(KB_ROOT)
    if git_root is None:
        print("[ERROR] Not inside a git repository (.git not found)", file=sys.stderr)
        return 1

    only = {s.strip() for s in args.only.split(",") if s.strip()} or None
    plan = load_plan(args.csv, only)
    if not plan:
        print("[WARN] No rows to move (check --only or CSV)")
        return 0

    mode = "EXECUTE" if args.execute else "DRY-RUN"
    print(f"[{mode}] git root: {git_root}")
    print(f"[{mode}] CSV: {args.csv}")
    print(f"[{mode}] moves: {len(plan)}")
    if only:
        print(f"[{mode}] filter: {sorted(only)}")
    print()

    stats = {"plan": 0, "ok": 0, "skip": 0, "fail": 0}
    for row in plan:
        src = rel_wiki_path(row["old_slug"])
        dst = rel_wiki_path(row["new_slug"])
        status, msg = run_git_mv(git_root, src, dst, args.execute)
        stats[status] = stats.get(status, 0) + 1
        prefix = {"plan": "PLAN", "ok": " OK ", "skip": "SKIP", "fail": "FAIL"}[status]
        print(f"  [{prefix}] {msg}")

    print()
    print(
        f"Summary: plan={stats.get('plan', 0)} ok={stats.get('ok', 0)} "
        f"skip={stats.get('skip', 0)} fail={stats.get('fail', 0)}"
    )

    if not args.execute:
        print()
        print("Dry-run only. Re-run with --execute to apply.")
        print("Suggested next steps after execute:")
        print("  cd moli-knowledge && python kb/tools/lint.py --strict")
        print("  cd moli-knowledge/kb && python tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb")
        return 0

    if stats.get("fail", 0) > 0:
        return 2
    print()
    print("Done. Next:")
    print("  cd moli-knowledge && python kb/tools/lint.py --strict")
    print("  cd moli-knowledge/kb && python tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
