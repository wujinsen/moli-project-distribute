package com.moli.order.server.seckill.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SeckillOrderRequest {

    @NotNull
    private Long activityId;

    @NotBlank
    private String userId;

    /**
     * Client-side idempotency key; optional in load-test mode.
     */
    private String requestId;
}
