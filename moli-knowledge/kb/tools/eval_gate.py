#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""AI-3 门禁判定（§1.2 唯一实现）· 读 baselines.json + report JSON。"""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path
from typing import Any

KB_DIR = Path(__file__).resolve().parent.parent
DEFAULT_BASELINES = KB_DIR / "eval" / "baselines.json"
GATE_METRIC_KEY = "3"


def load_baselines(path: Path | None = None) -> dict[str, Any]:
    p = path or Path(os.environ.get("KB_EVAL_BASELINES", str(DEFAULT_BASELINES)))
    with p.open(encoding="utf-8") as f:
        return json.load(f)


def strategy_baseline(baselines: dict[str, Any], strategy: str) -> dict[str, Any] | None:
    strategies = baselines.get("strategies") or {}
    return strategies.get(strategy)


def evaluate_gate(
    report: dict[str, Any],
    strategy: str,
    baselines: dict[str, Any] | None = None,
) -> tuple[bool, list[str], bool | None]:
    """返回 (passed, failure_messages, gate_pass_for_db)。"""
    bl = baselines if baselines is not None else load_baselines()
    strat = strategy_baseline(bl, strategy)
    if strat is None:
        return True, [], None

    tolerance = float(strat.get("tolerance", 0))
    min_hit3 = float(strat["hit3"]) - tolerance
    min_dirty = float(strat["dirty_hit3"]) - tolerance

    hit_at = report.get("hit_at") or {}
    hit3 = float(hit_at.get(GATE_METRIC_KEY) or hit_at.get(str(GATE_METRIC_KEY)) or 0)
    errors = int(report.get("errors") or 0)

    dirty_block = (report.get("by_difficulty") or {}).get("dirty") or {}
    dirty_hit_at = dirty_block.get("hit_at") or {}
    dirty_hit3 = float(
        dirty_hit_at.get(GATE_METRIC_KEY)
        or dirty_hit_at.get(str(GATE_METRIC_KEY))
        or 0
    )

    failures: list[str] = []
    if hit3 + 1e-9 < min_hit3:
        failures.append(
            f"hit@3 {hit3:.4f} < baseline {strat['hit3']:.4f} − tol {tolerance:.2f} (= {min_hit3:.4f})"
        )
    if errors > 0:
        failures.append(f"errors={errors} (require 0)")
    if dirty_hit3 + 1e-9 < min_dirty:
        failures.append(
            f"dirty hit@3 {dirty_hit3:.4f} < baseline {strat['dirty_hit3']:.4f} "
            f"− tol {tolerance:.2f} (= {min_dirty:.4f})"
        )

    passed = len(failures) == 0
    return passed, failures, passed


def min_hit_from_baselines(strategy: str, baselines: dict[str, Any] | None = None) -> float | None:
    bl = baselines if baselines is not None else load_baselines()
    strat = strategy_baseline(bl, strategy)
    if strat is None:
        return None
    return float(strat["hit3"]) - float(strat.get("tolerance", 0))


def mysql_connect_params() -> dict[str, Any]:
    return {
        "host": os.environ.get("KB_SYNC_HOST") or os.environ.get("MYSQL_HOST", "127.0.0.1"),
        "port": int(os.environ.get("KB_SYNC_PORT") or os.environ.get("MYSQL_PORT", "3306")),
        "user": os.environ.get("KB_SYNC_USER") or os.environ.get("MYSQL_USER", "root"),
        "password": os.environ.get("KB_SYNC_PASSWORD") or os.environ.get("MYSQL_PASSWORD", "12345678"),
        "database": os.environ.get("KB_SYNC_DB") or os.environ.get("MYSQL_DB", "moli"),
        "charset": "utf8mb4",
    }


def _decimal_or_none(val: Any) -> float | None:
    if val is None:
        return None
    return float(val)


