#!/usr/bin/env python3
"""Analyze wiki-moli broken links and suggest fixes."""
from __future__ import annotations

import json
import re
from collections import Counter, defaultdict
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
WIKI = KB / "wiki"
WIKI_MOLI = KB / "wiki-moli"
LINT_JSON = KB / "lint-wiki-moli.json"


def index(wiki_dir: Path) -> tuple[dict[str, str], dict[str, Path]]:
    by_stem: dict[str, str] = {}
    by_slug: dict[str, Path] = {}
    for p in wiki_dir.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        rel = p.relative_to(wiki_dir).with_suffix("").as_posix()
        by_slug[rel] = p
        by_stem[p.stem] = rel
        by_stem[rel.split("/")[-1]] = rel
    return by_stem, by_slug


def resolve_target(target: str, by_stem: dict[str, str]) -> str | None:
    if target in by_stem:
        return by_stem[target]
    stem = target.split("/")[-1]
    return by_stem.get(stem)


def main() -> None:
    data = json.loads(LINT_JSON.read_text(encoding="utf-8"))
    issues = [i for i in data["issues"] if i["kind"] == "broken_link"]
    ent_stem, _ = index(WIKI)
    moli_stem, _ = index(WIKI_MOLI)

    targets: list[str] = []
    refs: dict[str, list[str]] = defaultdict(list)
    for issue in issues:
        m = re.search(r"\[\[([^\]]+)\]\]", issue["detail"])
        if not m:
            continue
        t = m.group(1)
        targets.append(t)
        refs[t].append(issue["page"])

    out = KB / "tools" / "moli_broken_link_map.json"
    mapping: dict[str, dict] = {}
    for t in sorted(set(targets)):
        ent = resolve_target(t, ent_stem)
        moli = resolve_target(t, moli_stem)
        if moli:
            fix = {"type": "moli_slug", "value": moli}
        elif ent:
            fix = {"type": "enterprise_slug", "value": ent}
        else:
            fix = {"type": "unresolved", "value": None}
        mapping[t] = {
            "count": Counter(targets)[t],
            "refs": sorted(set(refs[t])),
            "enterprise": ent,
            "moli": moli,
            "fix": fix,
        }

    out.write_text(json.dumps(mapping, ensure_ascii=False, indent=2), encoding="utf-8")
    unresolved = [t for t, v in mapping.items() if v["fix"]["type"] == "unresolved"]
    ent_fix = [t for t, v in mapping.items() if v["fix"]["type"] == "enterprise_slug"]
    moli_fix = [t for t, v in mapping.items() if v["fix"]["type"] == "moli_slug"]
    print(f"total targets: {len(mapping)}")
    print(f"moli exists: {len(moli_fix)}")
    print(f"enterprise exists: {len(ent_fix)}")
    print(f"unresolved: {len(unresolved)}")
    print(f"written {out.relative_to(KB)}")


if __name__ == "__main__":
    main()
