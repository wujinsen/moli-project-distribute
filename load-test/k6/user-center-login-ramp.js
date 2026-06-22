/**
 * User-center login ramp.
 *
 * 默认 LOCAL_RAMP=true：单机开发机友好（最高约 300 RPS）。
 * 全量压测：-e STRESS_RAMP=true（最高 5000 RPS，需多实例/集群）。
 *
 * Usage:
 *   k6 run -e LOGIN_PASSWORD=123456 load-test/k6/user-center-login-ramp.js
 *   k6 run -e STRESS_RAMP=true -e LOGIN_PASSWORD=123456 load-test/k6/user-center-login-ramp.js
 */
import http from 'k6/http';
import { check } from 'k6';
import {
  LOGIN_PATH,
  loginPayload,
  pickLoginUser,
  isLoginSuccess,
  defaultThresholds,
} from './lib/config.js';

const stressRamp = __ENV.STRESS_RAMP === 'true';
const localRamp = __ENV.LOCAL_RAMP !== 'false' && !stressRamp;

const localStages = [
  { duration: '30s', target: 20 },
  { duration: '1m', target: 100 },
  { duration: '2m', target: 300 },
  { duration: '1m', target: 0 },
];

const stressStages = [
  { duration: '2m', target: 500 },
  { duration: '3m', target: 2000 },
  { duration: '3m', target: 5000 },
  { duration: '2m', target: 0 },
];

export const options = {
  scenarios: {
    login_ramp: {
      executor: 'ramping-arrival-rate',
      startRate: localRamp ? 10 : 100,
      timeUnit: '1s',
      preAllocatedVUs: localRamp ? 200 : 1000,
      maxVUs: localRamp ? 500 : 5000,
      stages: localRamp ? localStages : stressStages,
    },
  },
  thresholds: localRamp
    ? {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<2000', 'p(99)<5000'],
        'checks{check:login ok}': ['rate>0.95'],
      }
    : defaultThresholds,
};

export default function () {
  const loginRes = http.post(LOGIN_PATH, loginPayload(pickLoginUser(__VU)), {
    headers: { 'Content-Type': 'application/json' },
    timeout: localRamp ? '10s' : '5s',
    tags: { name: 'user_center_login_ramp' },
  });
  check(loginRes, {
    'login ok': (r) => isLoginSuccess(r),
    'login http 200': (r) => r.status === 200,
  });
}
