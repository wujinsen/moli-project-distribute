# -*- coding: utf-8 -*-
"""Generate 開発技術 markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402
from certify_devtech_analyses import ANALYSES  # noqa: E402
from certify_md_layout import render_stem_sections  # noqa: E402
from certify_stem_loader import stem_zh_for  # noqa: E402
from certify_stem_zh import STEM_ZH_HEADING  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402

EXAM_SLUG = "開発技術"

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
STEM = "開発技術"
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
    return stem_zh_for(qnum, q.get("text") or "", EXAM_SLUG, ANALYSES)


def _analysis_body(qnum: int, q: dict) -> str:
    if qnum in ANALYSES:
        body = ANALYSES[qnum]["body"].strip()
        gf = q.get("general_feedback", "")
        if gf:
            body += "\n\n**官方反馈（日文）：**\n" + gf
        return body
    gf = q.get("general_feedback", "")
    ans = q.get("correct_answer", "")
    lines = [f"**正确答案：** {ans}", ""]
    if gf:
        lines.extend(["**官方反馈（日文）：**", gf, ""])
    lines.extend([
        "**考点：** 见日文题干。",
        "",
        f"**详细推导：** 请对照 `{STEM}.md` 验算。",
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
        "tags: [certify, サーティファイ, 中文解析, 開発技術, 软件工程]",
        "sources:",
        f"  - kb/raw/school/certify/{STEM}.md",
        f"  - kb/raw/school/certify/{STEM}_ Attempt review.html",
        "related: [模擬問題サンプル-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ {STEM} · 中文详细解析",
        "",
        f"> 对应原题：`{STEM}.md` 及 Moodle 回顾 HTML。",
        "> 本卷 **20 题**，涵盖测试方法、UML、OOP、DFD；Q17/Q20 含 DFD 插图。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| 题数 | {n} |",
        f"| 本次得分 | {grade} |",
        "| 用途 | Certify 開発技術专题 |",
        "",
    ]
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
        ans = ANALYSES.get(qnum, {}).get("answer") or q.get("correct_answer", "")
        if qnum == 20 and ans.startswith("ータ"):
            ans = "データストア名"
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum, q), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. **测试：** ドライバ/スタブ、白/黑盒、トップ/ボトム、回归/负荷/性能。",
        "2. **UML 行为图：** ユースケース、シーケンス、ステート、アクティビティ 四者对照。",
        "3. **OOP：** 汎化・特化・カプセル化・インスタンス化 定义要背熟。",
        "4. **DFD：** 矩形=外部、圆=处理、双线=存储、箭头=数据流。",
        f"5. 更新 HTML 后：`python kb/tools/gen_certify_devtech_analysis.py`。",
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
