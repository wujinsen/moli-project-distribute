package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 阅读侧公告条目（**不含正文**）。
 *
 * <p>通知栏与列表只需要标题和时间；正文可能是很长的 Markdown，
 * 全部塞进列表响应会让首页请求无谓变大。正文走 {@code GET /notice/feed/{id}}。
 */
@Data
public class NoticeBriefVo {

    @ApiModelProperty(value = "公告 ID")
    private Long id;

    @ApiModelProperty(value = "公告标题")
    private String noticeTitle;

    @ApiModelProperty(value = "类型：1通知 2公告 3维护")
    private Integer noticeType;

    @ApiModelProperty(value = "1置顶 0普通")
    private Integer topFlag;

    @ApiModelProperty(value = "发布时间")
    private Date publishTime;

    @ApiModelProperty(value = "过期时间，NULL=长期有效")
    private Date expireTime;

    @ApiModelProperty(value = "相对当前用户已读水位是否为未读")
    private Boolean unread;

}
