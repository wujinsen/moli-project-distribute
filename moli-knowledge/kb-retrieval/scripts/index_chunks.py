#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从 MySQL kb_document_chunk 增量建 Chroma 向量索引（调用 sidecar POST /embed）。

用法：
  python kb-retrieval/scripts/index_chunks.py --dry-run
  python kb-retrieval/scripts/index_chunks.py
  python kb-retrieval/scripts/index_chunks.py --force --batch-size 64

前置：kb-retrieval sidecar 已启动（默认 http://127.0.0.1:8099）。
"""
from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

KB_DIR = Path(__file__).resolve().parent.parent.parent / "kb"
PUBLISHED_STATUS = 1
DEFAULT_BASE = os.environ.get("RETRIEVAL_BASE_URL", "http://127.0.0.1:8099")


def http_json(url: str, payload: dict, *, timeout: int = 600) -> dict:
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        method="POST",
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode())


def probe_health(base: str, timeout: int = 5) -> dict:
    req = urllib.request.Request(f"{base.rstrip('/')}/health", method="GET")
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode())


def chunk_text(heading: str | None, content: str) -> str:
    parts = []
    if heading and heading.strip():
        parts.append(heading.strip())
    if content and content.strip():
        parts.append(content.strip())
    return "\n".join(parts)


def fetch_chunks(conn, *, space_id: int | None = None) -> list[dict]:
    sql = (
        "SELECT id, document_id, space_id, slug, kb_type, category_id, "
        "heading, content, content_hash "
        "FROM kb_document_chunk "
        "WHERE is_delete=0 AND status=%s "
    )
    params: list = [PUBLISHED_STATUS]
    if space_id is not None:
        sql += "AND space_id=%s "
        params.append(space_id)
    sql += "ORDER BY id"
    with conn.cursor() as cur:
        cur.execute(sql, tuple(params))
        rows = cur.fetchall()
    items = []
    for row in rows:
        cid, doc_id, space_id, slug, kb_type, category_id, heading, content, chash = row
        text = chunk_text(heading, content)
        if not text.strip():
            continue
        items.append(
            {
                "chunkId": int(cid),
                "docId": int(doc_id),
                "spaceId": int(space_id),
                "slug": str(slug),
                "kbType": kb_type,
                "categoryId": int(category_id) if category_id is not None else None,
                "contentHash": str(chash),
                "text": text,
            }
        )
    return items


def fetch_db_chunk_ids(conn, *, space_id: int | None = None) -> set[int]:
    sql = "SELECT id FROM kb_document_chunk WHERE is_delete=0 AND status=%s"
    params: list = [PUBLISHED_STATUS]
    if space_id is not None:
        sql += " AND space_id=%s"
        params.append(space_id)
    with conn.cursor() as cur:
        cur.execute(sql, tuple(params))
        return {int(r[0]) for r in cur.fetchall()}


def fetch_indexed_ids(base: str, *, space_id: int | None = None) -> set[int]:
    probe_health(base)
    chroma_path = os.environ.get("CHROMA_PATH")
    if not chroma_path:
        chroma_path = str(Path(__file__).resolve().parent.parent / ".chroma")
    try:
        import chromadb

        client = chromadb.PersistentClient(path=chroma_path)
        col_name = os.environ.get("CHROMA_COLLECTION", "moli_kb_chunks_bgem3_v1")
        col = client.get_collection(col_name)
        if space_id is None:
            got = col.get(include=[])
            return {int(i) for i in (got.get("ids") or [])}
        got = col.get(where={"spaceId": int(space_id)}, include=[])
        return {int(i) for i in (got.get("ids") or [])}
    except Exception as e:  # noqa: BLE001
        print(f"[warn] 无法读取 Chroma 对账（{e}），跳过 deleteChunkIds")
        return set()


def main() -> int:
    ap = argparse.ArgumentParser(description="kb_document_chunk → sidecar /embed 离线索引")
    ap.add_argument("--base-url", default=DEFAULT_BASE, help="sidecar 基址")
    ap.add_argument("--host", default=os.environ.get("MYSQL_HOST", "127.0.0.1"))
    ap.add_argument("--port", type=int, default=int(os.environ.get("MYSQL_PORT", "3306")))
    ap.add_argument("--user", default=os.environ.get("MYSQL_USER", "root"))
    ap.add_argument("--password", default=os.environ.get("MYSQL_PASSWORD", "12345678"))
    ap.add_argument("--db", default=os.environ.get("MYSQL_DB", "moli"))
    ap.add_argument("--batch-size", type=int, default=int(os.environ.get("EMBED_BATCH_SIZE", "32")))
    ap.add_argument("--force", action="store_true", help="忽略 contentHash，强制重嵌")
    ap.add_argument("--dry-run", action="store_true", help="只统计，不调 /embed")
    ap.add_argument("--limit", type=int, default=0, help="调试：最多索引 N 段")
    ap.add_argument(
        "--space-id",
        type=int,
        default=None,
        help="只索引指定 space_id（如 moli-ops-manual=900000000000000003）",
    )
    args = ap.parse_args()

    base = args.base_url.rstrip("/")
    try:
        health = probe_health(base)
        print(f"sidecar OK · indexed={health.get('indexedChunks')} · model={health.get('model')}")
    except (urllib.error.URLError, urllib.error.HTTPError) as e:
        print(f"[error] sidecar 不可达 {base}/health（{e}）")
        return 2

    try:
        import pymysql
    except ImportError:
        print("[error] 需要 pymysql：pip install -r kb-retrieval/requirements.txt")
        return 2

    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.db,
        charset="utf8mb4",
    )
    try:
        items = fetch_chunks(conn, space_id=args.space_id)
        db_ids = fetch_db_chunk_ids(conn, space_id=args.space_id)
    finally:
        conn.close()

    if args.limit > 0:
        items = items[: args.limit]

    indexed_ids = fetch_indexed_ids(base, space_id=args.space_id)
    delete_ids = sorted(indexed_ids - db_ids)

    print(f"DB chunks={len(items)} · Chroma indexed={len(indexed_ids)} · to_delete={len(delete_ids)}")
    if args.dry_run:
        print("dry-run：未调用 /embed")
        return 0

    total_upserted = total_skipped = total_deleted = 0
    url = f"{base}/embed"
    batch_size = max(1, args.batch_size)

    # 先清理孤儿
    if delete_ids:
        for i in range(0, len(delete_ids), batch_size):
            batch_del = delete_ids[i : i + batch_size]
            resp = http_json(url, {"items": [], "deleteChunkIds": batch_del, "force": False})
            total_deleted += int(resp.get("deleted", 0))

    for i in range(0, len(items), batch_size):
        batch = items[i : i + batch_size]
        payload = {
            "items": batch,
            "deleteChunkIds": [],
            "force": args.force,
        }
        try:
            resp = http_json(url, payload)
        except urllib.error.HTTPError as e:
            body = e.read().decode(errors="replace")[:500]
            print(f"[error] /embed HTTP {e.code}: {body}")
            return 1
        total_upserted += int(resp.get("upserted", 0))
        total_skipped += int(resp.get("skipped", 0))
        print(
            f"  batch {i // batch_size + 1}: upserted={resp.get('upserted')} "
            f"skipped={resp.get('skipped')}"
        )

    print(
        f"\n== 完成 == upserted={total_upserted} skipped={total_skipped} "
        f"deleted={total_deleted}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
