package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("Agentic 知识库提问请求（AI-7）")
public class AgenticAskRequest {

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(value = "问题", required = true)
    private String question;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("多空间ID")
    private List<Long> spaceIds;

    @ApiModelProperty("引用列表候选页数上限")
    private Integer topK;

    @ApiModelProperty("LLM 上下文候选页数上限")
    private Integer llmContextTopK;

    @ApiModelProperty("是否启用 LLM（Agentic 省略时默认 true）")
    private Boolean useLlm;

    @ApiModelProperty("召回策略：ngram | hybrid | hybrid-rerank")
    private String retrievalStrategy;

    @ApiModelProperty("GraphRAG 扩跳覆盖")
    private Boolean graphExpand;

    @ApiModelProperty("是否启用 Agentic 编排；null=kb.agentic.enabled")
    private Boolean agentic;

    public AskRequest toAskRequest(boolean defaultUseLlm) {
        AskRequest req = new AskRequest();
        req.setQuestion(question);
        req.setSpaceId(spaceId);
        req.setSpaceIds(spaceIds);
        req.setTopK(topK);
        req.setLlmContextTopK(llmContextTopK);
        req.setUseLlm(useLlm != null ? useLlm : defaultUseLlm);
        req.setRetrievalStrategy(retrievalStrategy);
        req.setGraphExpand(graphExpand);
        return req;
    }
}
