package com.moli.order.server.seckill.dto;

import com.moli.order.server.seckill.enums.SeckillOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderResult {

    private SeckillOrderStatus status;
    private String orderId;
    private Long activityId;
    private Long remainStock;
}
