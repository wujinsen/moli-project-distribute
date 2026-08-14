#!/usr/bin/env python3
# -*- coding: utf-8
"""T22: audit_wujinsen_images citation regex + load_wiki_citations."""
from __future__ import annotations

import re
import sys
import tempfile
import unittest
from pathlib import Path

TOOLS = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(TOOLS))

import audit_wujinsen_images as audit  # noqa: E402

RAW_SRC = audit.RAW_SRC


class TestRawSrcRegex(unittest.TestCase):
    def _match(self, line: str) -> list[str]:
        return RAW_SRC.findall(line)

    def test_simple_path(self):
        rel = self._match("- raw/wujinsen_markdown/jvm/Minor GC和Full GC区别.note.md")
        self.assertEqual(rel, ["jvm/Minor GC和Full GC区别.note.md"])

    def test_spaces_in_title(self):
        rel = self._match(
            "- raw/wujinsen_markdown/jvm/《深入理解 Java 内存模型》读书笔记.note.md"
        )
        self.assertEqual(rel, ["jvm/《深入理解 Java 内存模型》读书笔记.note.md"])

    def test_square_brackets_in_path(self):
        rel = self._match(
            "- raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop大数据面试--Hadoop篇 [复制链接].note.md"
        )
        self.assertEqual(
            rel,
            ["大数据资料-王/hadoop/Hadoop大数据面试--Hadoop篇 [复制链接].note.md"],
        )

    def test_vue_abp_path(self):
        rel = self._match(
            "- raw/wujinsen_markdown/前端/Vue/[Abp vNext微服务实践] - vue-element-admin登录一.note.md"
        )
        self.assertEqual(
            rel,
            ["前端/Vue/[Abp vNext微服务实践] - vue-element-admin登录一.note.md"],
        )

    def test_attach_md_path(self):
        line = (
            "- raw/wujinsen_markdown/jvm/周志明的书.note.attach/"
            "Java虚拟机：JVM高级特性与最佳实践（第2版）.md"
        )
        rel = self._match(line)
        self.assertEqual(
            rel,
            ["jvm/周志明的书.note.attach/Java虚拟机：JVM高级特性与最佳实践（第2版）.md"],
        )

    def test_does_not_cross_newline(self):
        text = "- raw/wujinsen_markdown/a.note.md\n- raw/wujinsen_markdown/b.note.md"
        self.assertEqual(
            RAW_SRC.findall(text),
            ["a.note.md", "b.note.md"],
        )


class TestLoadWikiCitations(unittest.TestCase):
    def setUp(self) -> None:
        self._tmpdir = tempfile.TemporaryDirectory()
        self._orig_wiki = audit.WIKI
        audit.WIKI = Path(self._tmpdir.name)

    def tearDown(self) -> None:
        audit.WIKI = self._orig_wiki
        self._tmpdir.cleanup()

    def _write_hub(self, category: str, slug: str, raw_rels: list[str]) -> None:
        cat_dir = audit.WIKI / category
        cat_dir.mkdir(parents=True, exist_ok=True)
        sources = "\n".join(f"- raw/wujinsen_markdown/{r}" for r in raw_rels)
        content = (
            f"---\n"
            f"title: {slug}\n"
            f"slug: {slug}\n"
            f"sources:\n"
            f"{sources}\n"
            f"---\n\n"
            f"# {slug}\n"
        )
        (cat_dir / f"{slug}.md").write_text(content, encoding="utf-8")

    def test_loads_spaced_and_bracket_paths(self):
        spaced = "jvm/《深入理解 Java 内存模型》读书笔记.note.md"
        bracket = "大数据资料-王/hadoop/Hadoop大数据面试--Hadoop篇 [复制链接].note.md"
        self._write_hub("java", "jvm-内存与gc", [spaced])
        self._write_hub("bigdata", "hadoop-面试题", [bracket])

        cites = audit.load_wiki_citations()
        self.assertEqual(cites.get(spaced), ["java/jvm-内存与gc"])
        self.assertEqual(cites.get(bracket), ["bigdata/hadoop-面试题"])

    def test_skips_index_and_log(self):
        (audit.WIKI / "index.md").write_text(
            "- raw/wujinsen_markdown/should-skip.note.md\n", encoding="utf-8"
        )
        (audit.WIKI / "log.md").write_text(
            "- raw/wujinsen_markdown/also-skip.note.md\n", encoding="utf-8"
        )
        cites = audit.load_wiki_citations()
        self.assertNotIn("should-skip.note.md", cites)
        self.assertNotIn("also-skip.note.md", cites)

    def test_body_citation_counts(self):
        body_path = "并发编程/Netty/翻译文章/Java Netty 4.x 用户指南.note.md"
        cat_dir = audit.WIKI / "middleware"
        cat_dir.mkdir(parents=True, exist_ok=True)
        text = (
            "---\n"
            "slug: netty-reactor与线程模型\n"
            "---\n\n"
            f"见 raw/wujinsen_markdown/{body_path}\n"
        )
        (cat_dir / "netty-reactor与线程模型.md").write_text(text, encoding="utf-8")
        cites = audit.load_wiki_citations()
        self.assertEqual(cites.get(body_path), ["middleware/netty-reactor与线程模型"])


if __name__ == "__main__":
    unittest.main()
