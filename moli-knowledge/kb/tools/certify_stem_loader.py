# -*- coding: utf-8 -*-
"""从 certify_*_stems_full 加载完整中文题目，供 gen / patch 共用。"""
from __future__ import annotations

import importlib
from functools import lru_cache
from typing import Any

from certify_exam_registry import EXAM_STEMS
from certify_stem_zh import clean_stem_zh, exam_stem_zh, is_brief_stem


@lru_cache(maxsize=32)
def load_stems(slug: str) -> dict[int, str]:
    entry = EXAM_STEMS.get(slug)
    if not entry:
        return {}
    modname, _ = entry
    try:
        mod = importlib.import_module(modname)
        raw = getattr(mod, "STEMS", {})
        return {int(k): clean_stem_zh(str(v)) for k, v in raw.items()}
    except ImportError:
        return {}


def stem_zh_for(
    qnum: int,
    ja_text: str,
    slug: str,
    analyses: dict[int, Any] | None = None,
) -> str:
    """优先 stems_full → 非简略 analyses.stem_zh → LLM 规则翻译。"""
    stems = load_stems(slug)
    if qnum in stems:
        return stems[qnum]
    if analyses and qnum in analyses:
        hint = (analyses[qnum].get("stem_zh") or "").strip()
        if hint and not is_brief_stem(hint, ja_text):
            return clean_stem_zh(hint)
    return exam_stem_zh(ja_text or "", None)
