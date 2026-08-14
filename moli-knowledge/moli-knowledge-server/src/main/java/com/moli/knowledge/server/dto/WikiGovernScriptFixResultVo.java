package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 脚本修复结果")
public class WikiGovernScriptFixResultVo {

    @ApiModelProperty("成功写盘页数")
    private int fixedPages;

    @ApiModelProperty("跳过（无脚本项或文件不存在）")
    private int skippedPages;

    @ApiModelProperty("失败页数")
    private int failedPages;

    @ApiModelProperty("逐页明细")
    private List<WikiGovernPageFixResultVo> pages;
}
