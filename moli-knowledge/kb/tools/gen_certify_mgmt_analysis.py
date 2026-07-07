# -*- coding: utf-8 -*-
"""Generate マネジメント・ストラテジ markdown + 中文解析 from Moodle HTML."""
from __future__ import annotations

import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from moodle_quiz_html_to_md import html_to_markdown, _parse_questions, _parse_summary  # noqa: E402
from certify_mgmt_analyses import ANALYSES  # noqa: E402
from bs4 import BeautifulSoup  # noqa: E402

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"
STEM = "マネジメント_ストラテジ"
TITLE = "マネジメント・ストラテジ"
HTML = DIR / f"{TITLE}_ Attempt review.html"
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
        body = ANALYSES[qnum]["body"].strip()
        gf = q.get("general_feedback", "")
        if gf and "官方反馈" not in body:
            body += "\n\n**官方反馈（日文）：**\n" + gf
        return body
    gf = q.get("general_feedback", "")
    ans = q.get("correct_answer", "")
    lines = [f"**正确答案：** {ans}", ""]
    if gf:
        lines.extend(["**官方反馈（日文）：**", gf, ""])
    lines.extend(["**考点：** 见日文题干。", "", f"**详细推导：** 请对照 `{STEM}.md` 验算。"])
    return "\n".join(lines)


def _fix_md_frontmatter(md: str) -> str:
    today = date.today().isoformat()
    return re.sub(
        r"(?s)^---\n.*?^---\n",
        (
            "---\n"
            f"title: {TITLE}\n"
            f"slug: {STEM}\n"
            "type: exam\n"
            "status: active\n"
            "tags: [certify, サーティファイ, moodle, マネジメント, ストラテジ]\n"
            "sources:\n"
            f"  - kb/raw/school/certify/{TITLE}_ Attempt review.html\n"
            f"created: {today}\n"
            f"updated: {today}\n"
            "---\n"
        ),
        md,
        count=1,
    )


def build_zh_md(questions: list[dict], summary: dict) -> str:
    today = date.today().isoformat()
    grade = summary.get("Grade", "0%").replace("**", "")
    n = len(questions)
    lines = [
        "---",
        f"title: サーティファイ対策 {TITLE} 中文解析",
        f"slug: {STEM}-中文解析",
        "type: exam-analysis",
        "status: active",
        "tags: [certify, サーティファイ, 中文解析, マネジメント, ストラテジ]",
        "sources:",
        f"  - kb/raw/school/certify/{STEM}.md",
        f"  - kb/raw/school/certify/{TITLE}_ Attempt review.html",
        "related: [模擬問題5-中文解析, 開発技術-中文解析, サーティファイ対策]",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# サーティファイ {TITLE} · 中文详细解析",
        "",
        f"> 对应原题：`{STEM}.md` 及 Moodle 回顾 HTML（`{TITLE}_ Attempt review_files/` 为浏览器另存资源，题目正文在 HTML 内）。",
        f"> 本卷 **{n} 题**，涵盖项目管理、数据库、输入校验、备份恢复、估算与 UML 等。",
        "",
        "## 测验摘要",
        "",
        "| 项目 | 内容 |",
        "|------|------|",
        f"| 题数 | {n} |",
        f"| 本次得分 | {grade} |",
        "| 用途 | Certify マネジメント・ストラテジ 专题 |",
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
        ans = ANALYSES.get(qnum, {}).get("answer") or q.get("correct_answer", "")
        lines.extend(["### 正确答案", "", f"**{ans}**", "", "### 解析", "", _analysis_body(qnum, q), ""])
    lines.extend([
        "---",
        "",
        "## 复习建议",
        "",
        "1. **项目管理：** ガント/WBS/クリティカルパス/進捗遅れ计算要熟练。",
        "2. **DB 基础：** 主キー、ドメイン、選択/射影/結合、排他制御、ログ与ロールバック/フォワード。",
        "3. **输入校验：** ニューメリック/リミット/フォーマット/論理/バランス 五类对照记忆。",
        "4. **备份恢复：** フル+増分备份组合；障害时点决定需要哪些磁带。",
        "5. **术语：** SLA/TCO/FP法/データマート/クレンジング 与 データマイニング/ウェアハウス 勿混淆。",
        f"6. 更新 HTML 后：`python kb/tools/gen_certify_mgmt_analysis.py`。",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    soup = BeautifulSoup(HTML.read_text(encoding="utf-8", errors="replace"), "html.parser")
    summary = _parse_summary(soup)
    md = _fix_md_frontmatter(
        html_to_markdown(HTML, source_rel=f"kb/raw/school/certify/{TITLE}_ Attempt review.html")
    )
    md = md.replace("# サーティファイ対策", f"# {TITLE}")
    MD_OUT.write_text(md, encoding="utf-8")
    print(f"[ok] {MD_OUT.name}")
    questions = _parse_html_questions()
    zh = build_zh_md(questions, summary)
    ZH_OUT.write_text(zh, encoding="utf-8")
    print(f"[ok] {ZH_OUT.name} ({len(questions)} questions, analyses={len(ANALYSES)})")


if __name__ == "__main__":
    main()
