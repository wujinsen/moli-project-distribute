# -*- coding: utf-8 -*-
"""Certify 日文题干 → 完整中文题目（非简略题意）。"""
from __future__ import annotations

import json
import re
from functools import lru_cache
from pathlib import Path

STEM_ZH_HEADING = "### 中文题目"
# 兼容旧 MD 解析
STEM_ZH_HEADING_ALT = "### 中文题意"

_RE_JP_KANA = re.compile(r"[\u3041-\u309f\u30a1-\u30fe]")
_RE_JP_PARTICLE = re.compile(
    r"(?:^|[\s，。、；：])"
    r"(?:の|を|に|は|が|で|と|から|まで|より|へ|も|か|など|において|に関する|に対して|によって|である|どれか|幾ら|何)"
    r"(?:$|[\s，。、])"
)

_RE_IMAGE = re.compile(r"!\[[^\]]*\]\([^)]+\)")
_RE_CODE = re.compile(r"^```")
_RE_TABLE_ROW = re.compile(r"^\|")
_RE_LIST = re.compile(r"^[-*]\s")
_RE_HEAD = re.compile(r"^#{1,6}\s")

# 长短语优先
_PHRASES: list[tuple[str, str]] = [
    ("適切なものはどれか。ここで，", "下列哪一项是正确的？此处，"),
    ("適切なものはどれか。ここで、", "下列哪一项是正确的？此处，"),
    ("適切なものはどれか。", "下列哪一项是正确的？"),
    ("適切なものはどれか", "下列哪一项是正确的"),
    ("に入れる字句の組合せとして，適切なものはどれか。", "中应填入字句的组合，下列哪一项是正确的？"),
    ("に入れる字句の組合せとして、適切なものはどれか。", "中应填入字句的组合，下列哪一项是正确的？"),
    ("に入れる字句として，適切なものはどれか。", "中应填入的字句，下列哪一项是正确的？"),
    ("に入れる字句として、適切なものはどれか。", "中应填入的字句，下列哪一项是正确的？"),
    ("に入れる語句として，適切なものはどれか。", "中应填入的语句，下列哪一项是正确的？"),
    ("に入れる語句として、適切なものはどれか。", "中应填入的语句，下列哪一项是正确的？"),
    ("に関する記述中の", "有关……的表述中，"),
    ("に関する記述として、適切なものはどれか。", "有关……的表述，下列哪一项是正确的？"),
    ("に関する記述として，適切なものはどれか。", "有关……的表述，下列哪一项是正确的？"),
    ("の説明として，適切なものどれか。", "的说明，下列哪一项是正确的？"),
    ("の説明として、適切なものどれか。", "的说明，下列哪一项是正确的？"),
    ("適切なものどれか。", "下列哪一项是正确的？"),
    ("適切なものどれか", "下列哪一项是正确的"),
    ("として、適切なものはどれか。", "，下列哪一项是正确的？"),
    ("として，適切なものはどれか。", "，下列哪一项是正确的？"),
    ("はどれか。", "是哪一项？"),
    ("はどれか", "是哪一项"),
    ("は幾らか。", "是多少？"),
    ("は何行か。", "有多少行？"),
    ("は何秒か。", "是多少秒？"),
    ("は何日か。", "是多少天？"),
    ("は何個か。", "有多少个？"),
    ("は何種類か。", "有多少种？"),
    ("は何品目か。", "有多少个品种？"),
    ("は何万円か。", "是多少万日元？"),
    ("は何円か。", "是多少日元？"),
    ("から全ての作業が終了するまでの最短日数は何日か。", "从开始到全部作业结束的最短天数是多少天？"),
    ("次に示す条件のとき", "在下列所示条件时"),
    ("次に示す方式で", "按下列所示方式"),
    ("次のとおりに定義された", "按下列方式定义的"),
    ("次の順序で", "按下列顺序"),
    ("次のSQL文を実行した結果", "执行下列 SQL 语句的结果"),
    ("次の記述", "下列表述"),
    ("次の", "下列"),
    ("表は、", "下表是"),
    ("表の", "表的"),
    ("図で示される", "图中所示的"),
    ("図中の", "图中"),
    ("図は、", "下图是"),
    ("ここで、", "此处，"),
    ("ここで，", "此处，"),
    ("ものとする。", "。（按题设条件）"),
    ("ものとする", "（按题设条件）"),
    ("また、", "另外，"),
    ("及び", "及"),
    ("からなる", "由……组成的"),
    ("に対して、", "对于……，"),
    ("に対して", "对于"),
    ("において、", "在……中，"),
    ("において", "在……中"),
    ("によって", "通过"),
    ("ことができる", "能够"),
]

