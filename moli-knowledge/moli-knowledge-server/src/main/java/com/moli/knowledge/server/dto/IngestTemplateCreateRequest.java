package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("创建 Ingest 模板")
public class IngestTemplateCreateRequest {

    @ApiModelProperty("空间ID（空=默认 enterprise-kb）")
    private Long spaceId;

    @ApiModelProperty(value = "模板名称", required = true)
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @ApiModelProperty(value = "默认主题", required = true)
    @NotBlank(message = "主题不能为空")
    private String topic;

    @ApiModelProperty("期望类型")
    private String expectTypes;

    @ApiModelProperty("raw 路径列表")
    private List<String> rawPaths;

    @ApiModelProperty("可选 Plan JSON")
    private String planJson;
}
