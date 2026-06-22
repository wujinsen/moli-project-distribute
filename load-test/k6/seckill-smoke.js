/**
 * Smoke test: verify gateway -> order -> Redis seckill path.
 * Target: ~100 RPS, 1 minute.
 *
 * Usage:
 *   k6 run load-test/k6/seckill-smoke.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  SECKILL_PATH,
  PING_PATH,
  INIT_PATH,
  ACTIVITY_ID,
  randomUserId,
  seckillPayload,
  defaultThresholds,
} from './lib/config.js';

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: 20,
      duration: '1m',
    },
  },
  thresholds: defaultThresholds,
};

export function setup() {
  const init = http.post(`${INIT_PATH}?activityId=${ACTIVITY_ID}&stock=50000&name=smoke`);
  check(init, { 'init ok': (r) => r.status === 200 });
  const ping = http.get(PING_PATH);
  check(ping, { 'ping ok': (r) => r.status === 200 });
}

export default function () {
  const userId = randomUserId(__VU, __ITER);
  const res = http.post(SECKILL_PATH, seckillPayload(userId), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'seckill_order' },
  });
  check(res, {
    'status 200 or 409': (r) => r.status === 200 || r.status === 409 || r.status === 429,
  });
  sleep(0.05);
}
