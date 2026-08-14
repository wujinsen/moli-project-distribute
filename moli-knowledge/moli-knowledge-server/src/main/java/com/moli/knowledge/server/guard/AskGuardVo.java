package com.moli.knowledge.server.guard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Guardrails 摘要（AI-9 additive）")
public class AskGuardVo {

    @ApiModelProperty("注入 BLOCK，未调用 LLM")
    private boolean blocked;

    @ApiModelProperty("可疑注入 FLAG")
    private boolean flagged;

    @ApiModelProperty("拦截规则 id，无攻击原文")
    private String blockReason;

    @ApiModelProperty("是否做过 PII 脱敏")
    private boolean piiRedacted;

    @ApiModelProperty("脱敏类型：email|phone|id_card")
    private List<String> piiTypes = new ArrayList<>();

    @ApiModelProperty("Phase B：单轮 grounding 是否执行")
    private Boolean groundingApplied;

    @ApiModelProperty("Phase B：coverage 低于阈值")
    private Boolean groundingLow;

    @ApiModelProperty("Phase B：grounding coverage")
    private Double coverage;

    @ApiModelProperty("Phase B：unsupported 陈述")
    private List<String> unsupportedStatements = new ArrayList<>();
}
