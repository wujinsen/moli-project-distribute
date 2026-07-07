#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从 kb/raw/school/certify/*.md 与 *.html 提取片假名词汇，输出「片假名 | 英文 | 中文」Markdown。

用法：
  python kb/tools/certify_katakana_vocab.py --limit 10          # 预览 Top N
  python kb/tools/certify_katakana_vocab.py                     # 全量
  python kb/tools/certify_katakana_vocab.py -o path/to/out.md

扫描范围：certify 目录下顶层 `*.md`（排除 *中文解析*、词汇表）与 Moodle 导出的 `*.html`（不含 *_files/ 资源）。
英/中对照见同目录 `certify_katakana_translations.json`。
"""
from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from datetime import date
from pathlib import Path

KB_ROOT = Path(__file__).resolve().parent.parent
TOOLS_DIR = Path(__file__).resolve().parent
CERTIFY_DIR = KB_ROOT / "raw" / "school" / "certify"
TRANSLATIONS_JSON = TOOLS_DIR / "certify_katakana_translations.json"
DEFAULT_OUT = CERTIFY_DIR / "片假名词汇表.md"
PREVIEW_OUT = CERTIFY_DIR / "片假名词汇表_预览10.md"

KATAKANA_RE = re.compile(r"[\u30A0-\u30FF\u30FC\u30FB]+")

# 高频 IT / 考试用语对照（JSON 为主，此处作 fallback）
TRANSLATIONS: dict[str, tuple[str, str]] = {}


def load_translations() -> dict[str, tuple[str, str]]:
    merged: dict[str, tuple[str, str]] = {}
    if TRANSLATIONS_JSON.is_file():
        raw = json.loads(TRANSLATIONS_JSON.read_text(encoding="utf-8"))
        for word, pair in raw.items():
            if isinstance(pair, list) and len(pair) >= 2:
                merged[word] = (str(pair[0]), str(pair[1]))
    merged.update(TRANSLATIONS)
    return merged


# 明显由断词/乱码产生的碎片（丢弃，不进入词表）
BROKEN_TOKENS = frozenset({
    "ング", "イル", "コン", "ータストア", "ジャーナルファ", "ストラィヒ",
    "エパケット", "バィト", "ジョプ", "シリンダ", "プラックボックステスト",
    # 课程页噪声、和制英语后缀、OCR 断片
    "シス", "ラス", "アップ", "ビッ", "レジ", "ワーク", "アクセ",
    "アク", "ターン", "ミリ", "アルゴ", "キャベツ",
})

# 断片 → 完整词（频次合并到 canonical 后删除断片）
FRAGMENT_MERGE: dict[str, str] = {
    "スーパクラス": "スーパークラス",
    "サプライチェーン・": "サプライチェーンマネジメント",
    "サプライチェーン・マネジメント": "サプライチェーンマネジメント",
    "リーダ": "リーダー",
    "パレー": "パレート",
    "ページフォールト・": "ページフォールト",
    "ジスタ": "レジスタ",
    "デオメ": "ビデオメモリ",
    "モリコン": "メモリコンパクション",
    "クション": "トランザクション",
    "ティビティ": "アクティビティ",
    "システ": "システム",
    "テキス": "テキストデータ",
    "トデータ": "テキストデータ",
    "メッセー": "メッセージ",
    "ビデ": "ビデオメモリ",
    "プライベー": "プライベート",
    "エンタープラ": "エンタープライズモード",
    "イズモード": "エンタープライズモード",
    "クリティカ": "クリティカルパス",
    "ルパス": "クリティカルパス",
    "プロセ": "プロセス",
    "ラフ": "グラフ",
    "マル": "マルウェア",
    "ウェア": "マルウェア",
    "セス": "アクセス",
    "クセス": "アクセス",
    "リップフロップ": "フリップフロップ",
}

# 换行/HTML 拆词时，短左部应并入右侧完整词（如 マル+ウェア、ビデ+オメモリ）
_MERGE_LEFT_PREFIXES = frozenset({
    "ビデ", "デオ", "システ", "テキス", "メッセー", "モリコン", "クション",
    "プライベー", "エンタープラ", "クリティカ", "プロセ", "サプライチェーン・",
    "マル", "ウェア", "イズ", "ティビティ", "ルパス", "ラフ", "トデータ",
    "ジスタ", "アクセ", "エンタープラ", "ページフォールト・",
})

_KATA_SPLIT_RE = re.compile(
    r"([\u30A0-\u30FF\u30FC\u30FB]+)[ \u3000\t\n\r]+([\u30A0-\u30FF\u30FC\u30FB]+)"
)


def _should_merge_katakana(left: str, right: str) -> bool:
    if len(right) <= 2:
        return True
    if left in _MERGE_LEFT_PREFIXES:
        return True
    if len(left) >= 3:
        return True
    return False


def _normalize_katakana_splits(text: str) -> str:
    """合并 HTML 断行拆开的片假名；保留「イ システムB」等选项标签空格。"""
    prev = None
    while prev != text:
        prev = text

        def repl(m: re.Match[str]) -> str:
            left, right = m.group(1), m.group(2)
            return left + right if _should_merge_katakana(left, right) else m.group(0)

        text = _KATA_SPLIT_RE.sub(repl, text)
    return text


def _repair_compound_counts(counter: Counter[str]) -> Counter[str]:
    """将 MD/HTML 粘连产生的假复合词频次拆回完整词。"""
    repairs: dict[str, list[str]] = {
        "テストブラックボックステスト": ["ブラックボックステスト"],
        "テストホワイトボックステスト": ["ホワイトボックステスト"],
        "テストボトムアップテスト": ["ボトムアップテスト"],
        "テストリグレッションテスト": ["リグレッションテスト"],
        "モジュールダンプ": ["ダンプ"],
        "モジュールトレーサー": ["トレーサー"],
        "テストツールドライバ": ["テストツール", "ドライバ"],
        "ハードウェアコンピュータ": ["ハードウェア", "コンピュータ"],
        "クラスオブジェクト": ["クラス", "オブジェクト"],
        "オブジェクトクラス": ["クラス", "オブジェクト"],
    }
    drop_only = {
        "セキュリティサーティファイ",
        "アナウンスメントマネジメントストラテジー",
        "モリ",
    }
    repaired = Counter(counter)
    for bad in drop_only:
        repaired.pop(bad, None)
    for bad, targets in repairs.items():
        if bad not in repaired:
            continue
        n = repaired.pop(bad)
        for target in targets:
            repaired[target] += n
    return repaired


def _merge_fragment_counts(counter: Counter[str]) -> Counter[str]:
    merged = Counter(counter)
    for fragment, canonical in FRAGMENT_MERGE.items():
        if fragment in merged:
            merged[canonical] += merged.pop(fragment)
    for token in list(BROKEN_TOKENS):
        merged.pop(token, None)
    return _repair_compound_counts(merged)


def _should_skip_md(name: str) -> bool:
    return "中文解析" in name or name.startswith("片假名词汇")


def _html_to_text(path: Path) -> str:
    from bs4 import BeautifulSoup

    soup = BeautifulSoup(path.read_text(encoding="utf-8", errors="replace"), "html.parser")
    for tag in soup(["script", "style", "noscript"]):
        tag.decompose()
    return soup.get_text(separator=" ")


def _iter_source_files(certify_dir: Path) -> list[Path]:
    paths: list[Path] = []
    for path in sorted(certify_dir.glob("*.md")):
        if not _should_skip_md(path.name):
            paths.append(path)
    for path in sorted(certify_dir.glob("*.html")):
        paths.append(path)
    return paths


def _count_tokens_in_text(text: str, counter: Counter[str]) -> None:
    for token in KATAKANA_RE.findall(text):
        if token in BROKEN_TOKENS:
            continue
        core = re.sub(r"[\u30FB\u30FC]", "", token)
        if len(core) < 2:
            continue
        counter[token] += 1


def extract_katakana(certify_dir: Path) -> Counter[str]:
    counter: Counter[str] = Counter()
    for path in _iter_source_files(certify_dir):
        if path.suffix.lower() == ".html":
            text = _html_to_text(path)
        else:
            text = path.read_text(encoding="utf-8")
        text = _normalize_katakana_splits(text)
        _count_tokens_in_text(text, counter)
    return _merge_fragment_counts(counter)


def render_md(
    items: list[tuple[str, int]],
    translations: dict[str, tuple[str, str]],
    *,
    limit: int | None,
    title_suffix: str,
) -> str:
    today = date.today().isoformat()
    shown = len(items)
    lines = [
        "---",
        f"title: サーティファイ Certify · 片假名词汇{title_suffix}",
        "slug: certify-katakana-vocab",
        "type: reference",
        "status: draft",
        "tags: [certify, サーティファイ, 片假名, 词汇]",
        "sources:",
        "  - kb/raw/school/certify/*.md",
        "  - kb/raw/school/certify/*.html",
        "  - kb/tools/certify_katakana_translations.json",
        f"created: {today}",
        f"updated: {today}",
        "---",
        "",
        f"# Certify 片假名词汇{title_suffix}",
        "",
        f"> 提取自 `certify/` 试题 Markdown 与 Moodle HTML（排除 `*中文解析*` 与词汇表自身）；**{shown}** 条不重复片假名，按出现频次降序。已合并 HTML 断行拆词并剔除噪声断片。",
        "",
        "| 片假名 | 英文 | 中文 |",
        "|--------|------|------|",
    ]
    for word, _count in items:
        en, zh = translations.get(word, ("", ""))
        lines.append(f"| {word} | {en} | {zh} |")
    lines.extend(["", ""])
    return "\n".join(lines)


def main() -> int:
    ap = argparse.ArgumentParser(description="Extract katakana vocab from certify MD")
    ap.add_argument("-o", "--output", type=Path, help="输出 Markdown 路径")
    ap.add_argument("--limit", type=int, default=0, help="仅输出 Top N（0=全量）")
    ap.add_argument("--json", type=Path, help="另存频次 JSON")
    args = ap.parse_args()

    counter = extract_katakana(CERTIFY_DIR)
    ranked = counter.most_common()
    translations = load_translations()

    missing = [w for w, _ in ranked if w not in translations]
    if missing:
        print(f"[warn] {len(missing)} tokens missing translation: {missing[:5]}{'...' if len(missing) > 5 else ''}")

    if args.json:
        args.json.write_text(json.dumps(ranked, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"[ok] JSON -> {args.json} ({len(ranked)} unique)")

    limit = args.limit if args.limit > 0 else None
    subset = ranked if limit is None else ranked[:limit]
    suffix = f"（预览 {len(subset)} 条）" if limit else f"（全量 {len(subset)} 条）"

    if args.output:
        out = args.output
    elif limit == 10:
        out = PREVIEW_OUT
    else:
        out = DEFAULT_OUT

    out.write_text(render_md(subset, translations, limit=limit, title_suffix=suffix), encoding="utf-8")
    print(f"[ok] {out} ({len(subset)} rows, {len(ranked)} unique total)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
