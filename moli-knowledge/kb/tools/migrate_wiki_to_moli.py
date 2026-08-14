#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""将 enterprise-kb 源 wiki/ 中的茉莉项目文档迁入 wiki-moli/，并修 wikilink。

映射（分类=一级目录）：
  guides/*     → guides/ | product/ | develop/ | test/（按文件名规则）
  services/*   → develop/*（wiki-moli 已有则删 wiki 副本）
  concepts/*   → develop/concepts/*（develop/{stem} 已存在则删副本）
  articles/*   → develop/articles/*
  interview/*  → test/interview/*
  outputs/*    → develop/outputs/*

用法：
  python migrate_wiki_to_moli.py --dry-run
  python migrate_wiki_to_moli.py --batch guides,services,concepts,outputs
  python migrate_wiki_to_moli.py --batch articles,interview
  python migrate_wiki_to_moli.py --finalize   # stub wiki/index + 全局链修
"""
from __future__ import annotations

import argparse
import json
import re
import shutil
from datetime import date
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
WIKI = KB / "wiki"
WIKI_MOLI = KB / "wiki-moli"
SKIP_NAMES = {"index.md", "log.md"}

BATCH_DIRS = {
    "guides": ["guides"],
    "services": ["services"],
    "concepts": ["concepts"],
    "articles": ["articles"],
    "interview": ["interview"],
    "outputs": ["outputs"],
}

# 全局 slug 前缀（finalize 链修用，最长优先）
GLOBAL_SLUG_PREFIXES: list[tuple[str, str]] = [
    ("articles/", "develop/articles/"),
    ("interview/", "test/interview/"),
    ("concepts/", "develop/concepts/"),
    ("outputs/", "develop/outputs/"),
    ("services/", "develop/"),
]


def global_slug_pairs() -> list[tuple[str, str]]:
    """已迁目录的前缀映射 + guides 特例（产品/develop/test）。"""
    pairs = list(GLOBAL_SLUG_PREFIXES)
    guide_rules = [
        ("guides/订单秒杀产品说明", "product/订单秒杀产品说明"),
        ("guides/网关产品说明", "product/网关产品说明"),
        ("guides/知识库模块产品说明", "product/知识库模块产品说明"),
        ("guides/用户中心产品说明", "product/用户中心产品说明"),
        ("guides/BI服务产品说明", "product/BI服务产品说明"),
        ("guides/Ingest工作台产品方案", "develop/Ingest工作台产品方案"),
        ("guides/Wiki治理工作台产品方案", "develop/Wiki治理工作台产品方案"),
        ("guides/Wiki在线编辑与AI协助改稿", "develop/Wiki在线编辑与AI协助改稿"),
        ("guides/AI自我进化与MD审校流程", "develop/AI自我进化与MD审校流程"),
        ("guides/增量ingest与raw投喂指南", "develop/增量ingest与raw投喂指南"),
        ("guides/秒杀压测指南", "test/秒杀压测指南"),
        ("guides/压测报告解读指南", "test/压测报告解读指南"),
    ]
    pairs.extend(guide_rules)
    pairs.sort(key=lambda x: len(x[0]), reverse=True)
    return pairs


def guide_target(name: str) -> str | None:
    if name == "系统操作手册入口.md":
        return None
    if "产品说明" in name:
        return f"product/{name}"
    for key in ("Ingest", "Wiki治理", "Wiki在线", "AI自我进化", "增量ingest"):
        if key in name:
            return f"develop/{name}"
    for key in ("秒杀压测", "压测报告"):
        if key in name:
            return f"test/{name}"
    return f"guides/{name}"


def map_source_rel(rel: str) -> tuple[str | None, str]:
    """返回 (target_rel, action) action: move | skip_delete"""
    parts = rel.replace("\\", "/").split("/")
    top = parts[0]
    name = parts[-1]

    if top == "guides":
        tgt = guide_target(name)
        if tgt is None:
            return None, "skip_delete"
        return tgt, "move"

    if top == "services":
        stem = Path(name).stem
        flat = f"develop/{name}"
        if (WIKI_MOLI / flat).exists():
            return None, "skip_delete"
        return flat, "move"

    if top == "concepts":
        stem = Path(name).stem
        if (WIKI_MOLI / "develop" / name).exists():
            return None, "skip_delete"
        rest = "/".join(parts[1:])
        return f"develop/concepts/{rest}", "move"

    if top == "articles":
        rest = "/".join(parts[1:])
        return f"develop/articles/{rest}", "move"

    if top == "interview":
        rest = "/".join(parts[1:])
        return f"test/interview/{rest}", "move"

    if top == "outputs":
        rest = "/".join(parts[1:])
        return f"develop/outputs/{rest}", "move"

    raise ValueError(f"unknown top dir: {top}")


def collect_moves(batch_dirs: list[str]) -> list[tuple[Path, Path, str]]:
    moves: list[tuple[Path, Path, str]] = []
    tops: set[str] = set()
    for b in batch_dirs:
        tops.update(BATCH_DIRS.get(b, [b]))

    for top in sorted(tops):
        src_dir = WIKI / top
        if not src_dir.is_dir():
            continue
        for src in sorted(src_dir.rglob("*.md")):
            if src.name in SKIP_NAMES:
                continue
            rel = str(src.relative_to(WIKI)).replace("\\", "/")
            tgt_rel, action = map_source_rel(rel)
            if action == "skip_delete":
                moves.append((src, src, "delete"))
            elif tgt_rel:
                moves.append((src, WIKI_MOLI / tgt_rel, "move"))
    return moves


def slug_from_wiki_path(p: Path, root: Path) -> str:
    rel = str(p.relative_to(root).with_suffix("")).replace("\\", "/")
    return rel


def build_slug_map(moves: list[tuple[Path, Path, str]]) -> list[tuple[str, str]]:
    pairs: list[tuple[str, str]] = []
    for src, dst, action in moves:
        if action != "move":
            continue
        old = slug_from_wiki_path(src, WIKI)
        new = slug_from_wiki_path(dst, WIKI_MOLI)
        if old != new:
            pairs.append((old, new))
    pairs.sort(key=lambda x: len(x[0]), reverse=True)
    return pairs


def rewrite_text(text: str, slug_pairs: list[tuple[str, str]]) -> str:
    for old, new in slug_pairs:
        text = text.replace(f"kb/wiki/{old}", f"kb/wiki-moli/{new}")
        text = text.replace(f"wiki/{old}", f"wiki-moli/{new}")
        text = text.replace(f"[[{old}]]", f"[[{new}]]")
        text = text.replace(f"[[{old}|", f"[[{new}|")
        # legacy 裸路径前缀（index 里常见）
        if "/" in old:
            old_stem = old.split("/")[-1]
            new_stem = new.split("/")[-1]
            if old_stem == new_stem:
                text = re.sub(
                    rf"\]\({re.escape(old)}\)",
                    f"]({new})",
                    text,
                )
    text = text.replace("kb/wiki/", "kb/wiki-moli/")
    text = text.replace("moli-knowledge/kb/wiki/", "moli-knowledge/kb/wiki-moli/")
    for old_prefix in ("wiki/guides/", "wiki/services/", "wiki/concepts/", "wiki/articles/", "wiki/interview/", "wiki/outputs/"):
        text = text.replace(old_prefix, old_prefix.replace("wiki/", "wiki-moli/", 1))
    text = text.replace("enterprise-kb 内", "wiki-moli（茉莉系统手册）")
    return text


def patch_file(path: Path, slug_pairs: list[tuple[str, str]]) -> bool:
    try:
        original = path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return False
    fixed = rewrite_text(original, slug_pairs)
    if fixed != original:
        path.write_text(fixed, encoding="utf-8")
        return True
    return False


def migrate_edges(slug_pairs: list[tuple[str, str]], dry_run: bool) -> int:
    src_edges = WIKI / "graph" / "edges.jsonl"
    dst_edges = WIKI_MOLI / "graph" / "edges.jsonl"
    if not src_edges.is_file():
        return 0
    mapping = dict(slug_pairs)

    def remap(s: str) -> str:
        return mapping.get(s, s)

    added = 0
    lines_out: list[str] = []
    existing: set[str] = set()
    if dst_edges.is_file():
        existing = set(dst_edges.read_text(encoding="utf-8").splitlines())

    for line in src_edges.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            obj = json.loads(line)
        except json.JSONDecodeError:
            continue
        for key in ("src", "dst"):
            if key in obj and isinstance(obj[key], str):
                obj[key] = remap(obj[key])
        new_line = json.dumps(obj, ensure_ascii=False)
        if new_line not in existing:
            lines_out.append(new_line)
            added += 1

    if not dry_run and lines_out:
        dst_edges.parent.mkdir(parents=True, exist_ok=True)
        with dst_edges.open("a", encoding="utf-8") as f:
            for ln in lines_out:
                f.write(ln + "\n")
    return added


def write_wiki_stub(dry_run: bool) -> None:
    stub = """# 企业知识库（enterprise-kb）

> **茉莉项目（moli-project-distribute）全部文档**已迁至 **`kb/wiki-moli/`**，Web 空间 **`moli-ops-manual`（茉莉系统手册）**。  
> 入口：[[项目文档总览]]（在 wiki-moli / moli-ops-manual 空间打开）。

## 本目录状态

- `wiki/` 仅保留本说明；历史正文已迁入 `wiki-moli/`（2026-06-29 起）。
- 日本語試験 → `wiki-jp-exam/` · 空间 `jp-fe-ap-exam`。

## 同步

```bash
bash moli-knowledge/kb/tools/ci/run_sync.sh sync-all
```

详见 wiki-moli 内 [[wiki同步指南]]。
"""
    path = WIKI / "index.md"
    if not dry_run:
        path.write_text(stub, encoding="utf-8")


def append_log(summary: str, dry_run: bool) -> None:
    line = f"\n## [{date.today()}] migrate | wiki → wiki-moli | {summary}\n"
    log = WIKI_MOLI / "log.md"
    if dry_run:
        print(f"would append log: {line.strip()}")
        return
    with log.open("a", encoding="utf-8") as f:
        f.write(line)


def run_batch(batch_names: list[str], dry_run: bool) -> None:
    moves = collect_moves(batch_names)
    slug_pairs = build_slug_map(moves)

    moved = deleted = skipped = 0
    for src, dst, action in moves:
        if action == "delete":
            print(f"delete duplicate: {src.relative_to(KB)}")
            if not dry_run:
                src.unlink()
            deleted += 1
            continue
        if dst.exists():
            print(f"skip exists: {dst.relative_to(KB)}")
            skipped += 1
            continue
        print(f"move: {src.relative_to(KB)} -> {dst.relative_to(KB)}")
        if not dry_run:
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(src), str(dst))
        moved += 1

    edge_added = migrate_edges(slug_pairs, dry_run)
    summary = f"batch={','.join(batch_names)} move={moved} delete={deleted} skip={skipped} edges+={edge_added}"
    append_log(summary, dry_run)
    print(summary)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--batch", default="", help="comma-separated batches; empty = none")
    ap.add_argument("--finalize", action="store_true", help="write wiki/index stub after moves")
    ap.add_argument("--rewrite-all", action="store_true", help="rewrite wikilinks under kb/")
    args = ap.parse_args()

    batches = [b.strip() for b in args.batch.split(",") if b.strip()]
    if batches:
        run_batch(batches, args.dry_run)
    if args.rewrite_all or args.finalize:
        slug_pairs = global_slug_pairs()
        scan_roots = [KB / "wiki-moli", KB / "wiki-jp-exam", KB / "wiki", KB / "raw", KB.parent.parent / "docs"]
        patched = 0
        for root in scan_roots:
            if not root.is_dir():
                continue
            for md in root.rglob("*.md"):
                if patch_file(md, slug_pairs):
                    patched += 1
        print(f"global rewrite: patched {patched} markdown file(s)")
        append_log(f"rewrite-all patched={patched}", args.dry_run)
    if args.finalize:
        write_wiki_stub(args.dry_run)
        # remove empty dirs under wiki
        if not args.dry_run:
            for d in sorted(WIKI.rglob("*"), reverse=True):
                if d.is_dir() and d != WIKI and not any(d.iterdir()):
                    d.rmdir()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
