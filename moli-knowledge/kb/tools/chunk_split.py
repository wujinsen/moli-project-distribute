#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Markdown wiki 正文切段（供 sync_to_db 写入 kb_document_chunk）。

规则见 wiki-moli/develop/知识库-chunk切段规范.md §2。
"""
from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass

DETAILS_RE = re.compile(r"<details\b[^>]*>.*?</details>", re.IGNORECASE | re.DOTALL)
H1_RE = re.compile(r"^#\s+(.+)$", re.MULTILINE)

DEFAULT_MIN_CHARS = 80
DEFAULT_MAX_CHARS = 2000
DEFAULT_H3_THRESHOLD = 1500


@dataclass(frozen=True)
class ChunkDraft:
    chunk_index: int
    heading: str
    heading_level: int  # 0=页首, 2=##, 3=###
    content: str
    char_count: int
    content_hash: str


def _sha256(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def strip_details_blocks(text: str) -> str:
    return DETAILS_RE.sub("", text)


def _split_by_paragraphs(level: int, heading: str, content: str, max_chars: int) -> list[tuple[int, str, str]]:
    """超长节按空行段落再拆（最后手段）。"""
    lines = content.split("\n")
    header = lines[0] if lines and lines[0].startswith("#") else ""
    body = content[len(header):].strip() if header else content
    paras = [p.strip() for p in re.split(r"\n\s*\n", body) if p.strip()]
    if not paras:
        return [(level, heading, content)]

    out: list[tuple[int, str, str]] = []
    buf: list[str] = []
    buf_len = len(header) + 1 if header else 0

    def flush():
        nonlocal buf, buf_len
        if not buf:
            return
        piece = "\n\n".join(buf)
        if header:
            piece = header + "\n\n" + piece
        sub_head = heading if not out else f"{heading} (续)"
        out.append((level, sub_head, piece.strip()))
        buf = []
        buf_len = len(header) + 1 if header else 0

    for p in paras:
        add_len = len(p) + (2 if buf else 0)
        if buf and buf_len + add_len > max_chars:
            flush()
        buf.append(p)
        buf_len += add_len
        if buf_len >= max_chars:
            flush()
    flush()
    return out if out else [(level, heading, content)]


def _merge_small(sections: list[tuple[int, str, str]], min_chars: int) -> list[tuple[int, str, str]]:
    if not sections:
        return []
    merged: list[tuple[int, str, str]] = []
    for level, heading, content in sections:
        content = content.strip()
        if not content:
            continue
        if merged and len(content) < min_chars:
            pl, ph, pc = merged[-1]
            merged[-1] = (pl, ph, pc + "\n\n" + content)
        else:
            merged.append((level, heading, content))
    return merged


def split_markdown_body(
    body: str,
    *,
    min_chars: int = DEFAULT_MIN_CHARS,
    max_chars: int = DEFAULT_MAX_CHARS,
    h3_threshold: int = DEFAULT_H3_THRESHOLD,
) -> list[ChunkDraft]:
    """将 markdown 正文（不含 frontmatter）切成 ChunkDraft 列表。"""
    text = strip_details_blocks((body or "").strip())
    if not text:
        return []

    parts = re.split(r"(?=^## )", text, flags=re.MULTILINE)
    sections: list[tuple[int, str, str]] = []

    for i, part in enumerate(parts):
        part = part.strip()
        if not part:
            continue
        if i == 0 and not part.startswith("## "):
            sections.append((0, _first_h1(part) or "", part))
            continue
        lines = part.split("\n", 1)
        hline = lines[0].strip()
        heading = re.sub(r"^#+\s*", "", hline)
        level = 3 if hline.startswith("### ") else 2
        sections.append((level, heading, part))

    expanded: list[tuple[int, str, str]] = []
    for level, heading, content in sections:
        if level == 2 and len(content) > h3_threshold and re.search(r"^### ", content, re.MULTILINE):
            subs = re.split(r"(?=^### )", content, flags=re.MULTILINE)
            for sub in subs:
                sub = sub.strip()
                if not sub:
                    continue
                hline = sub.split("\n", 1)[0].strip()
                sub_head = re.sub(r"^#+\s*", "", hline)
                sub_level = 3 if hline.startswith("###") else 2
                expanded.append((sub_level, sub_head, sub))
        elif len(content) > max_chars:
            expanded.extend(_split_by_paragraphs(level, heading, content, max_chars))
        else:
            expanded.append((level, heading, content))

    merged = _merge_small(expanded, min_chars)

    drafts: list[ChunkDraft] = []
    for idx, (level, heading, content) in enumerate(merged):
        ch = content.strip()
        if not ch:
            continue
        drafts.append(
            ChunkDraft(
                chunk_index=idx,
                heading=(heading or "")[:255],
                heading_level=level,
                content=ch,
                char_count=len(ch),
                content_hash=_sha256(ch),
            )
        )
    return drafts


def _first_h1(text: str) -> str:
    m = H1_RE.search(text)
    return m.group(1).strip() if m else ""
