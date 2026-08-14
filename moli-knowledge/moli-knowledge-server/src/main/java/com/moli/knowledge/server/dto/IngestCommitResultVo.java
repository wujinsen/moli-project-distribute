package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.moli.knowledge.server.dto.KbWorkflowHintVo;

import java.util.List;

/**
 * Ingest 批次落盘 + （可选）Sync 结果报告。
 */
@Data
@ApiModel("Ingest 落盘报告")
public class IngestCommitResultVo {

    @ApiModelProperty("批次任务ID")
    private Long jobId;

    @ApiModelProperty("新建页数")
    private int created;

    @ApiModelProperty("更新页数（enrich）")
    private int updated;

    @ApiModelProperty("写入的 wiki 文件相对路径列表")
    private List<String> files;

    @ApiModelProperty("追加的 edges 数")
    private int edgesAppended;

    @ApiModelProperty("log.md 是否追加")
    private boolean logAppended;

    @ApiModelProperty("index.md 是否更新")
    private boolean indexUpdated;

    @ApiModelProperty("是否触发 Sync")
    private boolean syncTriggered;

    @ApiModelProperty("Sync 结果（触发时）")
    private SyncTriggerVo syncResult;

    @ApiModelProperty("建议下一步（Wiki 治理 Lint 等）")
    private List<KbWorkflowHintVo> nextSteps;
}
