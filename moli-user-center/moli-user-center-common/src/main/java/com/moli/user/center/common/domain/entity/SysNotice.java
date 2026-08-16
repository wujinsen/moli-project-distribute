package com.moli.user.center.common.domain.entity;

import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 通知公告。设计见 {@code docs/design/sys-config-notice.md} §4。
 *
 * <p>无 remark 字段：公告本身就是内容，再加备注属于冗余。
 */
@Data
public class SysNotice extends BaseEntity {

    /** 草稿。新建默认落此状态，避免半成品直接推给全员 */
    public static final int STATUS_DRAFT = 0;
    /** 已发布，阅读侧可见 */
    public static final int STATUS_PUBLISHED = 1;
    /** 已撤回。发错信息需要能撤下，而物理删除会丢失「对外发布过什么」的痕迹 */
    public static final int STATUS_REVOKED = 2;

    @ApiModelProperty(value = "公告标题")
    private String noticeTitle;

    @ApiModelProperty(value = "类型，对应字典 sys_notice_type：1通知 2公告 3维护")
    private Integer noticeType;

    @ApiModelProperty(value = "公告正文（Markdown 源文）")
    private String noticeContent;

    @ApiModelProperty(value = "0草稿 1已发布 2已撤回")
    private Integer status;

    @ApiModelProperty(value = "1置顶 0普通")
    private Integer topFlag;

    @ApiModelProperty(value = "发布时间，由发布动作写入")
    private Date publishTime;

    @ApiModelProperty(value = "过期时间，NULL=长期有效")
    private Date expireTime;

}
