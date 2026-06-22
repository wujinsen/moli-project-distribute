#!/usr/bin/env bash
# Git post-commit hook template (copied by install_git_hook.sh / install_git_hook.ps1)
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
WIKI_PREFIX="moli-knowledge/kb/wiki/"

if ! git diff-tree --no-commit-id --name-only -r HEAD | grep -q "^${WIKI_PREFIX}"; then
  exit 0
fi

echo "[kb-sync-hook] wiki changed, running sync_to_db.py ..."
python "${ROOT}/moli-knowledge/kb/tools/sync_to_db.py" \
  --host 127.0.0.1 --port 3306 --user root --password 12345678 \
  --db moli --space enterprise-kb
