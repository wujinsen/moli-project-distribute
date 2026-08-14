#!/usr/bin/env python3
"""Full audit + purge jp-fe-ap-exam from MySQL."""
import argparse
from datetime import datetime

import pymysql

JP_SPACE_ID = 900000000000000002
JP_SPACE_CODE = "jp-fe-ap-exam"

STRAY_SLUG_SQL = """
    slug LIKE 'fe/%%' OR slug LIKE 'ap/%%' OR slug LIKE 'certify/%%'
    OR slug LIKE 'interview/fe%%' OR slug LIKE '%%fe_kamoku%%'
    OR slug LIKE '%%kamoku_b%%' OR domain IN ('JP-FE', 'JP-AP')
"""


def connect():
    return pymysql.connect(
        host="127.0.0.1", port=3306, user="root", password="12345678",
        database="moli", charset="utf8mb4", autocommit=False,
    )


def audit(cur):
    print("=== kb_space (incl. deleted) ===")
    cur.execute(
        "SELECT id, space_code, space_name, is_delete FROM kb_space "
        "WHERE id=%s OR space_code=%s",
        (JP_SPACE_ID, JP_SPACE_CODE),
    )
    rows = cur.fetchall()
    if not rows:
        print("(no row)")
    for r in rows:
        print(r)

    for label, extra in [
        ("jp active docs", "space_id=%s AND is_delete=0"),
        ("jp soft-deleted docs", "space_id=%s AND is_delete=1"),
        ("stray active jp-like (any space)", f"is_delete=0 AND ({STRAY_SLUG_SQL})"),
    ]:
        if "%s" in extra:
            cur.execute(f"SELECT COUNT(*) FROM kb_document WHERE {extra}", (JP_SPACE_ID,))
        else:
            cur.execute(f"SELECT COUNT(*) FROM kb_document WHERE {extra}")
        print(f"{label}:", cur.fetchone()[0])

    cur.execute(
        f"SELECT d.id, s.space_code, d.slug, d.is_delete FROM kb_document d "
        f"LEFT JOIN kb_space s ON s.id=d.space_id WHERE {STRAY_SLUG_SQL} "
        f"ORDER BY d.is_delete, s.space_code, d.slug LIMIT 100"
    )
    stray = cur.fetchall()
    print("=== jp-like docs (all delete flags) ===")
    for r in stray:
        print(r)

    cur.execute(
        "SELECT COUNT(*) FROM kb_category WHERE space_id=%s AND is_delete=0",
        (JP_SPACE_ID,),
    )
    print("jp active categories:", cur.fetchone()[0])

    cur.execute(
        "SELECT COUNT(*) FROM kb_ingest_job WHERE space_id=%s",
        (JP_SPACE_ID,),
    )
    print("jp ingest jobs (all):", cur.fetchone()[0])
    cur.execute(
        "SELECT COUNT(*) FROM kb_ingest_job WHERE space_id=%s AND is_delete=0",
        (JP_SPACE_ID,),
    )
    print("jp ingest jobs (active):", cur.fetchone()[0])


def purge(cur, execute: bool):
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    stats = {}

    # documents + chunks (jp space + stray slugs)
    cur.execute(
        f"SELECT id FROM kb_document WHERE is_delete=0 AND (space_id=%s OR ({STRAY_SLUG_SQL}))",
        (JP_SPACE_ID,),
    )
    doc_ids = [r[0] for r in cur.fetchall()]
    stats["documents"] = len(doc_ids)

    if execute and doc_ids:
        placeholders = ",".join(["%s"] * len(doc_ids))
        cur.execute(
            f"UPDATE kb_document SET is_delete=1, update_time=%s WHERE id IN ({placeholders})",
            [now, *doc_ids],
        )
        cur.execute(
            f"UPDATE kb_document_chunk SET is_delete=1, update_time=%s "
            f"WHERE document_id IN ({placeholders}) AND is_delete=0",
            [now, *doc_ids],
        )

    # categories, tags scoped to jp space
    for table, col in [
        ("kb_category", "space_id"),
        ("kb_tag", "space_id"),
        ("kb_space_member", "space_id"),
    ]:
        cur.execute(
            f"SELECT COUNT(*) FROM {table} WHERE {col}=%s AND is_delete=0",
            (JP_SPACE_ID,),
        )
        stats[table] = cur.fetchone()[0]
        if execute and stats[table]:
            cur.execute(
                f"UPDATE {table} SET is_delete=1, update_time=%s WHERE {col}=%s AND is_delete=0",
                (now, JP_SPACE_ID),
            )

    # space itself
    cur.execute(
        "SELECT COUNT(*) FROM kb_space WHERE id=%s AND is_delete=0",
        (JP_SPACE_ID,),
    )
    stats["kb_space"] = cur.fetchone()[0]
    if execute and stats["kb_space"]:
        cur.execute(
            "UPDATE kb_space SET is_delete=1, update_time=%s WHERE id=%s AND is_delete=0",
            (now, JP_SPACE_ID),
        )

    cur.execute(
        "SELECT COUNT(*) FROM kb_ingest_job WHERE space_id=%s AND is_delete=0",
        (JP_SPACE_ID,),
    )
    stats["kb_ingest_job"] = cur.fetchone()[0]
    if execute and stats["kb_ingest_job"]:
        cur.execute(
            "UPDATE kb_ingest_job SET is_delete=1, update_time=%s "
            "WHERE space_id=%s AND is_delete=0",
            (now, JP_SPACE_ID),
        )

    return stats


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--execute", action="store_true", help="apply soft-delete")
    args = ap.parse_args()

    conn = connect()
    try:
        with conn.cursor() as cur:
            audit(cur)
            print("\n=== purge plan ===")
            stats = purge(cur, execute=False)
            for k, v in stats.items():
                print(f"  {k}: {v}")
            if args.execute:
                stats = purge(cur, execute=True)
                conn.commit()
                print("\n=== purged (soft-delete) ===")
                for k, v in stats.items():
                    print(f"  {k}: {v}")
                audit(cur)
            else:
                print("\n(dry-run; pass --execute to apply)")
    except Exception as e:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
