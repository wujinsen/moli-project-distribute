#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Evaluate wiki dir_slug vs kb_category and kb_type usage."""
import re
from collections import Counter, defaultdict
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
WIKI = KB / "wiki"
RAW = KB / "raw" / "wujinsen_markdown"

# Current enterprise-kb taxonomy (post bigdata)
CATEGORIES = {
    "database": "数据库",
    "cache": "缓存与 Redis",
    "java": "Java 与 JVM",
    "middleware": "微服务与中间件",
    "spring": "Spring 生态",
    "search": "搜索与 ES",
    "security": "网络与安全",
    "ops": "运维与 Linux",
    "patterns": "设计模式",
    "frontend": "前端",
    "bigdata": "大数据",
}

KB_TYPES = {"guide", "service", "concept", "article", "interview", "output"}
TYPE_LABELS = {
    "guide": "操作指南",
    "service": "服务实体",
    "concept": "概念",
    "article": "文章",
    "interview": "面试题",
    "output": "汇总",
}

# Remaining wujinsen top-level -> recommended mapping (ingest or skip)
RAW_TOP_MAP = {
    "AI": ("skip", "绘画/工具剪藏，非 enterprise 八股"),
    "产品": ("skip", "PM 方法论"),
    "写作": ("skip", "非技术 KB"),
    "硬件": ("skip", ""),
    "操作系统": ("skip", ""),
    "EnglishDoc": ("skip", ""),
    "Full Stack": ("skip", ""),
    "IM通讯": ("skip", ""),
    "学习方法": ("skip", ""),
    "英语学习": ("skip", ""),
    "大数据资料-王/QA": ("skip", "测试题海"),
    "大数据资料-王/loadrunner": ("skip", "压测工具"),
    "大数据资料-王/selecnium": ("skip", "自动化测试"),
}


def parse_fm(path: Path) -> dict:
    text = path.read_text(encoding="utf-8")
    m = re.match(r"^---\n(.*?)\n---", text, re.S)
    if not m:
        return {}
    meta = m.group(1)

    def g(key: str) -> str:
        mm = re.search(rf"^{key}:\s*(.+)$", meta, re.M)
        return mm.group(1).strip() if mm else ""

    return {"type": g("type"), "slug": g("slug") or path.stem}


def main() -> None:
    wiki_dirs: set[str] = set()
    type_counts: Counter = Counter()
    invalid_types: list[str] = []
    pages_by_dir: dict[str, int] = defaultdict(int)

    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        rel = p.relative_to(WIKI)
        if len(rel.parts) < 2:
            continue
        top = rel.parts[0]
        wiki_dirs.add(top)
        pages_by_dir[top] += 1
        meta = parse_fm(p)
        t = meta.get("type", "").lower()
        if t in KB_TYPES:
            type_counts[t] += 1
        elif t:
            invalid_types.append(f"{top}/{p.stem}: type={t}")

    missing_cat = sorted(wiki_dirs - set(CATEGORIES))
    extra_cat = sorted(set(CATEGORIES) - wiki_dirs)

    print("=== enterprise-kb 分类 (dir_slug) ===")
    print(f"已定义 kb_category: {len(CATEGORIES)}")
    print(f"wiki 一级目录: {len(wiki_dirs)}")
    for slug in sorted(CATEGORIES):
        n = pages_by_dir.get(slug, 0)
        flag = "OK" if slug in wiki_dirs else "(无 wiki 页)"
        print(f"  {slug:<12} {CATEGORIES[slug]:<14} wiki={n:3d}  {flag}")
    if missing_cat:
        print("\n[需扩充分类] wiki 目录未在 kb_category:")
        for d in missing_cat:
            print(f"  - {d} ({pages_by_dir[d]} 页)")
    if extra_cat:
        print("\n[空分类] 已建分类但 wiki 无页:", ", ".join(extra_cat))

    print("\n=== 体裁 (kb_type) ===")
    print(f"白名单: {len(KB_TYPES)} 种（无需按 raw 目录扩充分类）")
    for t in ["guide", "concept", "article", "interview", "service", "output"]:
        c = type_counts.get(t, 0)
        note = "" if c else " (enterprise-kb 暂无，UI 不展示 chip)"
        print(f"  {TYPE_LABELS[t]:<8} {t:<10} {c:3d}{note}")
    if invalid_types:
        print("\n[非法 type]", invalid_types[:10])

    # raw uncited by top
    cited: set[str] = set()
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        for s in re.findall(r"raw/wujinsen_markdown/([^\n]+)", p.read_text(encoding="utf-8")):
            cited.add(s.replace("\\", "/").strip())

    raw_by_top: Counter = Counter()
    uncited_by_top: Counter = Counter()
    for p in RAW.rglob("*.md"):
        rel = p.relative_to(RAW).as_posix()
        top = rel.split("/")[0]
        raw_by_top[top] += 1
        if rel not in cited:
            uncited_by_top[top] += 1

    print("\n=== wujinsen 剩余 raw（未 cited）按一级目录 ===")
    print("结论：剩余 bulk 应 skip 或并入已有 11 分类，不需新 dir_slug\n")
    for top, total in raw_by_top.most_common():
        u = uncited_by_top[top]
        if u == 0:
            continue
        rec = RAW_TOP_MAP.get(top, ("enrich-existing", f"并入 middleware/java/ops/bigdata 等已有 slug"))
        print(f"  {top}: uncited {u}/{total} -> {rec[0]} ({rec[1]})")


if __name__ == "__main__":
    main()
