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

# 所有 wiki 空间的唯一清单：wiki 目录 -> kb_space.space_code。
# dry-run-all / lint-strict-all / sync-all / verify-all 全部以它为准，避免单/多空间路径漂移。
KB_SPACES=(
  "wiki:enterprise-kb"
  "wiki-ops:moli-ops-manual"
  "wiki-jp-exam:jp-fe-ap-exam"
)

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
  lint-strict-all)
    # 多空间门禁：逐个 wiki 空间跑 --strict，任一失败即整体失败（merge 前严格治理时用）
    echo "[ci] lint-strict all wiki spaces ..."
    for entry in "${KB_SPACES[@]}"; do
      wiki_dir="${entry%%:*}"
      echo "[ci] lint-strict --wiki-dir ${wiki_dir} ..."
      python "${LINT_PY}" --strict --wiki-dir "${wiki_dir}" "${@:2}"
    done
    ;;
  lint-all)
    # 多空间体检报告：三空间都跑完，始终 exit 0（PR / 渐进治理，不拦截 merge）
    echo "[ci] lint report all wiki spaces (non-blocking) ..."
    had_issue=0
    for entry in "${KB_SPACES[@]}"; do
      wiki_dir="${entry%%:*}"
      echo "[ci] lint --wiki-dir ${wiki_dir} ..."
      if ! python "${LINT_PY}" --wiki-dir "${wiki_dir}" "${@:2}"; then
        had_issue=1
      fi
    done
    if [[ "${had_issue}" -ne 0 ]]; then
      echo "[ci] lint-all: issues found (report-only; fix wiki gradually, not blocking CI)"
    else
      echo "[ci] lint-all: no blocking issues"
    fi
    exit 0
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
    for extra in "04_kb_space_jp_exam.sql" "07_kb_space_ops_manual.sql"; do
      extra_path="${REPO_ROOT}/docs/sql/${extra}"
      if [[ -f "${extra_path}" ]]; then
        echo "[ci] import optional space seed ${extra} ..."
        mysql_cli "${KB_SYNC_DB}" < "${extra_path}"
      fi
    done
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
  purge-raw-archive)
    python "${SYNC_PY}" \
      --host "${KB_SYNC_HOST}" \
      --port "${KB_SYNC_PORT}" \
      --user "${KB_SYNC_USER}" \
      --password "${KB_SYNC_PASSWORD}" \
      --db "${KB_SYNC_DB}" \
      --space "${KB_SYNC_SPACE}" \
      --purge-raw-archive
    ;;
  purge-manual-web)
    python "${SYNC_PY}" \
      --host "${KB_SYNC_HOST}" \
      --port "${KB_SYNC_PORT}" \
      --user "${KB_SYNC_USER}" \
      --password "${KB_SYNC_PASSWORD}" \
      --db "${KB_SYNC_DB}" \
      --space "${KB_SYNC_SPACE}" \
      --purge-manual-web
    ;;
  purge-manual-web-all)
    python "${SYNC_PY}" \
      --host "${KB_SYNC_HOST}" \
      --port "${KB_SYNC_PORT}" \
      --user "${KB_SYNC_USER}" \
      --password "${KB_SYNC_PASSWORD}" \
      --db "${KB_SYNC_DB}" \
      --purge-manual-web --all-spaces
    ;;
  purge-manual-web-dry-run)
    python "${SYNC_PY}" \
      --host "${KB_SYNC_HOST}" \
      --port "${KB_SYNC_PORT}" \
      --user "${KB_SYNC_USER}" \
      --password "${KB_SYNC_PASSWORD}" \
      --db "${KB_SYNC_DB}" \
      --purge-manual-web --all-spaces --dry-run
    ;;
  verify)
    COUNT="$(mysql_cli -N -e "SELECT COUNT(*) FROM \`${KB_SYNC_DB}\`.kb_document WHERE is_delete=0 AND source='kb';")"
    echo "[ci] kb_document(source=kb, active)=${COUNT}"
    if [[ "${COUNT}" -lt 1 ]]; then
      echo "[ci] verify failed: expected at least 1 synced document" >&2
      exit 1
    fi
    ;;
  verify-all)
    # 逐空间校验：每个空间至少 1 篇 active 文档，任一空间为 0 即失败，避免被总量掩盖。
    echo "[ci] verify each wiki space has synced documents ..."
    fail=0
    for entry in "${KB_SPACES[@]}"; do
      space_code="${entry##*:}"
      count="$(mysql_cli -N -e "SELECT COUNT(*) FROM \`${KB_SYNC_DB}\`.kb_document d \
        JOIN \`${KB_SYNC_DB}\`.kb_space s ON s.id = d.space_id \
        WHERE s.space_code='${space_code}' AND s.is_delete=0 AND d.is_delete=0 AND d.source='kb';")"
      echo "[ci] space=${space_code} kb_document(source=kb, active)=${count}"
      if [[ "${count}" -lt 1 ]]; then
        echo "[ci] verify-all failed: space '${space_code}' has 0 synced documents" >&2
        fail=1
      fi
    done
    if [[ "${fail}" -ne 0 ]]; then
      exit 1
    fi
    ;;
  dry-run-all)
    echo "[ci] dry-run all wiki spaces (no DB write) ..."
    for entry in "${KB_SPACES[@]}"; do
      wiki_dir="${entry%%:*}"
      space_code="${entry##*:}"
      echo "[ci] dry-run --wiki-dir ${wiki_dir} --space ${space_code} ..."
      python "${SYNC_PY}" --dry-run --wiki-dir "${wiki_dir}" --space "${space_code}"
    done
    ;;
  sync-all)
    echo "[ci] sync all wiki spaces ..."
    for entry in "${KB_SPACES[@]}"; do
      wiki_dir="${entry%%:*}"
      space_code="${entry##*:}"
      echo "[ci] sync --wiki-dir ${wiki_dir} --space ${space_code} ..."
      python "${SYNC_PY}" \
        --wiki-dir "${wiki_dir}" \
        --host "${KB_SYNC_HOST}" \
        --port "${KB_SYNC_PORT}" \
        --user "${KB_SYNC_USER}" \
        --password "${KB_SYNC_PASSWORD}" \
        --db "${KB_SYNC_DB}" \
        --space "${space_code}"
    done
    ;;
  *)
    echo "Unknown mode: ${MODE} (dry-run | dry-run-all | lint | lint-all | lint-strict | lint-strict-all | init-schema | sync | sync-all | purge-raw-archive | purge-manual-web | purge-manual-web-all | purge-manual-web-dry-run | verify | verify-all)" >&2
    exit 1
    ;;
esac
