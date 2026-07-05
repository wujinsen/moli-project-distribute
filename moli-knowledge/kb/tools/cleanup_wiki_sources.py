#!/usr/bin/env python3
"""#1334: prune orphan sources, remove scaffold, fix corrupt frontmatter."""
from __future__ import annotations

import re
from pathlib import Path

from repair_wiki_lint import fix_related_extended, replace_concepts
from wujinsen_ingest_lib import append_log

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"
TODAY = "2026-07-05"
BATCH = "#1334"
SCAFFOLD_RE = re.compile(r"^enterprise-kb/[\w-]+ scaffold$")
UNCITED = "架构/DevOps/nexus/maven---nexus私服配置setting和pom.note.md"
MAVEN_SLUG = "ops/maven-多模块与依赖管理"


def norm(p: str) -> str:
    return p.replace("\\", "/")


def first_h1(body: str) -> str:
    for line in body.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return ""


def raw_path_exists(source: str) -> bool:
    if not source.startswith("raw/wujinsen_markdown/"):
        return True
    rel = source[len("raw/wujinsen_markdown/") :]
    return (RAW / rel.replace("/", "\\")).is_file()


def resolve_wujinsen_source(source: str) -> str | None:
    if not source.startswith("raw/wujinsen_markdown/"):
        return source if source else None
    if raw_path_exists(source):
        return source
    rel = source[len("raw/wujinsen_markdown/") :]
    parent = RAW / Path(rel).parent
    stem = Path(rel).name
    if not parent.is_dir():
        return None
    # exact suffix merge (maven---nexus...)
    for f in sorted(parent.glob("*.md")):
        fn = f.name
        if fn.startswith(stem) or stem in fn or fn.startswith(stem.replace("---", "")):
            return f"raw/wujinsen_markdown/{norm(str(f.relative_to(RAW)))}"
    return None


def extract_body(text: str) -> str:
    hash_m = re.search(r"\n(# .+)", text, re.S)
    if not hash_m:
        return text
    before = text[: hash_m.start()]
    sep = before.rfind("\n---\n")
    if sep >= 0:
        return text[sep + 5 :]
    return text[hash_m.start() + 1 :]


def extract_meta_lines(text: str) -> dict[str, str | list[str]]:
    body = extract_body(text)
    fm_region = text[: len(text) - len(body)] if body in text else text
    meta: dict[str, str | list[str]] = {"sources": []}
    key: str | None = None
    for line in fm_region.splitlines():
        if line.strip() == "---":
            continue
        m = re.match(r"^\s*-\s+(.*)$", line)
        if m:
            val = m.group(1).strip()
            if isinstance(meta.get("sources"), list):
                meta["sources"].append(val)
            continue
        m2 = re.match(r"^([A-Za-z_][\w-]*):\s*(.*)$", line)
        if not m2:
            continue
        key, val = m2.group(1), m2.group(2).strip()
        if val == "":
            meta[key] = []
        elif val.startswith("[") and val.endswith("]"):
            inner = val[1:-1].strip()
            meta[key] = [x.strip() for x in inner.split(",") if x.strip()] if inner else []
        else:
            meta[key] = val
    return meta


def clean_sources(sources: list[str]) -> list[str]:
    out: list[str] = []
    seen: set[str] = set()
    for s in sources:
        s = s.strip()
        if not s or SCAFFOLD_RE.match(s):
            continue
        resolved = resolve_wujinsen_source(s)
        if not resolved:
            continue
        if resolved in seen:
            continue
        seen.add(resolved)
        out.append(resolved)
    return sorted(out)


def rebuild_fm(meta: dict, path: Path, body: str) -> str:
    title = meta.get("title") or first_h1(body) or path.stem
    slug = meta.get("slug") or path.stem
    kb_type = meta.get("type") or "article"
    status = meta.get("status") or "active"
    tags = meta.get("tags") if isinstance(meta.get("tags"), list) else []
    related = meta.get("related") if isinstance(meta.get("related"), list) else []
    sources = clean_sources(meta.get("sources") or [])
    if not sources:
        sources = [f"raw/wujinsen_markdown/ (enterprise-kb/{path.parent.name} 专题页)"]
    created = meta.get("created") or TODAY
    updated = TODAY
    lines = [
        "---",
        f"title: {title}",
        f"slug: {slug}",
        f"type: {kb_type}",
        f"status: {status}",
    ]
    if tags:
        lines.append(f"tags: [{', '.join(tags)}]")
    lines.append("sources:")
    for s in sources:
        lines.append(f"- {s}")
    if related:
        rel_line = fix_related_extended(f"related: [{', '.join(related)}]")
        lines.append(rel_line)
    lines.append(f"created: {created}")
    lines.append(f"updated: {updated}")
    lines.append("---")
    return "\n".join(lines) + "\n\n"


def fix_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    body = extract_body(text)
    meta = extract_meta_lines(text)
    new_fm = rebuild_fm(meta, path, body)
    body = replace_concepts(body.lstrip("\n"))
    new_text = new_fm + body
    if new_text == text:
        return False
    path.write_text(new_text, encoding="utf-8")
    return True


def enrich_maven_nexus() -> None:
    path = WIKI / "ops" / "maven-多模块与依赖管理.md"
    if not path.exists():
        return
    text = path.read_text(encoding="utf-8")
    body = extract_body(text)
    meta = extract_meta_lines(text)
    src = f"raw/wujinsen_markdown/{UNCITED}"
    sources = clean_sources(list(meta.get("sources") or []) + [src])
    meta["sources"] = sources
    meta["title"] = meta.get("title") or "Maven 多模块与依赖管理"
    meta["slug"] = "maven-多模块与依赖管理"
    meta["type"] = meta.get("type") or "guide"
    path.write_text(rebuild_fm(meta, path, body) + replace_concepts(body.lstrip("\n")), encoding="utf-8")
    print("enriched", MAVEN_SLUG)


def main() -> None:
    changed = 0
    for p in sorted(WIKI.rglob("*.md")):
        if p.name in ("index.md", "log.md"):
            continue
        if fix_file(p):
            changed += 1
    enrich_maven_nexus()
    append_log(
        TODAY,
        BATCH,
        f"批次{BATCH} prune orphan sources + 去 scaffold + 重建 frontmatter {changed} 页 + 补 nexus raw",
    )
    print("rebuilt", changed)


if __name__ == "__main__":
    main()
