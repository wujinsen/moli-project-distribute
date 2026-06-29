#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""茉莉知识库 · Lint 体检 / 知识治理自动化（零依赖，标准库实现）

对应 AGENTS.md §6「操作三：Lint（健康检查）」与 ROADMAP §二「知识治理」。
扫全 ``kb/wiki/``，输出**分级**问题清单 + 修复建议；**只报告，不自动改写**
（重写动作需用户确认）。可作为 CLI 本地跑，也可在 CI 里做门禁（``--strict``）。

检查项（与 AGENTS.md §6 对齐，并补充 frontmatter 规范校验）
------------------------------------------------------------------
ERROR（结构性错误，CI 默认拦截）
  * broken_link     断链：正文 [[slug]] / frontmatter related / edges 目标解析不到
  * bad_type        frontmatter type 不在白名单（guide|service|concept|article|interview|output）
  * missing_title   缺标题（frontmatter title 且正文无 H1）
  * dup_slug        同一 slug（裸名）对应多个文件，[[]] 解析有歧义
WARN（治理告警，``--strict`` 时也拦截）
  * orphan          孤儿页：无任何入链（index.md 目录引用也算入链）
  * missing_source  缺来源：frontmatter sources 为空
  * missing_concept 缺概念页：被 ≥N 个页引用、却没有独立页的概念（断链的高频子集）
  * outdated        过时：被某页用 supersedes 边取代，但本页仍 status: active
  * slug_mismatch   frontmatter slug 与文件名不一致（影响 [[]] 解析）
  * missing_dates   frontmatter 缺 created / updated
INFO（参考，不影响退出码）
  * dup_content     正文完全相同（content_hash 撞）——仅 --dups 开启
  * near_dup        正文近似重复（Jaccard ≥ 阈值）——仅 --dups 开启
  * asym_related    related 单向（A→B 有、B→A 无）——仅 --related 开启

用法
----
    # 控制台分级报告（全库）
    python kb/tools/lint.py

    # 写 markdown 报告 + 追加一行 log.md（治理留痕）
    python kb/tools/lint.py --report --log

    # 机器可读
    python kb/tools/lint.py --json kb/lint-report.json

    # 近似重复检测（较慢，默认关闭）
    python kb/tools/lint.py --dups --dup-threshold 0.85

    # CI 门禁：有 ERROR 退出码=1；--strict 时 WARN 也算失败
    python kb/tools/lint.py --strict

    # 指定独立 wiki 目录（与 sync_to_db.py 一致）
    python kb/tools/lint.py --wiki-dir wiki-jp-exam

解析逻辑（frontmatter / 链接解析）复用 ``sync_to_db.py``，保证体检口径与实际入库一致。
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from datetime import datetime
from pathlib import Path

# 复用同步脚本的解析/解析口径，避免「体检通过但入库断链」的不一致
from sync_to_db import (
    DEFAULT_WIKI_DIR,
    KB_DIR,
    KB_TYPES,
    SPECIAL,
    STATUS_MAP,
    load_edges,
    parse_frontmatter,
    resolve,
)

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# 体检口径：忽略代码块 / 行内代码里的 [[..]]（那是语法示例，非真实链接）。
# 注意：这与 sync_to_db 的「裸提取」略有差异——同步会把示例记为 resolved=0，
# 而体检只报告**可操作**的真实断链，避免文档页的 `[[slug]]` 示例刷屏误报。
_FENCED = re.compile(r"```.*?```", re.S)
_INLINE_CODE = re.compile(r"`[^`\n]*`")
_WIKILINK = re.compile(r"\[\[([^\]]+)\]\]")

# 目录 / 时间线类文件（catalog/log），不作为知识页参与体检
_META_STEM = re.compile(r"^(index|log)(-.*)?$")
_INDEX_STEM = re.compile(r"^index(-.*)?$")


def extract_links_outside_code(body: str):
    """剔除围栏代码块与行内代码后，提取 [[目标]] 的目标（去重、保序）。"""
    clean = _INLINE_CODE.sub(" ", _FENCED.sub(" ", body))
    out, seen = [], set()
    for m in _WIKILINK.finditer(clean):
        tgt = m.group(1).split("|")[0].strip()
        if tgt and tgt not in seen:
            seen.add(tgt)
            out.append(tgt)
    return out

