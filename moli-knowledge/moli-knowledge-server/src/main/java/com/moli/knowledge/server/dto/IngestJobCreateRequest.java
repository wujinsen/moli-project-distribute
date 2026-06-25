package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建 Ingest 批次任务请求。
 */
@Data
@ApiModel("创建 Ingest 批次")
public class IngestJobCreateRequest {

    @ApiModelProperty("目标空间ID（空=默认 enterprise-kb）")
    private Long spaceId;

    @ApiModelProperty(value = "主题", required = true)
    @NotEmpty(message = "主题不能为空")
    private String topic;

    @ApiModelProperty("批次号（空则系统生成）")
    private String batchNo;

    @ApiModelProperty("期望产出类型，逗号分隔，可空")
    private String expectTypes;

    @ApiModelProperty(value = "勾选的 raw 路径（相对 rawRoot）", required = true)
    @NotEmpty(message = "至少选择一个 raw 源")
    private List<String> rawPaths;

    @ApiModelProperty("备注")
    private String remark;
}
