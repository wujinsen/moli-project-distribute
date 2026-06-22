package com.moli.order.server.seckill;

import com.alibaba.fastjson.JSON;
import com.moli.common.core.IdGenerator;
import com.moli.order.server.seckill.dto.SeckillActivityVo;
import com.moli.order.server.seckill.dto.SeckillOrderRequest;
import com.moli.order.server.seckill.dto.SeckillOrderResult;
import com.moli.order.server.seckill.entity.SeckillActivity;
import com.moli.order.server.seckill.enums.SeckillOrderStatus;
import com.moli.order.server.seckill.mapper.SeckillActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillProperties seckillProperties;
    private final SeckillActivityMapper seckillActivityMapper;

    private final DefaultRedisScript<String> seckillScript = buildScript();

    public SeckillOrderResult placeOrder(SeckillOrderRequest request) {
        if (request.getActivityId() == null || StringUtils.isBlank(request.getUserId())) {
            return SeckillOrderResult.builder()
                    .status(SeckillOrderStatus.INVALID_PARAM)
                    .build();
        }

        Long activityId = request.getActivityId();
        String userId = request.getUserId().trim();
        String requestId = StringUtils.defaultIfBlank(request.getRequestId(), IdGenerator.getStringId());
        Long orderId = IdGenerator.getId();

        String prefix = seckillProperties.getRedisKeyPrefix();
        String stockKey = SeckillRedisKeys.stockKey(prefix, activityId);
        String userKey = SeckillRedisKeys.userKey(prefix, activityId);
        String queueKey = SeckillRedisKeys.queueKey(prefix, activityId);
        String metricsKey = SeckillRedisKeys.metricsKey(prefix);

        Map<String, Object> payload = new HashMap<>(8);
        payload.put("orderId", orderId);
        payload.put("activityId", activityId);
        payload.put("userId", userId);
        payload.put("requestId", requestId);
        payload.put("ts", System.currentTimeMillis());

        String luaResult = stringRedisTemplate.execute(
                seckillScript,
                Arrays.asList(stockKey, userKey, queueKey),
                userId,
                JSON.toJSONString(payload),
                metricsKey
        );

        SeckillOrderStatus status = mapLuaResult(luaResult);
        Long remainStock = parseLong(stringRedisTemplate.opsForValue().get(stockKey));

        if (status == SeckillOrderStatus.SOLD_OUT) {
            stringRedisTemplate.opsForHash().increment(metricsKey, "sold_out", 1);
        } else if (status == SeckillOrderStatus.DUPLICATE) {
            stringRedisTemplate.opsForHash().increment(metricsKey, "duplicate", 1);
        }

        return SeckillOrderResult.builder()
                .status(status)
                .orderId(status == SeckillOrderStatus.SUCCESS ? String.valueOf(orderId) : null)
                .activityId(activityId)
                .remainStock(remainStock)
                .build();
    }

    public SeckillActivityVo getActivity(Long activityId) {
        String prefix = seckillProperties.getRedisKeyPrefix();
        String stockKey = SeckillRedisKeys.stockKey(prefix, activityId);
        String metaKey = SeckillRedisKeys.activityMetaKey(prefix, activityId);

        Long stock = parseLong(stringRedisTemplate.opsForValue().get(stockKey));
        String name = (String) stringRedisTemplate.opsForHash().get(metaKey, "name");
        Integer status = parseInt((String) stringRedisTemplate.opsForHash().get(metaKey, "status"));

        if (stock == null && name == null) {
            SeckillActivity activity = seckillActivityMapper.selectById(activityId);
            if (activity == null) {
                return null;
            }
            warmActivityCache(activity);
            stock = activity.getStock() - activity.getSold();
            name = activity.getName();
            status = activity.getStatus();
        }

        long sold = 0L;
        if (stock != null) {
            String initStock = (String) stringRedisTemplate.opsForHash().get(metaKey, "initStock");
            if (initStock != null) {
                sold = Long.parseLong(initStock) - stock;
            }
        }

        return SeckillActivityVo.builder()
                .activityId(activityId)
                .name(name)
                .stock(stock)
                .sold(sold)
                .status(status)
                .build();
    }

    public Map<String, Object> metrics() {
        String metricsKey = SeckillRedisKeys.metricsKey(seckillProperties.getRedisKeyPrefix());
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(metricsKey);
        Map<String, Object> result = new HashMap<>(entries.size() + 4);
        entries.forEach((k, v) -> result.put(String.valueOf(k), v));
        result.put("loadTestMode", seckillProperties.isLoadTestMode());
        result.put("asyncDb", seckillProperties.isAsyncDb());
        return result;
    }

    public SeckillActivityVo initActivityForLoadTest(Long activityId, Long stock, String name) {
        if (!seckillProperties.isLoadTestMode()) {
            throw new IllegalStateException("load-test mode is disabled");
        }

        long initStock = stock != null ? stock : seckillProperties.getDefaultStock();
        String activityName = StringUtils.defaultIfBlank(name, "loadtest-" + activityId);

        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            activity = new SeckillActivity();
            activity.setId(activityId);
            activity.setName(activityName);
            activity.setStock(initStock);
            activity.setSold(0L);
            activity.setStatus(1);
            activity.setCreateTime(new Date());
            seckillActivityMapper.insert(activity);
        } else {
            activity.setStock(initStock);
            activity.setSold(0L);
            activity.setStatus(1);
            seckillActivityMapper.updateById(activity);
        }

        warmActivityCache(activity);
        resetRedisActivity(activityId, initStock, activityName);

        return getActivity(activityId);
    }

    private void warmActivityCache(SeckillActivity activity) {
        resetRedisActivity(activity.getId(), activity.getStock(), activity.getName());
    }

    private void resetRedisActivity(Long activityId, long stock, String name) {
        String prefix = seckillProperties.getRedisKeyPrefix();
        String stockKey = SeckillRedisKeys.stockKey(prefix, activityId);
        String userKey = SeckillRedisKeys.userKey(prefix, activityId);
        String queueKey = SeckillRedisKeys.queueKey(prefix, activityId);
        String metaKey = SeckillRedisKeys.activityMetaKey(prefix, activityId);

        stringRedisTemplate.delete(Arrays.asList(stockKey, userKey, queueKey, metaKey));
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
        stringRedisTemplate.opsForHash().put(metaKey, "name", name);
        stringRedisTemplate.opsForHash().put(metaKey, "status", "1");
        stringRedisTemplate.opsForHash().put(metaKey, "initStock", String.valueOf(stock));
        stringRedisTemplate.expire(stockKey, 7, TimeUnit.DAYS);
        stringRedisTemplate.expire(userKey, 7, TimeUnit.DAYS);
        stringRedisTemplate.expire(queueKey, 7, TimeUnit.DAYS);
        stringRedisTemplate.expire(metaKey, 7, TimeUnit.DAYS);
    }

    private SeckillOrderStatus mapLuaResult(String luaResult) {
        if ("OK".equals(luaResult)) {
            return SeckillOrderStatus.SUCCESS;
        }
        if ("DUPLICATE".equals(luaResult)) {
            return SeckillOrderStatus.DUPLICATE;
        }
        if ("SOLD_OUT".equals(luaResult)) {
            return SeckillOrderStatus.SOLD_OUT;
        }
        return SeckillOrderStatus.ACTIVITY_NOT_FOUND;
    }

    private DefaultRedisScript<String> buildScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(SeckillLuaScripts.SECKILL_ORDER);
        script.setResultType(String.class);
        return script;
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    private Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
