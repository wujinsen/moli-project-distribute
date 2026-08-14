#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Phase 0 lint pass: bare related/source_pages ops slugs, edges, doc examples."""
from __future__ import annotations

import json
import re
import sys
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from sync_to_db import load_edges, resolve  # noqa: E402

HERE = Path(__file__).resolve().parent
WIKI = HERE.parent / "wiki"
EDGES = WIKI / "graph" / "edges.jsonl"
LOG = WIKI / "log.md"

HUB = "项目文档总览"
OPS = {
    "docker部署指南", "minio-附件存储指南", "nginx反向代理与前端部署指南",
    "swagger接口调试指南", "wiki同步指南", "前端开发与联调指南", "故障排查指南",
    "数据库初始化指南", "本地启动指南", "权限管理操作指南", "查询与体检指南",
    "登录与鉴权指南", "知识库使用指南", "用户中心", "网关", "订单服务",
    "bi服务", "知识库服务", "rbac-权限模型", "认证与会话机制",
}

WIKI_LINK_MAP = {
    "services/知识库服务": HUB,
    "知识库服务": HUB,
    "swagger接口调试指南": HUB,
    "权限管理操作指南": HUB,
    "spring-事务面试题": "spring-事务",
    "spring-boot-自动配置原理": "spring-boot-自动配置",
    "user-center-shiro-starter": "shiro-starter与跨服务校验",
    "concepts/GlobalExceptionHandler": None,
    "GlobalExceptionHandler": None,
    "concepts/k6": "秒杀压测指南",
    "k6": "秒杀压测指南",
    "促销-规则引擎规划": None,
}


def load_slug_index():
    by_slug: dict = {}
    by_stem: dict = {}
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        rel = p.relative_to(WIKI).with_suffix("").as_posix()
        by_slug[rel] = rel
        by_stem[p.stem] = rel
        by_stem[rel.split("/")[-1]] = rel
    return by_slug, by_stem


def replace_bare_ops(text: str) -> str:
    for slug in sorted(OPS, key=len, reverse=True):
        text = re.sub(
            rf"(?<=[\[,\s]){re.escape(slug)}(?=[,\]\s])",
            HUB,
            text,
        )
    return text


def replace_wikilinks(text: str) -> str:
    for old, new in sorted(WIKI_LINK_MAP.items(), key=lambda x: len(x[0]), reverse=True):
        if new is None:
            text = re.sub(r"\[\[" + re.escape(old) + r"\]\]", "", text)
            text = re.sub(r",\s*" + re.escape(old), "", text)
            text = re.sub(re.escape(old) + r",\s*", "", text)
        else:
            text = re.sub(r"\[\[" + re.escape(old) + r"\]\]", f"[[{new}]]", text)
            text = re.sub(
                rf"(?<=[\[,\s]){re.escape(old)}(?=[,\]\s])",
                new,
                text,
            )
    for slug in OPS:
        text = re.sub(r"\[\[" + re.escape(slug) + r"\]\]", f"[[{HUB}]]", text)
    return text


def fix_doc_examples(text: str) -> str:
    repl = {
        "[[不存在的页]]": "`[[不存在的页]]`",
        "[[slug]]": "`[[slug]]`",
        "[[链接]]": "`[[链接]]`",
        "[[双链]]": "`[[双链]]`",
    }
    for a, b in repl.items():
        if a in text and b not in text:
            # only replace in guides (doc), not if already backtick-wrapped
            text = text.replace(a, b)
    # undo double backticks if any
    text = text.replace("``[[", "`[[")
    return text


def clean_edges(by_slug, by_stem) -> int:
    if not EDGES.exists():
        return 0
    kept, removed = [], 0
    for line in EDGES.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            o = json.loads(line)
        except json.JSONDecodeError:
            kept.append(line)
            continue
        to = o.get("to", "")
        dst = resolve(to, by_slug, by_stem) if to else None
        frm = o.get("from", "")
        src = resolve(frm, by_slug, by_stem) if frm else None
        if not src or (to and not dst):
            removed += 1
            continue
        if to and dst and dst != to:
            o["to"] = dst
            line = json.dumps(o, ensure_ascii=False)
        kept.append(line)
    EDGES.write_text("\n".join(kept) + ("\n" if kept else ""), encoding="utf-8")
    return removed


def main() -> None:
    for p in WIKI.rglob("*.md"):
        if p.name in ("index.md", "log.md"):
            continue
        old = p.read_text(encoding="utf-8")
        new = fix_doc_examples(replace_wikilinks(replace_bare_ops(old)))
        new = re.sub(r"\n{3,}", "\n\n", new)
        new = re.sub(r",\s*,", ",", new)
        new = re.sub(r"\[\s*\]", "", new)
        if new != old:
            p.write_text(new, encoding="utf-8")

    by_slug, by_stem = load_slug_index()
    er = clean_edges(by_slug, by_stem)

    with LOG.open("a", encoding="utf-8") as f:
        f.write(f"## [{date.today().isoformat()}] maintenance | Phase0 lint pass：bare related 替换、断链修复、edges 清理 {er} 条\n")
    print(f"edges_removed={er}")


if __name__ == "__main__":
    main()
