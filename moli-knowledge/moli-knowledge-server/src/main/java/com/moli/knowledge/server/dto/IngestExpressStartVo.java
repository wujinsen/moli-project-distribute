package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * T18 · 创建批次并 prepare 一步返回。
 */
@Data
@ApiModel("Ingest 一键预览启动结果")
public class IngestExpressStartVo {

    @ApiModelProperty("新建批次")
    private IngestJobVo job;

    @ApiModelProperty("prepare 结果")
    private IngestPrepareResultVo prepare;
}
