/**
 * Million-QPS seckill spike (distributed execution required).
 *
 * Single machine cannot generate 1M RPS. Run N workers with execution segments:
 *
 *   # Worker 0 of 100 (each worker targets 10k RPS -> 1M aggregate)
 *   k6 run --execution-segment "0:1/100:0" --execution-segment-sequence "0" \
 *     -e TARGET_RPS=10000 load-test/k6/seckill-million.js
 *
 *   # Worker 1
 *   k6 run --execution-segment "1/100:2/100:0" --execution-segment-sequence "1" \
 *     -e TARGET_RPS=10000 load-test/k6/seckill-million.js
 *
 * Or use load-test/scripts/run-distributed-k6.sh / run-distributed-k6.ps1
 *
 * Usage (local ceiling check, 5k RPS):
 *   k6 run -e TARGET_RPS=5000 load-test/k6/seckill-million.js
 */
import http from 'k6/http';
import { check } from 'k6';
import {
  SECKILL_PATH,
  INIT_PATH,
  METRICS_PATH,
  ACTIVITY_ID,
  randomUserId,
  seckillPayload,
  millionThresholds,
} from './lib/config.js';

const targetRps = Number(__ENV.TARGET_RPS || 10000);
const duration = __ENV.DURATION || '5m';

export const options = {
  scenarios: {
    seckill_spike: {
      executor: 'constant-arrival-rate',
      rate: targetRps,
      timeUnit: '1s',
      duration: duration,
      preAllocatedVUs: Math.min(targetRps * 2, 50000),
      maxVUs: Math.min(targetRps * 5, 200000),
    },
  },
  thresholds: millionThresholds,
  discardResponseBodies: true,
  noConnectionReuse: false,
};

export function setup() {
  http.post(`${INIT_PATH}?activityId=${ACTIVITY_ID}&stock=5000000&name=million-qps`);
}

export default function () {
  const userId = randomUserId(__VU, __ITER);
  const res = http.post(SECKILL_PATH, seckillPayload(userId), {
    headers: { 'Content-Type': 'application/json' },
    timeout: '3s',
    tags: { name: 'seckill_million' },
  });
  check(res, {
    'hot path': (r) => r.status === 200 || r.status === 409 || r.status === 429,
  });
}

export function teardown() {
  const metrics = http.get(METRICS_PATH);
  if (metrics.status === 200) {
    console.log('server metrics:', metrics.body);
  }
}
