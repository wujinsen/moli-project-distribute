#!/usr/bin/env python3
"""Pack minimum raw png bundle for production (B/D-strategy /kb/raw/asset refs).

Reads ``raw-asset-paths.txt`` (or regenerates via stats_raw_asset_refs), copies files
from ``kb/raw/`` into ``raw-asset-bundle.tar.gz``.

Dev (Windows/Linux):
    cd moli-knowledge/kb
    python tools/stats_raw_asset_refs.py --manifest-out tools/raw-asset-paths.txt
    python tools/pack_raw_assets.py

Upload to EC2:
    tools/raw-asset-bundle.tar.gz
    tools/deploy_raw_assets.sh

On EC2:
    bash deploy_raw_assets.sh
"""
from __future__ import annotations

import argparse
import subprocess
import sys
import tarfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
KB = HERE.parent
RAW = KB / "raw"
DEFAULT_MANIFEST = HERE / "raw-asset-paths.txt"
DEFAULT_TAR = HERE / "raw-asset-bundle.tar.gz"


def load_manifest(path: Path) -> list[str]:
    lines: list[str] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        lines.append(line.replace("\\", "/"))
    return lines


def regenerate_manifest(manifest: Path) -> None:
    subprocess.run(
        [
            sys.executable,
            str(HERE / "stats_raw_asset_refs.py"),
            "--manifest-out",
            str(manifest),
        ],
        cwd=str(KB),
        check=True,
    )


def pack(manifest: Path, out_tar: Path, *, refresh: bool) -> int:
    if refresh or not manifest.is_file():
        print(f"[pack] regenerating {manifest.name} ...")
        regenerate_manifest(manifest)

    rels = load_manifest(manifest)
    if not rels:
        print("[error] manifest empty", file=sys.stderr)
        return 1

    missing: list[str] = []
    members: list[tuple[Path, str]] = []
    total = 0
    for rel in rels:
        src = RAW / rel
        if not src.is_file():
            missing.append(rel)
            continue
        total += src.stat().st_size
        members.append((src, rel))

    if missing:
        print(f"[error] {len(missing)} manifest paths missing under kb/raw/", file=sys.stderr)
        for m in missing[:10]:
            print(f"  - {m}", file=sys.stderr)
        return 1

    out_tar.parent.mkdir(parents=True, exist_ok=True)
    with tarfile.open(out_tar, "w:gz") as tf:
        for src, arcname in members:
            tf.add(src, arcname=arcname)

    print(f"[pack] wrote {out_tar}")
    print(f"       files: {len(members)}")
    print(f"       size:  {total:,} bytes ({total / 1024 / 1024:.2f} MiB)")
    print(f"       tar:   {out_tar.stat().st_size / 1024 / 1024:.2f} MiB (compressed)")
    print()
    print("Upload to EC2 (same directory):")
    print(f"  {out_tar.name}")
    print("  deploy_raw_assets.sh")
    print("Then on EC2: bash deploy_raw_assets.sh")
    return 0


def main() -> int:
    ap = argparse.ArgumentParser(description="Pack raw-asset-paths.txt into raw-asset-bundle.tar.gz")
    ap.add_argument(
        "--manifest",
        type=Path,
        default=DEFAULT_MANIFEST,
        help=f"manifest file (default: {DEFAULT_MANIFEST.name})",
    )
    ap.add_argument(
        "--out",
        type=Path,
        default=DEFAULT_TAR,
        help=f"output tar.gz (default: {DEFAULT_TAR.name})",
    )
    ap.add_argument(
        "--refresh-manifest",
        action="store_true",
        help="regenerate manifest from wiki before packing",
    )
    args = ap.parse_args()
    return pack(args.manifest, args.out, refresh=args.refresh_manifest)


if __name__ == "__main__":
    sys.exit(main())
