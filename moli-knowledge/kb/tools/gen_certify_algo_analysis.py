# -*- coding: utf-8 -*-
"""Generate 技術要素（アルゴ） markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402
from certify_algo_analyses import ANALYSES  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
STEM = "技術要素（アルゴ）"
HTML = DIR / f"{STEM}_ Attempt review.html"
MD_OUT = DIR / f"{STEM}.md"
ZH_OUT = DIR / f"{STEM}_中文解析.md"


def _parse_html_questions() -> list[dict]:
    soup = BeautifulSoup(HTML.read_text(encoding="utf-8", errors="replace"), "html.parser")
    qs = _parse_questions(soup)
    for que, q in zip(soup.select("div.que"), qs):
        gf = que.select_one(".generalfeedback")
        if gf:
            q["general_feedback"] = gf.get_text("\n", strip=True)
    return qs


def _stem_zh(qnum: int, q: dict) -> str:
    if qnum in ANALYSES:
        return ANALYSES[qnum]["stem_zh"]
    text = re.sub(r"!\[[^\]]*\]\([^)]+\)", "[图]", q.get("text") or "")
    return text[:500] + ("..." if len(text) > 500 else "")


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
        f"**详细推导：** 请对照 `{STEM}.md` 或 HTML 插图验算。",
        "",
        f"**易错点：** 第 {qnum} 题需结合题面数据手算。",
    ])
    return "\n".join(lines)


def build_zh_md(questions: list[dict], summary: dict) -> str:
    today = date.today().isoformat()
    grade = summary.get("Grade", "0%").replace("**", "")
    n = len(questions)
    lines = [
        "---",
        f"title: サーティファイ対策 {STEM} 中文解析",
        f"slug: {STEM}-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析, アルゴリズム, 算法]",
        "sources:",
        f"  - kb/raw/school/certify/{STEM}.md",
        f"  - kb/raw/school/certify/{STEM}_ Attempt review.html",
        "related: [模擬問題5-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ {STEM} · 中文详细解析",
        "",
        f"> 对应原题：`{STEM}.md` 及 Moodle 回顾 HTML。",
        "> 本卷为 **算法专题**（10 题）；含树/图/哈希/排序等，请对照 HTML 内嵌插图。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| 题数 | {n} |",
        f"| 本次得分 | {grade} |",
        "| 用途 | Certify 技术要素 · 算法 |",
        "",
    ]
    for q in questions:
        qnum = int(q["number"])
        lines.extend(["---", "", f"## 第 {qnum} 题", "", "### 日文题干", ""])
        if q.get("text"):
            lines.append(q["text"])
        else:
            lines.append("（见 HTML）")
        lines.extend(["", "### 中文题意", "", _stem_zh(qnum, q), ""])
        if q.get("options"):
            lines.extend(["### 选项", ""])
            for opt in q["options"]:
                marks_m = []
                if opt.get("selected"):
                    marks_m.append("已选")
                if opt.get("is_correct"):
                    marks_m.append("正解")
                suffix = f"（{', '.join(marks_m)}）" if marks_m else ""
                lbl = opt.get("label") or "?"
                lines.append(f"- **{lbl}.** {opt.get('text', '')}{suffix}")
            lines.append("")
        ans = q.get("correct_answer") or ANALYSES.get(qnum, {}).get("answer", "")
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum, q), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. **BST**：插入/判定/遍历三件套；Q2、Q4 务必手画。",
        "2. **排序**：冒泡比较次数 n(n−1)/2；快排=分区（Q8、Q9、Q10）。",
        "3. **线性搜索期望**：(n+1)/2 与 n 的加权（Q1）。",
        f"4. 更新 HTML 后：`python kb/tools/gen_certify_algo_analysis.py`。",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    soup = BeautifulSoup(HTML.read_text(encoding="utf-8", errors="replace"), "html.parser")
    summary = _parse_summary(soup)
    md = html_to_markdown(HTML, source_rel=f"kb/raw/school/certify/{STEM}_ Attempt review.html")
    MD_OUT.write_text(md, encoding="utf-8")
    print(f"[ok] {MD_OUT.name}")
    questions = _parse_html_questions()
    zh = build_zh_md(questions, summary)
    ZH_OUT.write_text(zh, encoding="utf-8")
    print(f"[ok] {ZH_OUT.name} ({len(questions)} questions)")


if __name__ == "__main__":
    main()