# 严重级别
ERROR, WARN, INFO = "error", "warn", "info"
LEVEL_ORDER = {ERROR: 0, WARN: 1, INFO: 2}
LEVEL_LABEL = {ERROR: "✗ 错误", WARN: "△ 告警", INFO: "· 参考"}

# 各 kind 的默认级别与中文标题
KIND_META = {
    "broken_link":     (ERROR, "断链（[[]]/related/edges 目标不存在）"),
    "bad_type":        (ERROR, "type 非法（不在白名单）"),
    "missing_title":   (ERROR, "缺标题（无 frontmatter title 且无 H1）"),
    "dup_slug":        (ERROR, "slug 歧义（裸名撞，多个文件）"),
    "orphan":          (WARN,  "孤儿页（无任何入链）"),
    "missing_source":  (WARN,  "缺来源（frontmatter sources 为空）"),
    "missing_concept": (WARN,  "缺概念页（被多页引用却无独立页）"),
    "outdated":        (WARN,  "过时（被 supersedes 取代仍 active）"),
    "slug_mismatch":   (WARN,  "slug 与文件名不一致"),
    "missing_dates":   (WARN,  "缺 created / updated"),
    "space_branding":  (WARN,  "enterprise-kb 通用语料含「茉莉」或项目 branding"),
    "dup_content":     (INFO,  "正文完全重复"),
    "near_dup":        (INFO,  "正文近似重复"),
    "asym_related":    (INFO,  "related 单向（不对称）"),
}


class Issue:
    __slots__ = ("level", "kind", "page", "detail", "suggest")

    def __init__(self, kind, page, detail="", suggest=""):
        self.level = KIND_META[kind][0]
        self.kind = kind
        self.page = page
        self.detail = detail
        self.suggest = suggest

    def to_dict(self):
        return {"level": self.level, "kind": self.kind, "page": self.page,
                "detail": self.detail, "suggest": self.suggest}


class Page:
    __slots__ = ("slug", "stem", "title", "kb_type", "status", "tags", "sources",
                 "related", "created", "updated", "meta_slug", "rel", "body",
                 "content_hash", "wikilinks", "has_title")

    def __init__(self, **kw):
        for k in self.__slots__:
            setattr(self, k, kw.get(k))


# ---------------------------------------------------------------------------
# 加载 wiki（含 index.md 链接，用于孤儿判定；log.md 不参与）
# ---------------------------------------------------------------------------

def _first_h1(body: str) -> str:
    for line in body.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return ""


def load_pages(wiki_dir: Path):
    """返回 (pages, by_slug, by_stem, dup_stems, index_links)。

    * pages：list[Page]，已排除 index.md / log.md。
    * by_slug：全路径 slug -> Page。
    * by_stem：裸名 -> 全路径 slug（撞名只留第一个，撞名记 dup_stems）。
    * index_links：index.md 正文里 [[..]] 的目标（裸名/全名），用于孤儿判定。
    """
    pages: list[Page] = []
    dup_stems: dict[str, list[str]] = {}
    index_links: list[str] = []
    if not wiki_dir.exists():
        return pages, {}, {}, {}, []

    for path in sorted(wiki_dir.rglob("*.md")):
        try:
            text = path.read_text(encoding="utf-8")
        except Exception as e:  # noqa: BLE001
            print(f"[warn] 读取失败 {path}: {e}")
            continue
        meta, body = parse_frontmatter(text)
        if _INDEX_STEM.match(path.stem):       # index / index-*：目录，链接算入链
            index_links.extend(extract_links_outside_code(body))
            continue
        if _META_STEM.match(path.stem) or path.stem in SPECIAL:  # log / log-*
            continue
        rel = path.relative_to(wiki_dir).as_posix()
        slug = rel[:-3] if rel.endswith(".md") else rel
        h1 = _first_h1(body)
        tags = meta.get("tags") if isinstance(meta.get("tags"), list) else []
        pages.append(Page(
            slug=slug,
            stem=path.stem,
            title=meta.get("title") or h1 or path.stem,
            kb_type=meta.get("type") or "concept",
            status=STATUS_MAP.get(str(meta.get("status", "")).lower(), 1),
            tags=[t for t in tags if t],
            sources=meta.get("sources", []) or [],
            related=meta.get("related", []) or [],
            created=meta.get("created"),
            updated=meta.get("updated"),
            meta_slug=meta.get("slug"),
            rel=rel,
            body=body,
            content_hash=hashlib.sha256(text.encode("utf-8")).hexdigest(),
            wikilinks=extract_links_outside_code(body),
            has_title=bool(meta.get("title") or h1),
        ))

    by_slug = {p.slug: p for p in pages}
    by_stem: dict[str, str] = {}
    stem_files: dict[str, list[str]] = {}
    for p in pages:
        stem_files.setdefault(p.stem, []).append(p.slug)
        by_stem.setdefault(p.stem, p.slug)
    dup_stems = {s: v for s, v in stem_files.items() if len(v) > 1}
    return pages, by_slug, by_stem, dup_stems, index_links


