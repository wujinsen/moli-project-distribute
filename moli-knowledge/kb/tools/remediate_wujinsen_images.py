#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""T22 R2: remediate wujinsen images into enterprise-kb wiki (strategies A/B/D).

Usage:
  python moli-knowledge/kb/tools/remediate_wujinsen_images.py --dry-run --strategy D --limit 10
  python moli-knowledge/kb/tools/remediate_wujinsen_images.py --apply --strategy D --limit 10 --single-cite
  python moli-knowledge/kb/tools/remediate_wujinsen_images.py --apply --strategy A --raw-path "BigData/Kafka/Kafka深度解析(1).note.md"
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from datetime import date
from pathlib import Path
from urllib.parse import quote

HERE = Path(__file__).resolve().parent
KB = HERE.parent
RAW = KB / "raw" / "wujinsen_markdown"
RAW_ROOT = KB / "raw"
WIKI = KB / "wiki"
LOG = WIKI / "log.md"
MANIFEST_JSON = HERE / "WUJINSEN_IMAGE_REMEDIATION.json"
MANIFEST_MD = HERE / "WUJINSEN_IMAGE_REMEDIATION.md"

DEFAULT_SPACE_ID = "900000000000000001"
IMG_EXT = {".png", ".jpg", ".jpeg", ".gif", ".webp"}
IMG_MD = re.compile(r"!\[[^\]]*\]\(\s*<?([^>)]+)>?\s*\)", re.I)
MARKER_PREFIX = "<!-- t22-wujinsen-images:"
SECTION_HEADER = "## 原文插图（wujinsen）"


def norm(p: str) -> str:
    return p.replace("\\", "/")


def companion_image_dirs(md_path: Path) -> list[Path]:
    out: list[Path] = []
    name = md_path.name
    parent = md_path.parent
    if not name.endswith(".note.md"):
        return out
    stem = name[: -len(".note.md")]
    for suffix in (".note_images", "_note_images"):
        p = parent / f"{stem}{suffix}"
        if p.is_dir():
            out.append(p)
    alt = parent / f"{name}.note_images"
    if alt.is_dir():
        out.append(alt)
    return out


def load_manifest() -> list[dict]:
    if not MANIFEST_JSON.exists():
        import subprocess

        subprocess.run(
            [sys.executable, str(HERE / "audit_wujinsen_images.py"), "--json", str(MANIFEST_JSON)],
            check=True,
        )
    return json.loads(MANIFEST_JSON.read_text(encoding="utf-8"))


def raw_rel_from_key(raw_path: str) -> str:
    p = norm(raw_path)
    if p.startswith("raw/wujinsen_markdown/"):
        return p.split("wujinsen_markdown/", 1)[-1]
    if p.startswith("wujinsen_markdown/"):
        return p.split("wujinsen_markdown/", 1)[-1]
    return p


def resolve_wiki_file(slug: str) -> Path | None:
    slug = norm(slug.strip())
    if slug.endswith(".md"):
        slug = slug[:-3]
    if "/" in slug:
        cat, name = slug.split("/", 1)
        direct = WIKI / cat / f"{name}.md"
        if direct.exists():
            return direct
        folder = WIKI / cat
        if folder.is_dir():
            for p in folder.glob("*.md"):
                if p.name in ("index.md", "log.md"):
                    continue
                text = p.read_text(encoding="utf-8", errors="ignore")
                fm = text.split("---", 2)
                if len(fm) >= 3:
                    sm = re.search(r"^slug:\s*(.+)$", fm[1], re.M)
                    if sm and sm.group(1).strip() == name:
                        return p
                if p.stem == name:
                    return p
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        rel = p.relative_to(WIKI).as_posix()
        stem_slug = rel[:-3] if rel.endswith(".md") else rel
        if stem_slug == slug or p.stem == slug:
            return p
    return None


