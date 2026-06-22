#!/usr/bin/env bash
# CI / 本地统一入口：dry-run、初始化 schema、写库同步。
# 用法：
#   bash moli-knowledge/kb/tools/ci/run_sync.sh dry-run
#   bash moli-knowledge/kb/tools/ci/run_sync.sh init-schema
#   bash moli-knowledge/kb/tools/ci/run_sync.sh sync
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
TOOLS="$(cd "${HERE}/.." && pwd)"
REPO_ROOT="$(cd "${TOOLS}/../../.." && pwd)"
SCHEMA="${REPO_ROOT}/docs/sql/03_knowledge_schema.sql"
SYNC_PY="${TOOLS}/sync_to_db.py"
LINT_PY="${TOOLS}/lint.py"

MODE="${1:-dry-run}"

KB_SYNC_HOST="${KB_SYNC_HOST:-127.0.0.1}"
KB_SYNC_PORT="${KB_SYNC_PORT:-3306}"
KB_SYNC_USER="${KB_SYNC_USER:-root}"
KB_SYNC_PASSWORD="${KB_SYNC_PASSWORD:-12345678}"
KB_SYNC_DB="${KB_SYNC_DB:-moli}"
KB_SYNC_SPACE="${KB_SYNC_SPACE:-enterprise-kb}"

mysql_cli() {
  mysql -h"${KB_SYNC_HOST}" -P"${KB_SYNC_PORT}" -u"${KB_SYNC_USER}" "-p${KB_SYNC_PASSWORD}" "$@"
}

case "${MODE}" in
  dry-run)
    python "${SYNC_PY}" --dry-run
    ;;
  lint)
    # 知识治理体检（report-only：不因 ERROR/WARN 失败，便于在 dry-run 里观察）
    python "${LINT_PY}" "${@:2}"
    ;;
  lint-strict)
    # 门禁模式：有 ERROR 即失败；加 --strict 时 WARN 也失败
    python "${LINT_PY}" --strict "${@:2}"
    ;;
  init-schema)
    if [[ ! -f "${SCHEMA}" ]]; then
      echo "Schema not found: ${SCHEMA}" >&2
      exit 1
    fi
    echo "[ci] create database ${KB_SYNC_DB} if needed ..."
    mysql_cli -e "CREATE DATABASE IF NOT EXISTS \`${KB_SYNC_DB}\` DEFAULT CHARSET utf8mb4;"
    echo "[ci] import kb schema (skip sys_* seed if tables absent) ..."
    # 03 脚本末尾含 sys_system 增量 INSERT，CI 最小库无 user-center 表，截断到 sys 段之前
    awk '/INSERT INTO `sys_system`/ {exit} {print}' "${SCHEMA}" | mysql_cli "${KB_SYNC_DB}"
    echo "[ci] schema ready."
    ;;
  sync)
    python "${SYNC_PY}" \
      --host "${KB_SYNC_HOST}" \
      --port "${KB_SYNC_PORT}" \
      --user "${KB_SYNC_USER}" \
      --password "${KB_SYNC_PASSWORD}" \
      --db "${KB_SYNC_DB}" \
      --space "${KB_SYNC_SPACE}"
    ;;
  archive-raw)
    python "${TOOLS}/ingest_raw_archive.py" \
      --host "${KB_SYNC_HOST}" \
      --port "${KB_SYNC_PORT}" \
      --user "${KB_SYNC_USER}" \
      --password "${KB_SYNC_PASSWORD}" \
      --db "${KB_SYNC_DB}" \
      --space "${KB_SYNC_SPACE}"
    ;;
  archive-raw-dry)
    python "${TOOLS}/ingest_raw_archive.py" --dry-run
    ;;
  l2-build)
    python "${TOOLS}/ingest_parallel.py" --build-clusters
    ;;
  l2-run)
    python "${TOOLS}/ingest_parallel.py" --workers "${KB_L2_WORKERS:-10}" "$@"
    ;;
  verify)
    COUNT="$(mysql_cli -N -e "SELECT COUNT(*) FROM \`${KB_SYNC_DB}\`.kb_document WHERE is_delete=0 AND source='kb';")"
    echo "[ci] kb_document(source=kb, active)=${COUNT}"
    if [[ "${COUNT}" -lt 1 ]]; then
      echo "[ci] verify failed: expected at least 1 synced document" >&2
      exit 1
    fi
    ;;
  *)
    echo "Unknown mode: ${MODE} (dry-run | lint | lint-strict | init-schema | sync | archive-raw | archive-raw-dry | l2-build | l2-run | verify)" >&2
    exit 1
    ;;
esac
