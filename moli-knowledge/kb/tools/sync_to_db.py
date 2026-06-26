#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""kb -> kb_document 单向增量同步脚本。

把 kb/wiki/（或 --wiki-dir 指定目录）下 Agent 维护的 markdown 知识页，单向、增量、幂等地写入
moli-knowledge-server 的 MySQL（kb_document / kb_tag / kb_document_tag /
kb_relation / kb_sync_log）。方向严格单向：kb(markdown) -> DB；DB 侧不回写。

设计要点
--------
* slug：用 wiki 相对路径去扩展名（如 ``services/用户中心``），空间内唯一，
  与 graph/edges.jsonl 的节点命名一致，便于关系解析。
* 幂等：按 (space_id, slug) upsert；content_hash 未变则 skip，只同步变更页。
* 删除：DB 中 source='kb' 且 slug 已不在 wiki 的，置 is_delete=1。
* 关系：正文 [[..]] -> links_to；frontmatter related -> related；
  graph/edges.jsonl -> 边自带 type（depends_on/relates_to/...）。
  目标解析不到的记为 resolved=0（断链）。
* index.md / log.md 不作为文档同步。

用法
----
    # 仅解析、打印计划，不连数据库（推荐先跑这个核对）
    python kb/tools/sync_to_db.py --dry-run

    # 真正写库（需要 pymysql：pip install -r kb/tools/requirements-sync.txt）
    python kb/tools/sync_to_db.py \
        --host 127.0.0.1 --port 3306 --user root --password 12345678 \
        --db moli --space enterprise-kb

    # 独立 wiki 目录 → 指定空间（如日本語試験）
    python kb/tools/sync_to_db.py --wiki-dir wiki-jp-exam --space jp-fe-ap-exam --dry-run

    # CI 统一入口（GitHub Actions 同款）
    bash kb/tools/ci/run_sync.sh dry-run
    bash kb/tools/ci/run_sync.sh init-schema   # 需 mysql 客户端
    bash kb/tools/ci/run_sync.sh sync

    # 清理 Web 直连 MySQL 遗留行（T14e 停用 POST /kb/document 后）
    bash kb/tools/ci/run_sync.sh purge-manual-web-dry-run   # 预览
    bash kb/tools/ci/run_sync.sh purge-manual-web-all       # 软删全空间
    powershell -File moli-knowledge/kb/tools/purge_manual_web.ps1              # Windows 预览
    powershell -File moli-knowledge/kb/tools/purge_manual_web.ps1 -Execute     # Windows 执行

参数默认值对齐 moli-knowledge-server 的 application-dev.yml 与建表种子数据。
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
import time
from datetime import datetime
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# ---------------------------------------------------------------------------
# 路径与常量
# ---------------------------------------------------------------------------

HERE = Path(__file__).resolve().parent          # kb/tools
KB_DIR = HERE.parent                             # kb
DEFAULT_WIKI_DIR = KB_DIR / "wiki"

SPECIAL = {"index", "log"}

# frontmatter type -> kb_document.kb_type（同名直存，这里仅做白名单校验）
KB_TYPES = {"guide", "service", "concept", "article", "interview", "output"}

# frontmatter status -> kb_document.status
STATUS_MAP = {"draft": 0, "active": 1, "archived": 2}


# ---------------------------------------------------------------------------
# 简易雪花 ID（不依赖 DB 自增；与全家桶 bigint 主键兼容）
# ---------------------------------------------------------------------------

class IdGen:
    """时间戳 + 自增序列，单机单线程足够，落在正 bigint 范围内。"""

    def __init__(self) -> None:
        self._epoch = 1_700_000_000_000  # 2023-11 起算
        self._seq = 0

    def next(self) -> int:
        now = int(time.time() * 1000) - self._epoch
        self._seq = (self._seq + 1) & 0x3FFF        # 14 位序列
        return (now << 14) | self._seq


