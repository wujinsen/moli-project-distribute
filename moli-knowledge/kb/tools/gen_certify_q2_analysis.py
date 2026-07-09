# -*- coding: utf-8 -*-
"""Generate 模擬問題2_中文解析.md from Moodle HTML + detailed analysis."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions  # noqa: E402
from certify_q2_analyses import ANALYSES  # noqa: E402
from certify_md_layout import render_stem_sections  # noqa: E402
from certify_stem_loader import stem_zh_for  # noqa: E402
from certify_stem_zh import STEM_ZH_HEADING  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402

EXAM_SLUG = "模擬問題2"

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
HTML = DIR / "模擬問題2_ Attempt review.html"
MD_OUT = DIR / "模擬問題2.md"
ZH_OUT = DIR / "模擬問題2_中文解析.md"


def _parse_html_questions() -> list[dict]:
    soup = BeautifulSoup(HTML.read_text(encoding="utf-8", errors="replace"), "html.parser")
    qs = _parse_questions(soup)
    for que, q in zip(soup.select("div.que"), qs):
        gf = que.select_one(".generalfeedback")
        if gf:
            q["general_feedback"] = gf.get_text("\n", strip=True)
    return qs


def _stem_zh(qnum: int, q: dict) -> str:
    return stem_zh_for(qnum, q.get("text") or "", EXAM_SLUG, ANALYSES)


def _analysis_body(qnum: int, q: dict) -> str:
    if qnum in ANALYSES:
        return ANALYSES[qnum]["body"].strip()
    gf = q.get("general_feedback", "")
    ans = q.get("correct_answer", "")
    lines = [f"**正确答案：** {ans}", ""]
    if gf:
        lines.extend(["**官方反馈（日文）：**", gf, ""])
    lines.extend([
        "**考点：** 见日文题干与选项。",
        "",
        "**详细推导：** 请对照 `模擬問題2.md` 或 HTML 插图中的表格/图形，按 Certify 标准公式计算或逐项排除。",
        "",
        f"**易错点：** 本卷第 {qnum} 题需结合题面数据验算，勿凭印象选。",
    ])
    return "\n".join(lines)


def build_zh_md(questions: list[dict]) -> str:
    today = date.today().isoformat()
    marks = "0/50"
    lines = [
        "---",
        "title: サーティファイ対策 模擬問題2 中文解析",
        "slug: 模擬問題2-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析]",
        "sources:",
        "  - kb/raw/school/certify/模擬問題2.md",
        "  - kb/raw/school/certify/模擬問題2_ Attempt review.html",
        "related: [模擬問題1-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        "# サーティファイ 模擬問題2 · 中文详细解析",
        "",
        "> 对应原题：`模擬問題2.md`（50 题）及 Moodle 回顾 HTML。",
        "> 含表格/图片的题请 **对照 HTML 插图**；部分题含 `generalfeedback` 逐步解答。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        "| 题数 | 50 |",
        f"| 本次得分 | {marks}（0%） |",
        "| 用途 | 日本 Certify / サーティファイ IT 基础对策 |",
        "",
    ]
    for q in questions:
        qnum = int(q["number"])
        lines.append("---")
        lines.append("")
        lines.append(f"## 第 {qnum} 题")
        lines.append("")
        lines.append("### 日文题干")
        lines.append("")
        text = q.get("text") or ""
        if text:
            lines.append(text)
        else:
            lines.append("（见 HTML）")
        lines.append("")
        lines.extend(["", STEM_ZH_HEADING, "", _stem_zh(qnum, q), ""])
        if q.get("options"):
            lines.append("### 选项")
            lines.append("")
            for opt in q["options"]:
                marks = []
                if opt.get("selected"):
                    marks.append("已选")
                if opt.get("is_correct"):
                    marks.append("正解")
                suffix = f"（{', '.join(marks)}）" if marks else ""
                lbl = opt.get("label") or "?"
                lines.append(f"- **{lbl}.** {opt.get('text', '')}{suffix}")
            lines.append("")
        ans = q.get("correct_answer") or (ANALYSES.get(qnum, {}).get("answer", ""))
        lines.append("### 正确答案")
        lines.append("")
        lines.append(f"**{ans}**")
        lines.append("")
        lines.append("### 解析")
        lines.append("")
        lines.append(_analysis_body(qnum, q))
        lines.append("")
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. 本卷 0 分说明多数题未作答；建议按 **计算机基础 → OS/DB → 网络 → 经营** 分块重做。",
        "2. Q1、Q12 等题 HTML 含 **generalfeedback** 逐步解，优先精读。",
        "3. 含 `[图]` 的题务必打开 `模擬問題2_ Attempt review.html` 对照表格。",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    # 1) Markdown 原题
    md = html_to_markdown(HTML, source_rel="kb/raw/school/certify/模擬問題2_ Attempt review.html")
    MD_OUT.write_text(md, encoding="utf-8")
    print(f"[ok] {MD_OUT.name}")

    # 2) 中文解析
    questions = _parse_html_questions()
    zh = build_zh_md(questions)
    ZH_OUT.write_text(zh, encoding="utf-8")
    print(f"[ok] {ZH_OUT.name} ({len(questions)} questions)")


if __name__ == "__main__":
    main()
