# -*- coding: utf-8 -*-
"""Generate ランダム問題（模擬問題）* markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from bs4 import BeautifulSoup  # noqa: E402
from certify_analysis_lookup import lookup_by_question, lookup_stats  # noqa: E402
from certify_export_questions import norm  # noqa: E402
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"

# (HTML title stem, output file stem)
RANDOM_SETS: list[tuple[str, str]] = [
    ("ランダム問題（模擬問題）サンプル", "ランダム問題（模擬問題）サンプル"),
    ("ランダム問題（模擬問題）1", "ランダム問題（模擬問題）1"),
    ("ランダム問題（模擬問題）2", "ランダム問題（模擬問題）2"),
    ("ランダム問題（模擬問題）5", "ランダム問題（模擬問題）5"),
    ("ランダム問題（模擬問題）3", "ランダム問題（模擬問題）3"),
]


def _parse_html_questions(html: Path) -> list[dict]:
    soup = BeautifulSoup(html.read_text(encoding="utf-8", errors="replace"), "html.parser")
    qs = _parse_questions(soup)
    for que, q in zip(soup.select("div.que"), qs):
        gf = que.select_one(".generalfeedback")
        if gf:
            q["general_feedback"] = gf.get_text("\n", strip=True)
        q["text_norm"] = norm(q.get("text") or "")
    return qs


def _stem_zh(q: dict, source_stem: str) -> str:
    hit = lookup_by_question(q, source_stem)
    if hit and hit.get("stem_zh"):
        return hit["stem_zh"]
    text = re.sub(r"!\[[^\]]*\]\([^)]+\)", "[图]", q.get("text") or "")
    return text[:500] + ("..." if len(text) > 500 else "")


def _analysis_body(qnum: int, q: dict, stem: str, source_stem: str) -> str:
    hit = lookup_by_question(q, source_stem)
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
        f"**详细推导：** 请对照 `{stem}.md` 或 HTML 插图验算。",
        "",
        f"**易错点：** 本卷第 {qnum} 题需结合题面数据，勿凭印象选。",
    ])
    return "\n".join(lines)


def _fix_md_frontmatter(md: str, title: str, stem: str) -> str:
    today = date.today().isoformat()
    return re.sub(
        r"(?s)^---\n.*?^---\n",
        (
            "---\n"
            f"title: {title}\n"
            f"slug: {stem}\n"
            "type: exam\n"
            "status: active\n"
            "tags: [certify, サーティファイ, moodle, ランダム問題]\n"
            "sources:\n"
            f"  - kb/raw/school/certify/{stem}_ Attempt review.html\n"
            f"created: {today}\n"
            f"updated: {today}\n"
            "---\n"
        ),
        md,
        count=1,
    )


def build_zh_md(title: str, stem: str, questions: list[dict], summary: dict) -> str:
    today = date.today().isoformat()
    marks = summary.get("Marks", "0/50").replace("**", "")
    grade = summary.get("Grade", "0%").replace("**", "")
    hit, total = lookup_stats(questions, title)
    n = len(questions)
    lines = [
        "---",
        f"title: サーティファイ対策 {title} 中文解析",
        f"slug: {stem}-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析, ランダム問題]",
        "sources:",
        f"  - kb/raw/school/certify/{stem}.md",
        f"  - kb/raw/school/certify/{stem}_ Attempt review.html",
        "related: [模擬問題1-中文解析, 模擬問題2-中文解析, 模擬問題5-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ {title} · 中文详细解析",
        "",
        f"> 对应原题：`{stem}.md` 及 Moodle 回顾 HTML。",
        f"> 本卷为 **随机抽题**（题序与固定模擬問題卷不同）；解析按 **题干文本** 从已有模擬問題/专题库匹配（覆盖 {hit}/{total} 题）。",
        "> 含表格/图片的题请 **对照 HTML 插图** 验算。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| 题数 | {n} |",
        f"| 本次得分 | {marks}（{grade}） |",
        "| 用途 | Certify ランダム模擬（综合 IT 基础） |",
        "",
    ]
    for q in questions:
        qnum = int(q["number"])
        lines.extend(["---", "", f"## 第 {qnum} 题", "", "### 日文题干", ""])
        if q.get("text"):
            lines.append(q["text"])
        else:
            lines.append("（见 HTML）")
        lines.extend(["", "### 中文题意", "", _stem_zh(q, title), ""])
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
        hit = lookup_by_question(q, title)
        ans = q.get("correct_answer") or (hit or {}).get("answer", "")
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum, q, stem, title), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. ランダム卷题序每次不同，建议按 **考点**（数制/OS/DB/网络/经营）归类错题，而非记题号。",
        "2. 同一题干可对照固定卷：`模擬問題1/2/5/6`、サンプル、開発技術 等。",
        "3. 更新 HTML 后：`python kb/tools/gen_certify_random_analysis.py`。",
        "",
    ])
    return "\n".join(lines)


def generate_one(title: str, stem: str) -> tuple[int, int]:
    html = DIR / f"{stem}_ Attempt review.html"
    if not html.exists():
        print(f"[skip] missing {html.name}")
        return 0, 0
    soup = BeautifulSoup(html.read_text(encoding="utf-8", errors="replace"), "html.parser")
    summary = _parse_summary(soup)
    questions = _parse_html_questions(html)
    if len(questions) < 10:
        print(f"[skip] {stem}: only {len(questions)} questions (need full HTML)")
        return 0, 0

    md_out = DIR / f"{stem}.md"
    zh_out = DIR / f"{stem}_中文解析.md"
    md = _fix_md_frontmatter(
        html_to_markdown(html, source_rel=f"kb/raw/school/certify/{stem}_ Attempt review.html"),
        title,
        stem,
    )
    md_out.write_text(md, encoding="utf-8")
    zh = build_zh_md(title, stem, questions, summary)
    zh_out.write_text(zh, encoding="utf-8")
    hit, total = lookup_stats(questions, title)
    print(f"[ok] {stem}.md + 中文解析 ({total} q, analyses {hit}/{total})")
    return hit, total


def main() -> None:
    for title, stem in RANDOM_SETS:
        generate_one(title, stem)
    partial = DIR / "ランダム問題（模擬問題）3_ Attempt review (page 1 of 50).html"
    full = DIR / "ランダム問題（模擬問題）3_ Attempt review.html"
    if partial.exists() and not full.exists():
        print("[note] ランダム問題3 仅 page 1/50，请 Moodle「Show all on one page」后另存完整 HTML")


if __name__ == "__main__":
    main()
