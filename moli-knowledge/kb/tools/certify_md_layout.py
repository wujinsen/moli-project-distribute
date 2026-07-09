# -*- coding: utf-8 -*-
"""Certify 中文解析 MD 题块布局：原题（日文）与中文题目分区。"""
from __future__ import annotations

import re

from certify_stem_zh import STEM_ZH_HEADING, has_japanese_residue, translate_exam_stem

HEADING_ORIGINAL = "### 原题（日文）"
HEADING_STEM_JA_LEGACY = "### 日文题干"
OPTIONS_SUBHEADING = "**选项**"

# 兼容旧解析
HEADING_ZH_LEGACY = "### 中文题意"


def _needs_option_translation(text: str) -> bool:
    s = (text or "").strip()
    if not s:
        return False
    if has_japanese_residue(s):
        return True
    return bool(re.search(r"[\u3041-\u309f\u30a1-\u30fe]", s))


def translate_option_text(ja_text: str) -> str:
    """将单条日文选项译为中文；纯数值/符号则原样返回。"""
    s = (ja_text or "").strip()
    if not s or not _needs_option_translation(s):
        return s
    zh = translate_exam_stem(s).strip()
    if has_japanese_residue(zh):
        return s
    return zh


def format_option_lines(
    options: list[dict],
    *,
    translate: bool = False,
) -> list[str]:
    """options: [{label, text, suffix?}]"""
    lines: list[str] = [OPTIONS_SUBHEADING, ""]
    for opt in options:
        lbl = opt.get("label") or "?"
        text = opt.get("text") or ""
        if translate:
            text = translate_option_text(text)
        suffix = opt.get("suffix") or ""
        suf = f"（{suffix}）" if suffix else ""
        lines.append(f"- **{lbl}.** {text}{suf}")
    lines.append("")
    return lines


def parse_option_bullets(block: str) -> list[dict]:
    opts: list[dict] = []
    for line in block.splitlines():
        m = re.match(r"^- \*\*([a-dA-Dア-エイ-オウ-ン])\.\*\*\s*(.+?)(?:（([^）]+)）)?\s*$", line.strip())
        if not m:
            continue
        opts.append({"label": m.group(1).lower(), "text": m.group(2).strip(), "suffix": m.group(3) or ""})
    return opts


def render_stem_sections(
    ja_stem: str,
    zh_stem: str,
    options: list[dict],
) -> list[str]:
    """生成 原题（日文）+ 中文题目 两段（含各自选项）。"""
    lines: list[str] = [HEADING_ORIGINAL, "", ja_stem.strip(), ""]
    if options:
        lines.extend(format_option_lines(options, translate=False))
    lines.extend([STEM_ZH_HEADING, "", zh_stem.strip(), ""])
    if options:
        lines.extend(format_option_lines(options, translate=True))
    return lines
