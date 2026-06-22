package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("文档保存请求")
public class DocumentSaveRequest {

    @ApiModelProperty("文档ID，更新时必填")
    private Long id;

    @NotNull(message = "空间ID不能为空")
    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("摘要")
    private String summary;

    @ApiModelProperty("正文")
    private String content;

    @ApiModelProperty("文档类型 markdown/rich")
    private String docType;

    @ApiModelProperty("状态 0草稿 1已发布")
    private Integer status;

    @ApiModelProperty("标签ID列表")
    private List<Long> tagIds;

    @ApiModelProperty("变更说明")
    private String changeLog;
}
