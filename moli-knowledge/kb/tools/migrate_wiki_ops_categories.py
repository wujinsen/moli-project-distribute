#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""将 wiki-ops 文档迁移到新分类目录（develop/product/ops/test/guides）。"""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

HERE = Path(__file__).resolve().parent
WIKI_OPS = HERE.parent / "wiki-ops"

# 源相对路径 -> 目标一级目录
MOVES: dict[str, str] = {
    # product
    "guides/产品需求文档索引.md": "product/产品需求文档索引.md",
    # develop（索引 + 原 services/concepts）
    "guides/技术方案与架构索引.md": "develop/技术方案与架构索引.md",
    "guides/API契约索引.md": "develop/API契约索引.md",
    "guides/SQL与数据字典索引.md": "develop/SQL与数据字典索引.md",
    "guides/模块README索引.md": "develop/模块README索引.md",
    "guides/文档健康度巡检.md": "develop/文档健康度巡检.md",
    "services/用户中心.md": "develop/用户中心.md",
    "services/网关.md": "develop/网关.md",
    "services/订单服务.md": "develop/订单服务.md",
    "services/bi服务.md": "develop/bi服务.md",
    "services/知识库服务.md": "develop/知识库服务.md",
    "concepts/rbac-权限模型.md": "develop/rbac-权限模型.md",
    "concepts/认证与会话机制.md": "develop/认证与会话机制.md",
    # ops
    "guides/v1发布Runbook.md": "ops/v1发布Runbook.md",
    "guides/生产环境检查清单.md": "ops/生产环境检查清单.md",
    "guides/SQL迁移顺序.md": "ops/SQL迁移顺序.md",
    "guides/监控与日志.md": "ops/监控与日志.md",
    "guides/发布回滚指南.md": "ops/发布回滚指南.md",
    "guides/知识库工作台运维SOP.md": "ops/知识库工作台运维SOP.md",
    "guides/user-center-运维要点.md": "ops/user-center-运维要点.md",
    "guides/wiki同步指南.md": "ops/wiki同步指南.md",
    "guides/故障排查指南.md": "ops/故障排查指南.md",
    "guides/nginx反向代理与前端部署指南.md": "ops/nginx反向代理与前端部署指南.md",
    "guides/docker部署指南.md": "ops/docker部署指南.md",
    "guides/minio-附件存储指南.md": "ops/minio-附件存储指南.md",
    # test
    "guides/测试文档索引.md": "test/测试文档索引.md",
}

# slug 前缀重写（edges / 正文全路径引用）
PREFIX_REWRITE = [
    ("guides/产品需求文档索引", "product/产品需求文档索引"),
    ("guides/技术方案与架构索引", "develop/技术方案与架构索引"),
    ("guides/API契约索引", "develop/API契约索引"),
    ("guides/SQL与数据字典索引", "develop/SQL与数据字典索引"),
    ("guides/模块README索引", "develop/模块README索引"),
    ("guides/文档健康度巡检", "develop/文档健康度巡检"),
    ("guides/v1发布Runbook", "ops/v1发布Runbook"),
    ("guides/生产环境检查清单", "ops/生产环境检查清单"),
    ("guides/SQL迁移顺序", "ops/SQL迁移顺序"),
    ("guides/监控与日志", "ops/监控与日志"),
    ("guides/发布回滚指南", "ops/发布回滚指南"),
    ("guides/知识库工作台运维SOP", "ops/知识库工作台运维SOP"),
    ("guides/user-center-运维要点", "ops/user-center-运维要点"),
    ("guides/wiki同步指南", "ops/wiki同步指南"),
    ("guides/故障排查指南", "ops/故障排查指南"),
    ("guides/nginx反向代理与前端部署指南", "ops/nginx反向代理与前端部署指南"),
    ("guides/docker部署指南", "ops/docker部署指南"),
    ("guides/minio-附件存储指南", "ops/minio-附件存储指南"),
    ("guides/测试文档索引", "test/测试文档索引"),
    ("services/用户中心", "develop/用户中心"),
    ("services/网关", "develop/网关"),
    ("services/订单服务", "develop/订单服务"),
    ("services/bi服务", "develop/bi服务"),
    ("services/知识库服务", "develop/知识库服务"),
    ("concepts/rbac-权限模型", "develop/rbac-权限模型"),
    ("concepts/认证与会话机制", "develop/认证与会话机制"),
]

