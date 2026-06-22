/**
 * Gateway baseline: ping only (no Redis Lua). Use to isolate gateway capacity.
 */
import http from 'k6/http';
import { check } from 'k6';
import { PING_PATH } from './lib/config.js';

const targetRps = Number(__ENV.TARGET_RPS || 50000);

export const options = {
  scenarios: {
    ping: {
      executor: 'constant-arrival-rate',
      rate: targetRps,
      timeUnit: '1s',
      duration: __ENV.DURATION || '2m',
      preAllocatedVUs: Math.min(targetRps, 30000),
      maxVUs: Math.min(targetRps * 2, 100000),
    },
  },
  discardResponseBodies: true,
};

export default function () {
  const res = http.get(PING_PATH, { timeout: '2s' });
  check(res, { 'pong': (r) => r.status === 200 });
}
