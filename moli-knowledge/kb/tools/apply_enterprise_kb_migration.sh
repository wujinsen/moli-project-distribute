#!/usr/bin/env bash
# enterprise-kb 目录迁移 · git mv 批量执行（包装 apply_enterprise_kb_migration.py）
#
# Usage:
#   ./apply_enterprise_kb_migration.sh              # dry-run
#   ./apply_enterprise_kb_migration.sh --execute    # 真正移动
#   ./apply_enterprise_kb_migration.sh --execute --only database,cache
#
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec python3 "$SCRIPT_DIR/apply_enterprise_kb_migration.py" "$@"