# ---------------------------------------------------------------------------
# frontmatter 解析（与 serve.py 保持一致的轻量解析）
# ---------------------------------------------------------------------------

def _strip(s: str) -> str:
    return s.strip().strip('"').strip("'").strip()


def parse_frontmatter(text: str):
    if not text.startswith("---"):
        return {}, text
    end = text.find("\n---", 3)
    if end == -1:
        return {}, text
    raw = text[3:end].strip("\n")
    body = text[end + 4:].lstrip("\n")
    meta: dict = {}
    key = None
    for line in raw.splitlines():
        if not line.strip():
            continue
        if line.startswith(("  - ", "- ")) and key:
            meta.setdefault(key, [])
            if isinstance(meta[key], list):
                meta[key].append(_strip(line.split("-", 1)[1]))
            continue
        m = re.match(r"^([A-Za-z_][\w-]*):\s*(.*)$", line)
        if not m:
            continue
        key, val = m.group(1), m.group(2).strip()
        if val == "":
            meta[key] = []
        elif val.startswith("[") and val.endswith("]"):
            inner = val[1:-1].strip()
            meta[key] = [_strip(x) for x in inner.split(",")] if inner else []
        else:
            meta[key] = _strip(val)
    return meta, body


def _first_h1(body: str) -> str:
    for line in body.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return ""


WIKILINK = re.compile(r"\[\[([^\]]+)\]\]")


def extract_wikilinks(body: str):
    """返回正文中 [[目标|显示]] 的目标 slug 列表（去重、保序）。"""
    out, seen = [], set()
    for m in WIKILINK.finditer(body):
        target = m.group(1).split("|")[0].strip()
        if target and target not in seen:
            seen.add(target)
            out.append(target)
    return out


# ---------------------------------------------------------------------------
# 加载 wiki 页
# ---------------------------------------------------------------------------

class Doc:
    __slots__ = ("slug", "stem", "title", "kb_type", "status", "tags", "domain",
                 "sources", "related", "rel_path", "body", "content_hash",
                 "wikilinks")

    def __init__(self, **kw):
        for k in self.__slots__:
            setattr(self, k, kw.get(k))


def load_docs(wiki_dir: Path, rel_prefix: str):
    """扫描 wiki 目录，返回 (docs:list[Doc], by_slug, by_stem)。"""
    docs = []
    if not wiki_dir.exists():
        return docs, {}, {}
    for path in sorted(wiki_dir.rglob("*.md")):
        if path.stem in SPECIAL:
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except Exception as e:               # noqa: BLE001
            print(f"[warn] 读取失败 {path}: {e}")
            continue
        meta, body = parse_frontmatter(text)
        rel = path.relative_to(wiki_dir).as_posix()
        slug = rel[:-3] if rel.endswith(".md") else rel   # 去 .md，如 services/用户中心
        stem = path.stem
        kb_type = meta.get("type") or "concept"
        if kb_type not in KB_TYPES:
            kb_type = "concept"
        tags = meta.get("tags") if isinstance(meta.get("tags"), list) else []
        domain = meta.get("domain")
        if isinstance(domain, str):
            domain = domain.strip() or None
        else:
            domain = None
        docs.append(Doc(
            slug=slug,
            stem=stem,
            title=meta.get("title") or _first_h1(body) or stem,
            kb_type=kb_type,
            status=STATUS_MAP.get(str(meta.get("status", "")).lower(), 1),
            tags=[t for t in tags if t],
            domain=domain,
            sources=meta.get("sources", []) or [],
            related=meta.get("related", []) or [],
            rel_path=rel_prefix + "/" + rel,
            body=body,
            content_hash=hashlib.sha256(text.encode("utf-8")).hexdigest(),
            wikilinks=extract_wikilinks(body),
        ))
    by_slug = {d.slug: d for d in docs}            # 全路径 slug
    by_stem: dict = {}                             # 裸 slug -> 全路径（用于 [[]]/related 解析）
    for d in docs:
        by_stem.setdefault(d.stem, d.slug)
    return docs, by_slug, by_stem


