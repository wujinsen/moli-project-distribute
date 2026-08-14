#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""将 wiki 中无效的相对文件链接转为仓库路径（反引号），供 Web 端可读。

Web 知识库只解析 [[slug]] 双链，不解析 ../../../../docs/... 类 markdown 链接。
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB_DIR = HERE.parent

# [text](../../../../path) 或 [`path`](../../../../path)
LINK_RE = re.compile(
    r"\[`?([^\]`\[]+)`?\]\((\.\./)+([^)]+)\)"
)

# ![alt](../../../../path)
IMAGE_RE = re.compile(
    r"!\[([^\]]*)\]\((\.\./)+([^)]+)\)"
)


def to_repo_path(raw_target: str) -> str:
    """Normalize relative link target to repo-root path."""
    t = raw_target.strip().lstrip("/")
    # wiki-moli/guides -> kb/raw via ../../raw
    if t.startswith("raw/"):
        return f"kb/{t}"
    return t


def fix_content(text: str) -> str:
    def link_repl(m: re.Match) -> str:
        _label, _dots, target = m.group(1), m.group(2), m.group(3)
        return f"`{to_repo_path(target)}`"

    def image_repl(m: re.Match) -> str:
        alt, _dots, target = m.group(1), m.group(2), m.group(3)
        path = to_repo_path(target)
        label = alt.strip() or "图"
        return f"> **{label}**：`{path}`（请在 IDE 中打开仓库文件查看）"

    text = IMAGE_RE.sub(image_repl, text)
    text = LINK_RE.sub(link_repl, text)
    return text


def main() -> int:
    wiki_dirs = [KB_DIR / "wiki-moli"]
    if len(sys.argv) > 1:
        wiki_dirs = [KB_DIR / sys.argv[1]]

    changed_files = 0
    for wiki_dir in wiki_dirs:
        if not wiki_dir.is_dir():
            print(f"skip missing: {wiki_dir}", file=sys.stderr)
            continue
        for md in sorted(wiki_dir.rglob("*.md")):
            if md.name in {"index.md", "log.md"}:
                continue
            original = md.read_text(encoding="utf-8")
            fixed = fix_content(original)
            if fixed != original:
                md.write_text(fixed, encoding="utf-8")
                changed_files += 1
                print(f"fixed: {md.relative_to(KB_DIR)}")

    print(f"done: {changed_files} file(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
