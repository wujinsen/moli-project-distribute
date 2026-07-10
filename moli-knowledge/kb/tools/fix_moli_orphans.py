#!/usr/bin/env python3
"""Generate wiki-moli orphan hub pages and index links."""
from __future__ import annotations

import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lint import lint  # noqa: E402

KB = Path(__file__).resolve().parent.parent
WIKI_MOLI = KB / "wiki-moli"
TODAY = date.today().isoformat()
INDEX_MARKER = "## 知识治理索引（自动生成）"


def _stem(slug: str) -> str:
    return slug.split("/")[-1]


def main() -> int:
    issues, _ = lint(WIKI_MOLI)
    orphans = sorted({i.page for i in issues if i.kind == "orphan"})
    if not orphans:
        print("no orphans; skip (index/hub unchanged)")
        return 0

    practice = [p for p in orphans if p.startswith("develop/茉莉实践-")]
    topics = [p for p in orphans if p.startswith("develop/茉莉-") and p not in practice]
    topics += [
        p for p in orphans
        if p.startswith("develop/") and p not in practice and not p.startswith("develop/茉莉实践-")
    ]
    products = [p for p in orphans if p.startswith("product/")]
    guides_misc = [p for p in orphans if p.startswith("guides/")]
    ops_misc = [p for p in orphans if p.startswith("ops/")]

    topic_lines = "\n".join(f"- [[{_stem(p)}]]" for p in sorted(set(topics)))
    hub = f"""---
title: 茉莉专题索引
slug: 茉莉专题索引
type: guide
status: active
tags: [索引, 茉莉专题, 知识治理]
sources:
  - moli-knowledge/kb/tools/fix_moli_orphans.py
related: [茉莉知识体系1000批总索引, 技术方案与架构索引]
created: {TODAY}
updated: {TODAY}
---

# 茉莉专题索引

`develop/茉莉-*` 专题卡片与相关 develop 页入链枢纽（断链治理自动生成）。

## 专题卡片

{topic_lines}

## 相关

- [[茉莉知识体系100批索引]] · [[茉莉知识体系1000批总索引]]
- [[技术方案与架构索引]]
"""
    (WIKI_MOLI / "develop" / "茉莉专题索引.md").write_text(hub, encoding="utf-8")
    print("wrote develop/茉莉专题索引.md", len(topics), "links")

    index_path = WIKI_MOLI / "index.md"
    text = index_path.read_text(encoding="utf-8")
    product_line = (
        "- 产品说明：" + " · ".join(f"[[{_stem(p)}]]" for p in products)
        if products else "- 产品说明：（无孤儿 product 页）"
    )
    misc = guides_misc + ops_misc
    misc_line = (
        "- 运维 / 指南：" + " · ".join(f"[[{_stem(p)}]]" for p in misc)
        if misc else "- 运维 / 指南：（无孤儿 guides/ops 页）"
    )
    block = f"""{INDEX_MARKER}

- [[茉莉知识体系100批索引]] · [[茉莉知识体系1000批总索引]] · [[茉莉专题索引]]
{product_line}
{misc_line}
"""
    if INDEX_MARKER in text:
        text = text.split(INDEX_MARKER)[0].rstrip() + "\n\n" + block
    else:
        text = text.rstrip() + "\n\n" + block
    index_path.write_text(text, encoding="utf-8")
    print("updated index.md", "orphans=", len(orphans))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
