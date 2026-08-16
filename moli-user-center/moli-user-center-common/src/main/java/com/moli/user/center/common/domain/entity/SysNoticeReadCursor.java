package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户公告已读水位。
 *
 * <p><b>不继承 {@code BaseEntity}</b>：本表主键是 {@code user_id}（业务主键，外部传入），
 * 没有独立的雪花 id，也不需要创建人/修改人审计 —— 水位只属于用户自己。
 *
 * <p>用水位而非「每用户每公告一行」的关联表：行数 = 用户数，不随公告数增长。
 * 代价是无法乱序标记单条已读，对通知栏场景是恰当取舍（详见设计 §4.3）。
 */
@Data
public class SysNoticeReadCursor implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户 ID，一用户一行")
    @TableId(type = IdType.INPUT)
    private Long userId;

    @ApiModelProperty(value = "最后一次读公告列表的时间水位")
    private Date lastReadTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

}