def resolve(target: str, by_slug: dict, by_stem: dict):
    """把 [[..]] / related / edges 的目标解析为全路径 slug，解析不到返回 None。"""
    t = target.strip()
    if t in by_slug:
        return t
    if t in by_stem:
        return by_stem[t]
    # 容错：去掉可能的类型前缀再按裸名匹配
    base = t.split("/")[-1]
    if base in by_stem:
        return by_stem[base]
    return None


def load_edges(edges_file: Path):
    """读取 graph/edges.jsonl，返回 [(from, to, type), ...]。"""
    edges = []
    if not edges_file.exists():
        return edges
    for line in edges_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            o = json.loads(line)
        except Exception:                          # noqa: BLE001
            continue
        if o.get("from") and o.get("to"):
            edges.append((o["from"], o["to"], o.get("type", "relates_to")))
    return edges


# ---------------------------------------------------------------------------
# 关系计算：[[..]] -> links_to；related -> related；edges -> 边 type
# ---------------------------------------------------------------------------

def build_relations(docs, by_slug, by_stem, edges):
    """返回 relations:list[dict]，每条 {src, dst|None, dst_title, type, resolved}。

    src/dst 为全路径 slug；dst 为 None 表示断链。
    """
    rels = []
    seen = set()                                   # (src, dst_or_title, type) 去重

    def add(src_slug, target_raw, rtype):
        dst = resolve(target_raw, by_slug, by_stem)
        key = (src_slug, dst or ("?" + target_raw), rtype)
        if key in seen:
            return
        seen.add(key)
        rels.append({
            "src": src_slug,
            "dst": dst,
            "dst_title": target_raw if dst is None else by_slug[dst].title,
            "type": rtype,
            "resolved": 1 if dst else 0,
        })

    for d in docs:
        for tgt in d.wikilinks:
            if resolve(tgt, by_slug, by_stem) != d.slug:   # 不自指
                add(d.slug, tgt, "links_to")
        for tgt in d.related:
            if resolve(tgt, by_slug, by_stem) != d.slug:
                add(d.slug, tgt, "related")

    for frm, to, etype in edges:
        src = resolve(frm, by_slug, by_stem)
        if src is None:
            continue                                # 源不存在的边跳过
        add(src, to, etype)

    return rels


# ---------------------------------------------------------------------------
# Dry-run 报告
# ---------------------------------------------------------------------------

def report_dry_run(docs, rels, wiki_dir: Path):
    print("=" * 60)
    print(f"扫描 {wiki_dir}：{len(docs)} 个文档页（已排除 index.md / log.md）")
    print("=" * 60)
    by_type: dict = {}
    for d in docs:
        by_type.setdefault(d.kb_type, []).append(d)
    for t in sorted(by_type):
        print(f"\n[{t}] {len(by_type[t])} 页")
        for d in by_type[t]:
            st = {0: "草稿", 1: "已发布", 2: "已归档"}.get(d.status, "?")
            tags = ("#" + " #".join(d.tags)) if d.tags else "(无标签)"
            print(f"  - {d.slug}  «{d.title}»  [{st}]  {tags}")
            print(f"      hash={d.content_hash[:12]}…  sources={len(d.sources)}")

    resolved = [r for r in rels if r["resolved"]]
    broken = [r for r in rels if not r["resolved"]]
    print("\n" + "=" * 60)
    print(f"关系：{len(rels)} 条（已解析 {len(resolved)} · 断链 {len(broken)}）")
    print("=" * 60)
    rt: dict = {}
    for r in resolved:
        rt.setdefault(r["type"], 0)
        rt[r["type"]] += 1
    for t in sorted(rt):
        print(f"  {t}: {rt[t]}")
    if broken:
        print("\n  断链（目标解析不到，将记为 resolved=0）：")
        for r in broken:
            print(f"    {r['src']}  --{r['type']}-->  [[{r['dst_title']}]]")

    all_tags = sorted({t for d in docs for t in d.tags})
    print(f"\n标签合计：{len(all_tags)} 个 -> {', '.join(all_tags)}")
    print("\n[dry-run] 未连接数据库，未写入任何数据。")


