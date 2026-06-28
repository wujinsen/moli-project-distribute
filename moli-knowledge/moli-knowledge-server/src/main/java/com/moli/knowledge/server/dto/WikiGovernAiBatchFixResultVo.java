package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · AI 批量修复结果")
public class WikiGovernAiBatchFixResultVo {

    @ApiModelProperty("成功写盘页数")
    private int fixedPages;

    @ApiModelProperty("跳过页数")
    private int skippedPages;

    @ApiModelProperty("失败页数")
    private int failedPages;

    @ApiModelProperty("使用的模型")
    private String model;

    @ApiModelProperty("逐页明细")
    private List<WikiGovernPageFixResultVo> pages;
}