def list_raw_images(md_path: Path) -> list[tuple[str, str]]:
    """Return [(raw_api_path, label), ...] sorted by filename."""
    seen: set[str] = set()
    out: list[tuple[str, str]] = []

    def add_file(f: Path, label: str | None = None) -> None:
        if not f.is_file() or f.suffix.lower() not in IMG_EXT:
            return
        api_path = norm(f.relative_to(RAW_ROOT).as_posix())
        if api_path in seen:
            return
        seen.add(api_path)
        out.append((api_path, label or f.name))

    try:
        text = md_path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        text = ""
    for m in IMG_MD.finditer(text):
        ref = m.group(1).strip().strip("<>")
        if ref.startswith("http"):
            continue
        candidate = (md_path.parent / ref).resolve()
        if candidate.is_file():
            alt = m.group(0)
            lab_m = re.match(r"!\[([^\]]*)\]", alt)
            label = lab_m.group(1).strip() if lab_m and lab_m.group(1).strip() else candidate.name
            add_file(candidate, label)

    for d in companion_image_dirs(md_path):
        for f in sorted(d.iterdir()):
            add_file(f)
    out.sort(key=lambda x: x[0])
    return out


def raw_asset_url(api_path: str, space_id: str, gateway_prefix: str) -> str:
    q = quote(api_path, safe="/")
    return f"{gateway_prefix}/kb/raw/asset?spaceId={space_id}&path={q}"


def wiki_asset_rel(filename: str) -> str:
    return f"assets/{filename}"


def marker_for(raw_key: str) -> str:
    return f"{MARKER_PREFIX}{raw_key} -->"


def build_section(
    raw_key: str,
    images: list[tuple[str, str]],
    strategy: str,
    space_id: str,
    gateway_prefix: str,
    annex_slug: str | None = None,
) -> str:
    title = Path(raw_rel_from_key(raw_key)).stem
    if title.endswith(".note"):
        title = title[:-5]
    lines = [
        "",
        marker_for(raw_key),
        SECTION_HEADER,
        "",
        f"> 图源 `{raw_key}` · T22 **{strategy}** 档",
        "",
        f"### 来自：{title}",
        "",
    ]
    for api_path, label in images:
        fname = Path(api_path).name
        if strategy == "D":
            url = raw_asset_url(api_path, space_id, gateway_prefix)
            lines.append(f"![{label}]({url})")
        elif strategy == "A" and annex_slug:
            lines.append(f"![{label}]({wiki_asset_rel(fname)})")
        else:
            url = raw_asset_url(api_path, space_id, gateway_prefix)
            lines.append(f"![{label}]({url})")
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def append_section(wiki_path: Path, section: str, dry_run: bool) -> bool:
    text = wiki_path.read_text(encoding="utf-8")
    marker = section.splitlines()[1] if section.splitlines() else ""
    if marker in text:
        return False
    if dry_run:
        return True
    wiki_path.write_text(text.rstrip() + "\n" + section, encoding="utf-8")
    return True


def slugify_annex(raw_rel: str) -> str:
    stem = Path(raw_rel).stem
    if stem.endswith(".note"):
        stem = stem[:-5]
    s = re.sub(r'[\\/:<>|"?*\s]+', "-", stem).strip("-")
    s = re.sub(r"-+", "-", s)
    return f"annex-{s}"[:80]


def hub_category(slug: str) -> str:
    return slug.split("/", 1)[0] if "/" in slug else "articles"


