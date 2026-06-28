package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * T18 · prepare：Express Plan + 生成草稿一步结果。
 */
@Data
@ApiModel("Ingest prepare 结果")
public class IngestPrepareResultVo {

    @ApiModelProperty("批次详情（含最新 plan）")
    private IngestJobVo job;

    @ApiModelProperty("草稿生成统计")
    private IngestGenerateResultVo generate;

    @ApiModelProperty("当前草稿列表")
    private List<IngestDraftVo> drafts;
}
