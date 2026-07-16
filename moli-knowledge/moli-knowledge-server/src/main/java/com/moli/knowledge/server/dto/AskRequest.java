package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("知识库提问请求")
public class AskRequest {

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(value = "问题", required = true)
    private String question;

    @ApiModelProperty("空间ID（省略则全库；与 spaceIds 同时传时以 spaceIds 为准）")
    private Long spaceId;

    @ApiModelProperty("多空间ID（非空时在上述空间内检索，须均有读权限）")
    private List<Long> spaceIds;

    @ApiModelProperty("引用列表候选页数上限；省略则用 kb.ask.citation-top-k（默认 8）")
    private Integer topK;

    @ApiModelProperty("LLM 上下文候选页数上限；省略则用 kb.ask.llm-context-top-k（默认 3）")
    private Integer llmContextTopK;

    @ApiModelProperty("是否启用 LLM 生成式作答（默认 false；须后端 kb.llm 已配置且 usable）")
    private Boolean useLlm = false;
}
