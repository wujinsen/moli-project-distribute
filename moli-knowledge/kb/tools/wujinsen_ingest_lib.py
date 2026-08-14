#!/usr/bin/env python3
"""Shared helpers for wujinsen → enterprise-kb ingest scripts."""
from __future__ import annotations

import os
import re
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"


def norm(p: str) -> str:
    return p.replace("\\", "/")


def should_skip_file(fn: str) -> bool:
    if "同步发生冲突" in fn:
        return True
    if fn == "dfsdfa.note.md":
        return True
    if fn.endswith(".note.attach"):
        return True
    if "无标题笔记" in fn and fn.count("无标题") >= 1:
        return False  # still cite if in folder; filter at prefix level if needed
    return False


def find_prefix_by_marker(marker: str) -> str | None:
    """Return top-level or nested prefix dir containing marker file."""
    for f in RAW.rglob(marker):
        if f.is_file():
            return norm(os.path.relpath(f.parent, RAW))
    return None


def list_raw_md(prefix: str) -> list[str]:
    root = RAW / prefix.replace("/", os.sep)
    if not root.exists():
        return []
    out: list[str] = []
    if root.is_file() and str(root).endswith(".md"):
        rel = norm(os.path.relpath(root, RAW))
        return [f"raw/wujinsen_markdown/{rel}"]
    for dp, _, fns in os.walk(root):
        for fn in fns:
            if not fn.endswith(".md") or should_skip_file(fn):
                continue
            rel = norm(os.path.relpath(os.path.join(dp, fn), RAW))
            out.append(f"raw/wujinsen_markdown/{rel}")
    return sorted(out)


def list_raw_md_match(prefix: str, *needles: str) -> list[str]:
    """Filter raw paths under prefix whose relative path contains any needle."""
    if not needles:
        return list_raw_md(prefix)
    hits: list[str] = []
    for src in list_raw_md(prefix):
        rel = src.split("wujinsen_markdown/", 1)[-1]
        if any(n in rel for n in needles):
            hits.append(src)
    return hits


def collect_sources(prefixes: list[str], *needles: str) -> list[str]:
    out: list[str] = []
    for p in prefixes:
        if needles:
            out.extend(list_raw_md_match(p, *needles))
        else:
            out.extend(list_raw_md(p))
    return sorted(set(out))


def slug_to_path(slug: str) -> Path:
    cat, stem = slug.split("/", 1)
    return WIKI / cat / f"{stem}.md"


def split_frontmatter(text: str) -> tuple[str, str]:
    fm_end = text.index("---", 3)
    return text[: fm_end + 4], text[fm_end + 4 :]


def get_sources(fm: str) -> list[str]:
    block = re.search(r"^sources:\n((?:[ \t]+-[^\n]+\n?)*)", fm, re.M)
    if not block:
        return []
    return [ln.strip()[2:].strip() for ln in block.group(1).splitlines() if ln.strip().startswith("-")]


def set_sources(fm: str, sources: list[str]) -> str:
    lines = ["sources:"] + [f" - {s}" for s in sources]
    if re.search(r"^sources:\n", fm, re.M):
        return re.sub(
            r"^sources:\n(?:[ \t]+-[^\n]+\n?)*",
            "\n".join(lines) + "\n",
            fm,
            count=1,
            flags=re.M,
        )
    return fm.replace("---\n", "---\n" + "\n".join(lines) + "\n", 1)


def set_updated(fm: str, today: str) -> str:
    if re.search(r"^updated:", fm, re.M):
        return re.sub(r"^updated:.*$", f"updated: {today}", fm, flags=re.M)
    return fm.replace("---\n", f"---\nupdated: {today}\n", 1)


def append_batch(body: str, batch: str, label: str, note: str) -> str:
    marker = f"## 批次{batch} 增补（{label}）"
    if marker in body:
        return body
    return body.rstrip() + f"\n\n{marker}\n\n{note}\n"


def append_log(today: str, batch: str, line_body: str) -> None:
    log_path = WIKI / "log.md"
    if not log_path.exists():
        return
    content = log_path.read_text(encoding="utf-8")
    if batch in content:
        return
    log_path.write_text(content.rstrip() + f"\n## [{today}] ingest | {line_body}\n", encoding="utf-8")


def enrich_wiki_page(
    slug: str,
    new_sources: set[str],
    today: str,
    batch: str,
    label: str,
    note: str | None = None,
    body_extra: str | None = None,
) -> bool:
    path = slug_to_path(slug)
    if not path.exists():
        print("SKIP missing wiki:", slug)
        return False
    text = path.read_text(encoding="utf-8")
    fm, body = split_frontmatter(text)
    old = get_sources(fm)
    merged = sorted(set(old) | new_sources)
    changed = merged != old or note or body_extra
    if not changed:
        return False
    fm = set_sources(fm, merged)
    fm = set_updated(fm, today)
    if body_extra and body_extra.strip() not in body:
        body = body.rstrip() + body_extra + "\n"
    if note:
        body = append_batch(body, batch, label, note)
    path.write_text(fm + body, encoding="utf-8")
    print(f"OK {slug}: sources {len(old)} -> {len(merged)}")
    return True


def build_slug_sources(
    single: dict[str, str],
    multi: dict[str, list[str]] | None = None,
    file_map: dict[str, list[str]] | None = None,
) -> dict[str, set[str]]:
    mapping: dict[str, set[str]] = {}

    def add(slug: str, srcs: list[str]) -> None:
        mapping.setdefault(slug, set()).update(srcs)

    for prefix, slug in single.items():
        add(slug, list_raw_md(prefix))

    for prefix, slugs in (multi or {}).items():
        srcs = list_raw_md(prefix)
        for slug in slugs:
            add(slug, srcs)

    for slug, rel_paths in (file_map or {}).items():
        for rel in rel_paths:
            add(slug, [f"raw/wujinsen_markdown/{norm(rel)}"])

    return mapping


def apply_enrich_batch(
    slug_sources: dict[str, set[str]],
    today: str,
    batch: str,
    label: str,
    notes: dict[str, str],
    body_extras: dict[str, str] | None = None,
) -> list[str]:
    touched: list[str] = []
    extras = body_extras or {}
    for slug in sorted(slug_sources):
        if enrich_wiki_page(
            slug,
            slug_sources[slug],
            today,
            batch,
            label,
            notes.get(slug),
            extras.get(slug),
        ):
            touched.append(slug)
    return touched
