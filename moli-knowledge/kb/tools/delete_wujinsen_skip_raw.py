#!/usr/bin/env python3
"""Physically delete wujinsen skip raw per Phase 3 manifest (#1331)."""
from __future__ import annotations

import os
import shutil
from pathlib import Path

from gen_phase3_wujinsen_plan import RAW, classify, load_cited, norm
from wujinsen_ingest_lib import append_log

TODAY = "2026-07-05"
BATCH = "#1331"
DELETED_LOG = Path(__file__).resolve().parent / "WUJINSEN_SKIP_DELETED.md"
MANIFEST = Path(__file__).resolve().parent / "WUJINSEN_SKIP_MANIFEST.md"


def companion_paths(md_path: Path) -> list[Path]:
    """Remove sidecar asset dirs for .note.md files."""
    out: list[Path] = []
    name = md_path.name
    parent = md_path.parent
    if name.endswith(".note.md"):
        stem = name[: -len(".note.md")]
        for suffix in (".note_images", "_note_images", ".note.attach"):
            p = parent / f"{stem}{suffix}"
            if p.exists():
                out.append(p)
        # Some notes use stem.note_images without stripping .note
        alt = parent / f"{name}.note_images"
        if alt.exists():
            out.append(alt)
    return out


def prune_empty_dirs(root: Path) -> int:
    removed = 0
    for dp, dirs, files in os.walk(root, topdown=False):
        p = Path(dp)
        if p == root:
            continue
        if not any(p.iterdir()):
            p.rmdir()
            removed += 1
    return removed


def main() -> None:
    cited = load_cited()
    raw_all = sorted(norm(os.path.relpath(str(f), str(RAW))) for f in RAW.rglob("*.md"))
    uncited = [r for r in raw_all if r not in cited]

    to_delete: list[str] = []
    skipped_other: list[tuple[str, str]] = []
    for rel in uncited:
        info = classify(rel)
        if info["action"] == "skip":
            to_delete.append(rel)
        else:
            skipped_other.append((rel, info["action"]))

    if skipped_other:
        print("WARN: uncited but not skip (will NOT delete):")
        for rel, act in skipped_other[:20]:
            print(" ", act, rel)
        if len(skipped_other) > 20:
            print(" ... +", len(skipped_other) - 20)

    deleted_md: list[str] = []
    deleted_assets: list[str] = []
    missing: list[str] = []

    for rel in sorted(to_delete):
        path = RAW / rel.replace("/", os.sep)
        if not path.is_file():
            missing.append(rel)
            continue
        for asset in companion_paths(path):
            rel_a = norm(os.path.relpath(str(asset), str(RAW)))
            shutil.rmtree(asset) if asset.is_dir() else asset.unlink()
            deleted_assets.append(rel_a)
        path.unlink()
        deleted_md.append(rel)

    dirs_pruned = prune_empty_dirs(RAW)

    lines = [
        "# wujinsen skip raw · 已物理删除",
        "",
        f"> {TODAY} · 批次 {BATCH} · 按 `WUJINSEN_SKIP_MANIFEST.md` 定案",
        f"> 删除 **{len(deleted_md)}** 篇 `.md` · 附属目录/文件 **{len(deleted_assets)}** · 空目录 **{dirs_pruned}**",
        "",
    ]
    if missing:
        lines.append(f"> 缺失（已删或未找到）：**{len(missing)}**")
        lines.append("")
    lines.append("## 删除清单")
    lines.append("")
    for rel in deleted_md:
        lines.append(f"- `{rel}`")
    DELETED_LOG.write_text("\n".join(lines) + "\n", encoding="utf-8")

    if MANIFEST.exists():
        text = MANIFEST.read_text(encoding="utf-8")
        text = text.replace(
            "raw 文件仍保留 unless 另行删除",
            f"**已于 {TODAY} {BATCH} 物理删除**（见 `WUJINSEN_SKIP_DELETED.md`）",
        )
        MANIFEST.write_text(text, encoding="utf-8")

    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} wujinsen skip raw 物理删 → md {len(deleted_md)} + assets {len(deleted_assets)} + prune dirs {dirs_pruned}",
    )

    raw_left = len(list(RAW.rglob("*.md")))
    print("planned skip delete", len(to_delete))
    print("deleted md", len(deleted_md), "assets", len(deleted_assets), "missing", len(missing))
    print("dirs pruned", dirs_pruned, "raw md left", raw_left)
    print("Wrote", DELETED_LOG.name)


if __name__ == "__main__":
    main()
