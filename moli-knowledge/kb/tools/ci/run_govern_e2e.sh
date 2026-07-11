#!/usr/bin/env bash
# KB 治理 + Sync 联调 smoke（需已登录 token）。
# 用法：
#   export KB_E2E_BASE_URL=http://127.0.0.1:8080/KnowledgeServer
#   export KB_E2E_TOKEN=<Bearer token>
#   export KB_E2E_SPACE_ID=900000000000000002   # moli-ops-manual
#   bash moli-knowledge/kb/tools/ci/run_govern_e2e.sh
#
# 流程：lint-space → auto-fix（含 syncAfter）→ 轮询 sync/status
set -euo pipefail

BASE="${KB_E2E_BASE_URL:?set KB_E2E_BASE_URL}"
TOKEN="${KB_E2E_TOKEN:?set KB_E2E_TOKEN}"
SPACE_ID="${KB_E2E_SPACE_ID:?set KB_E2E_SPACE_ID}"
AUTH=(-H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json")

echo "[kb:e2e] lint-space spaceId=${SPACE_ID}"
LINT_JSON="$(curl -sf "${AUTH[@]}" -X POST "${BASE}/kb/wiki-moli/lint-space" \
  -d "{\"spaceId\":${SPACE_ID},\"strict\":false}")"
ISSUE_COUNT="$(echo "${LINT_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',{}).get('issues') or []))" 2>/dev/null || echo 0)"
echo "[kb:e2e] issues=${ISSUE_COUNT}"

if [[ "${ISSUE_COUNT}" == "0" ]]; then
  echo "[kb:e2e] no script-fixable issues; skip auto-fix (expected when wiki clean)"
else
  echo "[kb:e2e] auto-fix with syncAfter=true"
  curl -sf "${AUTH[@]}" -X POST "${BASE}/kb/wiki-moli/govern/auto-fix" \
    -d "{\"spaceId\":${SPACE_ID},\"issues\":[],\"scriptFix\":true,\"aiFix\":false,\"relintAfter\":true,\"syncAfter\":true}" \
    | python3 -m json.tool
fi

echo "[kb:e2e] poll sync status (max 60s)"
for _ in $(seq 1 30); do
  STATUS_JSON="$(curl -sf "${AUTH[@]}" "${BASE}/kb/sync/status?spaceId=${SPACE_ID}")"
  RUNNING="$(echo "${STATUS_JSON}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('running',False))")"
  LAST="$(echo "${STATUS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}); print(d.get('lastStatus',''))")"
  echo "[kb:e2e] running=${RUNNING} lastStatus=${LAST}"
  if [[ "${RUNNING}" == "False" || "${RUNNING}" == "false" ]]; then
    break
  fi
  sleep 2
done

echo "[kb:e2e] done"
