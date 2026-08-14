#!/usr/bin/env python3
"""AI-10 DeepResearch smoke: fixed topic, two runs, compare citation slug sets."""

from __future__ import annotations

import argparse
import json
import os
import sys
import uuid

from deep_research.models import ResearchOptions, ResearchSidecarRequest
from deep_research.orchestrator import run_research


DEFAULT_TOPIC = "茉莉微服务架构"
DEFAULT_SPACE_ID = 900000000000000003


def _slug_set(result) -> set[str]:
    slugs = {c.slug for c in (result.citations or []) if c.slug}
    if result.report_md:
        import re

        for match in re.finditer(r"source_pages:\s*\[(.*?)\]", result.report_md):
            inner = match.group(1)
            for part in inner.split(","):
                part = part.strip().strip("'\"")
                if part:
                    slugs.add(part)
    return slugs


def run_once(topic: str, space_id: int, auth_token: str | None):
    req = ResearchSidecarRequest(
        runId=str(uuid.uuid4()),
        topic=topic,
        spaceId=space_id,
        authToken=auth_token,
        options=ResearchOptions(
            maxSections=4,
            maxRetrieveRounds=2,
            latencyBudgetMs=120_000,
            topK=8,
            perSectionTopK=8,
            coverageThreshold=0.75,
        ),
    )
    return run_research(req)


def main() -> int:
    parser = argparse.ArgumentParser(description="DeepResearch smoke (AI-10 Phase B)")
    parser.add_argument("--topic", default=DEFAULT_TOPIC)
    parser.add_argument("--space-id", type=int, default=DEFAULT_SPACE_ID)
    parser.add_argument("--auth-token", default=os.environ.get("KB_AUTH_TOKEN"))
    parser.add_argument("--json-out", help="Write comparison JSON to path")
    args = parser.parse_args()

    if args.auth_token:
        os.environ.setdefault("KB_AUTH_TOKEN", args.auth_token)

    print(f"[smoke] topic={args.topic!r} spaceId={args.space_id}")
    r1 = run_once(args.topic, args.space_id, args.auth_token)
    r2 = run_once(args.topic, args.space_id, args.auth_token)

    s1 = _slug_set(r1)
    s2 = _slug_set(r2)
    stable = s1 == s2 and len(s1) > 0

    report = {
        "topic": args.topic,
        "run1": {"status": r1.status, "slugCount": len(s1), "slugs": sorted(s1)},
        "run2": {"status": r2.status, "slugCount": len(s2), "slugs": sorted(s2)},
        "slugSetStable": stable,
        "reportMdSample": (r1.report_md or "")[:400],
    }

    print(json.dumps(report, ensure_ascii=False, indent=2))
    if args.json_out:
        with open(args.json_out, "w", encoding="utf-8") as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        print(f"[smoke] wrote {args.json_out}")

    if not r1.report_md or not r2.report_md:
        print("[smoke] FAIL: missing reportMd", file=sys.stderr)
        return 1
    if not stable:
        print("[smoke] WARN: slug sets differ (may be OK if KB corpus changed)", file=sys.stderr)
    print("[smoke] OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
