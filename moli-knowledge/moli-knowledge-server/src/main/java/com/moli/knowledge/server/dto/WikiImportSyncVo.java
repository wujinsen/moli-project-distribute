package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 成品导入 · Sync 摘要")
public class WikiImportSyncVo {

    @ApiModelProperty("是否触发 Sync")
    private boolean triggered;

    @ApiModelProperty("Sync 是否成功")
    private boolean success;

    @ApiModelProperty("Sync 成功后 kb_document.id")
    private Long documentId;

    @ApiModelProperty("失败或摘要信息")
    private String message;
}
