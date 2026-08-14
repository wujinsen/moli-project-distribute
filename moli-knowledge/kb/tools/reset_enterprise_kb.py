#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""重置 enterprise-kb（kb/wiki）：删除批量空壳页，保留有实质内容的页。

空壳特征：`_gen_batches_287_1286.py` 模板（「核心概念与常见误区」+「批次 **#」）。

用法：
  python reset_enterprise_kb.py --dry-run
  python reset_enterprise_kb.py
  python reset_enterprise_kb.py --archive   # 删除前先备份到 kb/.archive/
"""
from __future__ import annotations

import argparse
import json
import shutil
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
WIKI = KB / "wiki"
ARCHIVE = KB / ".archive" / f"wiki-purge-{date.today().isoformat()}"
CORPUS = ("articles", "concepts", "interview")
SKIP = {"index.md", "log.md"}
BATCH_PROGRESS = HERE / ".batch_287_1286_progress.json"


def is_batch_skeleton(text: str) -> bool:
    return "核心概念与常见误区" in text and "批次 **#" in text


def is_quality(text: str) -> bool:
    if is_batch_skeleton(text):
        return False
    if text.count("\n## ") >= 2 and len(text) > 500:
        return True
    if "```" in text and len(text) > 400:
        return True
    return False


def write_index(kept: dict[str, list[str]]) -> None:
    lines = [
        "# 企业知识库（enterprise-kb）",
        "",
        "> **通用技术文库**（articles / concepts / interview）。",
        "> **项目手册**见 `moli-ops-manual` · `kb/wiki-moli/`。",
        "",
        "本库已清除 `_gen_batches_287_1286` 批量空壳页；正文来自 raw 提炼 ingest。",
        "",
        "## 规模",
        "",
        f"| 目录 | 保留页数 |",
        f"|------|----------|",
    ]
    total = 0
    for sub in CORPUS:
        n = len(kept.get(sub, []))
        total += n
        lines.append(f"| `{sub}/` | {n} |")
    lines.append(f"| **合计** | **{total}** |")
    lines.append("")
    lines.append("## 同步")
    lines.append("")
    lines.append("```bash")
    lines.append("bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all")
    lines.append("```")
    lines.append("")
    (WIKI / "index.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


def append_log(deleted: int, kept: int) -> None:
    log = WIKI / "log.md"
    if not log.exists():
        log.write_text("# wiki 变更日志\n", encoding="utf-8")
    line = (
        f"\n## [{date.today().isoformat()}] reset | "
        f"purge batch skeletons deleted={deleted} kept={kept}\n"
    )
    with log.open("a", encoding="utf-8") as f:
        f.write(line)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--archive", action="store_true", help="删除前备份到 kb/.archive/")
    args = ap.parse_args()

    deleted = 0
    kept: dict[str, list[str]] = {s: [] for s in CORPUS}

    for sub in CORPUS:
        root = WIKI / sub
        if not root.is_dir():
            continue
        for md in sorted(root.glob("*.md")):
            text = md.read_text(encoding="utf-8")
            rel = f"{sub}/{md.name}"
            if is_quality(text):
                kept[sub].append(md.stem)
                continue
            deleted += 1
            if args.dry_run:
                print(f"DELETE {rel}")
                continue
            if args.archive:
                tgt = ARCHIVE / rel
                tgt.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(md, tgt)
            md.unlink()

    total_kept = sum(len(v) for v in kept.values())

    if not args.dry_run:
        # 清 graph（边大量指向已删页）
        graph = WIKI / "graph" / "edges.jsonl"
        if graph.exists():
            if args.archive:
                shutil.copy2(graph, ARCHIVE / "graph-edges.jsonl.bak")
            graph.write_text("", encoding="utf-8")
        write_index(kept)
        append_log(deleted, total_kept)
        if BATCH_PROGRESS.exists():
            if args.archive:
                shutil.copy2(BATCH_PROGRESS, ARCHIVE / BATCH_PROGRESS.name)
            BATCH_PROGRESS.unlink()

    print(
        f"{'[dry-run] ' if args.dry_run else ''}"
        f"deleted={deleted} kept={total_kept} "
        f"(articles={len(kept['articles'])} concepts={len(kept['concepts'])} "
        f"interview={len(kept['interview'])})"
    )


if __name__ == "__main__":
    main()
