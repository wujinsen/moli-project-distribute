package com.moli.user.center.common.domain.vo;

import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 公告后台列表查询条件（继承 BaseEntity 以获得 pageNum / pageSize）。
 */
@Data
public class NoticeVo extends BaseEntity {

    @ApiModelProperty(value = "标题，模糊匹配")
    private String noticeTitle;

    @ApiModelProperty(value = "类型：1通知 2公告 3维护")
    private Integer noticeType;

    @ApiModelProperty(value = "状态：0草稿 1已发布 2已撤回")
    private Integer status;

}
