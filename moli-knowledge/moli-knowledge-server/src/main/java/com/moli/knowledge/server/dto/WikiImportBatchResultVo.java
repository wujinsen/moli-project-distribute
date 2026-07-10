package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Wiki 批量导入结果（T20c）")
public class WikiImportBatchResultVo {

    @ApiModelProperty("成功导入项")
    private List<WikiImportResultVo> imported = new ArrayList<>();

    @ApiModelProperty("失败项")
    private List<WikiImportBatchFailVo> failed = new ArrayList<>();

    @ApiModelProperty("整批 Sync 摘要（sync=true 且至少一项成功时触发一次）")
    private WikiImportSyncVo sync;
}
