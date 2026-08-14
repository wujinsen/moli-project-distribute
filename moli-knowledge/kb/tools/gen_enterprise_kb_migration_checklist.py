#!/usr/bin/env python3
"""Generate grouped migration checklist from enterprise_kb_migration_draft.csv."""
from __future__ import annotations

import csv
from collections import defaultdict
from pathlib import Path

CSV = Path(__file__).resolve().parent / "enterprise_kb_migration_draft.csv"
OUT = Path(__file__).resolve().parent / "enterprise_kb_migration_checklist.md"

CATEGORY_ORDER = [
    "database",
    "cache",
    "java",
    "middleware",
    "spring",
    "search",
    "security",
    "ops",
    "patterns",
    "frontend",
    "uncategorized",
]

TYPE_ORDER = ["concept", "article", "interview", "output", "guide", "service"]
TYPE_LABEL = {
    "concept": "概念枢纽",
    "article": "文章",
    "interview": "面试题",
    "output": "汇总",
    "guide": "指南",
    "service": "服务",
}


def load_rows() -> list[dict]:
    with CSV.open(encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def is_multi_rule(reason: str) -> bool:
    return "," in reason and reason.startswith("rule:")


def main() -> None:
    rows = load_rows()
    by_cat: dict[str, list[dict]] = defaultdict(list)
    for r in rows:
        by_cat[r["new_dir_slug"]].append(r)

    lines: list[str] = [
        "# enterprise-kb 目录迁移 · 分组检查清单",
        "",
        "> 来源：`enterprise_kb_migration_draft.csv`（定稿前请在 CSV 的 `review_note` 列标注后再迁移）  ",
        "> 空间：`enterprise-kb` · wiki 源目录 `kb/wiki/`  ",
        "> 原则：**只改首段目录**，`kb_type`（frontmatter `type:`）保持不变。",
        "",
        "## 总览",
        "",
        "| new_dir_slug | 分类名 | 合计 | concept | article | interview | 待复核* |",
        "|--------------|--------|------|---------|---------|-----------|---------|",
    ]

    total_review = 0
    for slug in CATEGORY_ORDER:
        items = by_cat.get(slug, [])
        if not items:
            continue
        name = items[0]["new_category_name"]
        tc = sum(1 for i in items if i["kb_type"] == "concept")
        ta = sum(1 for i in items if i["kb_type"] == "article")
        ti = sum(1 for i in items if i["kb_type"] == "interview")
        tr = sum(1 for i in items if is_multi_rule(i["assign_reason"]))
        total_review += tr
        lines.append(
            f"| `{slug}` | {name} | {len(items)} | {tc} | {ta} | {ti} | {tr} |"
        )

    lines.extend(
        [
            "",
            "\\* **待复核**：`assign_reason` 命中多条规则，迁移前建议人工确认目录。",
            "",
            "## 迁移前通用检查（每类完成后打勾）",
            "",
            "- [ ] Web 已建分类（`dir_slug` + 中文名）",
            "- [ ] `git mv` 本类全部文件到新目录",
            "- [ ] `lint.py --strict` 无新增断链",
            "- [ ] `sync_to_db.py --wiki-dir wiki --space enterprise-kb`",
            "- [ ] Web 验证：分类 chip + 体裁 chip + 列表 AND 筛选",
            "",
            "---",
            "",
        ]
    )

    for cat_slug in CATEGORY_ORDER:
        items = by_cat.get(cat_slug, [])
        if not items:
            continue
        cat_name = items[0]["new_category_name"]
        by_type: dict[str, list[dict]] = defaultdict(list)
        for i in items:
            by_type[i["kb_type"]].append(i)

        lines.append(f"## `{cat_slug}` · {cat_name}（{len(items)} 篇）")
        lines.append("")
        lines.append(
            f"**Web 分类**：`dir_slug={cat_slug}` · "
            f"concept {len(by_type.get('concept', []))} · "
            f"article {len(by_type.get('article', []))} · "
            f"interview {len(by_type.get('interview', []))}"
        )
        lines.append("")

        # concept hubs first — migration anchors
        concepts = by_type.get("concept", [])
        if concepts:
            lines.append("### 概念枢纽（优先核对互链）")
            lines.append("")
            for i in sorted(concepts, key=lambda x: x["new_slug"]):
                flag = " ⚠️" if is_multi_rule(i["assign_reason"]) else ""
                lines.append(
                    f"- [ ] `{i['new_slug']}` — {i['title']}{flag}"
                )
            lines.append("")

        for kb_type in TYPE_ORDER:
            if kb_type == "concept":
                continue
            group = by_type.get(kb_type, [])
            if not group:
                continue
            lines.append(f"### {TYPE_LABEL.get(kb_type, kb_type)}（{len(group)}）")
            lines.append("")
            for i in sorted(group, key=lambda x: x["new_slug"]):
                flag = " ⚠️" if is_multi_rule(i["assign_reason"]) else ""
                note = i.get("review_note", "").strip()
                suffix = f" · _{note}_" if note else ""
                lines.append(
                    f"- [ ] `{i['old_slug']}` → `{i['new_slug']}`{flag}{suffix}"
                )
            lines.append("")

        # cross-type topic sanity (same stem prefix clusters)
        lines.append("### 本类自检")
        lines.append("")
        lines.append("- [ ] 同主题 concept / article / interview 是否都在本目录（如 mysql-* 三件套）")
        lines.append("- [ ] 概念枢纽 `related` / 正文 `[[..]]` 无 `concepts/` `articles/` `interview/` 旧前缀")
        lines.append("- [ ] 无遗漏进 `uncategorized`")
        lines.append("")
        lines.append("---")
        lines.append("")

    lines.extend(
        [
            "## 全库边界篇复核清单（⚠️ 多规则命中）",
            "",
            "以下篇目在 CSV 中 `assign_reason` 含多条规则，定稿时请确认 `new_dir_slug`：",
            "",
        ]
    )
    for r in sorted(rows, key=lambda x: x["new_slug"]):
        if is_multi_rule(r["assign_reason"]):
            lines.append(
                f"- [ ] `{r['new_slug']}` — {r['title']} "
                f"（{r['assign_reason']}）"
            )

    lines.extend(["", "## 旧目录退役", "", "- [ ] 确认 `concepts/`、`articles/`、`interview/` 已空", "- [ ] SQL 物理删旧 `kb_category`（concepts/articles/interview 三条）", "- [ ] 再 sync 一次确认文档 category_id 已指向新主题分类", ""])

    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote: {OUT}")


if __name__ == "__main__":
    main()
