#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Phase 0 governance: ops dedup + padding purge + link/index/edges cleanup.

Usage:
  python kb/tools/phase0_governance.py --dry-run
  python kb/tools/phase0_governance.py --apply
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from datetime import date
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

HERE = Path(__file__).resolve().parent
KB_DIR = HERE.parent
WIKI_DIR = KB_DIR / "wiki"
EDGES_FILE = WIKI_DIR / "graph" / "edges.jsonl"
INDEX_FILE = WIKI_DIR / "index.md"
LOG_FILE = WIKI_DIR / "log.md"

HUB_SLUG = "项目文档总览"
HUB_REL = f"guides/{HUB_SLUG}.md"

OPS_SLUGS = {
    "docker部署指南",
    "minio-附件存储指南",
    "nginx反向代理与前端部署指南",
    "swagger接口调试指南",
    "wiki同步指南",
    "前端开发与联调指南",
    "故障排查指南",
    "数据库初始化指南",
    "本地启动指南",
    "权限管理操作指南",
    "查询与体检指南",
    "登录与鉴权指南",
    "知识库使用指南",
    "用户中心",
    "网关",
    "订单服务",
    "bi服务",
    "知识库服务",
    "rbac-权限模型",
    "认证与会话机制",
}

TYPE_DIR = {
    "guide": "guides",
    "service": "services",
    "concept": "concepts",
    "article": "articles",
    "interview": "interview",
    "output": "outputs",
}

PADDING_BATCH_RE = re.compile(r"1000批计划\s*#287")
PADDING_TEMPLATE_RE = re.compile(r"核心概念与常见误区")
PADDING_GENERIC_SOURCE = "raw/wujinsen_markdown/"

HUB_CONTENT = f"""---
title: 系统操作手册入口
slug: {HUB_SLUG}
type: guide
status: active
tags: [运维, 操作手册, P0]
sources:
  - moli-knowledge/kb/wiki-moli/index.md
related: [增量ingest与raw投喂指南, AI自我进化与MD审校流程]
created: {date.today().isoformat()}
updated: {date.today().isoformat()}
---

# 系统操作手册入口

> **运维向操作文档**已独立为知识空间 **`moli-ops-manual`**（wiki 源 `kb/wiki-moli/`）。  
> 在 Web 端切换空间即可浏览；本页为 enterprise-kb 内的跳转说明，**不复制正文**。

## 在 Web 端打开

1. 知识库模块 → 空间选择器 → **`moli-ops-manual`（茉莉系统手册）**
2. 或按场景从该空间 index 进入：本地启动、数据库初始化、登录鉴权、权限管理、故障排查等

## 与 enterprise-kb 的分工

| 空间 | wiki 源 | 内容 |
|------|---------|------|
| **enterprise-kb** | `kb/wiki/` | 技术文章、概念、面试题、知识库治理（Ingest/AI 审校） |
| **moli-ops-manual** | `kb/wiki-moli/` | 系统部署、启动、鉴权、权限、联调、MinIO、Swagger 等操作手册 |

## 原 slug 对照（请在 ops 空间查找）

以下页面 **不再** 保留在 enterprise-kb 副本，请到 `moli-ops-manual` 空间阅读同名页：

- 操作指南：本地启动、数据库初始化、登录与鉴权、权限管理、故障排查、前端联调、Docker、Nginx、MinIO、Swagger、知识库使用、wiki 同步、查询与体检
- 微服务实体：用户中心、网关、订单服务、BI 服务、知识库服务
- 概念：RBAC 权限模型、认证与会话机制

## 同步命令（运维空间）

```bash
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual --dry-run
python moli-knowledge/kb/tools/sync_to_db.py --wiki-dir wiki-moli --space moli-ops-manual
```

## 相关

[[增量ingest与raw投喂指南]] · [[AI自我进化与MD审校流程]] · `kb/wiki-moli/index.md`
"""


def slug_to_paths(slug: str) -> list[Path]:
    hits = list(WIKI_DIR.rglob(f"{slug}.md"))
    return [p for p in hits if "graph" not in p.parts]


def is_padding_page(path: Path, text: str) -> bool:
    if path.name in ("index.md", "log.md"):
        return False
    if path.relative_to(WIKI_DIR).as_posix() == HUB_REL:
        return False
    if PADDING_BATCH_RE.search(text):
        return True
    if PADDING_TEMPLATE_RE.search(text) and PADDING_GENERIC_SOURCE in text:
        if re.search(r"批次\s+\*\*#\d{3,4}\*\*", text):
            return True
    return False


def replace_ops_links(text: str) -> tuple[str, int]:
    n = 0
    for slug in sorted(OPS_SLUGS, key=len, reverse=True):
        pat = re.compile(r"\[\[" + re.escape(slug) + r"\]\]")
        text, c = pat.subn(f"[[{HUB_SLUG}]]", text)
        n += c
    return text, n


