package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * T18 · publish：lint + commit（+ Sync）一步结果。
 */
@Data
@ApiModel("Ingest publish 结果")
public class IngestPublishResultVo {

    @ApiModelProperty("lint 预检结果")
    private IngestLintVo lint;

    @ApiModelProperty("是否已落盘")
    private boolean committed;

    @ApiModelProperty("落盘报告（committed=true 时有值）")
    private IngestCommitResultVo commit;

    @ApiModelProperty("本次批准页数（含 approveAll 新批准）")
    private int approvedCount;

    @ApiModelProperty("建议下一步（与 commit.nextSteps 一致）")
    private List<KbWorkflowHintVo> nextSteps;
}
