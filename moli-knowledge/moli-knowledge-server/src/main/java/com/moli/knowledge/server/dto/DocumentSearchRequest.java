package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("文档搜索请求")
public class DocumentSearchRequest {

    @ApiModelProperty("空间ID（与 spaceIds 同时传时以 spaceIds 为准）")
    private Long spaceId;

    @ApiModelProperty("多空间ID")
    private List<Long> spaceIds;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("多分类 OR 过滤；非空时优先于 categoryId")
    private List<Long> categoryIds;

    @ApiModelProperty("仅未分类（category_id IS NULL）；可与 categoryIds 组合表示 OR 含未分类")
    private Boolean uncategorizedOnly;

    @ApiModelProperty("体裁过滤：guide/service/concept/article/interview/output；空=不过滤")
    private String kbType;

    @ApiModelProperty("多体裁 OR 过滤；非空时优先于 kbType")
    private List<String> kbTypes;

    @ApiModelProperty("关键词")
    private String keyword;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("标签ID")
    private Long tagId;

    @ApiModelProperty("来源过滤：kb（wiki 同步）/ manual；文档管理传 kb")
    private String source;

    @ApiModelProperty("页码")
    private Integer pageNum = 1;

    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}
