# -*- coding: utf-8 -*-
"""Global Certify question lookup by normalized stem text."""
from __future__ import annotations

import importlib
import re
from functools import lru_cache
from pathlib import Path

from certify_export_questions import export, norm

KB = Path(__file__).resolve().parents[1]
DIR = KB / "raw/school/certify"

MODULE_SOURCES: list[tuple[str, str]] = [
    ("模擬問題2", "certify_q2_analyses"),
    ("模擬問題3", "certify_q3_analyses"),
    ("模擬問題5", "certify_q5_analyses"),
    ("模擬問題6", "certify_q6_analyses"),
    ("模擬問題サンプル", "certify_sample_analyses"),
    ("開発技術", "certify_devtech_analyses"),
    ("技術要素（アルゴ）", "certify_algo_analyses"),
    ("マネジメント・ストラテジ", "certify_mgmt_analyses"),
]


def _parse_zh_md(path: Path) -> dict[int, dict[str, str]]:
    """Parse existing 中文解析.md into {qnum: {stem_zh, body}}."""
    if not path.exists():
        return {}
    text = path.read_text(encoding="utf-8")
    out: dict[int, dict[str, str]] = {}
    chunks = re.split(r"\n---\n", text)
    for chunk in chunks:
        m = re.search(r"^## 第 (\d+) 题", chunk, re.M)
        if not m:
            continue
        qnum = int(m.group(1))
        stem_m = re.search(r"### 中文题意\s*\n\s*\n(.+?)\n\s*\n### ", chunk, re.S)
        body_m = re.search(r"### 解析\s*\n\s*\n(.+?)(?:\n---|\Z)", chunk, re.S)
        if stem_m and body_m:
            out[qnum] = {
                "stem_zh": stem_m.group(1).strip(),
                "body": body_m.group(1).strip(),
            }
    return out


def _load_q1_lookup() -> dict[str, dict[str, str]]:
    """模擬問題1: body from expand script, stem_zh from 中文解析."""
    from expand_certify_q1_analysis import DETAILED

    lookup: dict[str, dict[str, str]] = {}
    html = DIR / "模擬問題1_ Attempt review.html"
    zh_md = DIR / "模擬問題1_中文解析.md"
    if not html.exists():
        return lookup
    parsed = _parse_zh_md(zh_md)
    data = export(html)
    for q in data["questions"]:
        n = q["n"]
        if n not in DETAILED:
            continue
        entry = {
            "stem_zh": parsed.get(n, {}).get("stem_zh") or q["text"][:300],
            "answer": q["answer"],
            "body": DETAILED[n],
        }
        lookup[q["text_norm"]] = entry
    return lookup


@lru_cache(maxsize=1)
def build_lookup() -> dict[str, dict[str, str]]:
    lookup: dict[str, dict[str, str]] = {}
    lookup.update(_load_q1_lookup())

    for stem, modname in MODULE_SOURCES:
        html = DIR / f"{stem}_ Attempt review.html"
        if not html.exists():
            continue
        mod = importlib.import_module(modname)
        data = export(html)
        for q in data["questions"]:
            n = q["n"]
            if n not in mod.ANALYSES:
                continue
            entry = dict(mod.ANALYSES[n])
            entry.setdefault("answer", q["answer"])
            lookup[q["text_norm"]] = entry

    return lookup


def lookup_by_question(q: dict, source_stem: str | None = None) -> dict[str, str] | None:
    if source_stem:
        from certify_supplement_analyses import lookup_supplement

        qnum = int(q.get("number") or q.get("n") or 0)
        hit = lookup_supplement(source_stem, qnum)
        if hit:
            return hit
    key = q.get("text_norm") or norm(q.get("text") or "")
    return build_lookup().get(key)


def lookup_stats(questions: list[dict], source_stem: str | None = None) -> tuple[int, int]:
    hit = sum(1 for q in questions if lookup_by_question(q, source_stem))
    return hit, len(questions)
