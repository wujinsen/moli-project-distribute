package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 重复页合并提示结果")
public class WikiGovernMergeHintResultVo {

    @ApiModelProperty("逐 issue 提示")
    private List<WikiGovernMergeHintItemVo> items;
}
