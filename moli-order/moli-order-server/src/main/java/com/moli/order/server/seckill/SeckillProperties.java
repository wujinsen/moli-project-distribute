package com.moli.order.server.seckill;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "moli.seckill")
public class SeckillProperties {

    /**
     * Enables admin init endpoint and skips strict validation for load testing.
     */
    private boolean loadTestMode = false;

    /**
     * Persist successful orders asynchronously instead of blocking the hot path.
     */
    private boolean asyncDb = true;

    /**
     * Default stock when initializing an activity via admin API.
     */
    private long defaultStock = 100_000L;

    /**
     * Redis key prefix for seckill data.
     */
    private String redisKeyPrefix = "seckill:";

    /**
     * Batch size when draining the order queue to MySQL.
     */
    private int dbBatchSize = 500;

    /**
     * Poll interval in milliseconds for the async order consumer.
     */
    private long consumerPollMs = 50L;
}
