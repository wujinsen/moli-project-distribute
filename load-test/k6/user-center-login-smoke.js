/**
 * User-center login smoke test (via gateway).
 *
 * Usage:
 *   k6 run load-test/k6/user-center-login-smoke.js
 *   k6 run -e LOGIN_PASSWORD=123456 load-test/k6/user-center-login-smoke.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  LOGIN_PATH,
  USER_LIST_PATH,
  loginPayload,
  pickLoginUser,
  parseToken,
  authHeaders,
  defaultThresholds,
} from './lib/config.js';

export const options = {
  scenarios: {
    login_smoke: {
      executor: 'constant-vus',
      vus: 20,
      duration: '1m',
    },
  },
  thresholds: defaultThresholds,
};

export default function () {
  const userName = pickLoginUser(__VU);
  const loginRes = http.post(LOGIN_PATH, loginPayload(userName), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'user_center_login' },
  });
  check(loginRes, {
    'login 200': (r) => r.status === 200 && r.json('code') === 200,
  });

  const token = parseToken(loginRes);
  if (token) {
    const listRes = http.get(`${USER_LIST_PATH}?pageNum=1&pageSize=10`, {
      headers: authHeaders(token),
      tags: { name: 'user_center_user_list' },
    });
    check(listRes, { 'user list 200': (r) => r.status === 200 });
  }
  sleep(0.1);
}
