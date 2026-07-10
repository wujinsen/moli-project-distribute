#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""KBOPS-A3 · wiki 磁盘 vs kb_document 漂移检测。

口径与 sync_to_db.py 一致：slug = wiki 相对路径去 .md；content_hash = 全文 SHA-256；
DB 侧仅比较 source='kb' 且 is_delete=0 的行。

用法
----
    python kb/tools/detect_wiki_db_drift.py --space enterprise-kb
    python kb/tools/detect_wiki_db_drift.py --wiki-dir wiki-moli --space moli-ops-manual --json drift.json
    bash kb/tools/ci/run_sync.sh drift-all
"""

from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime
from pathlib import Path

from sync_to_db import KB_DIR, load_docs, resolve_wiki_dir

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


def fetch_db_kb_rows(args, space_id: int) -> dict[str, tuple[str | None, str | None]]:
    import pymysql

    conn = pymysql.connect(
        host=args.host, port=args.port, user=args.user,
        password=args.password, database=args.db, charset="utf8mb4",
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT slug, title, content_hash FROM kb_document "
                "WHERE space_id=%s AND is_delete=0 AND source='kb'",
                (space_id,),
            )
            return {r[0]: (r[1], r[2]) for r in cur.fetchall() if r[0]}
    finally:
        conn.close()


def resolve_space_id(args) -> int:
    import pymysql

    conn = pymysql.connect(
        host=args.host, port=args.port, user=args.user,
        password=args.password, database=args.db, charset="utf8mb4",
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id FROM kb_space WHERE space_code=%s AND is_delete=0",
                (args.space,),
            )
            row = cur.fetchone()
            if not row:
                print(f"[error] 找不到空间 space_code={args.space}", file=sys.stderr)
                sys.exit(3)
            return row[0]
    finally:
        conn.close()


def _wiki_dir_label(wiki_dir: Path) -> str:
    try:
        return str(wiki_dir.relative_to(KB_DIR))
    except ValueError:
        return str(wiki_dir)


def detect(args) -> dict:
    wiki_dir = resolve_wiki_dir(args.wiki_dir)
    rel_prefix = wiki_dir.name
    docs, _, _ = load_docs(wiki_dir, rel_prefix)
    wiki = {d.slug: d for d in docs}
    space_id = resolve_space_id(args)
    db = fetch_db_kb_rows(args, space_id)

    wiki_only, db_only, mismatches, in_sync = [], [], [], 0
    all_slugs = sorted(set(wiki.keys()) | set(db.keys()))
    for slug in all_slugs:
        w = wiki.get(slug)
        d = db.get(slug)
        if w and not d:
            wiki_only.append({
                "slug": slug, "title": w.title, "wikiHash": w.content_hash,
                "detail": "wiki 有页，DB 无活跃 kb 行（待 Sync）",
            })
        elif d and not w:
            db_only.append({
                "slug": slug, "title": d[0], "dbHash": d[1],
                "detail": "DB 有 kb 行，wiki 无文件",
            })
        elif w and d:
            if w.content_hash == d[1]:
                in_sync += 1
            else:
                mismatches.append({
                    "slug": slug, "title": d[0],
                    "wikiHash": w.content_hash, "dbHash": d[1],
                    "detail": "contentHash 不一致",
                })

    drifted = bool(wiki_only or db_only or mismatches)
    return {
        "spaceCode": args.space,
        "spaceId": space_id,
        "wikiDir": _wiki_dir_label(wiki_dir),
        "checkedAt": datetime.now().isoformat(timespec="seconds"),
        "wikiPageCount": len(wiki),
        "dbKbPageCount": len(db),
        "inSyncCount": in_sync,
        "wikiOnlyCount": len(wiki_only),
        "dbOnlyCount": len(db_only),
        "hashMismatchCount": len(mismatches),
        "drifted": drifted,
        "wikiOnly": wiki_only[: args.sample_limit],
        "dbOnly": db_only[: args.sample_limit],
        "hashMismatches": mismatches[: args.sample_limit],
    }


def print_report(report: dict) -> None:
    print("=" * 60)
    print(f"wiki↔DB 漂移  space={report['spaceCode']}  wiki={report['wikiDir']}")
    print(f"  in_sync={report['inSyncCount']}  wiki_only={report['wikiOnlyCount']}  "
          f"db_only={report['dbOnlyCount']}  hash_mismatch={report['hashMismatchCount']}")
    if report["wikiOnly"]:
        print("\n[wiki_only] 待 Sync：")
        for item in report["wikiOnly"][:10]:
            print(f"  - {item['slug']}")
    if report["dbOnly"]:
        print("\n[db_only] wiki 缺失：")
        for item in report["dbOnly"][:10]:
            print(f"  - {item['slug']}")
    if report["hashMismatches"]:
        print("\n[hash_mismatch]：")
        for item in report["hashMismatches"][:10]:
            print(f"  - {item['slug']}")
    print("=" * 60)


def main() -> int:
    ap = argparse.ArgumentParser(description="wiki ↔ kb_document 漂移检测（KBOPS-A3）")
    ap.add_argument("--wiki-dir", default="wiki", help="wiki 根目录，相对 kb/ 或绝对路径")
    ap.add_argument("--space", default="enterprise-kb", help="kb_space.space_code")
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=3306)
    ap.add_argument("--user", default="root")
    ap.add_argument("--password", default="12345678")
    ap.add_argument("--db", default="moli")
    ap.add_argument("--json", dest="json_path", default=None, help="写出 JSON 报告")
    ap.add_argument("--sample-limit", type=int, default=20)
    ap.add_argument("--fail-on-drift", action="store_true", help="有漂移时 exit 1")
    args = ap.parse_args()

    try:
        report = detect(args)
    except ImportError:
        print("[error] 需要 pymysql：pip install pymysql", file=sys.stderr)
        return 2

    print_report(report)
    if args.json_path:
        Path(args.json_path).write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"[ok] JSON -> {args.json_path}")

    if args.fail_on_drift and report["drifted"]:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
