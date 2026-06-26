package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki enrich 批次结果")
public class WikiEnrichResultVo {

    private String batchNo;
    private String topic;
    private Boolean dryRun;
    private List<WikiEnrichItemResultVo> items;
    private Boolean logAppended;
    private Boolean indexUpdated;
    private Integer edgesAppended;
    private Boolean syncTriggered;
    private SyncTriggerVo syncResult;
}