_WORDS: list[tuple[str, str]] = [
    ("16進数", "十六进制"),
    ("10進数", "十进制"),
    ("2進数", "二进制"),
    ("2 の補数", "2 的补码"),
    ("2分探索木", "二叉搜索树"),
    ("無向グラフ", "无向图"),
    ("隣接行列", "邻接矩阵"),
    ("レジスタ", "寄存器"),
    ("論理演算", "逻辑运算"),
    ("排他的論理和", "异或（XOR）"),
    ("論理積", "逻辑与（AND）"),
    ("論理和", "逻辑或（OR）"),
    ("算術シフト", "算术移位"),
    ("論理シフト", "逻辑移位"),
    ("さいころ", "骰子"),
    ("確率", "概率"),
    ("非負", "非负"),
    ("関数", "函数"),
    ("プログラム", "程序"),
    ("データベース", "数据库"),
    ("トランザクション", "事务"),
    ("ロールバック", "回滚"),
    ("ロールフォワード", "前滚"),
    ("チェックポイント", "检查点"),
    ("ログファイル", "日志文件"),
    ("アローダイアグラム", "箭线图（网络图）"),
    ("フルバックアップ", "完全备份"),
    ("増分バックアップ", "增量备份"),
    ("業務終了後", "业务结束后"),
    ("業務時間", "业务时间"),
    ("仕入", "进货"),
    ("販売", "销售"),
    ("在庫", "库存"),
    ("期待値", "期望值"),
    ("予想確率", "预测概率"),
    ("著作権", "著作权"),
    ("データフロー", "数据流"),
    ("データストア", "数据存储"),
    ("処理", "处理"),
    ("外部", "外部"),
    ("空の", "空的"),
    ("入力順", "输入顺序"),
    ("実行", "执行"),
    ("記述", "表述"),
    ("字句", "字句"),
    ("語句", "语句"),
    ("組合せ", "组合"),
    ("問", "问"),
]


@lru_cache(maxsize=1)
def _glossary() -> list[tuple[str, str]]:
    path = Path(__file__).resolve().parent / "certify_katakana_translations.json"
    items: list[tuple[str, str]] = []
    if path.is_file():
        data = json.loads(path.read_text(encoding="utf-8"))
        for jp, vals in data.items():
            if isinstance(vals, list) and len(vals) >= 2 and vals[1]:
                zh = str(vals[1]).split("；")[0].split(";")[0].strip()
                if zh and jp not in {w[0] for w in _WORDS}:
                    items.append((jp, zh))
    items.sort(key=lambda x: len(x[0]), reverse=True)
    return items


def strip_images(text: str) -> str:
    return _RE_IMAGE.sub("", text or "").strip()


def has_japanese_residue(text: str) -> bool:
    """中文题目中仍含大量日文假名/助词，视为未完整翻译。"""
    if not text:
        return True
    plain = strip_images(text)
    if _RE_JP_KANA.search(plain):
        return True
    hits = len(_RE_JP_PARTICLE.findall(plain))
    if hits >= 2:
        return True
    bad_markers = (
        "是哪一项？?",
        "请参考对应中文解析",
        "（待译）",
        "原始题干为图片题",
    )
    return any(m in plain for m in bad_markers)


_RE_NOTE = re.compile(r"^>\s*（原题.*$", re.M)


def prepare_stem_for_translation(ja_text: str) -> tuple[str, bool]:
    """去掉图片行与解析占位说明，返回 (纯文本题干, 是否含图)。"""
    had_image = bool(_RE_IMAGE.search(ja_text or ""))
    lines: list[str] = []
    for line in (ja_text or "").splitlines():
        if _RE_IMAGE.search(line):
            continue
        if line.strip().startswith("> （原题"):
            continue
        lines.append(line.rstrip())
    text = _RE_NOTE.sub("", "\n".join(lines))
    text = re.sub(r"\n{3,}", "\n\n", text).strip()
    return text, had_image


