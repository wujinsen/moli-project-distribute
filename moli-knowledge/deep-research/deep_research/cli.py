from __future__ import annotations

import argparse
import json
import sys
import uuid

from .config import KB_AUTH_TOKEN, KB_BASE_URL
from .models import ResearchOptions, ResearchSidecarRequest
from .orchestrator import run_research


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description="DeepResearch CLI smoke (Phase A)")
    ap.add_argument("--topic", required=True, help="调研主题")
    ap.add_argument("--space-id", type=int, default=None)
    ap.add_argument("--space-ids", type=str, default="", help="逗号分隔")
    ap.add_argument("--auth-token", default=KB_AUTH_TOKEN)
    ap.add_argument("--kb-base", default=KB_BASE_URL, help="仅展示；Retriever 用 KB_BASE_URL 环境变量")
    ap.add_argument("--max-sections", type=int, default=6)
    ap.add_argument("--strategy", default="hybrid")
    ap.add_argument("--agentic", action="store_true")
    args = ap.parse_args(argv)

    space_ids = [int(x) for x in args.space_ids.split(",") if x.strip()] or None
    req = ResearchSidecarRequest(
        runId=str(uuid.uuid4()),
        topic=args.topic,
        spaceId=args.space_id,
        spaceIds=space_ids,
        authToken=args.auth_token or None,
        options=ResearchOptions(
            maxSections=args.max_sections,
            retrievalStrategy=args.strategy,
            agentic=args.agentic,
        ),
    )
    print(f"kb_base={args.kb_base}")
    result = run_research(req)
    for evt in result.progress:
        print(f"[{evt.phase}] {evt.pct}% {evt.message}", file=sys.stderr)

    print(json.dumps(result.model_dump(by_alias=True), ensure_ascii=False, indent=2))
    return 0 if result.status in ("SUCCEEDED", "DEGRADED") else 1


if __name__ == "__main__":
    raise SystemExit(main())
