#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""茉莉知识库 · Wiki Enrich 治理 CLI（零依赖 + 可选 LLM）

对齐 Ingest 工作台 EnrichWriter 与 ``AGENTS.md`` §4.1：在**已有 wiki 页**上追加 patch。

治理工具链::

    raw / plan.json ──► enrich.py ──► wiki/*.md + log.md + index.md + edges.jsonl
                            │
                            └──► lint.py --strict ──► sync_to_db.py

单页模式
--------
    python kb/tools/enrich.py \\
        --slug guides/本地启动指南 \\
        --raw docs/foo.md --reason "补充步骤" \\
        --batch-no 42 --topic "用户中心" \\
        --apply

批次模式（Plan JSON，与 Ingest 工作台 plan.enrich[] 兼容）
----------------------------------------------------------
    python kb/tools/enrich.py --plan enrich-plan.json --apply

Plan 示例见 ``enrich-plan.example.json``。

治理开关（``--apply`` 时默认全开，可用 ``--no-governance`` 关闭）：
  - append ``wiki/log.md``
  - append ``wiki/index.md`` 批次段
  - append ``wiki/graph/edges.jsonl``（plan.edges 或 ``--edges-file``）

LLM：``serve.py`` 的 ``llm_config.json`` / 环境变量。
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass, field
from datetime import date
from pathlib import Path
from typing import Any

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

HERE = Path(__file__).resolve().parent
KB_DIR = HERE.parent
DEFAULT_WIKI_DIR = KB_DIR / "wiki"
DEFAULT_RAW_ROOT = KB_DIR / "raw"
RAW_SNIPPET_CHARS = 4000

TYPE_DIRS = {
    "guide": "guides",
    "service": "services",
    "concept": "concepts",
    "article": "articles",
    "interview": "interview",
    "output": "outputs",
}

ENRICH_WRITER_PROMPT = (
    "你是企业知识库 wiki 的增量补充器（EnrichWriter）。任务：给一篇已有 wiki 页补充一个新章节。\n"
    "硬性规则：\n"
    "1) 只输出**要追加的 markdown 章节**（从一个 `## 标题` 开始），禁止重复已有内容、"
    "禁止整页重写、禁止 frontmatter、禁止解释或代码围栏；\n"
    "2) 内容忠于给定 raw 源，与已有正文不冲突；如发现冲突，在章节内用「> 注：」标注；\n"
    "3) [[..]] 互链只用「已知 slug 列表」里的 slug；\n"
    "4) 若目标路径在 `wiki/articles|concepts|interview`（enterprise-kb），**禁止**追加含「茉莉」或项目手册 wikilink 的章节。"
)

sys.path.insert(0, str(HERE))
from serve import PROVIDERS, call_llm, resolve_key  # noqa: E402
from sync_to_db import parse_frontmatter, resolve_wiki_dir  # noqa: E402

_FENCE = re.compile(r"^```(?:markdown|md)?\s*\n(.*?)\n```\s*$", re.S)


@dataclass
class EnrichTask:
    slug: str
    reason: str = ""
    raw_paths: list[str] = field(default_factory=list)
    patch: str = ""
    patch_file: str = ""


@dataclass
class BatchContext:
    batch_no: str
    topic: str
    marker: str
    tasks: list[EnrichTask]
    edges: list[dict[str, Any]]
    default_raw: list[str] = field(default_factory=list)


def bare_slug(slug: str) -> str:
    s = slug.strip().replace("\\", "/")
    if s.endswith(".md"):
        s = s[:-3]
    if "/" in s:
        s = s.rsplit("/", 1)[-1]
    return s


def resolve_wiki_rel(wiki_dir: Path, plan_slug: str) -> Path | None:
    s = plan_slug.strip().replace("\\", "/")
    if s.endswith(".md"):
        s = s[:-3]
    base = wiki_dir.resolve()
    if "/" in s:
        f = (base / f"{s}.md").resolve()
        return f if f.is_file() and str(f).startswith(str(base)) else None
    bare = bare_slug(s)
    for sub in TYPE_DIRS.values():
        f = (base / sub / f"{bare}.md").resolve()
        if f.is_file():
            return f
    return None


def wiki_rel_path(wiki_dir: Path, file_path: Path) -> str:
    rel = file_path.resolve().relative_to(wiki_dir.resolve())
    return str(rel.with_suffix("")).replace("\\", "/")


def collect_known_slugs(wiki_dir: Path, limit: int = 120) -> list[str]:
    slugs: list[str] = []
    for p in sorted(wiki_dir.rglob("*.md")):
        if p.name in {"index.md", "log.md"}:
            continue
        slugs.append(wiki_rel_path(wiki_dir, p))
        if len(slugs) >= limit:
            break
    return slugs


def read_raw_snippets(raw_paths: list[str], raw_root: Path) -> str:
    parts: list[str] = []
    for rp in raw_paths:
        rel = rp.replace("\\", "/").lstrip("/")
        if rel.startswith("raw/"):
            rel = rel[4:]
        f = (raw_root / rel).resolve()
        if not str(f).startswith(str(raw_root.resolve())) or not f.is_file():
            parts.append(f"\n===== raw/{rel} （文件不存在）=====\n")
            continue
        text = f.read_text(encoding="utf-8")
        if len(text) > RAW_SNIPPET_CHARS:
            text = text[:RAW_SNIPPET_CHARS] + "\n…（已截断）"
        parts.append(f"\n===== raw/{rel} =====\n{text}\n")
    return "".join(parts) if parts else "（无 raw 源）"


def strip_fence(text: str) -> str:
    s = text.strip()
    m = _FENCE.match(s)
    if m:
        return m.group(1).strip()
    if s.startswith("```"):
        lines = s.splitlines()
        if lines and lines[0].startswith("```"):
            lines = lines[1:]
        if lines and lines[-1].strip() == "```":
            lines = lines[:-1]
        return "\n".join(lines).strip()
    return s


def merge_enrich(baseline: str, patch: str) -> str:
    if not baseline.strip():
        return patch.strip() + "\n" if patch.strip() else ""
    if not patch.strip():
        return baseline
    return baseline.rstrip() + "\n\n" + patch.strip() + "\n"


def dump_frontmatter(meta: dict) -> str:
    lines = ["---"]
    order = ("title", "slug", "type", "status", "tags", "sources", "related", "created", "updated")
    seen = set(order)
    for key in order:
        if key not in meta:
            continue
        val = meta[key]
        if isinstance(val, list):
            if not val:
                lines.append(f"{key}: []")
            elif all(isinstance(x, str) for x in val) and len(str(val)) < 120:
                lines.append(f"{key}: [{', '.join(val)}]")
            else:
                lines.append(f"{key}:")
                for item in val:
                    lines.append(f"  - {item}")
        else:
            lines.append(f"{key}: {val}")
    for key, val in meta.items():
        if key in seen:
            continue
        lines.append(f"{key}: {val}")
    lines.append("---")
    return "\n".join(lines) + "\n"


def update_frontmatter_meta(text: str, raw_paths: list[str]) -> str:
    meta, body = parse_frontmatter(text)
    meta["updated"] = date.today().isoformat()
    sources = meta.get("sources") or []
    if isinstance(sources, str):
        sources = [sources] if sources else []
    for rp in raw_paths:
        rel = rp.replace("\\", "/").lstrip("/")
        entry = rel if rel.startswith("raw/") else f"raw/{rel}"
        if entry not in sources:
            sources.append(entry)
    meta["sources"] = sources
    return dump_frontmatter(meta) + body.lstrip("\n")


def batch_marker(batch_no: str) -> str:
    return f"<!-- enrich-batch:{batch_no} -->"


def append_log_batch(
    wiki_dir: Path,
    batch_no: str,
    topic: str,
    slugs: list[str],
    marker: str,
) -> bool:
    log_file = wiki_dir / "log.md"
    if not log_file.exists():
        log_file.write_text("# ingest 日志\n\n", encoding="utf-8")
    content = log_file.read_text(encoding="utf-8")
    if marker in content:
        print(f"[enrich] log.md 已含 marker，跳过")
        return False
    today = date.today().isoformat()
    names = ", ".join(bare_slug(s) for s in slugs)
    line = (
        f"## [{today}] ingest | 批次#{batch_no} {topic} (enrich.py); "
        f"enrich {names} {marker}\n"
    )
    with log_file.open("a", encoding="utf-8") as f:
        f.write(line)
    print(f"[enrich] append log.md: {line.strip()}")
    return True


def append_index_batch(
    wiki_dir: Path,
    batch_no: str,
    slugs: list[str],
    marker: str,
) -> bool:
    index_file = wiki_dir / "index.md"
    if not index_file.exists():
        print("[enrich] index.md 不存在，跳过 index 段")
        return False
    content = index_file.read_text(encoding="utf-8")
    if marker in content:
        print(f"[enrich] index.md 已含 marker，跳过")
        return False
    today = date.today().isoformat()
    sb = [
        f"\n## 批次 #{batch_no}（Enrich CLI {today}） {marker}\n",
    ]
    for slug in slugs:
        sb.append(f"- [[{bare_slug(slug)}]] — enrich\n")
    with index_file.open("a", encoding="utf-8") as f:
        f.write("".join(sb))
    print(f"[enrich] append index.md 批次段 ({len(slugs)} 页)")
    return True


def append_edges(
    wiki_dir: Path,
    edges: list[dict[str, Any]],
    touched_bare: set[str],
) -> int:
    if not edges:
        return 0
    edges_file = wiki_dir / "graph" / "edges.jsonl"
    existing = edges_file.read_text(encoding="utf-8") if edges_file.exists() else ""
    today = date.today().isoformat()
    sb: list[str] = []
    count = 0
    for e in edges:
        fr = str(e.get("from", "")).strip()
        to = str(e.get("to", "")).strip()
        if not fr or not to:
            continue
        if bare_slug(fr) not in touched_bare and bare_slug(to) not in touched_bare:
            continue
        line_obj = {
            "from": fr,
            "to": to,
            "type": e.get("type") or "relates_to",
            "evidence": e.get("evidence") or "",
            "date": today,
        }
        line = json.dumps(line_obj, ensure_ascii=False)
        if line in existing or any(line in x for x in sb):
            continue
        sb.append(line + "\n")
        count += 1
    if count == 0:
        return 0
    edges_file.parent.mkdir(parents=True, exist_ok=True)
    with edges_file.open("a", encoding="utf-8") as f:
        f.writelines(sb)
    print(f"[enrich] append edges.jsonl: {count} 条")
    return count


def generate_patch(
    slug: str,
    baseline: str,
    raw_paths: list[str],
    raw_root: Path,
    reason: str,
    known_slugs: list[str],
    provider_id: str,
) -> str:
    prov = PROVIDERS.get(provider_id)
    if not prov or not resolve_key(prov):
        raise SystemExit(
            f"LLM provider '{provider_id}' 未配置 key。"
            "请填 kb/tools/llm_config.json 或设置环境变量。"
        )
    user = (
        f"目标页 slug：{slug}\n"
        f"补充原因：{reason or '（未说明）'}\n"
        f"已知 slug 列表（互链可用）：\n"
        + "\n".join(f"- {s}" for s in known_slugs[:80])
        + "\n\n已有页当前全文：\n"
        + baseline
        + "\n\nraw 源内容（已截断）：\n"
        + read_raw_snippets(raw_paths, raw_root)
    )
    raw = call_llm(
        prov,
        [
            {"role": "system", "content": ENRICH_WRITER_PROMPT},
            {"role": "user", "content": user},
        ],
        timeout=120,
    )
    patch = strip_fence(raw)
    if not patch.lstrip().startswith("##"):
        patch = "## 补充\n\n" + patch
    return patch


def resolve_patch(
    task: EnrichTask,
    rel_slug: str,
    baseline: str,
    raw_root: Path,
    known_slugs: list[str],
    provider_id: str,
) -> str:
    if task.patch_file:
        return strip_fence(Path(task.patch_file).read_text(encoding="utf-8"))
    if task.patch.strip():
        return strip_fence(task.patch)
    if task.raw_paths:
        return generate_patch(
            rel_slug, baseline, task.raw_paths, raw_root,
            task.reason, known_slugs, provider_id,
        )
    raise ValueError(f"任务 {task.slug} 需 patch / patchFile / raw 之一")


def load_plan(path: Path) -> BatchContext:
    obj = json.loads(path.read_text(encoding="utf-8"))
    batch_no = str(obj.get("batchNo") or obj.get("batch_no") or "cli")
    topic = str(obj.get("topic") or "enrich")
    marker = batch_marker(batch_no)
    default_raw = list(obj.get("raw") or [])
    tasks: list[EnrichTask] = []
    for item in obj.get("enrich") or []:
        slug = str(item.get("slug", "")).strip()
        if not slug:
            continue
        raws = list(item.get("raw") or item.get("sources") or default_raw)
        tasks.append(EnrichTask(
            slug=slug,
            reason=str(item.get("reason") or ""),
            raw_paths=[str(r) for r in raws],
            patch=str(item.get("patch") or ""),
            patch_file=str(item.get("patchFile") or item.get("patch_file") or ""),
        ))
    edges = list(obj.get("edges") or [])
    return BatchContext(batch_no=batch_no, topic=topic, marker=marker,
                        tasks=tasks, edges=edges, default_raw=default_raw)


def load_edges_file(path: Path) -> list[dict[str, Any]]:
    text = path.read_text(encoding="utf-8").strip()
    if not text:
        return []
    if text.startswith("["):
        return json.loads(text)
    out: list[dict[str, Any]] = []
    for line in text.splitlines():
        line = line.strip()
        if line:
            out.append(json.loads(line))
    return out


def run_batch(
    ctx: BatchContext,
    wiki_dir: Path,
    raw_root: Path,
    provider_id: str,
    apply: bool,
    update_meta: bool,
    do_log: bool,
    do_index: bool,
    do_edges: bool,
) -> int:
    if not ctx.tasks:
        print("[error] 无 enrich 任务", file=sys.stderr)
        return 2
    known = collect_known_slugs(wiki_dir)
    applied_slugs: list[str] = []
    exit_code = 0

    for task in ctx.tasks:
        wiki_file = resolve_wiki_rel(wiki_dir, task.slug)
        if wiki_file is None:
            print(f"[error] 找不到 wiki 页: {task.slug}", file=sys.stderr)
            exit_code = 2
            continue
        rel_slug = wiki_rel_path(wiki_dir, wiki_file)
        baseline = wiki_file.read_text(encoding="utf-8")
        print(f"\n[enrich] === {rel_slug} ===")
        try:
            patch = resolve_patch(task, rel_slug, baseline, raw_root, known, provider_id)
        except ValueError as e:
            print(f"[error] {e}", file=sys.stderr)
            exit_code = 2
            continue

        merged = merge_enrich(baseline, patch)
        if not apply:
            print("--- patch ---")
            print(patch[:2000] + ("…" if len(patch) > 2000 else ""))
            continue

        if update_meta and task.raw_paths:
            merged = update_frontmatter_meta(merged, task.raw_paths)
        wiki_file.write_text(merged, encoding="utf-8")
        print(f"[enrich] wrote: {wiki_file}")
        applied_slugs.append(rel_slug)

    if not apply:
        print(f"\n[enrich] dry-run：{len(ctx.tasks)} 项；加 --apply 写盘")
        return exit_code

    if applied_slugs:
        if do_log:
            append_log_batch(wiki_dir, ctx.batch_no, ctx.topic, applied_slugs, ctx.marker)
        if do_index:
            append_index_batch(wiki_dir, ctx.batch_no, applied_slugs, ctx.marker)
        if do_edges and ctx.edges:
            touched = {bare_slug(s) for s in applied_slugs}
            append_edges(wiki_dir, ctx.edges, touched)

    print("[enrich] 建议: python kb/tools/lint.py --strict && sync")
    return exit_code


def main(argv: list[str] | None = None) -> int:
    p = argparse.ArgumentParser(description="Wiki enrich 治理 CLI")
    p.add_argument("--slug", help="单页 slug（与 --plan 二选一）")
    p.add_argument("--plan", type=Path, help="Plan JSON（批次 enrich[] + edges[]）")
    p.add_argument("--raw", action="append", default=[], help="raw 相对路径（可重复）")
    p.add_argument("--reason", default="", help="补充原因")
    p.add_argument("--batch-no", default="cli", help="批次号")
    p.add_argument("--topic", default="enrich", help="log 主题")
    p.add_argument("--wiki-dir", default="wiki", help="wiki 根（相对 kb/）")
    p.add_argument("--raw-root", default="raw", help="raw 根（相对 kb/）")
    p.add_argument("--provider", default="deepseek-v3", choices=list(PROVIDERS.keys()))
    p.add_argument("--patch-file", help="单页：人工 patch 文件")
    p.add_argument("--patch", help="单页：inline patch 文本")
    p.add_argument("--output-patch", help="写出 LLM 生成的 patch")
    p.add_argument("--edges-file", type=Path, help="edges JSON 数组或 jsonl")
    p.add_argument("--apply", action="store_true", help="写盘（默认 preview）")
    p.add_argument("--governance", action="store_true",
                   help="显式开启 log+index+edges（--apply 时默认已开）")
    p.add_argument("--no-governance", action="store_true", help="不写 log/index/edges")
    p.add_argument("--no-log", action="store_true")
    p.add_argument("--no-index", action="store_true")
    p.add_argument("--no-edges", action="store_true")
    p.add_argument("--no-meta", action="store_true", help="不更新 frontmatter")
    args = p.parse_args(argv)

    wiki_dir = resolve_wiki_dir(args.wiki_dir)
    raw_root = resolve_wiki_dir(args.raw_root)

    gov_default = args.apply and not args.no_governance
    do_log = gov_default and not args.no_log
    do_index = gov_default and not args.no_index
    do_edges = gov_default and not args.no_edges

    if args.plan:
        ctx = load_plan(args.plan)
        if args.edges_file:
            ctx.edges.extend(load_edges_file(args.edges_file))
        if args.batch_no != "cli":
            ctx.batch_no = args.batch_no
        if args.topic != "enrich":
            ctx.topic = args.topic
        ctx.marker = batch_marker(ctx.batch_no)
        return run_batch(
            ctx, wiki_dir, raw_root, args.provider, args.apply,
            not args.no_meta, do_log, do_index, do_edges,
        )

    if not args.slug:
        p.error("请指定 --slug 或 --plan")

    batch_no = args.batch_no
    marker = batch_marker(batch_no)
    task = EnrichTask(
        slug=args.slug,
        reason=args.reason,
        raw_paths=list(args.raw),
        patch=args.patch or "",
        patch_file=args.patch_file or "",
    )
    edges = load_edges_file(args.edges_file) if args.edges_file else []
    ctx = BatchContext(
        batch_no=batch_no,
        topic=args.topic,
        marker=marker,
        tasks=[task],
        edges=edges,
    )
    return run_batch(
        ctx, wiki_dir, raw_root, args.provider, args.apply,
        not args.no_meta, do_log, do_index, do_edges,
    )


if __name__ == "__main__":
    raise SystemExit(main())
