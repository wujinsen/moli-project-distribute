#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""薄封装：sync 后一行命令建向量索引，转调 kb-retrieval/scripts/index_chunks.py。

契约 AI-2 §1.4 / §3 Phase W3 可选交付；正式实现在 sidecar 同目录 CLI。

  python kb/tools/build_vector_index.py
  python kb/tools/build_vector_index.py --dry-run --space-id 900000000000000003
"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

_INDEX_SCRIPT = (
    Path(__file__).resolve().parent.parent.parent
    / "kb-retrieval"
    / "scripts"
    / "index_chunks.py"
)


def main() -> int:
    if not _INDEX_SCRIPT.is_file():
        print(f"[error] 找不到 {_INDEX_SCRIPT}", file=sys.stderr)
        return 1
    cmd = [sys.executable, "-u", str(_INDEX_SCRIPT), *sys.argv[1:]]
    return subprocess.call(cmd)


if __name__ == "__main__":
    raise SystemExit(main())
