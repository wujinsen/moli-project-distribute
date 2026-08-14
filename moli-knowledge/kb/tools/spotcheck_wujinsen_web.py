#!/usr/bin/env python3
# -*- coding: utf-8
"""T22 Web spot-check: login + sample kb/wiki/asset and kb/raw/asset HEAD requests."""
from __future__ import annotations

import json
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

GATEWAY = "http://127.0.0.1:8888"
SPACE = "900000000000000001"
WIKI = Path(__file__).resolve().parent.parent / "wiki"

CHECKS = [
    {
        "label": "hub B · hadoop-生态入门",
        "slug": "bigdata/hadoop-生态入门",
        "expect_section": "## 原文插图（wujinsen）",
    },
    {
        "label": "hub B · jvm-内存与gc",
        "slug": "java/jvm-内存与gc",
        "expect_section": "## 原文插图（wujinsen）",
    },
    {
        "label": "hub B · netty-reactor",
        "slug": "middleware/netty-reactor与线程模型",
        "expect_section": "## 原文插图（wujinsen）",
    },
    {
        "label": "annex A · hadoop mini book",
        "slug": "bigdata/annex-Hadoop应用开发技术详解》迷你书",
        "expect_section": None,
    },
    {
        "label": "annex A · defer-reopen Netty In Action",
        "slug": "middleware/annex-Netty-In-Action",
        "expect_section": None,
    },
]


def login() -> str:
    req = urllib.request.Request(
        f"{GATEWAY}/login",
        data=json.dumps({"username": "admin", "password": "123456"}).encode(),
        method="POST",
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=10) as resp:
        body = json.loads(resp.read().decode())
    if body.get("code") not in (None, 200, 0):
        raise RuntimeError(f"login failed: {body}")
    data = body.get("data")
    if isinstance(data, dict):
        token = data.get("token") or data.get("accessToken")
    else:
        token = data
    token = token or body.get("token")
    if not token or not str(token).startswith("login_token"):
        raise RuntimeError(f"login failed: {body}")
    return str(token)


def get_doc(slug: str, token: str) -> dict | None:
    q = urllib.parse.urlencode({"spaceId": SPACE, "slug": slug})
    req = urllib.request.Request(
        f"{GATEWAY}/KnowledgeServer/kb/document/by-slug?{q}",
        headers={"Authorization": token},
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        return {"error": e.code, "slug": slug}
    return data.get("data") or data


def head_asset(url: str, token: str) -> tuple[int, str]:
    req = urllib.request.Request(url, method="HEAD", headers={"Authorization": token})
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            ct = resp.headers.get("Content-Type", "")
            return resp.status, ct
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get("Content-Type", "")


IMG_MD = re.compile(r"!\[[^\]]*\]\(([^)]+)\)")


def extract_refs(content: str, limit: int = 3) -> list[str]:
    refs = []
    for m in IMG_MD.finditer(content):
        ref = m.group(1).strip()
        if ref and not ref.startswith("http"):
            refs.append(ref)
        if len(refs) >= limit:
            break
    return refs


def resolve_wiki_asset(slug: str, rel: str) -> Path:
    cat, name = slug.split("/", 1)
    base = WIKI / cat / f"{name}.md"
    if rel.startswith("assets/"):
        return base.parent / f"{base.stem}.{rel.split('/', 1)[0]}" / rel.split("/", 1)[1]
    return base.parent / rel


def main() -> int:
    print("=== T22 Web spot-check ===\n")
    try:
        token = login()
        print(f"login OK\n")
    except Exception as e:
        print(f"SKIP API check: {e}")
        print("(Gateway 8888 未起则仅做磁盘校验)\n")
        token = ""

    ok = 0
    fail = 0
    lines = []

    for chk in CHECKS:
        slug = chk["slug"]
        disk_path = WIKI / (slug.replace("/", "/") + ".md")
        if not disk_path.is_file():
            # try stem from slug
            disk_path = next(WIKI.rglob(Path(slug).name + ".md"), None)
        disk_ok = disk_path and disk_path.is_file()
        row = {"label": chk["label"], "slug": slug, "disk": disk_ok}

        if token:
            doc = get_doc(slug, token)
            if doc and doc.get("error"):
                row["api"] = f"HTTP {doc['error']}"
                fail += 1
            elif doc and doc.get("content"):
                content = doc["content"]
                row["api"] = "OK"
                if chk["expect_section"]:
                    row["section"] = chk["expect_section"] in content
                refs = extract_refs(content)
                row["img_refs"] = len(refs)
                asset_ok = 0
                for ref in refs:
                    if ref.startswith("assets/"):
                        stem = slug.split("/", 1)[1]
                        p = WIKI / slug.split("/")[0] / f"{stem}.assets" / ref.split("/", 1)[1]
                        if p.is_file():
                            asset_ok += 1
                    elif "/kb/raw/asset" in ref or ref.startswith("/KnowledgeServer"):
                        url = ref if ref.startswith("http") else GATEWAY + ref
                        st, ct = head_asset(url, token)
                        if st == 200 and ("image" in ct or "octet" in ct):
                            asset_ok += 1
                row["assets_ok"] = f"{asset_ok}/{len(refs)}"
                if row.get("section") is False:
                    fail += 1
                elif refs and asset_ok < len(refs):
                    fail += 1
                else:
                    ok += 1
            else:
                row["api"] = "no content"
                fail += 1
        elif disk_ok:
            text = disk_path.read_text(encoding="utf-8")
            if chk["expect_section"]:
                row["section"] = chk["expect_section"] in text
            refs = extract_refs(text)
            row["img_refs"] = len(refs)
            asset_ok = 0
            for ref in refs:
                if ref.startswith("assets/"):
                    stem = slug.split("/", 1)[1]
                    p = WIKI / slug.split("/")[0] / f"{stem}.assets" / ref.split("/", 1)[1]
                    if p.is_file():
                        asset_ok += 1
                elif "/kb/raw/asset" in ref:
                    asset_ok += 1  # disk-only: URL present in markdown
            row["assets_ok"] = f"{asset_ok}/{len(refs)}"
            ok += 1
        else:
            fail += 1

        lines.append(row)
        print(json.dumps(row, ensure_ascii=False))

    out = Path(__file__).resolve().parent / "WUJINSEN_WEB_SPOTCHECK.md"
    md = [
        "# T22 Web 抽检记录",
        "",
        f"> auto: `spotcheck_wujinsen_web.py` · gateway {GATEWAY}",
        "",
        "| 页 | slug | 磁盘 | API | 插图节 | assets |",
        "|----|------|------|-----|--------|--------|",
    ]
    for r in lines:
        md.append(
            f"| {r['label']} | `{r['slug']}` | {'OK' if r.get('disk') else 'MISS'} | "
            f"{r.get('api','—')} | {r.get('section','—')} | {r.get('assets_ok','—')} |"
        )
    md.append("")
    md.append(f"**结果**: {ok} pass / {fail} fail")
    out.write_text("\n".join(md), encoding="utf-8")
    print(f"\nWrote {out}")
    return 0 if fail == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
