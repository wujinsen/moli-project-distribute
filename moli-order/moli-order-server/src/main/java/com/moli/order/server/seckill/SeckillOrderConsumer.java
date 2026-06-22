package com.moli.order.server.seckill;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moli.order.server.seckill.entity.SeckillOrder;
import com.moli.order.server.seckill.mapper.SeckillOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "moli.seckill", name = "async-db", havingValue = "true", matchIfMissing = true)
public class SeckillOrderConsumer {

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillProperties seckillProperties;
    private final SeckillOrderMapper seckillOrderMapper;

    @Scheduled(fixedDelayString = "${moli.seckill.consumer-poll-ms:50}")
    public void drainQueues() {
        // Scan known queue keys by activity meta is expensive; drain default activity 1 in load test.
        // Production should use MQ or shard-specific consumers.
        drainActivityQueue(1L);
    }

    public void drainActivityQueue(Long activityId) {
        String queueKey = SeckillRedisKeys.queueKey(seckillProperties.getRedisKeyPrefix(), activityId);
        int batchSize = seckillProperties.getDbBatchSize();
        List<SeckillOrder> batch = new ArrayList<>(batchSize);

        for (int i = 0; i < batchSize; i++) {
            String payload = stringRedisTemplate.opsForList().rightPop(queueKey);
            if (payload == null) {
                break;
            }
            SeckillOrder order = toEntity(payload);
            if (order != null) {
                batch.add(order);
            }
        }

        if (batch.isEmpty()) {
            return;
        }

        for (SeckillOrder order : batch) {
            try {
                seckillOrderMapper.insert(order);
            } catch (Exception ex) {
                log.warn("Failed to persist seckill order {}, requeue skipped: {}", order.getId(), ex.getMessage());
            }
        }

        stringRedisTemplate.opsForHash().increment(
                SeckillRedisKeys.metricsKey(seckillProperties.getRedisKeyPrefix()),
                "persisted",
                batch.size()
        );
    }

    private SeckillOrder toEntity(String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            SeckillOrder order = new SeckillOrder();
            order.setId(json.getLong("orderId"));
            order.setActivityId(json.getLong("activityId"));
            order.setUserId(json.getString("userId"));
            order.setRequestId(json.getString("requestId"));
            order.setStatus(1);
            order.setCreateTime(new Date());
            return order;
        } catch (Exception ex) {
            log.warn("Invalid seckill queue payload: {}", payload);
            return null;
        }
    }
}
