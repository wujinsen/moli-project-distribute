#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""AI-4 W8 · NL2SQL 评测：读 nl2sql_testset.jsonl，跑 ask E2E + validator 离线门禁。

用法：
  # 离线（仅 validator 段，CI 默认可跑）
  python eval_nl2sql.py --validator-only --gate

  # 全量 E2E（需 ai-server + ai-agent + token）
  set MOLI_AI_BASE=http://127.0.0.1:21000/AiServer
  set MOLI_AI_TOKEN=<login token>
  python eval_nl2sql.py --gate
"""
from __future__ import annotations

import argparse
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Any

try:
    import httpx
except ImportError:
    httpx = None

EVAL_DIR = Path(__file__).resolve().parent
SERVER_ROOT = EVAL_DIR.parent.parent
DEFAULT_TESTSET = EVAL_DIR / "nl2sql_testset.jsonl"
DEFAULT_BASELINES = EVAL_DIR / "baselines.json"


def load_jsonl(path: Path) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    with path.open(encoding="utf-8") as f:
        for line_no, line in enumerate(f, 1):
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            try:
                rows.append(json.loads(line))
            except json.JSONDecodeError as ex:
                raise ValueError(f"{path}:{line_no} invalid JSON: {ex}") from ex
    return rows


def load_baselines(path: Path | None = None) -> dict[str, Any]:
    p = path or Path(os.environ.get("BI_EVAL_BASELINES", str(DEFAULT_BASELINES)))
    with p.open(encoding="utf-8") as f:
        return json.load(f)


def sql_contains(sql: str | None, needles: list[str]) -> bool:
    if not sql:
        return False
    upper = sql.upper()
    return all(n.upper() in upper for n in needles)


def mvn_executable() -> str:
    for name in ("mvn", "mvn.cmd"):
        path = shutil.which(name)
        if path:
            return path
    return "mvn"


def run_validator_mvn() -> int:
    cmd = [
        mvn_executable(),
        "-q",
        "-f",
        str(SERVER_ROOT / "pom.xml"),
        "test",
        "-Dtest=Nl2sqlTestsetValidatorTest",
    ]
    print("[eval] running", " ".join(cmd))
    proc = subprocess.run(cmd, cwd=str(SERVER_ROOT))
    return proc.returncode


def login_token(base: str, user: str, password: str) -> str:
    if httpx is None:
        raise RuntimeError("httpx required for E2E eval: pip install httpx")
    login_base = os.environ.get("MOLI_LOGIN_BASE", "http://127.0.0.1:21000/UserCenter")
    with httpx.Client(timeout=30.0) as client:
        resp = client.post(
            f"{login_base.rstrip('/')}/login",
            json={"userName": user, "password": password, "code": "", "uuid": ""},
        )
        resp.raise_for_status()
        body = resp.json()
        token = (body.get("data") or {}).get("token")
        if not token:
            raise RuntimeError(f"login failed: {body}")
        return token


def eval_ask_case(
    case: dict[str, Any],
    base: str,
    token: str,
) -> tuple[bool, str]:
    if httpx is None:
        return False, "httpx not installed"
    question = case.get("question") or ""
    expect = (case.get("expect") or "").lower()
    assert_cfg = case.get("assert") or {}

    with httpx.Client(timeout=120.0) as client:
        resp = client.post(
            f"{base.rstrip('/')}/bi/chat/ask",
            headers={"Authorization": token},
            json={"question": question, "stream": False},
        )
        if resp.status_code != 200:
            return False, f"HTTP {resp.status_code}"
        body = resp.json()
        if expect == "success":
            if body.get("code") != 200:
                return False, f"code={body.get('code')} msg={body.get('msg')}"
            data = body.get("data") or {}
            if data.get("status") != "SUCCESS":
                return False, f"status={data.get('status')} reject={data.get('rejectCode')}"
            sql = data.get("sql") or ""
            for needle in assert_cfg.get("sql_contains") or []:
                if needle.upper() not in sql.upper():
                    return False, f"sql missing {needle}: {sql[:120]}"
            min_rows = assert_cfg.get("min_rows")
            if min_rows is not None and (data.get("rowCount") or 0) < int(min_rows):
                return False, f"rowCount={data.get('rowCount')}"
            return True, "ok"
        if expect == "rejected":
            if body.get("code") != 200:
                return False, f"code={body.get('code')} msg={body.get('msg')}"
            data = body.get("data") or {}
            if data.get("status") != "REJECTED":
                return False, f"status={data.get('status')} sql={data.get('sql')}"
            allowed = assert_cfg.get("reject_codes") or []
            if allowed and data.get("rejectCode") not in allowed:
                return False, f"rejectCode={data.get('rejectCode')} not in {allowed}"
            return True, "ok"
        return False, f"unknown expect={expect}"


def evaluate_gate(report: dict[str, Any], baselines: dict[str, Any]) -> tuple[bool, list[str]]:
    tol = float(baselines.get("tolerance", 0))
    min_exec = float(baselines.get("min_exec_accuracy", 0.8)) - tol
    min_reject = float(baselines.get("min_reject_accuracy", 1.0)) - tol
    exec_acc = float(report.get("exec_accuracy") or 0)
    reject_acc = float(report.get("reject_accuracy") or 0)
    failures: list[str] = []
    if exec_acc + 1e-9 < min_exec:
        failures.append(f"exec_accuracy {exec_acc:.4f} < {min_exec:.4f}")
    if reject_acc + 1e-9 < min_reject:
        failures.append(f"reject_accuracy {reject_acc:.4f} < {min_reject:.4f}")
    return len(failures) == 0, failures


def main() -> int:
    parser = argparse.ArgumentParser(description="AI-4 NL2SQL eval (W8)")
    parser.add_argument("--testset", type=Path, default=DEFAULT_TESTSET)
    parser.add_argument("--baselines", type=Path, default=DEFAULT_BASELINES)
    parser.add_argument("--gate", action="store_true", help="apply baselines.json gate")
    parser.add_argument("--validator-only", action="store_true", help="only run Java validator testset")
    parser.add_argument("--report-out", type=Path, default=None)
    args = parser.parse_args()

    cases = load_jsonl(args.testset)
    ask_success = [c for c in cases if c.get("mode") == "ask" and c.get("expect") == "success"]
    ask_reject = [c for c in cases if c.get("mode") == "ask" and c.get("expect") == "rejected"]
    validator_cases = [c for c in cases if c.get("mode") == "validator"]

    report: dict[str, Any] = {
        "testset": str(args.testset.name),
        "total": len(cases),
        "ask_success_total": len(ask_success),
        "ask_reject_total": len(ask_reject),
        "validator_total": len(validator_cases),
        "ask_success_pass": 0,
        "ask_reject_pass": 0,
        "validator_pass": 0,
        "failures": [],
    }

    # Validator slice (Java testset reader)
    val_rc = run_validator_mvn()
    if val_rc == 0:
        report["validator_pass"] = len(validator_cases)
    else:
        report["failures"].append("validator mvn test failed")
        report["validator_pass"] = 0

    if args.validator_only:
        report["exec_accuracy"] = None
        report["reject_accuracy"] = (
            report["validator_pass"] / len(validator_cases) if validator_cases else 1.0
        )
        print(json.dumps(report, ensure_ascii=False, indent=2))
        if args.report_out:
            args.report_out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        if args.gate:
            failures: list[str] = []
            if report["validator_pass"] != len(validator_cases):
                failures.append(
                    f"validator {report['validator_pass']}/{len(validator_cases)} not 100%"
                )
            if val_rc != 0:
                failures.append("validator mvn exit non-zero")
            for msg in failures:
                print(f"[GATE FAIL] {msg}")
            return 0 if not failures else 1
        return 0 if val_rc == 0 else 1

    base = os.environ.get("MOLI_AI_BASE") or os.environ.get("BI_CHAT_BASE")
    token = os.environ.get("MOLI_AI_TOKEN") or os.environ.get("BI_CHAT_TOKEN")
    if not base or not token:
        user = os.environ.get("MOLI_EVAL_USER", "admin")
        password = os.environ.get("MOLI_EVAL_PASS", "123456")
        if not base:
            print("[warn] MOLI_AI_BASE unset; skipping ask E2E")
        else:
            try:
                token = login_token(base, user, password)
            except Exception as ex:
                print(f"[warn] auto login failed: {ex}; skipping ask E2E")
                token = None

    if base and token:
        for case in ask_success:
            ok, reason = eval_ask_case(case, base, token)
            if ok:
                report["ask_success_pass"] += 1
            else:
                report["failures"].append(f"{case.get('id')}: {reason}")
        for case in ask_reject:
            ok, reason = eval_ask_case(case, base, token)
            if ok:
                report["ask_reject_pass"] += 1
            else:
                report["failures"].append(f"{case.get('id')}: {reason}")
    else:
        report["failures"].append("ask E2E skipped (no base/token)")

    exec_total = len(ask_success)
    exec_pass = report["ask_success_pass"]
    reject_total = len(ask_reject) + len(validator_cases)
    reject_pass = report["ask_reject_pass"] + report["validator_pass"]
    report["exec_accuracy"] = (exec_pass / exec_total) if exec_total else 1.0
    report["reject_accuracy"] = (reject_pass / reject_total) if reject_total else 1.0

    print(json.dumps(report, ensure_ascii=False, indent=2))
    if args.report_out:
        args.report_out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    if args.gate:
        ok, gate_failures = evaluate_gate(report, load_baselines(args.baselines))
        for msg in gate_failures:
            print(f"[GATE FAIL] {msg}")
        if not ok:
            return 1
    if report["failures"] and base and token:
        return 1
    return 0 if val_rc == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
