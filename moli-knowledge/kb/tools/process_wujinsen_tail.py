#!/usr/bin/env python3
# -*- coding: utf-8
"""T22 收尾：回迁 .note.attach 脏数据 + 归档 defer 未 cite raw。

Usage:
  python moli-knowledge/kb/tools/process_wujinsen_tail.py --dry-run
  python moli-knowledge/kb/tools/process_wujinsen_tail.py --apply
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from collections import Counter, defaultdict
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
MANIFEST = HERE / "WUJINSEN_IMAGE_REMEDIATION.json"
DEFER_MD = HERE / "WUJINSEN_DEFER_INVENTORY.md"
ATTACH_MD = HERE / "WUJINSEN_ATTACH_RESOLVED.md"
LOG = HERE.parent / "wiki" / "log.md"

DEFAULT_SPACE_ID = "900000000000000001"
GATEWAY = "/KnowledgeServer"


def load_manifest() -> list[dict]:
    return json.loads(MANIFEST.read_text(encoding="utf-8"))


def save_manifest(rows: list[dict]) -> None:
    MANIFEST.write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")


def append_log(line: str) -> None:
    if LOG.exists():
        LOG.write_text(LOG.read_text(encoding="utf-8").rstrip() + "\n" + line, encoding="utf-8")


def attach_rows(rows: list[dict]) -> list[dict]:
    return [
        r
        for r in rows
        if r.get("status") != "done"
        and ".attach" in r.get("raw_path", "")
        and r.get("cited_by")
    ]


def defer_rows(rows: list[dict]) -> list[dict]:
    return [r for r in rows if r.get("strategy") == "defer" and r.get("status") in ("pending", None)]


def remediate_attach(rows: list[dict], dry_run: bool) -> list[dict]:
    sys.path.insert(0, str(HERE))
    from remediate_wujinsen_images import apply_strategy_a, apply_strategy_bd

    results: list[dict] = []
    for row in attach_rows(rows):
        use = row.get("strategy", "A")
        if use == "C-or-A":
            use = "A"
        if use == "B":
            res = apply_strategy_bd(row, "B", DEFAULT_SPACE_ID, GATEWAY, dry_run, single_hub=False)
        else:
            res = apply_strategy_a(row, DEFAULT_SPACE_ID, GATEWAY, dry_run)
        results.append(res)
        if res.get("ok") and not dry_run:
            row["status"] = "done"
            row["applied_strategy"] = use
            if res.get("annex_slug"):
                row["annex_slug"] = res["annex_slug"]
        elif not res.get("ok") and not dry_run:
            row["status"] = "skip-no-images"
            row["skip_reason"] = res.get("error", "unknown")
    return results


def attach_done_rows(rows: list[dict]) -> list[dict]:
    return [
        r
        for r in rows
        if ".attach" in r.get("raw_path", "")
        and r.get("status") == "done"
        and r.get("cited_by")
    ]


def defer_closed_rows(rows: list[dict]) -> list[dict]:
    return [r for r in rows if r.get("strategy") == "defer" and r.get("status") == "defer-closed"]


def regen_reports_from_manifest(rows: list[dict]) -> None:
    """Rebuild attach/defer markdown from manifest (idempotent after --apply)."""
    attach = attach_done_rows(rows)
    attach_results = [
        {
            "ok": True,
            "annex_slug": r.get("annex_slug"),
            "error": r.get("skip_reason"),
        }
        for r in attach
    ]
    write_attach_report(attach, attach_results)

    defer = defer_closed_rows(rows)
    by_top: dict[str, list[dict]] = defaultdict(list)
    for r in defer:
        rel = r["raw_path"].split("wujinsen_markdown/", 1)[-1]
        top = rel.split("/", 1)[0]
        by_top[top].append(r)
    write_defer_inventory(defer, by_top)


def write_attach_report(attach: list[dict], results: list[dict]) -> None:
    lines = [
        "# wujinsen `.note.attach` 回迁记录",
        "",
        f"> {date.today().isoformat()} · `process_wujinsen_tail.py`",
        "",
        "| raw | 策略 | 结果 | annex / 说明 |",
        "|-----|------|------|--------------|",
    ]
    for row, res in zip(attach, results):
        ok = "OK" if res.get("ok") else f"FAIL: {res.get('error', '?')}"
        applied = row.get("applied_strategy") or row.get("strategy")
        annex = res.get("annex_slug") or row.get("annex_slug")
        if annex:
            detail = annex
        elif applied == "B":
            detail = "hub 插图节（strategy B）"
        else:
            detail = "—"
        lines.append(
            f"| `{row['raw_path']}` | {applied} | {ok} | {detail} |"
        )
    lines.append("")
    ATTACH_MD.write_text("\n".join(lines), encoding="utf-8")


def close_defer(rows: list[dict], dry_run: bool) -> int:
    defer = defer_rows(rows)
    by_top: dict[str, list[dict]] = defaultdict(list)
    for r in defer:
        rel = r["raw_path"].split("wujinsen_markdown/", 1)[-1]
        top = rel.split("/", 1)[0]
        by_top[top].append(r)
        if not dry_run:
            r["status"] = "defer-closed"
            r["defer_reason"] = "wiki 未 cite 该 raw；待 ingest 补 sources 后再回迁"
    write_defer_inventory(defer, by_top)
    return len(defer)


def write_defer_inventory(defer: list[dict], by_top: dict[str, list[dict]]) -> None:
    total_png = sum(r.get("png_files", 0) for r in defer)
    lines = [
        "# wujinsen 插图 defer 清单（未 cite · 暂不回迁）",
        "",
        f"> {date.today().isoformat()} · 共 **{len(defer)}** 条 raw · **{total_png}** png",
        "",
        "策略：wiki `sources` 未引用该 raw，**不**自动建 annex / 插图节。后续若 ingest 补 cite，可重新跑 `audit_wujinsen_images.py` + `remediate_wujinsen_images.py`。",
        "",
        "## 按 raw 顶层目录",
        "",
    ]
    for top in sorted(by_top.keys()):
        group = by_top[top]
        png = sum(r.get("png_files", 0) for r in group)
        lines.append(f"### `{top}`（{len(group)} 条 · {png} png）")
        lines.append("")
        lines.append("| raw | ref/png | 说明 |")
        lines.append("|-----|---------|------|")
        for r in sorted(group, key=lambda x: -x.get("png_files", 0))[:40]:
            rel = r["raw_path"].replace("raw/wujinsen_markdown/", "")
            lines.append(
                f"| `{rel}` | {r.get('image_refs', 0)}/{r.get('png_files', 0)} | defer-closed |"
            )
        if len(group) > 40:
            lines.append(f"| … | | 另有 {len(group) - 40} 条见 JSON |")
        lines.append("")
    lines.append("## 全量")
    lines.append("")
    lines.append("完整字段见 `WUJINSEN_IMAGE_REMEDIATION.json` 中 `strategy=defer` + `status=defer-closed`。")
    lines.append("")
    DEFER_MD.write_text("\n".join(lines), encoding="utf-8")


def refresh_manifest_stats(rows: list[dict]) -> None:
    """Re-count png for attach rows using fixed audit helpers."""
    sys.path.insert(0, str(HERE))
    from audit_wujinsen_images import count_images

    RAW = HERE.parent / "raw" / "wujinsen_markdown"
    for r in rows:
        if ".attach" not in r.get("raw_path", ""):
            continue
        rel = r["raw_path"].split("wujinsen_markdown/", 1)[-1]
        md = RAW / rel
        if md.is_file():
            refs, png = count_images(md)
            r["image_refs"] = refs
            r["png_files"] = png


def regenerate_audit_md() -> None:
    subprocess.run([sys.executable, str(HERE / "audit_wujinsen_images.py")], check=False)


def merge_audit_json_preserving_status(rows: list[dict]) -> None:
    """Refresh defer/attach stats from disk without wiping done status."""
    refresh_manifest_stats(rows)
    save_manifest(rows)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument(
        "--regen-reports",
        action="store_true",
        help="Rebuild WUJINSEN_ATTACH_RESOLVED.md + WUJINSEN_DEFER_INVENTORY.md from manifest",
    )
    args = parser.parse_args()

    if not MANIFEST.exists():
        print("Run audit_wujinsen_images.py --json first", file=sys.stderr)
        return 1

    rows = load_manifest()

    if args.regen_reports:
        regen_reports_from_manifest(rows)
        attach_n = len(attach_done_rows(rows))
        defer_n = len(defer_closed_rows(rows))
        print(f"Regenerated reports: attach {attach_n}, defer-closed {defer_n}")
        return 0

    dry_run = not args.apply or args.dry_run
    merge_audit_json_preserving_status(rows)

    attach = attach_rows(rows)
    print(f"Attach pending: {len(attach)}")
    results = remediate_attach(rows, dry_run)
    ok_attach = sum(1 for r in results if r.get("ok"))
    print(f"Attach remediate: {ok_attach}/{len(results)} {'(dry-run)' if dry_run else ''}")

    defer_n = close_defer(rows, dry_run)
    print(f"Defer closed: {defer_n} {'(dry-run)' if dry_run else ''}")

    if not dry_run:
        save_manifest(rows)
        write_attach_report(attach, results)
        append_log(
            f"## [{date.today().isoformat()}] remediate | T22 tail attach {ok_attach}/{len(attach)}, defer-closed {defer_n}"
        )
        regenerate_audit_md()

    return 0 if ok_attach == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