# ---------------------------------------------------------------------------
# 真正写库（pymysql）
# ---------------------------------------------------------------------------

def sync_to_db(docs, rels, args):
    try:
        import pymysql
    except ImportError:
        print("[error] 需要 pymysql：pip install pymysql\n"
              "        或先用 --dry-run 仅校验解析结果。")
        return 2

    conn = pymysql.connect(
        host=args.host, port=args.port, user=args.user,
        password=args.password, database=args.db, charset="utf8mb4",
        autocommit=False,
    )
    idgen = IdGen()
    batch_no = datetime.now().strftime("%Y%m%d%H%M%S")
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    stats = {"insert": 0, "update": 0, "skip": 0, "delete": 0, "fail": 0}
    try:
        with conn.cursor() as cur:
            # 解析 space_id
            cur.execute("SELECT id FROM kb_space WHERE space_code=%s AND is_delete=0",
                        (args.space,))
            row = cur.fetchone()
            if not row:
                print(f"[error] 找不到空间 space_code={args.space}，请先初始化 kb_space。")
                return 3
            space_id = row[0]
            print(f"目标空间 {args.space} -> space_id={space_id}")

            # 现存 kb 来源文档：slug -> (id, content_hash)
            cur.execute("SELECT id, slug, content_hash FROM kb_document "
                        "WHERE space_id=%s AND source='kb' AND is_delete=0", (space_id,))
            existing = {r[1]: (r[0], r[2]) for r in cur.fetchall()}
            slug_to_id: dict = {}

            # ---- upsert 文档 ----
            for d in docs:
                if d.slug in existing:
                    doc_id, old_hash = existing[d.slug]
                    slug_to_id[d.slug] = doc_id
                    if old_hash == d.content_hash:
                        stats["skip"] += 1
                        _log(cur, idgen, batch_no, space_id, doc_id, d.rel_path,
                             "skip", d.content_hash, now)
                        continue
                    cur.execute(
                        "UPDATE kb_document SET title=%s, summary=%s, content=%s, "
                        "kb_type=%s, domain=%s, status=%s, source_path=%s, "
                        "content_hash=%s, version_no=version_no+1, update_id=%s, "
                        "update_time=%s, publish_time=COALESCE(publish_time, %s) "
                        "WHERE id=%s",
                        (d.title, _summary(d), d.body, d.kb_type, _domain(d),
                         d.status, d.rel_path, d.content_hash, args.operator, now,
                         now if d.status == 1 else None, doc_id))
                    stats["update"] += 1
                    _log(cur, idgen, batch_no, space_id, doc_id, d.rel_path,
                         "update", d.content_hash, now)
                else:
                    doc_id = idgen.next()
                    slug_to_id[d.slug] = doc_id
                    cur.execute(
                        "INSERT INTO kb_document (id, create_id, create_time, "
                        "update_id, update_time, space_id, category_id, slug, source, "
                        "source_path, content_hash, title, summary, content, doc_type, "
                        "kb_type, domain, status, view_count, like_count, version_no, "
                        "publish_time, is_delete) VALUES "
                        "(%s,%s,%s,%s,%s,%s,NULL,%s,'kb',%s,%s,%s,%s,%s,'markdown',"
                        "%s,%s,%s,0,0,1,%s,0)",
                        (doc_id, args.operator, now, args.operator, now, space_id,
                         d.slug, d.rel_path, d.content_hash, d.title, _summary(d),
                         d.body, d.kb_type, _domain(d), d.status,
                         now if d.status == 1 else None))
                    stats["insert"] += 1
                    _log(cur, idgen, batch_no, space_id, doc_id, d.rel_path,
                         "insert", d.content_hash, now)

            # ---- 删除：DB 有、wiki 没有 ----
            wiki_slugs = {d.slug for d in docs}
            for slug, (doc_id, _h) in existing.items():
                if slug not in wiki_slugs:
                    cur.execute("UPDATE kb_document SET is_delete=1, update_time=%s "
                                "WHERE id=%s", (now, doc_id))
                    stats["delete"] += 1
                    _log(cur, idgen, batch_no, space_id, doc_id, slug,
                         "delete", None, now)

            # ---- 标签 ----
            _sync_tags(cur, idgen, space_id, docs, slug_to_id, args.operator, now)

            # ---- 关系 ----
            _sync_relations(cur, idgen, space_id, rels, slug_to_id, args.operator, now)

        conn.commit()
        print("\n同步完成：", " ".join(f"{k}={v}" for k, v in stats.items()))
        return 0
    except Exception as e:                          # noqa: BLE001
        conn.rollback()
        print(f"[error] 同步失败，已回滚：{e}")
        return 1
    finally:
        conn.close()


