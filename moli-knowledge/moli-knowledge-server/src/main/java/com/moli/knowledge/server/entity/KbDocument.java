package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document")
@ApiModel("知识文档")
public class KbDocument extends BaseEntity {

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("空间内唯一标识/URL")
    private String slug;

    @ApiModelProperty("来源 kb/manual")
    private String source;

    @ApiModelProperty("kb/ 原始 markdown 路径")
    private String sourcePath;

    @ApiModelProperty("正文+frontmatter 的 SHA-256")
    private String contentHash;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("摘要")
    private String summary;

    @ApiModelProperty("正文")
    private String content;

    @ApiModelProperty("内容格式 markdown/rich")
    private String docType;

    @ApiModelProperty("知识类型 guide/service/concept/article/interview/output")
    private String kbType;

    @ApiModelProperty("领域 FE/AP/DB...")
    private String domain;

    @ApiModelProperty("状态 0草稿 1已发布 2已归档")
    private Integer status;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("版本号")
    private Integer versionNo;

    @ApiModelProperty("发布时间")
    private Date publishTime;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
