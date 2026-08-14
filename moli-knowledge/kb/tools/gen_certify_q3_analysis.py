# -*- coding: utf-8 -*-
"""Generate 模擬問題3 markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import importlib
import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402
from certify_md_layout import render_stem_sections  # noqa: E402
from certify_stem_loader import stem_zh_for  # noqa: E402
from certify_stem_zh import STEM_ZH_HEADING  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402

EXAM = 3
EXAM_SLUG = "模擬問題3"
KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
HTML = DIR / f"模擬問題{EXAM}_ Attempt review.html"
MD_OUT = DIR / f"模擬問題{EXAM}.md"
ZH_OUT = DIR / f"模擬問題{EXAM}_中文解析.md"

ANALYSES = importlib.import_module(f"certify_q{EXAM}_analyses").ANALYSES


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
        f"**详细推导：** 请对照 `模擬問題{EXAM}.md` 或 HTML 插图验算。",
        "",
        f"**易错点：** 本卷第 {qnum} 题需结合题面数据，勿凭印象选。",
    ])
    return "\n".join(lines)


def build_zh_md(questions: list[dict], summary: dict) -> str:
    today = date.today().isoformat()
    marks = summary.get("Marks", "0/50").replace("**", "")
    grade = summary.get("Grade", "0%").replace("**", "")
    n = len(questions)
    lines = [
        "---",
        f"title: サーティファイ対策 模擬問題{EXAM} 中文解析",
        f"slug: 模擬問題{EXAM}-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析]",
        "sources:",
        f"  - kb/raw/school/certify/模擬問題{EXAM}.md",
        f"  - kb/raw/school/certify/模擬問題{EXAM}_ Attempt review.html",
        "related: [模擬問題1-中文解析, 模擬問題2-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ 模擬問題{EXAM} · 中文详细解析",
        "",
        f"> 对应原题：`模擬問題{EXAM}.md` 及 Moodle 回顾 HTML。",
        "> 含表格/图片的题请 **对照 HTML 插图**；含 `generalfeedback` 的题优先精读。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| HTML 内题数 | **{n}**（测验总分仍为 50 题） |",
        f"| 本次得分 | {marks}（{grade}） |",
        "| 用途 | 日本 Certify / サーティファイ IT 基础对策 |",
        "",
    ]
    if n < 50:
        lines.extend([
            f"> ⚠️ 当前 HTML **仅含 {n} 题**。请在 Moodle 回顾页勾选 **Show all questions on one page** 后重新「另存为」，以补全 Q{n+1}～Q50。",
            "",
        ])
    for q in questions:
        qnum = int(q["number"])
        lines.extend(["---", "", f"## 第 {qnum} 题", ""])
        ja = q.get("text") or "（见 HTML）"
        opts = []
        for opt in q.get("options") or []:
            marks_m = []
            if opt.get("selected"):
                marks_m.append("已选")
            if opt.get("is_correct"):
                marks_m.append("正解")
            opts.append({
                "label": opt.get("label") or "?",
                "text": opt.get("text", ""),
                "suffix": ", ".join(marks_m),
            })
        lines.extend(render_stem_sections(ja, _stem_zh(qnum, q), opts))
        ans = q.get("correct_answer") or ANALYSES.get(qnum, {}).get("answer", "")
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum, q), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. 本卷前半段偏重 **数制/补码/浮点/排序/CPU/缓存/磁盘/OS**；与模擬問題1、2 对照刷题。",
        "2. Q2、Q14、Q15、Q16 等含 **generalfeedback 逐步解**，建议手算一遍。",
        f"3. 补全 HTML 后重新运行：`python kb/tools/gen_certify_q{EXAM}_analysis.py`。",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    soup = BeautifulSoup(HTML.read_text(encoding="utf-8", errors="replace"), "html.parser")
    summary = _parse_summary(soup)
    md = html_to_markdown(HTML, source_rel=f"kb/raw/school/certify/模擬問題{EXAM}_ Attempt review.html")
    MD_OUT.write_text(md, encoding="utf-8")
    print(f"[ok] {MD_OUT.name}")
    questions = _parse_html_questions()
    zh = build_zh_md(questions, summary)
    ZH_OUT.write_text(zh, encoding="utf-8")
    print(f"[ok] {ZH_OUT.name} ({len(questions)} questions)")


if __name__ == "__main__":
    main()
