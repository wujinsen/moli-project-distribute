package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Wiki 成品导入结果（T20b）")
public class WikiImportResultVo {

    @ApiModelProperty("全 slug（含分类前缀）")
    private String slug;

    @ApiModelProperty("空间 ID")
    private Long spaceId;

    @ApiModelProperty("相对 kb/ 的路径，如 wiki-moli/ops/foo.md")
    private String relativePath;

    @ApiModelProperty("是否新建（非覆盖）")
    private boolean created;

    @ApiModelProperty("写入后 contentHash")
    private String contentHash;

    @ApiModelProperty("lint 预检告警文案")
    private List<String> lintWarnings = new ArrayList<>();

    @ApiModelProperty("Sync 摘要")
    private WikiImportSyncVo sync;

    @ApiModelProperty("建议下一步")
    private List<KbWorkflowHintVo> nextSteps = new ArrayList<>();

    @ApiModelProperty("T20e · 写入 wiki .assets 的相对路径列表（assets/foo.png）")
    private List<String> assetsImported = new ArrayList<>();
}
