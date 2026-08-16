package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 阅读侧聚合响应：有效公告 + 未读数。
 *
 * <p>合成一个响应而不是拆两个接口：前端渲染通知栏时两者必然同时需要，
 * 拆开会让首屏多一次往返，且存在「列表与角标来自两个时刻」的不一致。
 */
@Data
public class NoticeFeedVo {

    @ApiModelProperty(value = "当前有效公告，置顶优先、发布时间倒序")
    private List<NoticeBriefVo> list;

    @ApiModelProperty(value = "未读数量（相对已读水位）")
    private Integer unreadCount;

}
