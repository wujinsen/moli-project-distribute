package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki enrich 请求（单页或批次）")
public class WikiEnrichRequest {

    @ApiModelProperty("空间 ID（省略=默认 enterprise-kb）")
    private Long spaceId;

    @ApiModelProperty("批次号（治理 log/index 用）")
    private String batchNo;

    @ApiModelProperty("批次主题摘要")
    private String topic;

    /** 单页模式：与 items 二选一。 */
    @ApiModelProperty("单页 slug")
    private String slug;

    @ApiModelProperty("单页 patch")
    private String patch;

    @ApiModelProperty("单页 reason")
    private String reason;

    @ApiModelProperty("单页 raw 路径")
    private List<String> rawPaths;

    @ApiModelProperty("批次 enrich 项（优先于单页字段）")
    private List<WikiEnrichItemDto> items;

    @ApiModelProperty("追加 graph/edges.jsonl")
    private List<WikiEnrichEdgeDto> edges;

    @ApiModelProperty("更新 frontmatter sources/updated，默认 true")
    private Boolean updateMeta;

    @ApiModelProperty("append log.md，默认 true（非 dryRun 时）")
    private Boolean appendLog;

    @ApiModelProperty("append index.md 批次段，默认 true")
    private Boolean appendIndex;

    @ApiModelProperty("append edges.jsonl，默认 true")
    private Boolean appendEdges;

    @ApiModelProperty("仅预览不写盘")
    private Boolean dryRun;

    @ApiModelProperty("落盘后触发 Sync")
    private Boolean sync;
}
