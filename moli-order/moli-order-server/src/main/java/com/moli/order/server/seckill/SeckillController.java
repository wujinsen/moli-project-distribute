package com.moli.order.server.seckill;

import com.moli.common.core.MoliResult;
import com.moli.order.server.seckill.dto.SeckillActivityVo;
import com.moli.order.server.seckill.dto.SeckillOrderRequest;
import com.moli.order.server.seckill.dto.SeckillOrderResult;
import com.moli.order.server.seckill.enums.SeckillOrderStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/seckill")
@Api(tags = "秒杀压测")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;
    private final SeckillProperties seckillProperties;

    @GetMapping("/ping")
    @ApiOperation("网关/服务连通性探测（无鉴权）")
    public MoliResult<Map<String, Object>> ping() {
        Map<String, Object> body = new HashMap<>(4);
        body.put("pong", true);
        body.put("ts", System.currentTimeMillis());
        return MoliResult.success(body);
    }

    @GetMapping("/activity/{activityId}")
    @ApiOperation("查询秒杀活动库存（读 Redis 热数据）")
    public MoliResult<SeckillActivityVo> activity(@PathVariable Long activityId) {
        SeckillActivityVo vo = seckillService.getActivity(activityId);
        if (vo == null) {
            return MoliResult.errorMsg(404, "activity not found");
        }
        return MoliResult.success(vo);
    }

    @PostMapping("/order")
    @ApiOperation("秒杀下单（Redis Lua 原子扣减）")
    public MoliResult<SeckillOrderResult> order(@Validated @RequestBody SeckillOrderRequest request) {
        SeckillOrderResult result = seckillService.placeOrder(request);
        if (result.getStatus() == SeckillOrderStatus.SUCCESS) {
            return MoliResult.success(result);
        }
        if (result.getStatus() == SeckillOrderStatus.SOLD_OUT) {
            return MoliResult.errorMsg(409, "sold out");
        }
        if (result.getStatus() == SeckillOrderStatus.DUPLICATE) {
            return MoliResult.errorMsg(429, "duplicate request");
        }
        if (result.getStatus() == SeckillOrderStatus.INVALID_PARAM) {
            return MoliResult.errorMsg(400, "invalid param");
        }
        return MoliResult.errorMsg(404, "activity not found");
    }

    @GetMapping("/metrics")
    @ApiOperation("压测实时指标（Redis 计数）")
    public MoliResult<Map<String, Object>> metrics() {
        return MoliResult.success(seckillService.metrics());
    }

    @PostMapping("/admin/init")
    @ApiOperation("初始化秒杀活动（仅 load-test 模式）")
    public MoliResult<SeckillActivityVo> initForLoadTest(
            @RequestParam Long activityId,
            @RequestParam(required = false) Long stock,
            @RequestParam(required = false) String name) {
        if (!seckillProperties.isLoadTestMode()) {
            return MoliResult.errorMsg(403, "load-test mode disabled");
        }
        return MoliResult.success(seckillService.initActivityForLoadTest(activityId, stock, name));
    }
}
