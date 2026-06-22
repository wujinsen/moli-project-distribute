package com.moli.order.server.seckill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillActivityVo {

    private Long activityId;
    private String name;
    private Long stock;
    private Long sold;
    private Integer status;
}
