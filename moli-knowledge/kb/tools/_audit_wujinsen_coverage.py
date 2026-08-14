#!/usr/bin/env python3
"""Audit wujinsen_markdown coverage vs wiki sources."""
import os
import re
from collections import defaultdict
from pathlib import Path

KB = Path(__file__).resolve().parent.parent
RAW = KB / "raw" / "wujinsen_markdown"
WIKI = KB / "wiki"

SKIP_PREFIXES = [
    "DataBase/Oracle",
    "架构/Git",
    "架构/SAML",
    "架构/区块链",
    "架构/开发工具",
    "架构/消息队列/ActiveMQ",
    "架构/腾讯云",
    "架构/通信协议/Thrift",
    "面试笔试/2020程序员内推",
    "面试笔试/2020面试题整理",
    "面试笔试/大数据",
    "面试笔试/面试公司",
    "面试笔试/面试要求",
    # deleted earlier by user
    "我的资源",
    "language",
    "外语学习",
    "读书笔记",
    "Job",
    "暂未分类",
    "思考",
    "衣品",
    "书籍",
    "工具",
    "面试笔试/2025面试",
    "生活",
    "来自手机",
    "moli项目",
]

PLAN_SCOPE_PREFIXES = ["面试笔试", "架构", "DataBase"]


def norm(p: str) -> str:
    return p.replace("\\", "/")


def is_skip(rel: str) -> bool:
    return any(rel == p or rel.startswith(p + "/") for p in SKIP_PREFIXES)


def in_plan_scope(rel: str) -> bool:
    return any(rel == p or rel.startswith(p + "/") for p in PLAN_SCOPE_PREFIXES)


raw_all: list[str] = []
for dp, _, fns in os.walk(RAW):
    for fn in fns:
        if fn.endswith(".md"):
            raw_all.append(norm(os.path.relpath(os.path.join(dp, fn), RAW)))

cited: set[str] = set()
for dp, _, fns in os.walk(WIKI):
    for fn in fns:
        if not fn.endswith(".md") or fn in ("index.md", "log.md"):
            continue
        text = (Path(dp) / fn).read_text(encoding="utf-8")
        for s in re.findall(r"^\s*-\s*(raw/wujinsen_markdown/[^\n]+)", text, re.M):
            if s.strip().endswith("/"):
                continue
            cited.add(norm(s[len("raw/wujinsen_markdown/") :]))

uncited = [r for r in raw_all if r not in cited]
uncited_not_skip = [r for r in uncited if not is_skip(r)]
scope = [r for r in raw_all if in_plan_scope(r)]
scope_uncited = [r for r in scope if r not in cited and not is_skip(r)]

by_top = defaultdict(lambda: {"total": 0, "cited": 0, "uncited": 0, "skip": 0})
for r in raw_all:
    top = r.split("/")[0]
    by_top[top]["total"] += 1
    if is_skip(r):
        by_top[top]["skip"] += 1
    elif r in cited:
        by_top[top]["cited"] += 1
    else:
        by_top[top]["uncited"] += 1

print("=== wujinsen_markdown 覆盖审计 ===")
print(f"RAW .md 总数: {len(raw_all)}")
print(f"wiki sources 引用: {len(cited)} 篇（去重）")
print(f"未出现在 sources: {len(uncited)}")
print(f"  其中规划 skip/已删目录: {len(uncited) - len(uncited_not_skip)}")
print(f"  未 skip 且未 cited: {len(uncited_not_skip)}")
print()
print(f"规划范围（面试笔试+架构+DataBase）: {len(scope)} 篇")
print(f"  范围内未 cited 且非 skip: {len(scope_uncited)}")
print()
print("一级目录 (total / cited / uncited / skip):")
for k, v in sorted(by_top.items(), key=lambda x: -x[1]["total"]):
    print(f"  {v['total']:4d}  {v['cited']:4d}  {v['uncited']:4d}  {v['skip']:4d}  {k}")

if uncited_not_skip[:30]:
    print("\n未 ingest（非 skip）样例 ≤30:")
    for r in sorted(uncited_not_skip)[:30]:
        print(f"  - {r}")
    if len(uncited_not_skip) > 30:
        print(f"  ... 还有 {len(uncited_not_skip)-30} 篇")

if scope_uncited[:20]:
    print("\n规划范围内仍缺 sources（非 skip）≤20:")
    for r in sorted(scope_uncited)[:20]:
        print(f"  - {r}")
