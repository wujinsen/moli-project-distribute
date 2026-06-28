package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 按 plan 生成草稿的结果（T15e 断点续跑进度）。
 */
@Data
@ApiModel("Ingest 生成结果")
public class IngestGenerateResultVo {

    @ApiModelProperty("Plan 中应生成页数")
    private int total;

    @ApiModelProperty("本次新生成页数")
    private int generated;

    @ApiModelProperty("续跑跳过（已有草稿）页数")
    private int skipped;

    @ApiModelProperty("本次生成失败页数（单页隔离，不影响其余页）")
    private int failed;

    @ApiModelProperty("是否续跑模式")
    private boolean resume;

    @ApiModelProperty("是否模板模式（useLlmGenerate=false 或 LLM 不可用自动降级）")
    private boolean templateMode;

    @ApiModelProperty("是否因 LLM 不可用而从 useLlmGenerate=true 自动降级为模板模式")
    private boolean llmFallback;

    @ApiModelProperty("LLM 自动降级说明（供前端 Toast）")
    private String llmFallbackReason;

    @ApiModelProperty("当前全部草稿")
    private List<IngestDraftVo> drafts;
}