def _summary(d: "Doc") -> str:
    """取正文首个非标题、非空段落作摘要（<=500 字符）。"""
    for line in d.body.splitlines():
        s = line.strip()
        if s and not s.startswith("#") and not s.startswith("---"):
            return s[:500]
    return ""


def _domain(d: "Doc") -> str | None:
    """frontmatter domain 优先；否则从 tags 粗略推断。"""
    if d.domain:
        return d.domain
    low = [t.lower() for t in d.tags]
    if any(t in low for t in ("jp-fe", "基本情報", "日本語fe")):
        return "JP-FE"
    if any(t in low for t in ("jp-ap", "応用情報", "日本語ap")):
        return "JP-AP"
    if any(t in low for t in ("前端", "vue", "react")):
        return "FE"
    if any(t in low for t in ("mysql", "数据库", "db", "redis")):
        return "DB"
    if any(t in low for t in ("微服务", "spring", "java", "分布式")):
        return "MOLI"
    return None


def _log(cur, idgen, batch_no, space_id, doc_id, path, action, chash, now):
    cur.execute(
        "INSERT INTO kb_sync_log (id, batch_no, space_id, document_id, source_path, "
        "action, content_hash, status, message, create_time) VALUES "
        "(%s,%s,%s,%s,%s,%s,%s,'success',NULL,%s)",
        (idgen.next(), batch_no, space_id, doc_id, path, action, chash, now))


def _sync_tags(cur, idgen, space_id, docs, slug_to_id, operator, now):
    # 现有标签 name -> id（MySQL utf8mb4_ci 下大小写不敏感，需合并）
    cur.execute("SELECT id, tag_name FROM kb_tag WHERE space_id=%s AND is_delete=0",
                (space_id,))
    tag_id: dict[str, int] = {r[1]: r[0] for r in cur.fetchall()}

    def _tag_lookup(name: str) -> int | None:
        if name in tag_id:
            return tag_id[name]
        low = name.lower()
        for tn, tid in tag_id.items():
            if tn.lower() == low:
                return tid
        return None

    for d in docs:
        doc_id = slug_to_id.get(d.slug)
        if not doc_id:
            continue
        # 重建该文档的标签关联
        cur.execute("DELETE FROM kb_document_tag WHERE document_id=%s", (doc_id,))
        for name in d.tags:
            tid = _tag_lookup(name)
            if tid is None:
                tid = idgen.next()
                cur.execute(
                    "INSERT INTO kb_tag (id, create_id, create_time, update_id, "
                    "update_time, space_id, tag_name, color, is_delete) VALUES "
                    "(%s,%s,%s,%s,%s,%s,%s,NULL,0)",
                    (tid, operator, now, operator, now, space_id, name))
                tag_id[name] = tid
            cur.execute(
                "INSERT IGNORE INTO kb_document_tag (id, document_id, tag_id) "
                "VALUES (%s,%s,%s)", (idgen.next(), doc_id, tid))


