#!/usr/bin/env bash
# 生产 EC2：解压 raw-asset-bundle.tar.gz 到 kb/raw/
#
# 手工上传（与脚本同目录即可）：
#   raw-asset-bundle.tar.gz   (~12 MiB，212 张 png)
#   deploy_raw_assets.sh
#
# 执行：
#   bash deploy_raw_assets.sh
#
# 若报 set: pipefail / $'\r'：Windows 上传为 CRLF，先执行
#   sed -i 's/\r$//' deploy_raw_assets.sh
#
set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TAR="${1:-${SCRIPT_DIR}/raw-asset-bundle.tar.gz}"
KB_RAW="${KB_RAW_ROOT:-${MOLI_KB_RAW:-/opt/moli-project-distribute/moli-knowledge/kb/raw}}"

if [ ! -f "$TAR" ]; then
  echo "[error] bundle not found: $TAR" >&2
  echo "        upload raw-asset-bundle.tar.gz next to this script, or pass path as arg1" >&2
  exit 1
fi

mkdir -p "$KB_RAW"
if [ ! -d "$KB_RAW" ]; then
  echo "[error] cannot create raw root: $KB_RAW" >&2
  exit 1
fi

echo "[deploy] tar:    $TAR"
echo "[deploy] target: $KB_RAW"
tar -xzf "$TAR" -C "$KB_RAW"

COUNT="$(tar -tzf "$TAR" | wc -l | tr -d ' ')"
echo "[deploy] extracted $COUNT files under $KB_RAW"
echo "[deploy] sample verify:"
SAMPLE="$(tar -tzf "$TAR" | head -n 1)"
if [ -n "$SAMPLE" ] && [ -f "$KB_RAW/$SAMPLE" ]; then
  ls -la "$KB_RAW/$SAMPLE"
else
  echo "  (no sample)"
fi
echo "[deploy] done — refresh Web; no wiki re-sync needed."
