#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""enterprise-kb 方案 B：写入 10 个主题分类（kb_category）。

与 docs/sql/13_kb_category_enterprise_topic.sql 等价，便于在服务器上直接跑：
    python kb/tools/seed_enterprise_kb_topic_categories.py --dry-run
    python kb/tools/seed_enterprise_kb_topic_categories.py --execute

Sync 完成后再物理删旧三类：
    python kb/tools/seed_enterprise_kb_topic_categories.py --execute --delete-old

ID 段 900000000000000141–150，避免与 jp-fe-ap-exam(121–124)、outputs(116) 冲突。
"""

from __future__ import annotations

import argparse
import sys
from datetime import datetime

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

SPACE_CODE = "enterprise-kb"
SPACE_ID = 900000000000000001

# sort, id, dir_slug, category_name
TOPIC_CATEGORIES = [
    (1, 900000000000000141, "database", "数据库"),
    (2, 900000000000000142, "cache", "缓存与 Redis"),
    (3, 900000000000000143, "java", "Java 与 JVM"),
    (4, 900000000000000144, "middleware", "微服务与中间件"),
    (5, 900000000000000145, "spring", "Spring 生态"),
    (6, 900000000000000146, "search", "搜索与 ES"),
    (7, 900000000000000147, "security", "网络与安全"),
    (8, 900000000000000148, "ops", "运维与 Linux"),
    (9, 900000000000000149, "patterns", "设计模式"),
    (10, 900000000000000150, "frontend", "前端"),
]

OLD_DIR_SLUGS = ("concepts", "articles", "interview")


def print_plan(delete_old: bool) -> None:
    print(f"空间 {SPACE_CODE} (space_id={SPACE_ID})")
    print("\n§1 新建/更新主题分类（体裁走 frontmatter type:）：")
    for sort, cid, slug, name in TOPIC_CATEGORIES:
        print(f"  [{sort:2d}] id={cid}  dir_slug={slug:<12}  {name}")
    if delete_old:
        print("\n§2 物理删除旧分类（仅 enterprise-kb）：")
        for slug in OLD_DIR_SLUGS:
            print(f"  - dir_slug={slug}")


def upsert_categories(conn, operator: int) -> int:
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    n = 0
    with conn.cursor() as cur:
        for sort, cid, slug, name in TOPIC_CATEGORIES:
            cur.execute(
                """
                INSERT INTO kb_category
                  (id, create_id, create_time, update_id, update_time,
                   space_id, parent_id, category_name, icon, dir_slug,
                   sort, is_delete)
                VALUES (%s, %s, %s, %s, %s, %s, 0, %s, NULL, %s, %s, 0)
                ON DUPLICATE KEY UPDATE
                  category_name = VALUES(category_name),
                  dir_slug      = VALUES(dir_slug),
                  sort          = VALUES(sort),
                  is_delete     = 0,
                  update_time   = VALUES(update_time)
                """,
                (cid, operator, now, operator, now, SPACE_ID, name, slug, sort),
            )
            n += 1
    return n


def delete_old_categories(conn) -> int:
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE kb_document d
            INNER JOIN kb_category c ON d.category_id = c.id
            SET d.category_id = NULL, d.update_time = %s
            WHERE c.space_id = %s
              AND c.dir_slug IN (%s, %s, %s)
            """,
            (now, SPACE_ID, *OLD_DIR_SLUGS),
        )
        cur.execute(
            """
            DELETE FROM kb_category
            WHERE space_id = %s
              AND dir_slug IN (%s, %s, %s)
            """,
            (SPACE_ID, *OLD_DIR_SLUGS),
        )
        return cur.rowcount


def list_categories(conn) -> None:
    slugs = [c[2] for c in TOPIC_CATEGORIES] + list(OLD_DIR_SLUGS)
    placeholders = ",".join(["%s"] * len(slugs))
    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT id, category_name, dir_slug, sort
            FROM kb_category
            WHERE space_id = %s AND dir_slug IN ({placeholders})
            ORDER BY sort, id
            """,
            (SPACE_ID, *slugs),
        )
        rows = cur.fetchall()
    print("\n当前 kb_category（相关 dir_slug）：")
    for rid, name, slug, sort in rows:
        print(f"  id={rid}  sort={sort}  {slug:<12}  {name}")


def main() -> int:
    ap = argparse.ArgumentParser(description="enterprise-kb 主题分类种子（方案 B）")
    ap.add_argument("--dry-run", action="store_true", help="仅打印计划，不连库")
    ap.add_argument("--execute", action="store_true", help="写入 kb_category")
    ap.add_argument(
        "--delete-old",
        action="store_true",
        help="物理删除 concepts/articles/interview（请在 sync 之后）",
    )
    ap.add_argument(
        "--retire-old",
        action="store_true",
        help=argparse.SUPPRESS,
    )
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=3306)
    ap.add_argument("--user", default="root")
    ap.add_argument("--password", default="12345678")
    ap.add_argument("--db", default="moli")
    ap.add_argument("--operator", type=int, default=1, help="create_id / update_id")
    args = ap.parse_args()

    delete_old = args.delete_old or args.retire_old

    if not args.execute:
        print_plan(delete_old)
        print("\n[dry-run] 加 --execute 才会写库。")
        return 0

    try:
        import pymysql
    except ImportError:
        print("[error] 需要 pymysql：pip install -r kb/tools/requirements-sync.txt")
        return 2

    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.db,
        charset="utf8mb4",
        autocommit=False,
    )
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id FROM kb_space WHERE space_code=%s AND is_delete=0",
                (SPACE_CODE,),
            )
            row = cur.fetchone()
            if not row or row[0] != SPACE_ID:
                print(f"[error] 空间校验失败：期望 space_id={SPACE_ID}，实际={row}")
                return 3

        n = upsert_categories(conn, args.operator)
        print(f"§1 已 upsert {n} 条主题分类。")

        if delete_old:
            n_del = delete_old_categories(conn)
            print(f"§2 已物理删除 {n_del} 条旧分类（concepts/articles/interview）。")

        conn.commit()
        list_categories(conn)
        print("\n下一步：python kb/tools/sync_to_db.py --wiki-dir wiki --space enterprise-kb")
        return 0
    except Exception as exc:
        conn.rollback()
        print(f"[error] {exc}")
        return 1
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())
