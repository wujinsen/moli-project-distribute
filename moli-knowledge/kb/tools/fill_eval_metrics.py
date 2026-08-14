#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""跑评测并把 hit@k / MRR / coverage / 平均响应 等指标自动回填进 README / PORTFOLIO。

工作流：
  1）（可选 --run）先调 eval_ask.py 生成最新报告；
  2）读取 kb/eval/reports/ 下最新（或 --report 指定）的 report-*.json；
  3）计算 hit@1 / hit@3 / hit@k / MRR / coverage / kw_pass / 平均响应 / 样本数 / 知识页数；
  4）按标记锚点写进四个文档（幂等，可反复运行）：
        README.md / README.en.md / README.ja.md   -> <!-- KB_METRICS:START/END -->
        PORTFOLIO.md                               -> <!-- KB_METRICS_TABLE:START/END -->
  5）打印一份「可粘贴到简历 / 面试稿」的数值清单。

用法：
  # 服务已起 + wiki 已 sync 时，一步跑评测并回填（推荐）
  python moli-knowledge/kb/tools/fill_eval_metrics.py --run --use-llm

  # 只用已有的最新报告回填，不重新跑
  python moli-knowledge/kb/tools/fill_eval_metrics.py

  # 指定报告
  python moli-knowledge/kb/tools/fill_eval_metrics.py --report moli-knowledge/kb/eval/reports/report-20260716-101010.json
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from datetime import datetime
from pathlib import Path

TOOLS_DIR = Path(__file__).resolve().parent
KB_DIR = TOOLS_DIR.parent
REPO_ROOT = TOOLS_DIR.parents[2]  # tools -> kb -> moli-knowledge -> repo root
REPORT_DIR = KB_DIR / "eval" / "reports"
WIKI_DIRS = [KB_DIR / "wiki", KB_DIR / "wiki-moli", KB_DIR / "wiki-jp-exam"]

TARGET_INLINE = ["README.md", "README.en.md", "README.ja.md"]
TARGET_TABLE = ["PORTFOLIO.md"]

INLINE_RE = re.compile(r"(<!-- KB_METRICS:START -->)(.*?)(<!-- KB_METRICS:END -->)", re.DOTALL)
TABLE_RE = re.compile(r"(<!-- KB_METRICS_TABLE:START -->)(.*?)(<!-- KB_METRICS_TABLE:END -->)", re.DOTALL)


def run_eval(args: argparse.Namespace) -> int:
    """调用 eval_ask.py 生成新报告。"""
    cmd = [sys.executable, str(TOOLS_DIR / "eval_ask.py")]
    if args.use_llm:
        cmd.append("--use-llm")
    for flag, val in (("--login-base", args.login_base or args.gateway),
                      ("--username", args.username),
                      ("--password", args.password), ("--golden", args.golden),
                      ("--kb-base", args.kb_base), ("--top-k", args.top_k),
                      ("--llm-context-top-k", args.llm_context_top_k)):
        if val is not None:
            cmd += [flag, str(val)]
    print(f"[run] {' '.join(cmd)}\n")
    return subprocess.call(cmd)


def latest_report(report_arg: str | None) -> Path:
    if report_arg:
        p = Path(report_arg)
        if not p.is_absolute():
            p = REPO_ROOT / report_arg
        if not p.exists():
            raise SystemExit(f"[error] 报告不存在: {p}")
        return p
    reports = sorted(REPORT_DIR.glob("report-*.json"))
    if not reports:
        raise SystemExit(f"[error] {REPORT_DIR} 下没有 report-*.json，请先加 --run 跑评测。")
    return reports[-1]


def count_wiki_pages() -> int:
    total = 0
    for d in WIKI_DIRS:
        if d.exists():
            total += sum(1 for _ in d.rglob("*.md"))
    return total


def compute(report: dict) -> dict:
    top_k = report.get("top_k", 8)
    hit_at = report.get("hit_at") or {}
    scored = [r for r in report.get("results", []) if not r.get("error")]
    n = len(scored)

    def rate(pred) -> float | None:
        return (sum(1 for r in scored if pred(r)) / n) if n else None

    hit1 = rate(lambda r: r.get("first_rank") == 1)
    hit3 = hit_at.get("3")
    if hit3 is None:
        hit3 = rate(lambda r: 1 <= (r.get("first_rank") or 0) <= 3)
    hit5 = hit_at.get("5")
    if hit5 is None:
        hit5 = rate(lambda r: 1 <= (r.get("first_rank") or 0) <= 5)
    hitk = hit_at.get(str(top_k), report.get("hit_rate"))
    lat_ms = [r["elapsed_ms"] for r in scored if r.get("elapsed_ms") is not None]
    avg_lat_s = (sum(lat_ms) / len(lat_ms) / 1000) if lat_ms else None

    return {
        "top_k": top_k,
        "hit1": hit1,
        "hit3": hit3,
        "hit5": hit5,
        "hitk": hitk,
        "mrr": report.get("mrr"),
        "coverage": report.get("coverage"),
        "kw": report.get("kw_pass_rate"),
        "avg_lat_s": avg_lat_s,
        "samples": n or report.get("total"),
        "pages": count_wiki_pages(),
        "time": report.get("time", datetime.now().isoformat(timespec="seconds")),
    }


def pct(x: float | None) -> str:
    return f"{x * 100:.1f}%" if isinstance(x, (int, float)) else "N/A"


