package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("文档详情")
public class DocumentDetailVo {

    @ApiModelProperty("文档ID")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("slug")
    private String slug;

    @ApiModelProperty("知识类型 guide/service/concept/article/interview/output")
    private String kbType;

    @ApiModelProperty("领域")
    private String domain;

    @ApiModelProperty("来源 kb/manual")
    private String source;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("摘要")
    private String summary;

    @ApiModelProperty("正文")
    private String content;

    @ApiModelProperty("文档类型")
    private String docType;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("版本号")
    private Integer versionNo;

    @ApiModelProperty("发布时间")
    private Date publishTime;

    @ApiModelProperty("创建人")
    private Long createId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("标签ID列表")
    private List<Long> tagIds;

    @ApiModelProperty("是否已收藏")
    private Boolean favorited;
}
