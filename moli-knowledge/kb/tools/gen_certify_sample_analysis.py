# -*- coding: utf-8 -*-
"""Generate 模擬問題サンプル markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402
from certify_sample_analyses import ANALYSES  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
STEM = "模擬問題サンプル"
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
    text = re.sub(r"!\[[^\]]*\]\([^)]+\)", "[插图]", q.get("text") or "")
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
        "**考点：** 见日文题干插图。",
        "",
        f"**详细推导：** 请对照 `{STEM}.md` 或 HTML 内嵌 PNG 验算。",
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
        "tags: [certify, サーティファイ, 中文解析, 模擬問題サンプル]",
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
        "> 本卷为 **官方样题**（50 题）；题干均为内嵌 PNG 插图，选项仅标注 ア/イ/ウ/エ。",
        f"> **注意：** 当前 HTML 仅含 **{n}** 题（Q{n} 不完整，Q50 缺失）；建议重新保存「Show all questions on one page」。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| 解析题数 | {n}（Q1–Q{min(n, 48)} 完整；Q49 缺内容） |",
        f"| 本次得分 | {grade} |",
        "| 用途 | Certify 官方模拟样题 |",
        "",
    ]
    for q in questions:
        qnum = int(q["number"])
        lines.extend(["---", "", f"## 第 {qnum} 题", "", "### 日文题干", ""])
        if q.get("text"):
            lines.append(q["text"])
        else:
            lines.append("（HTML 中无题干内容，可能截断）")
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
        "1. **前半（Q1–Q6）：** 进制换算、补码、移位、集合、组合概率、组合数。",
        "2. **中段（Q7–Q24）：** 数据结构、CPU/存储/磁盘、OS、DB 锁与 SQL。",
        "3. **后半（Q25–Q48）：** 网络、安全、设计/测试、经营/统计。",
        "4. 更新 HTML 后：`python kb/tools/gen_certify_sample_analysis.py`。",
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
    print(f"[ok] {ZH_OUT.name} ({len(questions)} questions, analyses={len(ANALYSES)})")


if __name__ == "__main__":
    main()
