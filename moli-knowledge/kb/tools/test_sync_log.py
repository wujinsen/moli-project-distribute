#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""KBOPS-1: sync_to_db 日志辅助函数单测（无 DB）。"""

from __future__ import annotations

import sys
import unittest
from pathlib import Path
from unittest.mock import MagicMock

TOOLS = Path(__file__).resolve().parent
sys.path.insert(0, str(TOOLS))

import sync_to_db as mod  # noqa: E402


class SyncLogHelperTest(unittest.TestCase):
    def test_truncate_message(self):
        self.assertIsNone(mod._truncate_message(None))
        self.assertEqual("ok", mod._truncate_message("ok"))
        long = "x" * 600
        out = mod._truncate_message(long)
        self.assertLessEqual(len(out), 512)
        self.assertTrue(out.endswith("…"))

    def test_log_writes_status_and_message(self):
        cur = MagicMock()
        idgen = MagicMock()
        idgen.next.return_value = 99
        mod._log(cur, idgen, "batch1", 1, 2, "wiki/foo.md", "sync", "abc", "now",
                 status="fail", message="boom")
        sql, params = cur.execute.call_args[0]
        self.assertIn("status", sql)
        self.assertEqual(params[7], "fail")
        self.assertEqual(params[8], "boom")


if __name__ == "__main__":
    unittest.main()
