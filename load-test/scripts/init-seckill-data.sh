#!/usr/bin/env bash
# Initialize seckill activity before load test
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:21000}"
ACTIVITY_ID="${ACTIVITY_ID:-1}"
STOCK="${STOCK:-1000000}"

echo "Init activity ${ACTIVITY_ID} stock=${STOCK} at ${BASE_URL}"
curl -sf -X POST "${BASE_URL}/OrderServer/seckill/admin/init?activityId=${ACTIVITY_ID}&stock=${STOCK}&name=million-qps"
echo
curl -sf "${BASE_URL}/OrderServer/seckill/activity/${ACTIVITY_ID}"
echo
