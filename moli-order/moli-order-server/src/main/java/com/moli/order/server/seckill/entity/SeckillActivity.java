package com.moli.order.server.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seckill_activity")
public class SeckillActivity {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;

    private Long stock;

    private Long sold;

    /**
     * 1=active, 0=closed
     */
    private Integer status;

    private Date startTime;

    private Date endTime;

    private Date createTime;
}