def apply_strategy_a(
    row: dict,
    space_id: str,
    gateway_prefix: str,
    dry_run: bool,
) -> dict:
    raw_rel = raw_rel_from_key(row["raw_path"])
    md_path = RAW / raw_rel
    if not md_path.is_file():
        return {"ok": False, "error": "raw missing", "raw": row["raw_path"]}

    images = list_raw_images(md_path)
    if not images:
        return {"ok": False, "error": "no images", "raw": row["raw_path"]}

    cited = row.get("cited_by") or []
    if not cited:
        return {"ok": False, "error": "not cited", "raw": row["raw_path"]}

    hub = cited[0]
    cat = hub_category(hub)
    annex_slug = row.get("annex_slug") or slugify_annex(raw_rel)
    full_annex_slug = f"{cat}/{annex_slug}"
    annex_md = WIKI / cat / f"{annex_slug}.md"
    assets_dir = WIKI / cat / f"{annex_slug}.assets"

    copied = 0
    if not dry_run:
        assets_dir.mkdir(parents=True, exist_ok=True)
        for api_path, _ in images:
            src = RAW_ROOT / api_path
            dst = assets_dir / Path(api_path).name
            if src.is_file() and not dst.exists():
                dst.write_bytes(src.read_bytes())
                copied += 1

    if not annex_md.exists():
        body = md_path.read_text(encoding="utf-8", errors="ignore")
        # rewrite local image refs to assets/
        for api_path, _ in images:
            fname = Path(api_path).name
            body = re.sub(
                rf"!\[([^\]]*)\]\([^)]*{re.escape(fname)}[^)]*\)",
                rf"![\1]({wiki_asset_rel(fname)})",
                body,
            )
        fm = (
            "---\n"
            f"title: {Path(raw_rel).stem}（原文插图 annex）\n"
            f"slug: {annex_slug}\n"
            f"type: article\n"
            f"status: active\n"
            f"tags: [wujinsen, annex, 插图]\n"
            f"sources:\n"
            f"  - {row['raw_path']}\n"
            f"related: [{hub.split('/')[-1]}]\n"
            f"created: {date.today().isoformat()}\n"
            f"updated: {date.today().isoformat()}\n"
            "---\n\n"
        )
        if not dry_run:
            annex_md.write_text(fm + body.strip() + "\n", encoding="utf-8")

    link_line = f"\n\n原文插图 annex：[[{full_annex_slug}]]\n"
    hub_path = resolve_wiki_file(hub)
    hub_linked = False
    if hub_path and f"[[{full_annex_slug}]]" not in hub_path.read_text(encoding="utf-8", errors="ignore"):
        if dry_run:
            hub_linked = True
        else:
            txt = hub_path.read_text(encoding="utf-8")
            hub_path.write_text(txt.rstrip() + link_line, encoding="utf-8")
            hub_linked = True

    return {
        "ok": True,
        "strategy": "A",
        "raw": row["raw_path"],
        "annex_slug": full_annex_slug,
        "images": len(images),
        "copied": copied,
        "hub_link": hub,
        "hub_linked": hub_linked,
        "dry_run": dry_run,
    }


def apply_strategy_bd(
    row: dict,
    strategy: str,
    space_id: str,
    gateway_prefix: str,
    dry_run: bool,
    single_hub: bool,
) -> dict:
    raw_rel = raw_rel_from_key(row["raw_path"])
    md_path = RAW / raw_rel
    if not md_path.is_file():
        return {"ok": False, "error": "raw missing", "raw": row["raw_path"]}

    images = list_raw_images(md_path)
    if not images:
        return {"ok": False, "error": "no images", "raw": row["raw_path"]}

    cited = row.get("cited_by") or []
    if not cited:
        return {"ok": False, "error": "not cited", "raw": row["raw_path"]}

    targets = [cited[0]] if single_hub and strategy == "D" else cited
    section = build_section(row["raw_path"], images, strategy, space_id, gateway_prefix)
    touched: list[str] = []
    for slug in targets:
        wp = resolve_wiki_file(slug)
        if not wp:
            continue
        if append_section(wp, section, dry_run):
            touched.append(slug)

    return {
        "ok": bool(touched),
        "strategy": strategy,
        "raw": row["raw_path"],
        "images": len(images),
        "wiki_slugs": touched,
        "dry_run": dry_run,
    }


def save_manifest(rows: list[dict]) -> None:
    MANIFEST_JSON.write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding="utf-8")


def append_log(summary: str, dry_run: bool) -> None:
    if dry_run:
        return
    line = f"## [{date.today().isoformat()}] remediate | {summary}\n"
    if LOG.exists():
        LOG.write_text(LOG.read_text(encoding="utf-8").rstrip() + "\n" + line, encoding="utf-8")