# ---------------------------------------------------------------------------
# 近似重复（MinHash-lite，标准库，opt-in）
# ---------------------------------------------------------------------------

_WORD = re.compile(r"[a-z0-9]+")


def _shingles(body: str) -> set[int]:
    """正文 → shingle 集合（hash 后的 int）。拉丁词 3-gram + CJK 4 字滑窗。"""
    txt = re.sub(r"\s+", "", body.lower())
    grams: set[str] = set()
    words = _WORD.findall(body.lower())
    for i in range(len(words) - 2):
        grams.add(" ".join(words[i:i + 3]))
    cjk = "".join(ch for ch in txt if "\u4e00" <= ch <= "\u9fff")
    for i in range(len(cjk) - 3):
        grams.add(cjk[i:i + 4])
    return {hash(g) & 0xFFFFFFFF for g in grams}


def _minhash(sh: set[int], k: int = 24) -> tuple[int, ...]:
    return tuple(sorted(sh)[:k]) if sh else ()


def detect_near_dups(pages, threshold: float):
    """用 MinHash-lite 生成候选对，再算真实 Jaccard，返回 [(a, b, sim)]。"""
    sigs = {}
    sh_cache = {}
    for p in pages:
        if len(p.body) < 200:  # 过短页跳过，避免噪声
            continue
        sh = _shingles(p.body)
        if not sh:
            continue
        sh_cache[p.slug] = sh
        sigs[p.slug] = _minhash(sh)

    # 倒排：minhash 值 -> slugs，共享 ≥2 个 minhash 即为候选
    inv: dict[int, list[str]] = {}
    for slug, sig in sigs.items():
        for h in sig:
            inv.setdefault(h, []).append(slug)
    cand: set[tuple[str, str]] = set()
    for slugs in inv.values():
        if len(slugs) < 2 or len(slugs) > 60:  # 跳过超大桶（模板公共片段）
            continue
        for i in range(len(slugs)):
            for j in range(i + 1, len(slugs)):
                a, b = sorted((slugs[i], slugs[j]))
                cand.add((a, b))

    out = []
    for a, b in cand:
        sa, sb = sh_cache[a], sh_cache[b]
        inter = len(sa & sb)
        if not inter:
            continue
        sim = inter / len(sa | sb)
        if sim >= threshold:
            out.append((a, b, round(sim, 3)))
    out.sort(key=lambda x: -x[2])
    return out


# ---------------------------------------------------------------------------
# 核心体检
# ---------------------------------------------------------------------------

