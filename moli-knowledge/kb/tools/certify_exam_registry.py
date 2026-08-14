# -*- coding: utf-8 -*-
"""Certify 试卷 slug ↔ stems 模块 ↔ 源 MD 映射。"""
from __future__ import annotations

# slug -> (stems_module, skip_llm: 已有高质量手工翻译)
EXAM_STEMS: dict[str, tuple[str, bool]] = {
    "模擬問題1": ("certify_q1_stems_full", False),
    "模擬問題2": ("certify_q2_stems_full", False),
    "模擬問題3": ("certify_q3_stems_full", False),
    "模擬問題4": ("certify_q4_stems_full", True),
    "模擬問題5": ("certify_q5_stems_full", False),
    "模擬問題6": ("certify_q6_stems_full", False),
    "模擬問題サンプル": ("certify_sample_stems_full", False),
    "ストラテジー": ("certify_strategy_stems_full", False),
    "マネジメント": ("certify_mgmt_stems_full", False),
    "マネジメント_ストラテジ": ("certify_mgmt_strategy_stems_full", False),
    "開発技術": ("certify_devtech_stems_full", False),
    "技術要素(DB)": ("certify_db_stems_full", False),
    "技術要素（アルゴ）": ("certify_algo_stems_full", False),
    "ランダム問題（模擬問題）1": ("certify_random1_stems_full", False),
    "ランダム問題（模擬問題）2": ("certify_random2_stems_full", False),
    "ランダム問題（模擬問題）3": ("certify_random3_stems_full", False),
    "ランダム問題（模擬問題）5": ("certify_random5_stems_full", False),
    "ランダム問題（模擬問題）サンプル": ("certify_random_sample_stems_full", False),
}
