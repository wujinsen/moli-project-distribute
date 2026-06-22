/**
 * Ramp test: step up to 10k RPS (single machine ceiling for k6).
 * For higher throughput use seckill-million.js with distributed runners.
 *
 * Usage:
 *   k6 run load-test/k6/seckill-ramp.js
 */
import http from 'k6/http';
import { check } from 'k6';
import {
  SECKILL_PATH,
  INIT_PATH,
  ACTIVITY_ID,
  randomUserId,
  seckillPayload,
  defaultThresholds,
} from './lib/config.js';

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-arrival-rate',
      startRate: 500,
      timeUnit: '1s',
      preAllocatedVUs: 2000,
      maxVUs: 10000,
      stages: [
        { duration: '2m', target: 2000 },
        { duration: '3m', target: 5000 },
        { duration: '3m', target: 10000 },
        { duration: '2m', target: 0 },
      ],
    },
  },
  thresholds: defaultThresholds,
};

export function setup() {
  http.post(`${INIT_PATH}?activityId=${ACTIVITY_ID}&stock=2000000&name=ramp-test`);
}

export default function () {
  const userId = randomUserId(__VU, __ITER);
  const res = http.post(SECKILL_PATH, seckillPayload(userId), {
    headers: { 'Content-Type': 'application/json' },
    timeout: '5s',
  });
  check(res, {
    'accepted': (r) => r.status === 200 || r.status === 409 || r.status === 429,
  });
}