def lint(wiki_dir: Path, *, missing_concept_min: int = 3, do_dups: bool = False,
         dup_threshold: float = 0.85, do_related: bool = False):
    pages, by_slug, by_stem, dup_stems, index_links = load_pages(wiki_dir)
    # 大小写无关的裸名映射：用于把「写法不符」从「真缺页」里区分出来
    by_stem_lower: dict[str, str] = {}
    for p in pages:
        by_stem_lower.setdefault(p.stem.lower(), p.slug)
    edges = load_edges(wiki_dir.parent / "graph" / "edges.jsonl") \
        if (wiki_dir.parent / "graph" / "edges.jsonl").exists() \
        else load_edges(KB_DIR / "wiki" / "graph" / "edges.jsonl")
    issues: list[Issue] = []

    # 入链统计（resolved 的 wikilink + related + edges + index 目录引用）
    inbound: dict[str, set[str]] = {p.slug: set() for p in pages}
    broken_targets: dict[str, set[str]] = {}  # 断链目标 -> 引用它的页 slug 集合

    def note_inbound(src_slug: str, target_raw: str):
        dst = resolve(target_raw, by_slug, by_stem)
        if dst is None:
            broken_targets.setdefault(target_raw.strip(), set()).add(src_slug)
            return None
        if dst != src_slug:
            inbound[dst].add(src_slug)
        return dst

    for p in pages:
        for tgt in p.wikilinks:
            note_inbound(p.slug, tgt)
        for tgt in p.related:
            note_inbound(p.slug, tgt)
    for frm, to, _ in edges:
        src = resolve(frm, by_slug, by_stem)
        if src is None:
            continue
        note_inbound(src, to)
    # index.md 目录引用也算入链（catalog 不算孤儿）
    for tgt in index_links:
        dst = resolve(tgt, by_slug, by_stem)
        if dst:
            inbound[dst].add("index")

    # --- 逐页结构 / frontmatter 检查 ---
    for p in pages:
        if not p.has_title:
            issues.append(Issue("missing_title", p.slug,
                                 "无 frontmatter title 且正文无 H1",
                                 "补 frontmatter title 或正文首行加 `# 标题`"))
        if p.kb_type not in KB_TYPES:
            issues.append(Issue("bad_type", p.slug, f"type={p.kb_type!r}",
                                 f"改为白名单之一：{'|'.join(sorted(KB_TYPES))}"))
        if not p.sources:
            issues.append(Issue("missing_source", p.slug, "sources 为空",
                                 "补 frontmatter sources（raw 路径或 URL），保证可追溯"))
        if p.meta_slug and p.meta_slug != p.stem:
            issues.append(Issue("slug_mismatch", p.slug,
                                 f"frontmatter slug={p.meta_slug!r} ≠ 文件名 {p.stem!r}",
                                 "把 frontmatter slug 改成与文件名一致，避免 [[]] 解析歧义"))
        if not p.created or not p.updated:
            miss = [k for k in ("created", "updated") if not getattr(p, k)]
            issues.append(Issue("missing_dates", p.slug, f"缺 {', '.join(miss)}",
                                 "补 frontmatter created/updated（YYYY-MM-DD）"))
        parts = p.slug.replace("\\", "/").split("/")
        if wiki_dir.name == "wiki" and parts and parts[0] in ("articles", "concepts", "interview"):
            if "茉莉" in (p.body or ""):
                issues.append(Issue(
                    "space_branding", p.slug,
                    "enterprise-kb 通用语料正文含「茉莉」",
                    "删改 branding 或移到 wiki-moli（AGENTS.md §1.0.1；kb_space_governance.py）",
                ))

    # --- slug 歧义 ---
    for stem, slugs in sorted(dup_stems.items()):
        issues.append(Issue("dup_slug", None,
                             f"裸名 [[{stem}]] 对应 {len(slugs)} 个文件：{', '.join(slugs)}",
                             "重命名其中之一，或用全路径 [[目录/名]] 引用"))

    # --- 断链 + 缺概念页 ---
    for tgt, refs in sorted(broken_targets.items()):
        # 大小写/写法不符（目标其实存在，只是 slug 大小写或前缀写错）——可批量修
        ci = by_stem_lower.get(tgt.split("/")[-1].lower())
        if ci:
            suggest = f"写法/大小写不符，应为 [[{ci}]]（目标页存在）"
            for src in sorted(refs):
                issues.append(Issue("broken_link", src,
                                    f"[[{tgt}]] 解析不到（疑似 → {ci}）", suggest))
            continue
        if len(refs) >= missing_concept_min:
            issues.append(Issue("missing_concept", None,
                                 f"[[{tgt}]] 被 {len(refs)} 个页引用却无独立页",
                                 f"新建 concepts/{tgt}.md 收编该概念（引用方：{_short(refs)}）"))
        else:
            for src in sorted(refs):
                issues.append(Issue("broken_link", src, f"[[{tgt}]] 解析不到",
                                    "修正目标 slug，或创建被引用页"))

    # --- 孤儿页 ---
    for p in pages:
        if not inbound[p.slug]:
            issues.append(Issue("orphan", p.slug, "无任何入链（含 index 目录）",
                                "在相关页补 [[]] 引用，或加入 index.md 目录"))

    # --- 过时（supersedes）---
    for frm, to, etype in edges:
        if etype != "supersedes":
            continue
        old = resolve(to, by_slug, by_stem)
        new = resolve(frm, by_slug, by_stem)
        if old and by_slug[old].status == STATUS_MAP["active"]:
            issues.append(Issue("outdated", old,
                                 f"被 [[{new or frm}]] supersedes，但仍 status: active",
                                 "把本页 status 改为 archived，或在正文标注被取代"))

    # --- related 不对称（opt-in）---
    if do_related:
        rel_set = {p.slug: {resolve(t, by_slug, by_stem) for t in p.related}
                   for p in pages}
        for p in pages:
            for dst in rel_set[p.slug]:
                if dst and p.slug not in rel_set.get(dst, set()):
                    issues.append(Issue("asym_related", p.slug,
                                        f"related 含 [[{dst}]]，但对方未回指",
                                        "在对方页 frontmatter related 补本页，保持双向"))

    # --- 重复（opt-in）---
    if do_dups:
        seen_hash: dict[str, str] = {}
        for p in pages:
            if p.content_hash in seen_hash:
                issues.append(Issue("dup_content", p.slug,
                                    f"正文与 [[{seen_hash[p.content_hash]}]] 完全相同",
                                    "合并为一页或删除其一"))
            else:
                seen_hash[p.content_hash] = p.slug
        for a, b, sim in detect_near_dups(pages, dup_threshold):
            issues.append(Issue("near_dup", a,
                                f"与 [[{b}]] 近似（Jaccard={sim}）",
                                "考虑合并、或抽公共部分到概念页后互链"))

    stats = {
        "pages": len(pages),
        "issues": len(issues),
        "errors": sum(1 for i in issues if i.level == ERROR),
        "warnings": sum(1 for i in issues if i.level == WARN),
        "infos": sum(1 for i in issues if i.level == INFO),
    }
    by_kind: dict[str, int] = {}
    for i in issues:
        by_kind[i.kind] = by_kind.get(i.kind, 0) + 1
    stats["by_kind"] = by_kind
    return issues, stats


