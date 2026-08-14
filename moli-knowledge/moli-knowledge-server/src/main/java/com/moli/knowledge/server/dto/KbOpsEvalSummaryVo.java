package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("检索质量 Dashboard 摘要")
public class KbOpsEvalSummaryVo {

    @ApiModelProperty("golden 题量（最近一次 run）")
    private Integer goldenTotal;

    @ApiModelProperty("各策略最新一次评测摘要")
    private List<KbOpsEvalStrategySummaryVo> strategies = new ArrayList<>();
}
