#!/usr/bin/env python3
"""Import docs/design/*.md into wiki-moli/develop/ for Web browse + Sync.

Engineering contract remains in docs/design/; wiki pages are browse mirrors with
sources pointing back. Re-run after design doc updates.
"""
from __future__ import annotations

import re
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DESIGN = ROOT.parents[1] / "docs" / "design"
WIKI = ROOT / "wiki-moli" / "develop"

# design filename -> (slug, title, kb type, extra related slugs)
DESIGN_PAGES: dict[str, tuple[str, str, str, list[str]]] = {
    "user-center-overview.md": (
        "用户中心-概要设计",
        "用户中心 · 概要设计",
        "concept",
        ["用户中心", "用户中心-详细设计", "门户子系统-系统分组"],
    ),
    "user-center-detailed-design.md": (
        "用户中心-详细设计",
        "用户中心 · 详细设计",
        "concept",
        ["用户中心", "用户中心-概要设计"],
    ),
    "portal-system-group.md": (
        "门户子系统-系统分组",
        "多系统门户 · system_group 分组说明",
        "concept",
        ["用户中心-概要设计", "用户中心"],
    ),
    "gateway-design.md": (
        "API网关-概要设计",
        "API 网关 · 概要设计",
        "concept",
        ["网关", "服务调用与架构"],
    ),
    "order-seckill-design.md": (
        "订单秒杀-概要设计",
        "订单服务 · 秒杀链路设计",
        "concept",
        ["订单服务", "秒杀设计"],
    ),
    "knowledge-module-overview.md": (
        "知识库模块-概要设计",
        "知识库模块 · 概要设计",
        "concept",
        ["知识库服务", "知识库设计哲学-docs-as-code"],
    ),
    "kb-llm-platform-settings.md": (
        "知识库LLM平台设置",
        "知识库 LLM · 平台系统设置（T19 设计）",
        "concept",
        ["知识库模块-概要设计", "知识库服务"],
    ),
    "bi-module-overview.md": (
        "BI模块-概要设计",
        "BI 模块 · 概要设计",
        "concept",
        ["bi服务"],
    ),
    "kb-import-entry-design.md": (
        "知识库双入口导入设计",
        "知识库 · 双入口导入（T20 技术设计）",
        "concept",
        ["知识库模块-概要设计", "Ingest工作台产品方案"],
    ),
    "kb-ops-roadmap.md": (
        "知识库运维规划",
        "知识库内容管道运维规划",
        "concept",
        ["wiki同步指南", "知识库模块-概要设计"],
    ),
    "server-ops-module-roadmap.md": (
        "服务器运维模块规划",
        "服务器/基础设施运维模块规划",
        "concept",
        ["user-center-运维要点"],
    ),
}

LINK_MAP = {k: v[0] for k, v in DESIGN_PAGES.items()}


def strip_leading_h1(text: str) -> str:
    lines = text.splitlines()
    if lines and lines[0].startswith("# "):
        return "\n".join(lines[1:]).lstrip("\n")
    return text


def rewrite_design_links(text: str) -> str:
    for fname, slug in LINK_MAP.items():
        text = re.sub(
            rf"\[[^\]]*\]\({re.escape(fname)}\)",
            f"[[{slug}]]",
            text,
        )
    return text


def rewrite_markdown_file_links(text: str) -> str:
    """Web wiki: repo paths as backticks, not relative markdown links."""

    def docs_link(m: re.Match[str]) -> str:
        path = m.group(2)
        return f"`{path}`"

    text = re.sub(r"\[`([^`]+)`\]\((docs/[^)]+)\)", docs_link, text)
    text = re.sub(r"\[([^\]]+)\]\((docs/[^)]+)\)", docs_link, text)
    text = re.sub(r"\[([^\]]+)\]\((moli-[^)]+)\)", r"`\2`", text)
    text = re.sub(r"\[([^\]]+)\]\((README[^)]*)\)", r"`\1`", text)
    return text


def rewrite_repo_paths(text: str) -> str:
    replacements = [
        (r"\.\./diagrams/", "docs/diagrams/"),
        (r"\.\./zh-CN/", "docs/zh-CN/"),
        (r"\.\./sql/", "docs/sql/"),
        (r"\.\./api/", "docs/api/"),
        (r"\.\./product/", "docs/product/"),
        (r"\.\./test/", "docs/test/"),
        (r"\.\./ops/", "docs/ops/"),
        (r"\.\./\.\./moli-knowledge/", "moli-knowledge/"),
        (r"\.\./\.\./moli-", "moli-"),
        (r"\.\./\.\./README", "README.zh-CN.md"),
    ]
    for pat, repl in replacements:
        text = re.sub(pat, repl, text)
    return text


def rewrite_images(text: str) -> str:
    def repl(m: re.Match[str]) -> str:
        alt, path = m.group(1), m.group(2)
        path = path.replace("../diagrams/", "docs/diagrams/")
        label = alt.strip() or "架构图"
        return f"\n> **{label}**：`{path}`（请在仓库中打开 PNG；源文件见同目录 `.drawio`）\n"

    return re.sub(r"!\[([^\]]*)\]\(([^)]+)\)", repl, text)


def build_frontmatter(
    slug: str, title: str, kb_type: str, source: str, related: list[str]
) -> str:
    today = date.today().isoformat()
    base_related = ["技术方案与架构索引"]
    merged = []
    for s in related + base_related:
        if s not in merged and s != slug:
            merged.append(s)
    rel_yaml = "\n".join(f"  - {s}" for s in merged)
    return f"""---
title: {title}
slug: {slug}
type: {kb_type}
status: active
tags: [设计, 概要设计, 架构]
sources:
  - docs/design/{source}
related:
{rel_yaml}
created: {today}
updated: {today}
---

> **浏览镜像**：工程契约权威仍在 `docs/design/{source}`；改设计请先改契约再重新运行本脚本或 Tab3 导入。

"""


def import_one(filename: str, meta: tuple[str, str, str, list[str]]) -> Path:
    slug, title, kb_type, related = meta
    src = DESIGN / filename
    body = src.read_text(encoding="utf-8")
    body = strip_leading_h1(body)
    body = rewrite_images(body)
    body = rewrite_repo_paths(body)
    body = rewrite_design_links(body)
    body = rewrite_markdown_file_links(body)
    content = build_frontmatter(slug, title, kb_type, filename, related) + body
    out = WIKI / f"{slug}.md"
    out.write_text(content, encoding="utf-8", newline="\n")
    return out


def main() -> None:
    if not DESIGN.is_dir():
        raise SystemExit(f"Missing {DESIGN}")
    WIKI.mkdir(parents=True, exist_ok=True)
    written = []
    for fname, meta in DESIGN_PAGES.items():
        path = DESIGN / fname
        if not path.is_file():
            print(f"skip missing {path}")
            continue
        out = import_one(fname, meta)
        written.append(out.relative_to(ROOT))
        print(f"wrote {out}")
    print(f"done: {len(written)} pages")


if __name__ == "__main__":
    main()
