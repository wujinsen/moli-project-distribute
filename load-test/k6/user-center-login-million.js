/**
 * Distributed user-center login for million-QPS class tests.
 *
 * Usage:
 *   k6 run -e TARGET_RPS=5000 load-test/k6/user-center-login-million.js
 *   ./load-test/scripts/run-distributed-k6.ps1 -Script user-center-login-million.js -Workers 100 -TargetRps 1000000
 */
import http from 'k6/http';
import { check } from 'k6';
import {
  LOGIN_PATH,
  loginPayload,
  pickLoginUser,
  parseToken,
  millionThresholds,
} from './lib/config.js';

const targetRps = Number(__ENV.TARGET_RPS || 5000);
const duration = __ENV.DURATION || '5m';

export const options = {
  scenarios: {
    login_spike: {
      executor: 'constant-arrival-rate',
      rate: targetRps,
      timeUnit: '1s',
      duration: duration,
      preAllocatedVUs: Math.min(targetRps * 2, 30000),
      maxVUs: Math.min(targetRps * 5, 100000),
    },
  },
  thresholds: millionThresholds,
  discardResponseBodies: true,
};

export default function () {
  const res = http.post(LOGIN_PATH, loginPayload(pickLoginUser(__VU)), {
    headers: { 'Content-Type': 'application/json' },
    timeout: '3s',
    tags: { name: 'user_center_login_million' },
  });
  check(res, {
    'login hot path': (r) => r.status === 200 && parseToken(r) !== null,
  });
}