def _short(refs, n: int = 4) -> str:
    s = sorted(refs)
    return ", ".join(s[:n]) + (f" …(+{len(s) - n})" if len(s) > n else "")


# ---------------------------------------------------------------------------
# 输出
# ---------------------------------------------------------------------------

def print_console(issues, stats, top: int):
    print("=" * 64)
    print(f"茉莉知识库体检：{stats['pages']} 页 · "
          f"问题 {stats['issues']}（错误 {stats['errors']} · "
          f"告警 {stats['warnings']} · 参考 {stats['infos']}）")
    print("=" * 64)
    if not issues:
        print("✓ 全部通过，未发现问题。")
        return
    # 按 kind 分组（kind 内按 level 已固定）
    groups: dict[str, list[Issue]] = {}
    for i in issues:
        groups.setdefault(i.kind, []).append(i)
    order = sorted(groups, key=lambda k: (LEVEL_ORDER[KIND_META[k][0]], k))
    for kind in order:
        items = groups[kind]
        level, label = KIND_META[kind]
        print(f"\n{LEVEL_LABEL[level]} · {label}  ({len(items)})")
        for it in items[:top]:
            where = f"[[{it.page}]] " if it.page else ""
            print(f"  - {where}{it.detail}")
            if it.suggest:
                print(f"      ↳ 建议：{it.suggest}")
        if len(items) > top:
            print(f"  …… 还有 {len(items) - top} 条（--top 调整 / --json 看全量）")


