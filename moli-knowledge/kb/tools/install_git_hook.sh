#!/usr/bin/env bash
# Git post-commit hook：wiki 有变更时自动跑 kb → MySQL 同步。
# 安装：bash moli-knowledge/kb/tools/install_git_hook.sh
# 或 Windows：powershell -ExecutionPolicy Bypass -File moli-knowledge/kb/tools/install_git_hook.ps1
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
HOOK="${ROOT}/.git/hooks/post-commit"
cp "${ROOT}/moli-knowledge/kb/tools/post-commit-kb-sync.sh" "${HOOK}"
chmod +x "${HOOK}"
echo "Installed: ${HOOK}"
