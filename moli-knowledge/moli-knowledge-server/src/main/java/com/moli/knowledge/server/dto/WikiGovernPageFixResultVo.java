package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 单页修复明细")
public class WikiGovernPageFixResultVo {

    @ApiModelProperty("slug")
    private String slug;

    @ApiModelProperty("ok / skipped / failed")
    private String status;

    @ApiModelProperty("处理的 issue kind")
    private List<String> kinds;

    @ApiModelProperty("失败原因")
    private String message;

    @ApiModelProperty("dryRun 时返回建议全文")
    private String previewContent;
}
