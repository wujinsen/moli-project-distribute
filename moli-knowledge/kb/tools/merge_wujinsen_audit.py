#!/usr/bin/env python3
# -*- coding: utf-8
"""Merge fresh wujinsen audit into manifest, preserving done rows and reopening cited defer.

Usage:
  python moli-knowledge/kb/tools/merge_wujinsen_audit.py --dry-run
  python moli-knowledge/kb/tools/merge_wujinsen_audit.py --apply
"""
from __future__ import annotations

import argparse
import json
import sys
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
MANIFEST = HERE / "WUJINSEN_IMAGE_REMEDIATION.json"
LOG = HERE.parent / "wiki" / "log.md"
PLAN_MD = HERE / "WUJINSEN_DEFER_INGEST_PLAN.md"


def load_manifest() -> list[dict]:
    return json.loads(MANIFEST.read_text(encoding="utf-8"))


def save_manifest(rows: list[dict]) -> None:
    MANIFEST.write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")


def fresh_audit_rows() -> list[dict]:
    sys.path.insert(0, str(HERE))
    from audit_wujinsen_images import load_skip_deleted_raw, suggest_strategy, count_images, norm

    RAW = HERE.parent / "raw" / "wujinsen_markdown"
    from audit_wujinsen_images import load_wiki_citations

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
    return rows


def merge(old_rows: list[dict], fresh_rows: list[dict]) -> tuple[list[dict], dict]:
    old_by_path = {r["raw_path"]: r for r in old_rows}
    fresh_by_path = {r["raw_path"]: r for r in fresh_rows}
    stats = {
        "done_kept": 0,
        "reopened_defer": 0,
        "still_defer": 0,
        "new_pending": 0,
    }
    merged: list[dict] = []
    for raw_path, fresh in fresh_by_path.items():
        old = old_by_path.get(raw_path, {})
        row = dict(fresh)
        if old.get("status") == "done":
            row["status"] = "done"
            for k in ("annex_slug", "applied_strategy", "skip_reason"):
                if old.get(k):
                    row[k] = old[k]
            stats["done_kept"] += 1
        elif old.get("status") == "defer-closed" and fresh.get("cited_by"):
            row["status"] = "pending"
            row.pop("defer_reason", None)
            stats["reopened_defer"] += 1
        elif not fresh.get("cited_by") and old.get("status") == "defer-closed":
            row["status"] = "defer-closed"
            row["defer_reason"] = old.get(
                "defer_reason", "wiki 未 cite 该 raw；待 ingest 补 sources 后再回迁"
            )
            stats["still_defer"] += 1
        elif fresh.get("cited_by") and old.get("status") != "done":
            row["status"] = "pending"
            stats["new_pending"] += 1
        merged.append(row)
    return merged, stats


def write_ingest_plan(rows: list[dict]) -> None:
    """High-value defer-closed rows still without wiki cite."""
    defer = [r for r in rows if r.get("status") == "defer-closed"]
    defer.sort(key=lambda x: (-x.get("png_files", 0), x["raw_path"]))
    lines = [
        "# wujinsen defer 高价值 ingest 规划",
        "",
        f"> {date.today().isoformat()} · 修复 cite 正则后仍 **未 cite** 的 defer 行（按 png 降序）",
        "",
        "补 cite：在对应 hub 页 `sources` 追加 `raw/wujinsen_markdown/…` → `merge_wujinsen_audit.py --apply` → remediate。",
        "",
        f"## 仍 defer（{len(defer)} 条）",
        "",
        "| png | raw | 建议 hub |",
        "|-----|-----|----------|",
    ]
    # Heuristic hub suggestions by top-level dir
    hub_hint = {
        "BigData": "bigdata/hadoop-生态入门 或专题 hub",
        "DataBase": "database/mysql-* / cache/redis-*",
        "Spring": "patterns/spring-*",
        "jvm": "java/jvm-*",
        "架构": "middleware/* / patterns/*",
        "面试笔试": "对应 *-面试题 hub",
        "大数据资料-王": "bigdata/* / middleware/netty-*",
        "并发编程": "java/java-并发 / middleware/netty-*",
        "源码分析": "middleware/dubbo-* / patterns/*",
        "前端": "frontend/*",
        "javaweb": "patterns/*",
        "性能优化": "database/mysql-*",
        "数据结构与算法": "patterns/*",
    }
    for r in defer[:60]:
        rel = r["raw_path"].replace("raw/wujinsen_markdown/", "")
        top = rel.split("/", 1)[0]
        hint = hub_hint.get(top, "见 wiki index 同主题页")
        lines.append(f"| {r.get('png_files', 0)} | `{rel}` | {hint} |")
    if len(defer) > 60:
        lines.append(f"| … | 另有 {len(defer) - 60} 条 | JSON |")
    lines.append("")
    PLAN_MD.write_text("\n".join(lines), encoding="utf-8")


def append_log(stats: dict) -> None:
    if not LOG.exists():
        return
    line = (
        f"## [{date.today().isoformat()}] audit-merge | "
        f"reopened {stats['reopened_defer']}, still defer {stats['still_defer']}, "
        f"done kept {stats['done_kept']}"
    )
    LOG.write_text(LOG.read_text(encoding="utf-8").rstrip() + "\n" + line, encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    dry_run = not args.apply or args.dry_run

    old_rows = load_manifest()
    fresh_rows = fresh_audit_rows()
    merged, stats = merge(old_rows, fresh_rows)

    pending = [r for r in merged if r.get("status") == "pending" and r.get("cited_by")]
    print(
        f"merge: done_kept={stats['done_kept']} reopened_defer={stats['reopened_defer']} "
        f"still_defer={stats['still_defer']} pending_cited={len(pending)}"
        + (" (dry-run)" if dry_run else "")
    )

    if not dry_run:
        save_manifest(merged)
        write_ingest_plan(merged)
        append_log(stats)
        subprocess_regen = HERE / "process_wujinsen_tail.py"
        import subprocess

        subprocess.run([sys.executable, str(subprocess_regen), "--regen-reports"], check=False)
    else:
        write_ingest_plan(merged)

    return 0


if __name__ == "__main__":
    sys.exit(main())
