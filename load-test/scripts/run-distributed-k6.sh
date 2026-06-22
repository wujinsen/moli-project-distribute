#!/usr/bin/env bash
# Launch N k6 workers with execution segments for million-QPS aggregate load.
# Requires k6 in PATH. Run on separate machines or containers for best results.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
K6_SCRIPT="${ROOT_DIR}/k6/seckill-million.js"

WORKERS="${WORKERS:-10}"
TARGET_RPS="${TARGET_RPS:-10000}"
BASE_URL="${BASE_URL:-http://localhost:21000}"
DURATION="${DURATION:-5m}"
LOG_DIR="${LOG_DIR:-${ROOT_DIR}/results/$(date +%Y%m%d-%H%M%S)}"

mkdir -p "${LOG_DIR}"

perWorkerRps=$((TARGET_RPS / WORKERS))
if [ "${perWorkerRps}" -lt 1 ]; then
  perWorkerRps=1
fi

echo "Launching ${WORKERS} k6 workers, ${perWorkerRps} RPS each -> ~$((perWorkerRps * WORKERS)) aggregate RPS"
echo "Logs: ${LOG_DIR}"

for i in $(seq 0 $((WORKERS - 1))); do
  start=$(awk "BEGIN {printf \"%.6f\", ${i}/${WORKERS}}")
  end=$(awk "BEGIN {printf \"%.6f\", $((i + 1))/${WORKERS}}")
  segment="${start}:${end}:0"
  echo "Worker ${i}: segment ${segment}"
  k6 run \
    --execution-segment "${segment}" \
    --execution-segment-sequence "${i}" \
    -e BASE_URL="${BASE_URL}" \
    -e TARGET_RPS="${perWorkerRps}" \
    -e DURATION="${DURATION}" \
    -o "json=${LOG_DIR}/worker-${i}.json" \
    "${K6_SCRIPT}" &
done

wait
echo "All workers finished. Summary files in ${LOG_DIR}"
