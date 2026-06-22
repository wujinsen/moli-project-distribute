package com.moli.order.server.seckill;

public final class SeckillLuaScripts {

    private SeckillLuaScripts() {
    }

    /**
     * Atomic seckill: dedupe user, decrement stock, enqueue order payload.
     * Returns: OK | DUPLICATE | SOLD_OUT | NOT_FOUND
     */
    public static final String SECKILL_ORDER = ""
            + "local stockKey = KEYS[1]\n"
            + "local userKey = KEYS[2]\n"
            + "local queueKey = KEYS[3]\n"
            + "local userId = ARGV[1]\n"
            + "local orderPayload = ARGV[2]\n"
            + "if redis.call('EXISTS', stockKey) == 0 then\n"
            + "  return 'NOT_FOUND'\n"
            + "end\n"
            + "if redis.call('SISMEMBER', userKey, userId) == 1 then\n"
            + "  return 'DUPLICATE'\n"
            + "end\n"
            + "local stock = redis.call('DECR', stockKey)\n"
            + "if stock < 0 then\n"
            + "  redis.call('INCR', stockKey)\n"
            + "  return 'SOLD_OUT'\n"
            + "end\n"
            + "redis.call('SADD', userKey, userId)\n"
            + "redis.call('LPUSH', queueKey, orderPayload)\n"
            + "redis.call('HINCRBY', ARGV[3], 'success', 1)\n"
            + "return 'OK'";
}
