#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Moodle 测验回顾页（Save As HTML）→ Markdown 试题 raw。

适用：浏览器「另存为」的 mod/quiz/review.php 页面（Boost 主题）。
提取：测验摘要、题号、题干、选项、正确答案、作答状态。

用法：
  python kb/tools/moodle_quiz_html_to_md.py path/to/review.html
  python kb/tools/moodle_quiz_html_to_md.py kb/raw/school/certify/*.html -o kb/raw/school/certify/out

依赖：beautifulsoup4（pip install beautifulsoup4）
"""
from __future__ import annotations

import argparse
import re
import sys
from datetime import date
from pathlib import Path

try:
    from bs4 import BeautifulSoup, NavigableString, Tag
except ImportError:
    print("[error] 需要 beautifulsoup4：pip install beautifulsoup4", file=sys.stderr)
    sys.exit(2)


def _inline_html_to_md(node: Tag | NavigableString) -> str:
    """将 Moodle 题干/选项内联 HTML 转为可读 Markdown 文本。"""
    if isinstance(node, NavigableString):
        return str(node)
    if not isinstance(node, Tag):
        return ""

    name = (node.name or "").lower()
    if name == "br":
        return "\n"
    if name == "sup":
        inner = "".join(_inline_html_to_md(c) for c in node.children).strip()
        return f"^{inner}" if inner else ""
    if name == "sub":
        inner = "".join(_inline_html_to_md(c) for c in node.children).strip()
        return f"_{inner}" if inner else ""
    if name in {"script", "style"}:
        return ""
    if name == "img":
        alt = node.get("alt") or node.get("title") or "image"
        src = node.get("src") or ""
        return f"![{alt}]({src})" if src else alt

    parts = [_inline_html_to_md(c) for c in node.children]
    text = "".join(parts)
    if name in {"p", "div", "li"}:
        return text.strip() + "\n"
    if name in {"strong", "b"}:
        t = text.strip()
        return f"**{t}**" if t else ""
    if name in {"em", "i"}:
        t = text.strip()
        return f"*{t}*" if t else ""
    return text


def _block_text(el: Tag | None) -> str:
    if el is None:
        return ""
    raw = _inline_html_to_md(el)
    raw = re.sub(r"[ \t]+\n", "\n", raw)
    raw = re.sub(r"\n{3,}", "\n\n", raw)
    return raw.strip()


def _parse_summary(soup: BeautifulSoup) -> dict[str, str]:
    table = soup.select_one("table.quizreviewsummary")
    out: dict[str, str] = {}
    if not table:
        return out
    for row in table.select("tr"):
        th = row.find("th")
        td = row.find("td")
        if th and td:
            out[_block_text(th)] = _block_text(td)
    return out


def _quiz_title(soup: BeautifulSoup) -> str:
    h1 = soup.select_one("h1")
    if h1:
        t = _block_text(h1)
        if t:
            return t
    title = soup.find("title")
    if title:
        t = title.get_text(strip=True)
        t = re.sub(r": Attempt review.*$", "", t).strip()
        if t:
            return t
    return "Moodle Quiz"


def _question_type(que: Tag) -> str:
    classes = que.get("class") or []
    for c in classes:
        if c in {"multichoice", "truefalse", "shortanswer", "numerical", "essay", "match", "gapselect"}:
            return c
    return "unknown"


def _parse_options(que: Tag) -> list[dict]:
    options: list[dict] = []
    for div in que.select("div.answer > div"):
        label = div.find("label")
        if not label:
            continue
        num_el = label.select_one(".answernumber")
        num = num_el.get_text(strip=True) if num_el else ""
        # 去掉 answernumber 后的选项正文
        label_clone = BeautifulSoup(str(label), "html.parser").find("label")
        if label_clone:
            for rm in label_clone.select(".answernumber"):
                rm.decompose()
            text = _block_text(label_clone)
        else:
            text = ""
        checked = div.find("input", checked=True) is not None
        is_correct = "correct" in (div.get("class") or [])
        options.append({
            "label": num.rstrip(". ").strip(),
            "text": text,
            "selected": checked,
            "is_correct": is_correct,
        })
    return options


def _parse_questions(soup: BeautifulSoup) -> list[dict]:
    questions: list[dict] = []
    for que in soup.select("div.que"):
        qno_el = que.select_one(".qno")
        qno = qno_el.get_text(strip=True) if qno_el else str(len(questions) + 1)
        state_el = que.select_one(".state")
        grade_el = que.select_one(".grade")
        qtext_el = que.select_one(".qtext")
        right_el = que.select_one(".rightanswer")

        right = _block_text(right_el)
        if right.lower().startswith("the correct answer is:"):
            right = right.split(":", 1)[-1].strip()

        questions.append({
            "number": qno,
            "type": _question_type(que),
            "state": _block_text(state_el),
            "grade": _block_text(grade_el),
            "text": _block_text(qtext_el),
            "options": _parse_options(que),
            "correct_answer": right,
            "classes": " ".join(que.get("class") or []),
        })
    return questions


def _nav_total(soup: BeautifulSoup) -> int | None:
    buttons = soup.select("div.qn_buttons a.qnbutton")
    if buttons:
        return len(buttons)
    m = re.search(r"page \d+ of (\d+)", soup.find("title").get_text() if soup.find("title") else "")
    return int(m.group(1)) if m else None


def html_to_markdown(html_path: Path, source_rel: str | None = None) -> str:
    text = html_path.read_text(encoding="utf-8", errors="replace")
    soup = BeautifulSoup(text, "html.parser")

    title = _quiz_title(soup)
    summary = _parse_summary(soup)
    questions = _parse_questions(soup)
    nav_total = _nav_total(soup)

    slug_base = re.sub(r"[^\w\u4e00-\u9fff-]+", "-", title).strip("-").lower() or "moodle-quiz"
    today = date.today().isoformat()
    if source_rel:
        src = source_rel
    else:
        src = html_path.as_posix()
        kb_root = Path(__file__).resolve().parent.parent  # kb/
        try:
            src = html_path.resolve().relative_to(kb_root.parent).as_posix()
            if not src.startswith("moli-knowledge/"):
                src = html_path.resolve().relative_to(kb_root).as_posix()
                src = f"kb/{src}"
        except ValueError:
            pass

    lines: list[str] = [
        "---",
        f"title: {title}",
        f"slug: {slug_base}",
        "type: exam",
        "status: active",
        "tags: [certify, サーティファイ, moodle]",
        "sources:",
        f"  - {src}",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# {title}",
        "",
    ]

    if summary:
        lines.append("## 测验摘要")
        lines.append("")
        for k, v in summary.items():
            lines.append(f"- **{k}**：{v}")
        lines.append("")

    if nav_total and len(questions) < nav_total:
        lines.extend([
            f"> ⚠️ 本 HTML 仅含 **{len(questions)}** 题；导航显示共 **{nav_total}** 题。",
            "> 请在 Moodle 回顾页点击 **Show all questions on one page** 后重新另存为，或保存全部 50 页 HTML 再批量转换。",
            "",
        ])

    if not questions:
        lines.append("_未解析到题目（`div.que`）。请确认 HTML 为 Moodle quiz review 页。_")
        lines.append("")
        return "\n".join(lines)

    for q in questions:
        lines.append(f"## 问 {q['number']}")
        lines.append("")
        if q["state"] or q["grade"]:
            meta = " · ".join(x for x in [q["state"], q["grade"]] if x)
            lines.append(f"_{meta}_")
            lines.append("")
        if q["text"]:
            lines.append(q["text"])
            lines.append("")
        if q["options"]:
            lines.append("**选项**")
            lines.append("")
            for opt in q["options"]:
                marks = []
                if opt["selected"]:
                    marks.append("已选")
                if opt["is_correct"]:
                    marks.append("正解")
                suffix = f"（{', '.join(marks)}）" if marks else ""
                label = opt["label"] or "?"
                lines.append(f"- **{label}.** {opt['text']}{suffix}")
            lines.append("")
        if q["correct_answer"]:
            lines.append(f"**正确答案**：{q['correct_answer']}")
            lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def main() -> int:
    ap = argparse.ArgumentParser(description="Moodle quiz review HTML → Markdown")
    ap.add_argument("inputs", nargs="+", help="HTML 文件或 glob 路径")
    ap.add_argument("-o", "--out-dir", help="输出目录（默认与 HTML 同目录）")
    ap.add_argument("--stdout", action="store_true", help="打印到 stdout，不写文件")
    args = ap.parse_args()

    paths: list[Path] = []
    for pat in args.inputs:
        p = Path(pat)
        if p.exists():
            paths.append(p)
        else:
            paths.extend(sorted(Path().glob(pat)))

    if not paths:
        print("[error] 未找到输入文件", file=sys.stderr)
        return 1

    out_dir = Path(args.out_dir) if args.out_dir else None

    for html_path in paths:
        if html_path.suffix.lower() != ".html":
            continue
        md = html_to_markdown(html_path)
        if args.stdout:
            print(md)
            continue
        target_dir = out_dir or html_path.parent
        target_dir.mkdir(parents=True, exist_ok=True)
        stem = re.sub(r"_\s*Attempt review.*$", "", html_path.stem, flags=re.I)
        stem = re.sub(r"[^\w\u4e00-\u9fff-]+", "_", stem).strip("_") or "quiz"
        out_path = target_dir / f"{stem}.md"
        out_path.write_text(md, encoding="utf-8")
        print(f"[ok] {html_path.name} -> {out_path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
