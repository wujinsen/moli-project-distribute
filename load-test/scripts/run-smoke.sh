#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
k6 run "${SCRIPT_DIR}/../k6/seckill-smoke.js" "$@"