def select_rows(
    rows: list[dict],
    strategy: str | None,
    limit: int | None,
    raw_path: str | None,
    single_cite: bool,
    max_png: int | None,
    min_png: int,
    status: str,
) -> list[dict]:
    out = []
    for r in rows:
        if r.get("status") == "done" and status != "done":
            continue
        if r.get("strategy") == "skip-deleted":
            continue
        if r.get("strategy") == "defer" and not raw_path:
            continue
        if not r.get("cited_by"):
            continue
        if raw_path:
            rel = raw_rel_from_key(r["raw_path"])
            if raw_path not in rel and rel not in raw_path:
                continue
        if single_cite and r.get("cited_count", len(r.get("cited_by", []))) != 1:
            continue
        png = r.get("png_files", 0)
        if max_png is not None and png > max_png:
            continue
        if png < min_png:
            continue
        out.append(r)
    if strategy:
        # D can override manifest suggestion
        pass
    out.sort(key=lambda x: (x.get("png_files", 0), x["raw_path"]))
    if limit:
        out = out[:limit]
    return out


def main() -> int:
    parser = argparse.ArgumentParser(description="T22 wujinsen image remediation")
    parser.add_argument("--apply", action="store_true", help="write wiki files (default dry-run)")
    parser.add_argument("--dry-run", action="store_true", help="explicit dry-run")
    parser.add_argument("--strategy", choices=["A", "B", "D"], help="force strategy (default from manifest)")
    parser.add_argument("--limit", type=int, help="max rows to process")
    parser.add_argument("--raw-path", help="filter by raw relative path substring")
    parser.add_argument("--single-cite", action="store_true", help="only rows citing exactly one wiki slug")
    parser.add_argument("--max-png", type=int, help="skip rows with more png files than this")
    parser.add_argument("--min-png", type=int, default=1)
    parser.add_argument("--space-id", default=DEFAULT_SPACE_ID)
    parser.add_argument(
        "--gateway-prefix",
        default="/KnowledgeServer",
        help="URL prefix before /kb/raw/asset",
    )
    parser.add_argument("--refresh-manifest", action="store_true", help="regenerate JSON via audit first")
    args = parser.parse_args()
    dry_run = not args.apply or args.dry_run

    if args.refresh_manifest:
        import subprocess

        subprocess.run(
            [sys.executable, str(HERE / "audit_wujinsen_images.py"), "--json", str(MANIFEST_JSON)],
            check=True,
        )

    rows = load_manifest()
    selected = select_rows(
        rows,
        args.strategy,
        args.limit,
        args.raw_path,
        args.single_cite,
        args.max_png,
        args.min_png,
        status="pending",
    )

    if not selected:
        print("No rows matched filters.")
        return 1

    results: list[dict] = []
    for row in selected:
        use = args.strategy or row.get("strategy", "D")
        if use == "C-or-A":
            use = "A" if args.strategy == "A" else ("B" if len(row.get("cited_by", [])) > 1 else "D")
        if args.strategy:
            use = args.strategy
        if use == "A":
            res = apply_strategy_a(row, args.space_id, args.gateway_prefix, dry_run)
        elif use == "B":
            res = apply_strategy_bd(row, "B", args.space_id, args.gateway_prefix, dry_run, single_hub=False)
        else:
            res = apply_strategy_bd(row, "D", args.space_id, args.gateway_prefix, dry_run, single_hub=True)
        results.append(res)
        status = "done" if res.get("ok") else "failed"
        print(json.dumps(res, ensure_ascii=False))
        if res.get("ok") and not dry_run:
            for r in rows:
                if r["raw_path"] == row["raw_path"]:
                    r["status"] = status
                    r["applied_strategy"] = use
                    if res.get("annex_slug"):
                        r["annex_slug"] = res["annex_slug"]
                    break

    if not dry_run:
        save_manifest(rows)
        ok_n = sum(1 for r in results if r.get("ok"))
        strat_label = args.strategy or "mixed"
        append_log(f"T22 R2 {strat_label}档 {ok_n}/{len(results)} raw", dry_run=False)

    ok = sum(1 for r in results if r.get("ok"))
    print(f"Done: {ok}/{len(results)} {'(dry-run)' if dry_run else '(applied)'}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
