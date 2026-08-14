# -*- coding: utf-8 -*-
"""Generate 模擬問題4_中文解析.md from 模擬問題4.md + certify_q4_analyses."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from parse_certify_exam_md import parse_exam_md  # noqa: E402
from certify_q4_analyses import ANALYSES  # noqa: E402
from certify_md_layout import render_stem_sections  # noqa: E402
from certify_stem_loader import stem_zh_for  # noqa: E402
from certify_stem_zh import STEM_ZH_HEADING  # noqa: E402

EXAM_SLUG = "模擬問題4"

EXAM = 4
KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
MD_SRC = DIR / f"模擬問題{EXAM}.md"
ZH_OUT = DIR / f"模擬問題{EXAM}_中文解析.md"


def _questions_by_num() -> dict[int, dict]:
    parsed = {int(q["number"]): q for q in parse_exam_md(MD_SRC) if isinstance(q["number"], int)}
    for n in range(1, 51):
        if n not in parsed and n in ANALYSES:
            parsed[n] = {"number": n, "text": "（原题缺页，见 pic/ 或待补拍）", "options": [], "correct_answer": ""}
    return parsed


def _stem_zh(qnum: int, q: dict) -> str:
    return stem_zh_for(qnum, q.get("text") or "", EXAM_SLUG, ANALYSES)


def _analysis_body(qnum: int) -> str:
    if qnum in ANALYSES:
        return ANALYSES[qnum]["body"].strip()
    return (
        f"**考点：** 见日文题干。\n\n"
        f"**详细推导：** 请对照 `模擬問題{EXAM}.md` 及 `pic/` 原题拍照。\n\n"
        f"**易错点：** 本卷第 {qnum} 题需结合题面验算。"
    )


def _answer(qnum: int) -> str:
    return ANALYSES.get(qnum, {}).get("answer", "（待确认）")


def build_zh_md() -> str:
    today = date.today().isoformat()
    qs = _questions_by_num()
    lines = [
        "---",
        f"title: サーティファイ対策 模擬問題{EXAM} 中文解析",
        f"slug: 模擬問題{EXAM}-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析, 模擬問題4]",
        "sources:",
        f"  - kb/raw/school/certify/模擬問題{EXAM}.md",
        "  - kb/raw/school/certify/pic/",
        "related: [模擬問題1-中文解析, 模擬問題4, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ 模擬問題{EXAM} · 中文详细解析",
        "",
        f"> 对应原题：`模擬問題{EXAM}.md` 及 `pic/` 目录拍照（无 Moodle HTML）。",
        "> 图示题在 **日文题干** 中内嵌 `pic/crops/` 裁剪图（非整页拍照）。",
        "> 每题结构：**考点 → 详细推导 → 易错点**（概念题含选项分析）。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        "| 题数 | 50 |",
        "| 来源 | 纸质试题拍照 OCR/人工转录 |",
        "| 缺页 | 无（50 题齐全） |",
        "| 用途 | 日本 Certify / サーティファイ IT 基础对策 |",
        "",
    ]
    for qnum in range(1, 51):
        q = qs.get(qnum, {"text": "", "options": []})
        lines.extend(["---", "", f"## 第 {qnum} 题", ""])
        ja = q.get("text") or "（见原题 md）"
        opts = [
            {"label": o.get("label") or "?", "text": o.get("text", ""), "suffix": o.get("suffix") or ""}
            for o in (q.get("options") or [])
        ]
        lines.extend(render_stem_sections(ja, _stem_zh(qnum, q), opts))
        ans = _answer(qnum)
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. **计算题必手算：** Q1/Q10/Q11/Q12/Q19/Q24/Q35/Q46/Q47/Q49 代入题面数字验算一遍。",
        "2. **图示题：** Q2/Q4/Q6/Q18/Q30/Q35/Q46 题干已含裁剪图；纯文字题无需对照 pic/。",
        "3. **经营/管理题：** Q38 BPM、Q39 RPA、Q40 定价、Q41 PLC 与 Q42 SWOT 等可对照记忆。",
        f"4. **重新生成：** `python kb/tools/gen_certify_q{EXAM}_analysis.py`",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    if not MD_SRC.is_file():
        raise SystemExit(f"Missing {MD_SRC}")
    zh = build_zh_md()
    ZH_OUT.write_text(zh, encoding="utf-8")
    print(f"[ok] {ZH_OUT.name} ({len(_questions_by_num())} questions referenced)")


if __name__ == "__main__":
    main()
