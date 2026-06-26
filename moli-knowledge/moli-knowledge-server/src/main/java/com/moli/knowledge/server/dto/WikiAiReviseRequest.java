package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki AI 改稿请求")
public class WikiAiReviseRequest {

    @ApiModelProperty(value = "slug", required = true)
    private String slug;

    @ApiModelProperty("所属空间ID")
    private Long spaceId;

    @ApiModelProperty(value = "改稿指令", required = true)
    private String instruction;

    @ApiModelProperty("可选；不传则服务端读 wiki 文件")
    private String baselineContent;

    @ApiModelProperty("来自体检的问题上下文")
    private IssueContext issueContext;

    @Data
    @ApiModel("体检问题上下文")
    public static class IssueContext {
        @ApiModelProperty("broken_link / orphan / no_summary 等")
        private String issueType;
        @ApiModelProperty("问题详情")
        private String detail;
    }
}