def build_markdown(issues, stats, wiki_dir: Path) -> str:
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    lines = [
        "# 知识库体检报告（Lint）",
        "",
        f"> 生成：{now} ｜ 目录：`{wiki_dir.as_posix()}` ｜ 工具：`kb/tools/lint.py`",
        "> 本报告由 Agent 治理自动化生成，**只报告不改写**；修复需人确认。",
        "",
        f"- 页数：**{stats['pages']}**",
        f"- 问题：**{stats['issues']}**（错误 {stats['errors']} · 告警 "
        f"{stats['warnings']} · 参考 {stats['infos']}）",
        "",
        "| 类型 | 级别 | 数量 |",
        "|------|------|------|",
    ]
    for kind, n in sorted(stats["by_kind"].items(),
                          key=lambda kv: (LEVEL_ORDER[KIND_META[kv[0]][0]], kv[0])):
        level, label = KIND_META[kind]
        lines.append(f"| {label} | {LEVEL_LABEL[level]} | {n} |")
    groups: dict[str, list[Issue]] = {}
    for i in issues:
        groups.setdefault(i.kind, []).append(i)
    order = sorted(groups, key=lambda k: (LEVEL_ORDER[KIND_META[k][0]], k))
    for kind in order:
        level, label = KIND_META[kind]
        items = groups[kind]
        lines += ["", f"## {LEVEL_LABEL[level]} · {label}（{len(items)}）", ""]
        for it in items:
            where = f"`{it.page}` " if it.page else ""
            lines.append(f"- {where}{it.detail}"
                         + (f" — _建议_：{it.suggest}" if it.suggest else ""))
    lines.append("")
    return "\n".join(lines)


def append_log(stats):
    log = KB_DIR / "wiki" / "log.md"
    bk = stats["by_kind"]
    summary = " ".join(f"{k}={v}" for k, v in sorted(bk.items())) or "clean"
    line = (f"## [{datetime.now().strftime('%Y-%m-%d')}] lint | "
            f"{stats['pages']}页 err={stats['errors']} warn={stats['warnings']} "
            f"info={stats['infos']} | {summary}\n")
    with log.open("a", encoding="utf-8") as f:
        f.write(line)
    return log


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main() -> int:
    ap = argparse.ArgumentParser(description="茉莉知识库 Lint / 知识治理体检")
    ap.add_argument("--wiki-dir", default=None,
                    help="wiki 目录（相对 kb/ 或绝对路径），默认 kb/wiki")
    ap.add_argument("--json", dest="json_path", default=None,
                    help="同时输出机器可读 JSON 到指定文件")
    ap.add_argument("--report", nargs="?", const=str(KB_DIR / "lint-report.md"),
                    default=None, help="输出 markdown 报告（默认 kb/lint-report.md）")
    ap.add_argument("--log", action="store_true",
                    help="向 kb/wiki/log.md 追加一行体检记录")
    ap.add_argument("--dups", action="store_true",
                    help="启用重复 / 近似重复检测（较慢）")
    ap.add_argument("--dup-threshold", type=float, default=0.85,
                    help="近似重复 Jaccard 阈值（默认 0.85）")
    ap.add_argument("--related", action="store_true",
                    help="检测 related 单向（不对称）")
    ap.add_argument("--missing-concept-min", type=int, default=3,
                    help="断链目标被引用达到此数量时升级为『缺概念页』（默认 3）")
    ap.add_argument("--top", type=int, default=30,
                    help="控制台每类最多展示条数（默认 30）")
    ap.add_argument("--strict", action="store_true",
                    help="退出码：有 ERROR 即 1；本开关下 WARN 也算失败")
    args = ap.parse_args()

    if args.wiki_dir:
        wiki_dir = Path(args.wiki_dir)
        if not wiki_dir.is_absolute():
            wiki_dir = KB_DIR / args.wiki_dir
    else:
        wiki_dir = DEFAULT_WIKI_DIR

    issues, stats = lint(
        wiki_dir,
        missing_concept_min=args.missing_concept_min,
        do_dups=args.dups,
        dup_threshold=args.dup_threshold,
        do_related=args.related,
    )

    print_console(issues, stats, args.top)

    if args.json_path:
        Path(args.json_path).write_text(
            json.dumps({"stats": stats, "issues": [i.to_dict() for i in issues]},
                       ensure_ascii=False, indent=2),
            encoding="utf-8")
        print(f"\n[json] 已写入 {args.json_path}")
    if args.report:
        Path(args.report).write_text(
            build_markdown(issues, stats, wiki_dir), encoding="utf-8")
        print(f"[report] 已写入 {args.report}")
    if args.log:
        log = append_log(stats)
        print(f"[log] 已追加体检记录 → {log}")

    fail = stats["errors"] > 0 or (args.strict and stats["warnings"] > 0)
    if fail:
        print(f"\n[FAIL] 体检未通过"
              f"（errors={stats['errors']}"
              f"{', warnings=' + str(stats['warnings']) if args.strict else ''}）")
        return 1
    print("\n[OK] 体检通过（无阻断级问题）")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
