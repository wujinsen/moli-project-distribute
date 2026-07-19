package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Agentic 知识库问答响应（AI-7）")
public class AgenticAskVo {

    @ApiModelProperty("答案")
    private String answer;

    @ApiModelProperty("generative | retrieval")
    private String mode;

    @ApiModelProperty("作用域")
    private String scope;

    @ApiModelProperty("作用域原因")
    private String scopeReason;

    @ApiModelProperty("LLM provider")
    private String provider;

    @ApiModelProperty("LLM model")
    private String model;

    @ApiModelProperty("引用列表")
    private List<AskResponse.Citation> citations = new ArrayList<>();

    @ApiModelProperty("kb_qa_log.id")
    private Long qaLogId;

    @ApiModelProperty("本次是否实际跑了 Agentic 编排")
    private boolean agentic;

    @ApiModelProperty("实际 round 数")
    private int rounds;

    @ApiModelProperty("改写后的主问")
    private String rewrittenQuery;

    @ApiModelProperty("拆解子问题")
    private List<String> subQuestions = new ArrayList<>();

    @ApiModelProperty("末轮自检 coverage；Phase A 为 null")
    private Double coverage;

    @ApiModelProperty("自检 unsupported 陈述")
    private List<String> unsupportedStatements = new ArrayList<>();

    @ApiModelProperty("每轮命中 slug 摘要")
    private List<List<String>> retrievedSlugsPerRound = new ArrayList<>();

    @ApiModelProperty("是否降级（LLM 不可用 / useLlm=false 等）")
    private boolean degraded;

    @ApiModelProperty("Guardrails 摘要（AI-9 additive）")
    private com.moli.knowledge.server.guard.AskGuardVo guard;
}
