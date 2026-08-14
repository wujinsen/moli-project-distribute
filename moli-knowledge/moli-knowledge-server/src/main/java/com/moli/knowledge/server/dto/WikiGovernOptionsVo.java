package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Wiki 治理 LLM 选项（kb.llm 模型列表）")
public class WikiGovernOptionsVo {

    @ApiModelProperty("kb.llm 是否可用（enabled + api-key）")
    private boolean llmAvailable;

    @ApiModelProperty("kb.llm.provider 标识")
    private String provider;

    @ApiModelProperty("默认模型 ID")
    private String defaultModel;

    @ApiModelProperty("可选模型（治理页下拉）")
    private List<WikiGovernModelVo> models = new ArrayList<>();

    @ApiModelProperty("脚本可修复的 issue.kind")
    private List<String> scriptFixableKinds = new ArrayList<>();

    @ApiModelProperty("需 LLM 修复的 issue.kind")
    private List<String> aiFixableKinds = new ArrayList<>();

    @ApiModelProperty("仅人工处理的 issue.kind")
    private List<String> manualOnlyKinds = new ArrayList<>();
}
