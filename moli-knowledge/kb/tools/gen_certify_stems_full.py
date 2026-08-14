#!/usr/bin/env python3
"""从 exam .md 提取日文题干，写入 *_stems_full.py（中文题目纯文本，不含图片）。"""
from __future__ import annotations

import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from parse_certify_exam_md import parse_exam_md  # noqa: E402
from certify_stem_zh import strip_images  # noqa: E402

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
TOOLS = Path(__file__).resolve().parent

# 已有高质量手工/审校翻译的模块（优先）
MANUAL_MODULES: dict[str, str] = {
    "模擬問題4": "certify_q4_stems_full_manual",
}

_RE_IMAGE = re.compile(r"!\[[^\]]*\]\([^)]+\)")


def _text_only_stem(s: str) -> str:
    lines = []
    for line in (s or "").splitlines():
        if _RE_IMAGE.search(line):
            continue
        lines.append(line.rstrip())
    return re.sub(r"\n{3,}", "\n\n", "\n".join(lines)).strip()


def extract_stems_from_md(md_path: Path) -> dict[int, str]:
    qs = parse_exam_md(md_path)
    return {int(q["number"]): _text_only_stem(q.get("text") or "") for q in qs if isinstance(q["number"], int)}


def write_stems_py(out_path: Path, stems: dict[int, str], *, ja_fallback: bool = False) -> None:
    lines = ["# -*- coding: utf-8 -*-", '"""完整中文题目（纯文本，不含图片）。"""', "", "STEMS: dict[int, str] = {"]
    for n in sorted(stems):
        val = stems[n]
        if ja_fallback:
            val = f"（待译）{val[:200]}..."
        lines.append(f"    {n}: {val!r},")
    lines.extend(["}", ""])
    out_path.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    import argparse

    ap = argparse.ArgumentParser()
    ap.add_argument("exam_md", help="e.g. 模擬問題4.md")
    ap.add_argument("--out", help="output module name without .py")
    args = ap.parse_args()
    md = DIR / args.exam_md
    out = TOOLS / f"{args.out or md.stem.replace('（', '_').replace('）', '_') + '_stems_full'}.py"
    stems = extract_stems_from_md(md)
    write_stems_py(out, stems, ja_fallback=True)
    print(f"Wrote {out.name} ({len(stems)} placeholders — replace with full Chinese)")


if __name__ == "__main__":
    main()