def clean_index(text: str) -> str:
    lines = text.splitlines()
    out: list[str] = []
    skip_prefixes = tuple(f"[[{s}]]" for s in OPS_SLUGS)
    hub_line = f"- [[{HUB_SLUG}]] — 茉莉项目文档见空间 `moli-ops-manual`（**茉莉系统手册** · `kb/wiki-moli/`）"
    hub_added = False
    for line in lines:
        stripped = line.strip()
        if stripped.startswith(skip_prefixes):
            continue
        if stripped.startswith("> **系统操作手册**") or stripped.startswith("> **茉莉系统手册**"):
            out.append(
                "> **茉莉项目文档**在 **`moli-ops-manual`（茉莉系统手册）** · `kb/wiki-moli/`。"
                "enterprise-kb 仅保留占位 index。"
            )
            continue
        if stripped.startswith("> 全库内容目录"):
            out.append(line)
            if not hub_added:
                out.append("")
                out.append("## 茉莉系统手册（跨空间）")
                out.append("")
                out.append(hub_line)
                hub_added = True
            continue
        out.append(line)
    return "\n".join(out) + "\n"


def clean_edges(slugs_to_remove: set[str]) -> tuple[list[str], int]:
    if not EDGES_FILE.exists():
        return [], 0
    kept: list[str] = []
    removed = 0
    for line in EDGES_FILE.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            obj = json.loads(line)
        except json.JSONDecodeError:
            kept.append(line)
            continue
        frm = obj.get("from", "").split("/")[-1]
        to = obj.get("to", "").split("/")[-1]
        if frm in slugs_to_remove or to in slugs_to_remove:
            removed += 1
            continue
        kept.append(line)
    return kept, removed


def append_log(ops_deleted: int, padding_deleted: int, links: int, edges: int) -> str:
    today = date.today().isoformat()
    return (
        f"## [{today}] maintenance | Phase0 治理：删 enterprise-kb 运维重复 {ops_deleted} 页，"
        f"清 padding {padding_deleted} 页，替换运维 [[链接]] {links} 处，清理 edges {edges} 条\n"
    )


def main() -> int:
    ap = argparse.ArgumentParser(description="Phase 0 wiki governance")
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args()
    if not args.dry_run and not args.apply:
        ap.error("specify --dry-run or --apply")
    dry = args.dry_run

    stats = {
        "ops_delete": 0,
        "padding_delete": 0,
        "link_replacements": 0,
        "edges_removed": 0,
        "files_patched": 0,
    }

    # 1) Collect ops files to delete
    ops_files: list[Path] = []
    for slug in OPS_SLUGS:
        for p in slug_to_paths(slug):
            if p.exists():
                ops_files.append(p)
    ops_files = sorted(set(ops_files))

    # 2) Collect padding files
    padding_files: list[Path] = []
    for p in WIKI_DIR.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        rel = p.relative_to(WIKI_DIR).as_posix()
        if rel == HUB_REL:
            continue
        if rel in {f.relative_to(WIKI_DIR).as_posix() for f in ops_files}:
            continue
        text = p.read_text(encoding="utf-8")
        if is_padding_page(p, text):
            padding_files.append(p)

    all_delete = sorted(set(ops_files + padding_files))
    slugs_removed = OPS_SLUGS | {p.stem for p in padding_files}

    print(f"[plan] ops_delete={len(ops_files)} padding_delete={len(padding_files)} total_delete={len(all_delete)}")

    # 3) Link replacement in remaining files
    patch_targets = [
        p for p in WIKI_DIR.rglob("*.md")
        if p not in all_delete and p.name not in ("index.md",)
    ]
    for p in patch_targets:
        old = p.read_text(encoding="utf-8")
        new, n = replace_ops_links(old)
        if n:
            stats["link_replacements"] += n
            stats["files_patched"] += 1
            if not dry:
                p.write_text(new, encoding="utf-8")
            print(f"  links {n:3d}  {p.relative_to(WIKI_DIR).as_posix()}")

    # 4) Hub page
    hub_path = WIKI_DIR / HUB_REL
    print(f"[plan] write hub {HUB_REL}")
    if not dry:
        hub_path.parent.mkdir(parents=True, exist_ok=True)
        hub_path.write_text(HUB_CONTENT, encoding="utf-8")

    # 5) index.md
    if INDEX_FILE.exists():
        new_index = clean_index(INDEX_FILE.read_text(encoding="utf-8"))
        if not dry:
            INDEX_FILE.write_text(new_index, encoding="utf-8")
        print("[plan] update index.md")

    # 6) edges
    kept, er = clean_edges(slugs_removed)
    stats["edges_removed"] = er
    if not dry and EDGES_FILE.exists():
        EDGES_FILE.write_text("\n".join(kept) + ("\n" if kept else ""), encoding="utf-8")
    print(f"[plan] edges_remove={er}")

    # 7) Delete files
    stats["ops_delete"] = len(ops_files)
    stats["padding_delete"] = len(padding_files)
    for p in all_delete:
        print(f"  delete  {p.relative_to(WIKI_DIR).as_posix()}")
        if not dry:
            p.unlink()

    # 8) log.md
    log_line = append_log(stats["ops_delete"], stats["padding_delete"],
                          stats["link_replacements"], stats["edges_removed"])
    if not dry:
        with LOG_FILE.open("a", encoding="utf-8") as f:
            f.write(log_line)
    print(log_line.strip())

    print("[done]", stats)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
