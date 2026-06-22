package com.moli.order.server.seckill;

public final class SeckillRedisKeys {

    private SeckillRedisKeys() {
    }

    public static String stockKey(String prefix, Long activityId) {
        return prefix + "stock:" + activityId;
    }

    public static String userKey(String prefix, Long activityId) {
        return prefix + "user:" + activityId;
    }

    public static String queueKey(String prefix, Long activityId) {
        return prefix + "queue:" + activityId;
    }

    public static String activityMetaKey(String prefix, Long activityId) {
        return prefix + "meta:" + activityId;
    }

    public static String metricsKey(String prefix) {
        return prefix + "metrics";
    }
}