def _sync_relations(cur, idgen, space_id, rels, slug_to_id, operator, now):
    # 先清掉本空间旧关系（全量重建，保持与 wiki 一致）
    cur.execute("DELETE FROM kb_relation WHERE space_id=%s", (space_id,))
    for r in rels:
        src_id = slug_to_id.get(r["src"])
        if not src_id:
            continue
        dst_id = slug_to_id.get(r["dst"]) if r["dst"] else None
        cur.execute(
            "INSERT INTO kb_relation (id, create_id, create_time, update_id, "
            "update_time, space_id, source_doc_id, target_doc_id, target_title, "
            "relation_type, resolved, weight, is_delete) VALUES "
            "(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,1,0)",
            (idgen.next(), operator, now, operator, now, space_id, src_id,
             dst_id, r["dst_title"], r["type"], r["resolved"]))


# ---------------------------------------------------------------------------
# main
# ---------------------------------------------------------------------------

def resolve_wiki_dir(path_arg: str) -> Path:
    p = Path(path_arg)
    if not p.is_absolute():
        p = KB_DIR / p
    return p.resolve()


def purge_raw_archive(args) -> int:
    """Soft-delete legacy rows imported with source='raw' (removed L1 pipeline)."""
    return _purge_by_source(args, source="raw", label="purge-raw-archive")


def purge_manual_web(args) -> int:
    """Soft-delete legacy Web MySQL-only rows (source='manual' or unset source without wiki slug)."""
    import pymysql

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.db,
        charset="utf8mb4",
        autocommit=False,
    )
    where = (
        "is_delete=0 AND (source='manual' OR "
        "(source IS NULL AND (slug IS NULL OR slug='')))"
    )
    try:
        with conn.cursor() as cur:
            if args.space:
                cur.execute(
                    "SELECT id FROM kb_space WHERE space_code=%s AND is_delete=0",
                    (args.space,),
                )
                row = cur.fetchone()
                if not row:
                    print(f"[error] 找不到空间 space_code={args.space}")
                    return 3
                space_id = row[0]
                cur.execute(
                    f"SELECT COUNT(*) FROM kb_document WHERE space_id=%s AND {where}",
                    (space_id,),
                )
            else:
                cur.execute(f"SELECT COUNT(*) FROM kb_document WHERE {where}")

            count = cur.fetchone()[0]
            if args.dry_run:
                print(f"purge-manual-web (dry-run): would soft-delete {count} rows"
                      + (f" (space={args.space})" if args.space else " (all spaces)"))
                _print_manual_purge_sample(cur, args.space)
                return 0

            if count == 0:
                print("purge-manual-web: 0 rows (nothing to do)")
                return 0

            if args.space:
                cur.execute(
                    f"UPDATE kb_document SET is_delete=1, update_time=%s "
                    f"WHERE space_id=%s AND {where}",
                    (now, space_id),
                )
            else:
                cur.execute(
                    f"UPDATE kb_document SET is_delete=1, update_time=%s WHERE {where}",
                    (now,),
                )
            print(f"purge-manual-web: {count} rows soft-deleted"
                  + (f" (space={args.space})" if args.space else " (all spaces)"))
        conn.commit()
        return 0
    except Exception as e:  # noqa: BLE001
        conn.rollback()
        print(f"[error] purge-manual-web 失败，已回滚：{e}")
        return 1
    finally:
        conn.close()


