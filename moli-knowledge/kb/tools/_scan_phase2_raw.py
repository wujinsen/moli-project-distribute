#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
from collections import defaultdict
from pathlib import Path

RAW = Path(__file__).resolve().parent.parent / "raw" / "wujinsen_markdown"
P1_TOP = {"面试笔试", "架构", "DataBase"}


def count_md(p: Path) -> int:
    return sum(1 for _ in p.rglob("*.md"))


def main():
    rows = []
    for top in sorted(RAW.iterdir()):
        if not top.is_dir() or top.name in P1_TOP:
            continue
        n = count_md(top)
        if n == 0:
            continue
        subs = []
        for sub in sorted(top.iterdir()):
            if sub.is_dir():
                sn = count_md(sub)
                if sn:
                    subs.append((sub.name, sn))
        rows.append((top.name, n, subs))

    print(f"Phase2 top-level dirs: {len(rows)}")
    total = sum(r[1] for r in rows)
    print(f"Phase2 total md: {total}\n")
    for name, n, subs in rows:
        print(f"{name}: {n}")
        for sn, sc in subs[:25]:
            print(f"  {sn}: {sc}")
        if len(subs) > 25:
            print(f"  ... +{len(subs)-25} subdirs")


if __name__ == "__main__":
    main()
