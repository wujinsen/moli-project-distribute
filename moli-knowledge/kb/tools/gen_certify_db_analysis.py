# -*- coding: utf-8 -*-
"""Generate 技術要素(DB) markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from certify_md_layout import render_stem_sections  # noqa: E402
from certify_stem_loader import stem_zh_for  # noqa: E402
from certify_stem_zh import STEM_ZH_HEADING  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402
from certify_analysis_lookup import lookup_by_question, lookup_stats  # noqa: E402
from certify_export_questions import norm  # noqa: E402
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
STEM = "技術要素(DB)"
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
        q["text_norm"] = norm(q.get("text") or "")
    return qs


def _stem_zh(qnum: int, q: dict) -> str:
    return stem_zh_for(qnum, q.get("text") or "", STEM)


def _analysis_body(qnum: int, q: dict) -> str:
    hit = lookup_by_question(q, STEM)
    if hit and hit.get("body"):
        body = hit["body"].strip()
        gf = q.get("general_feedback", "")
        if gf and "官方反馈" not in body:
            body += "\n\n**官方反馈（日文）：**\n" + gf
        return body
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


def _fix_md_frontmatter(md: str) -> str:
    today = date.today().isoformat()
    return re.sub(
        r"(?s)^---\n.*?^---\n",
        (
            "---\n"
            f"title: {STEM}\n"
            f"slug: {STEM}\n"
            "type: exam\n"
            "status: active\n"
            "tags: [certify, サーティファイ, moodle, DB, データベース]\n"
            "sources:\n"
            f"  - kb/raw/school/certify/{STEM}_ Attempt review.html\n"
            f"created: {today}\n"
            f"updated: {today}\n"
            "---\n"
        ),
        md,
        count=1,
    )


def build_zh_md(questions: list[dict], summary: dict) -> str:
    today = date.today().isoformat()
    marks = summary.get("Marks", "0/29").replace("**", "")
    grade = summary.get("Grade", "0%").replace("**", "")
    hit, total = lookup_stats(questions, STEM)
    n = len(questions)
    lines = [
        "---",
        f"title: サーティファイ対策 {STEM} 中文解析",
        f"slug: {STEM}-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析, DB, データベース]",
        "sources:",
        f"  - kb/raw/school/certify/{STEM}.md",
        f"  - kb/raw/school/certify/{STEM}_ Attempt review.html",
        "related: [マネジメント_ストラテジ-中文解析, 模擬問題5-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ {STEM} · 中文详细解析",
        "",
        f"> 对应原题：`{STEM}.md` 及 Moodle 回顾 HTML。",
        f"> 本卷 **{n} 题** DB 专题；解析匹配 **{hit}/{total}**（题干库 + 补充解析）。",
        "> 含 SQL/UML/锁表/通配符等题请 **对照 HTML 插图**。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| 题数 | {n} |",
        f"| 本次得分 | {marks}（{grade}） |",
        "| 用途 | Certify 技术要素 · 数据库 |",
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
        hit = lookup_by_question(q, STEM)
        ans = q.get("correct_answer") or (hit or {}).get("answer", "")
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum, q), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. **校验五类：** Numeric / Format / Limit / Balance / Logic 对照例题记忆。",
        "2. **DB 术语：** 主キー、ドメイン、正規化、ログ、Rollback/Roll-forward、ストアドプロシージャ。",
        "3. **SQL/锁：** 選択/射影/結合；共有 vs 専有；SQL 结果行数手算。",
        f"4. 更新 HTML 后：`python kb/tools/gen_certify_db_analysis.py`。",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    soup = BeautifulSoup(HTML.read_text(encoding="utf-8", errors="replace"), "html.parser")
    summary = _parse_summary(soup)
    md = _fix_md_frontmatter(
        html_to_markdown(HTML, source_rel=f"kb/raw/school/certify/{STEM}_ Attempt review.html")
    )
    MD_OUT.write_text(md, encoding="utf-8")
    print(f"[ok] {MD_OUT.name}")
    questions = _parse_html_questions()
    zh = build_zh_md(questions, summary)
    ZH_OUT.write_text(zh, encoding="utf-8")
    hit, total = lookup_stats(questions, STEM)
    print(f"[ok] {ZH_OUT.name} ({total} q, analyses {hit}/{total})")


if __name__ == "__main__":
    main()
