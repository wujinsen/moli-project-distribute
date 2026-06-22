/**
 * Shared k6 configuration for Moli load tests.
 */
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:21000';
export const ACTIVITY_ID = Number(__ENV.ACTIVITY_ID || 1);

// user-center: 默认直连 8888（VIA_GATEWAY=true 时走 gateway）
export const VIA_GATEWAY = __ENV.VIA_GATEWAY === 'true';
export const UC_BASE_URL = __ENV.UC_BASE_URL || 'http://localhost:8888';
export const UC_PREFIX = VIA_GATEWAY ? `${BASE_URL}/UserCenter` : UC_BASE_URL;

// Order / seckill（始终经 gateway，除非单独改 BASE_URL）
export const SECKILL_PATH = `${BASE_URL}/OrderServer/seckill/order`;
export const PING_PATH = `${BASE_URL}/OrderServer/seckill/ping`;
export const INIT_PATH = `${BASE_URL}/OrderServer/seckill/admin/init`;
export const METRICS_PATH = `${BASE_URL}/OrderServer/seckill/metrics`;

// User-center
export const LOGIN_PATH = `${UC_PREFIX}/loadtest/login`;
export const USER_LIST_PATH = `${UC_PREFIX}/user/list`;
export const MENU_ROUTERS_PATH = `${UC_PREFIX}/menu/getRouters`;
export const UC_HEALTH_PATH = `${UC_PREFIX}/actuator/health`;

export const LOGIN_USER = __ENV.LOGIN_USER || 'zhangsan';
export const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || '123456';

// 默认单用户；多用户压测请显式 -e LOGIN_USER_POOL=zhangsan,lisi,...
const LOGIN_USER_POOL = (__ENV.LOGIN_USER_POOL || 'zhangsan')
  .split(',')
  .map((s) => s.trim())
  .filter(Boolean);

export function pickLoginUser(vu) {
  if (LOGIN_USER_POOL.length === 0) {
    return LOGIN_USER;
  }
  return LOGIN_USER_POOL[(vu - 1) % LOGIN_USER_POOL.length];
}

export function loginPayload(userName) {
  return JSON.stringify({
    userName: userName || pickLoginUser(__VU),
    password: LOGIN_PASSWORD,
  });
}

export function authHeaders(token) {
  return {
    'Content-Type': 'application/json',
    Authorization: token,
  };
}

export function parseLoginBody(res) {
  try {
    return res.json();
  } catch (e) {
    return null;
  }
}

export function isLoginSuccess(res) {
  if (res.status !== 200) {
    return false;
  }
  const body = parseLoginBody(res);
  return !!(body && body.code === 200 && body.data && body.data.token);
}

export function parseToken(res) {
  const body = parseLoginBody(res);
  if (!body || body.code !== 200 || !body.data || !body.data.token) {
    return null;
  }
  return body.data.token;
}

export function randomUserId(vu, iter) {
  const segment = Number(__ENV.K6_EXECUTION_SEGMENT || '0:1');
  const segmentIndex = Math.floor(segment * 1000000);
  return `u-${segmentIndex}-${vu}-${iter}-${Date.now()}`;
}

export function seckillPayload(userId) {
  return JSON.stringify({
    activityId: ACTIVITY_ID,
    userId: userId,
    requestId: `${userId}-${__ITER}`,
  });
}

export const defaultThresholds = {
  http_req_failed: ['rate<0.05'],
  http_req_duration: ['p(95)<800', 'p(99)<2000'],
};

export const millionThresholds = {
  http_req_failed: ['rate<0.01'],
  http_req_duration: ['p(95)<500', 'p(99)<1500'],
};
