/**
 * Mixed scenario: login -> authenticated APIs -> seckill (realistic flash-sale flow).
 *
 * Usage:
 *   k6 run load-test/k6/mixed-login-seckill.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  LOGIN_PATH,
  USER_LIST_PATH,
  MENU_ROUTERS_PATH,
  SECKILL_PATH,
  INIT_PATH,
  ACTIVITY_ID,
  loginPayload,
  pickLoginUser,
  parseToken,
  authHeaders,
  randomUserId,
  seckillPayload,
  defaultThresholds,
} from './lib/config.js';

export const options = {
  scenarios: {
    mixed: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 100 },
        { duration: '3m', target: 500 },
        { duration: '2m', target: 0 },
      ],
    },
  },
  thresholds: defaultThresholds,
};

export function setup() {
  http.post(`${INIT_PATH}?activityId=${ACTIVITY_ID}&stock=500000&name=mixed-flow`);
}

export default function () {
  const roll = Math.random();

  if (roll < 0.15) {
    const loginRes = http.post(LOGIN_PATH, loginPayload(pickLoginUser(__VU)), {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'mixed_login' },
    });
    check(loginRes, { login: (r) => r.status === 200 && parseToken(r) !== null });
    sleep(0.05);
    return;
  }

  const loginRes = http.post(LOGIN_PATH, loginPayload(pickLoginUser(__VU)), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'mixed_login' },
  });
  const token = parseToken(loginRes);
  if (!token) {
    return;
  }

  if (roll < 0.55) {
    const listRes = http.get(`${USER_LIST_PATH}?pageNum=1&pageSize=10`, {
      headers: authHeaders(token),
      tags: { name: 'mixed_user_list' },
    });
    check(listRes, { list: (r) => r.status === 200 });
  } else if (roll < 0.75) {
    const menuRes = http.get(MENU_ROUTERS_PATH, {
      headers: authHeaders(token),
      tags: { name: 'mixed_menu' },
    });
    check(menuRes, { menu: (r) => r.status === 200 });
  } else {
    const userId = randomUserId(__VU, __ITER);
    const seckillRes = http.post(SECKILL_PATH, seckillPayload(userId), {
      headers: authHeaders(token),
      tags: { name: 'mixed_seckill' },
    });
    check(seckillRes, {
      seckill: (r) => r.status === 200 || r.status === 409 || r.status === 429,
    });
  }
  sleep(0.05);
}
