package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 治理 · 一键修复结果")
public class WikiGovernAutoFixResultVo {

    @ApiModelProperty("修复前 issue 数")
    private int issuesBefore;

    @ApiModelProperty("修复后 issue 数（relintAfter=true 时有值）")
    private Integer issuesAfter;

    @ApiModelProperty("脚本修复摘要")
    private WikiGovernScriptFixResultVo scriptFix;

    @ApiModelProperty("AI 修复摘要")
    private WikiGovernAiBatchFixResultVo aiFix;

    @ApiModelProperty("复检结果")
    private WikiSpaceLintVo relint;

    @ApiModelProperty("Sync 结果（syncAfter=true 时有值）")
    private SyncTriggerVo sync;
}
