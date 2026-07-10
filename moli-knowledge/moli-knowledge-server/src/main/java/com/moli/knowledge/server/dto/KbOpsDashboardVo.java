package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("知识库运维 Dashboard（KBOPS-9）")
public class KbOpsDashboardVo {

    @ApiModelProperty("查询范围空间ID；null=全部可读空间")
    private Long spaceId;

    @ApiModelProperty("Sync 批次趋势（按日）")
    private List<KbOpsSyncTrendPointVo> syncTrend = new ArrayList<>();

    @ApiModelProperty("Lint 工单汇总")
    private KbOpsLintSummaryVo lintSummary = new KbOpsLintSummaryVo();

    @ApiModelProperty("kb_relation 未解析断链数（resolved=0）")
    private long unresolvedRelationCount;

    @ApiModelProperty("LLM 运行时可用性摘要")
    private KbOpsLlmSummaryVo llm = new KbOpsLlmSummaryVo();
}