def _print_manual_purge_sample(cur, space_code, limit: int = 10) -> None:
    """Print a few rows that would be purged (dry-run helper)."""
    base_where = (
        "(source='manual' OR (source IS NULL AND (slug IS NULL OR slug='')))"
    )
    if space_code:
        cur.execute(
            "SELECT d.id, d.slug, d.title, d.source FROM kb_document d "
            "JOIN kb_space s ON s.id=d.space_id "
            f"WHERE s.space_code=%s AND d.is_delete=0 AND {base_where} "
            "ORDER BY d.update_time DESC LIMIT %s",
            (space_code, limit),
        )
    else:
        cur.execute(
            f"SELECT id, slug, title, source FROM kb_document "
            f"WHERE is_delete=0 AND {base_where} ORDER BY update_time DESC LIMIT %s",
            (limit,),
        )
    rows = cur.fetchall()
    if not rows:
        return
    print("  sample:")
    for row in rows:
        print(f"    id={row[0]} slug={row[1]!r} source={row[3]!r} title={row[2]!r}")


def _purge_by_source(args, source: str, label: str) -> int:
    import pymysql

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
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
                (args.space,),
            )
            row = cur.fetchone()
            if not row:
                print(f"[error] 找不到空间 space_code={args.space}")
                return 3
            space_id = row[0]
            cur.execute(
                "SELECT COUNT(*) FROM kb_document "
                "WHERE space_id=%s AND source=%s AND is_delete=0",
                (space_id, source),
            )
            count = cur.fetchone()[0]
            cur.execute(
                "UPDATE kb_document SET is_delete=1, update_time=%s "
                "WHERE space_id=%s AND source=%s AND is_delete=0",
                (now, space_id, source),
            )
            print(f"{label}: {count} rows (space={args.space})")
        conn.commit()
        return 0
    except Exception as e:  # noqa: BLE001
        conn.rollback()
        print(f"[error] {label} 失败，已回滚：{e}")
        return 1
    finally:
        conn.close()


def main():
    ap = argparse.ArgumentParser(description="kb -> kb_document 单向增量同步")
    ap.add_argument("--dry-run", action="store_true", help="仅解析并打印计划，不连库")
    ap.add_argument("--wiki-dir", default="wiki",
                    help="wiki 根目录，相对 kb/ 或绝对路径（默认 wiki）")
    ap.add_argument("--edges", default=None,
                    help="graph/edges.jsonl 路径（默认 <wiki-dir>/graph/edges.jsonl）")
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=3306)
    ap.add_argument("--user", default="root")
    ap.add_argument("--password", default="12345678")
    ap.add_argument("--db", default="moli")
    ap.add_argument("--space", default="enterprise-kb", help="kb_space.space_code")
    ap.add_argument("--operator", type=int, default=1, help="写入审计用的 create_id/update_id")
    ap.add_argument(
        "--purge-raw-archive",
        action="store_true",
        help="软删 source='raw' 的遗留归档行（已废弃的 L1 直写 DB，与 wiki 同步无关）",
    )
    ap.add_argument(
        "--purge-manual-web",
        action="store_true",
        help="软删 Web 直连 MySQL 遗留行（source=manual 或无 slug 的 NULL source）",
    )
    ap.add_argument(
        "--all-spaces",
        action="store_true",
        help="与 --purge-manual-web 联用：清理全部空间（忽略 --space）",
    )
    args = ap.parse_args()

    if args.purge_raw_archive:
        return purge_raw_archive(args)
    if args.purge_manual_web:
        if args.all_spaces:
            args.space = None
        return purge_manual_web(args)

    wiki_dir = resolve_wiki_dir(args.wiki_dir)
    rel_prefix = wiki_dir.name
    edges_file = Path(args.edges).resolve() if args.edges else wiki_dir / "graph" / "edges.jsonl"

    docs, by_slug, by_stem = load_docs(wiki_dir, rel_prefix)
    if not docs:
        print(f"[warn] {wiki_dir} 下没有可同步的文档页。")
        return 0
    edges = load_edges(edges_file)
    rels = build_relations(docs, by_slug, by_stem, edges)

    if args.dry_run:
        report_dry_run(docs, rels, wiki_dir)
        return 0
    return sync_to_db(docs, rels, args)


if __name__ == "__main__":
    sys.exit(main())