def emit_eval_run(
    report: dict[str, Any],
    report_path: Path,
    *,
    strategy: str | None,
    gate_pass: bool | None,
    git_sha: str | None = None,
) -> bool:
    """Insert kb_eval_run; 缺库参数时只 warn 并返回 False。"""
    try:
        import pymysql
    except ImportError:
        print("[warn] --emit-db 需要 pymysql，已跳过落库")
        return False

    params = mysql_connect_params()
    if not params.get("password") and not os.environ.get("KB_SYNC_PASSWORD"):
        pass  # allow empty password for local dev

    hit_at = report.get("hit_at") or {}
    abs_report = report_path.resolve()
    kb_root = KB_DIR.resolve()
    try:
        rel_path = abs_report.relative_to(kb_root)
    except ValueError:
        rel_path = report_path

    run_at = report.get("time") or report.get("run_at")
    if not run_at:
        from datetime import datetime

        run_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    elif "T" in str(run_at):
        run_at = str(run_at).replace("T", " ")[:19]

    row = {
        "run_at": run_at,
        "strategy": strategy,
        "use_llm": 1 if report.get("use_llm") else 0,
        "golden_total": int(report.get("total") or 0),
        "answerable_total": int(report.get("answerable_total") or 0),
        "negative_total": int(report.get("negative_total") or 0),
        "errors": int(report.get("errors") or 0),
        "hit1": _decimal_or_none(hit_at.get("1") or hit_at.get(1)),
        "hit3": _decimal_or_none(hit_at.get("3") or hit_at.get(3)),
        "hit5": _decimal_or_none(hit_at.get("5") or hit_at.get(5)),
        "hit8": _decimal_or_none(hit_at.get("8") or hit_at.get(8)),
        "mrr": _decimal_or_none(report.get("mrr")),
        "coverage": _decimal_or_none(report.get("coverage")),
        "refusal_accuracy": _decimal_or_none(report.get("refusal_accuracy")),
        "p95_ms": report.get("p95_ms"),
        "by_difficulty_json": json.dumps(report.get("by_difficulty") or {}, ensure_ascii=False),
        "report_path": str(rel_path).replace("\\", "/"),
        "git_sha": (git_sha or os.environ.get("GITHUB_SHA") or os.environ.get("GIT_SHA") or "")[:64]
        or None,
        "gate_pass": None if gate_pass is None else (1 if gate_pass else 0),
    }

    sql = """
        INSERT INTO kb_eval_run (
            run_at, strategy, use_llm, golden_total, answerable_total, negative_total, errors,
            hit1, hit3, hit5, hit8, mrr, coverage, refusal_accuracy, p95_ms,
            by_difficulty_json, report_path, git_sha, gate_pass
        ) VALUES (
            %(run_at)s, %(strategy)s, %(use_llm)s, %(golden_total)s, %(answerable_total)s,
            %(negative_total)s, %(errors)s, %(hit1)s, %(hit3)s, %(hit5)s, %(hit8)s,
            %(mrr)s, %(coverage)s, %(refusal_accuracy)s, %(p95_ms)s,
            %(by_difficulty_json)s, %(report_path)s, %(git_sha)s, %(gate_pass)s
        )
    """
    try:
        conn = pymysql.connect(**params, connect_timeout=10, read_timeout=30, write_timeout=30)
    except Exception as e:  # noqa: BLE001
        print(f"[warn] --emit-db 连库失败（{e}），已跳过落库")
        return False
    try:
        with conn.cursor() as cur:
            cur.execute(sql, row)
        conn.commit()
        print(f"落库: kb_eval_run strategy={strategy} gate_pass={gate_pass}")
        return True
    except Exception as e:  # noqa: BLE001
        print(f"[warn] --emit-db insert 失败（{e}）")
        return False
    finally:
        conn.close()


def main() -> int:
    import argparse

    ap = argparse.ArgumentParser(description="对 eval report JSON 做 baselines 门禁判定")
    ap.add_argument("report", type=Path, help="report JSON 路径")
    ap.add_argument("--strategy", required=True, choices=["ngram", "hybrid", "hybrid-rerank"])
    ap.add_argument("--baselines", type=Path, default=None)
    args = ap.parse_args()

    report = json.loads(args.report.read_text(encoding="utf-8"))
    bl = load_baselines(args.baselines)
    passed, failures, _ = evaluate_gate(report, args.strategy, bl)
    if passed:
        print(f"GATE PASS  strategy={args.strategy}")
        return 0
    print(f"GATE FAIL  strategy={args.strategy}")
    for msg in failures:
        print(f"  - {msg}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
