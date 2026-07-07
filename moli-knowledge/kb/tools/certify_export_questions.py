# -*- coding: utf-8 -*-
"""Export questions from certify HTML to JSON."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from bs4 import BeautifulSoup
from moodle_quiz_html_to_md import _parse_questions, _parse_summary


def norm(text: str) -> str:
    t = re.sub(r"!\[[^\]]*\]\([^)]+\)", "[IMG]", text or "")
    t = re.sub(r"\s+", " ", t).strip()
    return t[:120]


def export(html_path: Path, out_path: Path | None = None) -> dict:
    soup = BeautifulSoup(html_path.read_text(encoding="utf-8", errors="replace"), "html.parser")
    summary = _parse_summary(soup)
    qs = _parse_questions(soup)
    items = []
    for q in qs:
        items.append({
            "n": int(q["number"]),
            "text": q.get("text") or "",
            "text_norm": norm(q.get("text") or ""),
            "answer": q.get("correct_answer") or "",
            "opts": [o.get("text", "") for o in (q.get("options") or [])],
            "gf": q.get("general_feedback") or "",
        })
    data = {"summary": summary, "questions": items, "source": html_path.name}
    if out_path:
        out_path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    return data


if __name__ == "__main__":
    html = Path(sys.argv[1])
    out = Path(sys.argv[2]) if len(sys.argv) > 2 else None
    d = export(html, out)
    print(f"{html.name}: {len(d['questions'])} questions")
