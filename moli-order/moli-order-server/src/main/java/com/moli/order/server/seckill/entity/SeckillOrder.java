package com.moli.order.server.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long activityId;

    private String userId;

    private String requestId;

    private Integer status;

    private Date createTime;
}
