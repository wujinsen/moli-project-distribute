#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""chunk_split 单测。"""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

TOOLS = Path(__file__).resolve().parents[1]
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

from chunk_split import split_markdown_body, strip_details_blocks  # noqa: E402


SAMPLE = """# 知识库三操作

> 核心理念：编译一次，持续保鲜。

## 1. 架构一览

简短节。

## 2. Ingest（提炼入库）

读 raw 源，写入 wiki，更新 index/log。流程包括：定分类、查重 enrich/create、补交叉引用、
更新 index.md 与 log.md。一个源通常会触及 5–15 个页。

## 3. Query（向知识库提问）

定作用域，读 index 选 ≤15 页，整页读入，带 [[slug]] 引用作答。综合多页时可 crystallize 回写 outputs。

## 4. Lint（健康体检）

扫描断链、孤儿、矛盾、过时、缺来源等问题，只报告并给修复建议。

## 相关

见 index。
"""


class TestChunkSplit(unittest.TestCase):
    def test_split_by_h2(self):
        chunks = split_markdown_body(SAMPLE)
        self.assertGreaterEqual(len(chunks), 3)
        headings = [c.heading for c in chunks]
        self.assertTrue(any("Ingest" in h for h in headings))
        self.assertTrue(any("Query" in h for h in headings))

    def test_merge_small_section(self):
        chunks = split_markdown_body(SAMPLE)
        small = [c for c in chunks if c.heading == "相关"]
        self.assertEqual(len(small), 0, "短文「相关」应合并到上一节")

    def test_strip_details(self):
        body = "正文\n<details><summary>x</summary>\nhidden\n</details>\n尾"
        self.assertNotIn("hidden", strip_details_blocks(body))

    def test_empty_body(self):
        self.assertEqual(split_markdown_body(""), [])
        self.assertEqual(split_markdown_body("   "), [])

    def test_single_chunk_no_h2(self):
        chunks = split_markdown_body("# 仅标题\n\n一段正文。")
        self.assertEqual(len(chunks), 1)
        self.assertIn("一段正文", chunks[0].content)


if __name__ == "__main__":
    unittest.main()