def num(x: float | None, nd: int = 3) -> str:
    return f"{x:.{nd}f}" if isinstance(x, (int, float)) else "N/A"


def sec(x: float | None) -> str:
    return f"{x:.2f}s" if isinstance(x, (int, float)) else "N/A"


LABELS = {
    "zh": {"lat": "平均响应", "q": "样本", "qs": "题", "cov": "coverage", "kw": "关键词命中"},
    "en": {"lat": "avg latency", "q": "", "qs": "questions", "cov": "coverage", "kw": "keyword pass"},
    "ja": {"lat": "平均応答", "q": "サンプル", "qs": "問", "cov": "coverage", "kw": "キーワード命中"},
}
SEP = {"zh": " ｜ ", "en": " | ", "ja": " | "}


def lang_of(fname: str) -> str:
    if fname.endswith(".en.md"):
        return "en"
    if fname.endswith(".ja.md"):
        return "ja"
    return "zh"


def render_inline(m: dict, lang: str) -> str:
    L, sep = LABELS[lang], SEP[lang]
    parts = [
        f"hit@1 `{pct(m['hit1'])}`",
        f"hit@3 `{pct(m['hit3'])}`",
        f"hit@5 `{pct(m['hit5'])}`",
        f"hit@{m['top_k']} `{pct(m['hitk'])}`",
        f"MRR `{num(m['mrr'])}`",
        f"{L['cov']} `{pct(m['coverage'])}`",
        f"{L['lat']} `{sec(m['avg_lat_s'])}`",
    ]
    if m["kw"] is not None:
        parts.append(f"{L['kw']} `{pct(m['kw'])}`")
    q = f"{L['q']} `{m['samples']}` {L['qs']}".strip()
    parts.append(q)
    return sep.join(parts)


def render_table(m: dict) -> str:
    rows = [
        "| 指标 | 数值 |",
        "|------|------|",
        f"| hit@1 | `{pct(m['hit1'])}` |",
        f"| hit@3 | `{pct(m['hit3'])}` |",
        f"| hit@5 | `{pct(m['hit5'])}` |",
        f"| hit@{m['top_k']} | `{pct(m['hitk'])}` |",
        f"| MRR | `{num(m['mrr'])}` |",
        f"| coverage | `{pct(m['coverage'])}` |",
        f"| 平均响应 | `{sec(m['avg_lat_s'])}` |",
    ]
    if m["kw"] is not None:
        rows.append(f"| 关键词命中 | `{pct(m['kw'])}` |")
    rows.append(f"| 知识页 / 测试题量 | `{m['pages']}` 页 / `{m['samples']}` 题 |")
    date = m["time"].split("T")[0]
    rows.append(f"| 评测时间 | `{date}` |")
    return "\n".join(rows)


def inject(path: Path, regex: re.Pattern, body: str) -> bool:
    if not path.exists():
        print(f"[skip] 不存在: {path}")
        return False
    text = path.read_text(encoding="utf-8")
    if not regex.search(text):
        print(f"[skip] 未找到标记: {path.name}")
        return False
    new = regex.sub(lambda mo: f"{mo.group(1)}\n{body}\n{mo.group(3)}", text)
    if new != text:
        path.write_text(new, encoding="utf-8")
        print(f"[ok]   已回填: {path.name}")
        return True
    print(f"[=]    无变化: {path.name}")
    return False


def main() -> int:
    ap = argparse.ArgumentParser(description="跑评测并把指标自动回填进 README / PORTFOLIO")
    ap.add_argument("--run", action="store_true", help="先调 eval_ask.py 生成最新报告")
    ap.add_argument("--use-llm", action="store_true", help="传给 eval_ask.py：生成式评测")
    ap.add_argument("--report", help="指定 report-*.json（默认取最新）")
    ap.add_argument("--login-base", default=os.environ.get("MOLI_LOGIN_BASE", ""))
    ap.add_argument("--gateway", default=os.environ.get("MOLI_GATEWAY", ""))
    ap.add_argument("--username")
    ap.add_argument("--password")
    ap.add_argument("--golden")
    ap.add_argument("--kb-base", default=os.environ.get("MOLI_KB_BASE", ""))
    ap.add_argument("--top-k", type=int)
    ap.add_argument("--llm-context-top-k", type=int)
    args = ap.parse_args()

    if args.run:
        code = run_eval(args)
        if code != 0:
            print(f"[warn] eval_ask.py 退出码 {code}，仍尝试用已有最新报告回填。")

    report_path = latest_report(args.report)
    report = json.loads(report_path.read_text(encoding="utf-8"))
    m = compute(report)

    print(f"\n== 指标（来自 {report_path.name}）==")
    print(render_inline(m, "zh"))
    print()

    for name in TARGET_INLINE:
        p = REPO_ROOT / name
        inject(p, INLINE_RE, render_inline(m, lang_of(name)))
    for name in TARGET_TABLE:
        inject(REPO_ROOT / name, TABLE_RE, render_table(m))

    print("\n== 可粘贴到简历 / 面试稿 ==")
    print(f"hit@1={pct(m['hit1'])}  hit@3={pct(m['hit3'])}  hit@5={pct(m['hit5'])}  "
          f"hit@{m['top_k']}={pct(m['hitk'])}  "
          f"MRR={num(m['mrr'])}  coverage={pct(m['coverage'])}  平均响应={sec(m['avg_lat_s'])}  "
          f"样本={m['samples']}题  知识页={m['pages']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
