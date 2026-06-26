package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki enrich 单页任务")
public class WikiEnrichItemDto {

    @ApiModelProperty(value = "目标 slug", required = true)
    private String slug;

    @ApiModelProperty("追加段落 patch（与 rawPaths 二选一；优先 patch）")
    private String patch;

    @ApiModelProperty("补充原因（rawPaths 时喂 LLM）")
    private String reason;

    @ApiModelProperty("raw 相对路径（无 patch 时调 EnrichWriter LLM）")
    private List<String> rawPaths;
}
