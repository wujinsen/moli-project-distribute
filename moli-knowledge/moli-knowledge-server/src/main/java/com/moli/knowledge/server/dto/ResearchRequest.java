package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("DeepResearch 请求（AI-10）")
public class ResearchRequest {

    @NotBlank
    @Size(max = 500)
    @ApiModelProperty(value = "调研主题", required = true)
    private String topic;

    @ApiModelProperty("单空间")
    private Long spaceId;

    @ApiModelProperty("多空间")
    private List<Long> spaceIds;

    @ApiModelProperty("是否 Ingest 回写 outputs/（Phase B）")
    private Boolean writeback;

    @ApiModelProperty("回写 slug stem")
    private String slug;

    @ApiModelProperty("难节是否允许 /kb/ask/agentic")
    private Boolean agentic;

    @ApiModelProperty("GraphRAG 开关透传")
    private Boolean graphExpand;

    @ApiModelProperty("召回策略，默认 hybrid")
    private String retrievalStrategy;

    @ApiModelProperty("Planner 节数上限")
    private Integer maxSections;

    @ApiModelProperty("Retriever 回补轮次上限")
    private Integer maxRetrieveRounds;

    @ApiModelProperty("整次预算毫秒")
    private Integer latencyBudgetMs;

    @ApiModelProperty("每 query topK")
    private Integer topK;
}