IMAGE_ONLY_ZH = "本题题干以图片形式给出，请对照「日文题干」中的图片阅读。"


def clean_stem_zh(text: str) -> str:
    """去掉 LLM/旧版遗留的占位说明行。"""
    lines: list[str] = []
    for line in (text or "").splitlines():
        s = line.strip()
        if s.startswith("> （原题"):
            continue
        if "原题含选项" in s or "请对照 HTML 或试卷" in s or "请对照HTML或试卷" in s:
            continue
        lines.append(line.rstrip())
    return re.sub(r"\n{3,}", "\n\n", "\n".join(lines)).strip()


def is_image_only_stem(zh: str) -> bool:
    s = (zh or "").strip()
    return s == IMAGE_ONLY_ZH or s.startswith("本题题干以图片形式给出")


def is_brief_stem(zh: str, ja: str) -> bool:
    """判断现有中文是否为简略题意（应被完整翻译取代）。"""
    if not zh or not ja:
        return True
    if is_image_only_stem(zh):
        return False
    if has_japanese_residue(zh):
        return True
    ja_plain = prepare_stem_for_translation(ja)[0]
    zh_plain = strip_images(zh)
    ja_len = len(re.sub(r"\s+", "", ja_plain))
    zh_len = len(re.sub(r"\s+", "", zh_plain))
    if ja_len <= 40:
        return zh_len < ja_len * 0.5
    # 含问号的完整单句题目（常见 Certify 短题干）
    if any(c in zh_plain for c in "？?。") and zh_len >= 20 and zh_len >= ja_len * 0.28:
        return False
    if zh_len < ja_len * 0.25:
        return True
    # 单行极短摘要（无问号）
    lines = [ln for ln in zh_plain.splitlines() if ln.strip() and not ln.startswith("![")]
    if len(lines) == 1 and "？" not in lines[0] and "?" not in lines[0] and len(lines[0]) < 50:
        return True
    return False


def _translate_line(line: str) -> str:
    s = line
    s = s.replace("，", "，").replace("。", "。")
    s = s.replace("[ ]", "【　】").replace("□", "【　】")
    s = s.replace("「", "「").replace("」", "」")
    for jp, zh in _PHRASES:
        s = s.replace(jp, zh)
    for jp, zh in _WORDS:
        s = s.replace(jp, zh)
    for jp, zh in _glossary():
        s = s.replace(jp, zh)
    return s


def translate_exam_stem(ja_text: str) -> str:
    """将日文题干译为完整中文题目，保留表格/代码/图片行。"""
    if not ja_text or not ja_text.strip():
        return "（见日文题干）"
    out: list[str] = []
    in_code = False
    for raw in ja_text.splitlines():
        line = raw.rstrip()
        if not line.strip():
            if out and out[-1] != "":
                out.append("")
            continue
        if _RE_CODE.match(line):
            in_code = not in_code
            out.append(line)
            continue
        if in_code or _RE_IMAGE.search(line) or _RE_TABLE_ROW.match(line) or _RE_HEAD.match(line):
            out.append(line)
            continue
        if _RE_LIST.match(line):
            # 条件列表等保留结构，翻译内容
            body = re.sub(r"^[-*]\s+", "", line)
            out.append("- " + _translate_line(body))
            continue
        out.append(_translate_line(line))
    text = "\n".join(out).strip()
    # 清理「的」滥用：还原常见 IT 专名中的误替换
    text = text.replace("下列哪一项是正确的？的说明", "的说明，下列哪一项是正确的？")
    return text or "（见日文题干）"


def exam_stem_zh(ja_text: str, hint: str | None = None) -> str:
    """优先完整翻译日文题干；仅当 hint 已是完整题目且足够长时才保留 hint。"""
    full = translate_exam_stem(ja_text or "")
    if hint and not is_brief_stem(hint, ja_text or ""):
        return hint.strip()
    return full