PATH_TEXT_REWRITE = [
    ("wiki-ops/guides/产品需求文档索引", "wiki-ops/product/产品需求文档索引"),
    ("wiki-ops/guides/技术方案与架构索引", "wiki-ops/develop/技术方案与架构索引"),
    ("wiki-ops/guides/API契约索引", "wiki-ops/develop/API契约索引"),
    ("wiki-ops/guides/SQL与数据字典索引", "wiki-ops/develop/SQL与数据字典索引"),
    ("wiki-ops/guides/模块README索引", "wiki-ops/develop/模块README索引"),
    ("wiki-ops/guides/文档健康度巡检", "wiki-ops/develop/文档健康度巡检"),
    ("wiki-ops/guides/v1发布Runbook", "wiki-ops/ops/v1发布Runbook"),
    ("wiki-ops/guides/生产环境检查清单", "wiki-ops/ops/生产环境检查清单"),
    ("wiki-ops/guides/SQL迁移顺序", "wiki-ops/ops/SQL迁移顺序"),
    ("wiki-ops/guides/监控与日志", "wiki-ops/ops/监控与日志"),
    ("wiki-ops/guides/发布回滚指南", "wiki-ops/ops/发布回滚指南"),
    ("wiki-ops/guides/知识库工作台运维SOP", "wiki-ops/ops/知识库工作台运维SOP"),
    ("wiki-ops/guides/user-center-运维要点", "wiki-ops/ops/user-center-运维要点"),
    ("wiki-ops/guides/wiki同步指南", "wiki-ops/ops/wiki同步指南"),
    ("wiki-ops/guides/故障排查指南", "wiki-ops/ops/故障排查指南"),
    ("wiki-ops/guides/nginx反向代理与前端部署指南", "wiki-ops/ops/nginx反向代理与前端部署指南"),
    ("wiki-ops/guides/docker部署指南", "wiki-ops/ops/docker部署指南"),
    ("wiki-ops/guides/minio-附件存储指南", "wiki-ops/ops/minio-附件存储指南"),
    ("wiki-ops/guides/测试文档索引", "wiki-ops/test/测试文档索引"),
    ("wiki-ops/services/", "wiki-ops/develop/"),
    ("wiki-ops/concepts/", "wiki-ops/develop/"),
    ("kb/wiki-ops/services/", "kb/wiki-ops/develop/"),
    ("kb/wiki-ops/concepts/", "kb/wiki-ops/develop/"),
]


def move_files() -> None:
    for src_rel, dst_rel in MOVES.items():
        src = WIKI_OPS / src_rel
        dst = WIKI_OPS / dst_rel
        if not src.exists():
            print(f"[skip] missing {src_rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        if dst.exists():
            dst.unlink()
        shutil.move(str(src), str(dst))
        print(f"moved: {src_rel} -> {dst_rel}")


def rewrite_edges() -> None:
    edges_file = WIKI_OPS / "graph" / "edges.jsonl"
    if not edges_file.exists():
        return
    lines = []
    for line in edges_file.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        o = json.loads(line)
        for old, new in PREFIX_REWRITE:
            if o.get("from") == old:
                o["from"] = new
            if o.get("to") == old:
                o["to"] = new
        lines.append(json.dumps(o, ensure_ascii=False))
    edges_file.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"updated: {edges_file.relative_to(WIKI_OPS.parent)}")


def rewrite_wiki_ops_text() -> None:
    for md in WIKI_OPS.rglob("*.md"):
        text = md.read_text(encoding="utf-8")
        orig = text
        for old, new in PREFIX_REWRITE:
            text = text.replace(old, new)
        for old, new in PATH_TEXT_REWRITE:
            text = text.replace(old, new)
        # 旧目录说明
        text = re.sub(
            r"本空间 \*\*services/\*\*",
            "本空间 **develop/**（微服务实体）",
            text,
        )
        if text != orig:
            md.write_text(text, encoding="utf-8")
            print(f"patched: {md.relative_to(WIKI_OPS)}")


def cleanup_empty_dirs() -> None:
    for d in ["services", "concepts"]:
        p = WIKI_OPS / d
        if p.exists() and not any(p.rglob("*.md")):
            try:
                p.rmdir()
                print(f"removed empty: {d}/")
            except OSError:
                pass


def main() -> int:
    move_files()
    rewrite_edges()
    rewrite_wiki_ops_text()
    cleanup_empty_dirs()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
