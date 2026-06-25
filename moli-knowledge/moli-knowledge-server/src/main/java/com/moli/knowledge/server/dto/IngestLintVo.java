package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ingest 批次 lint 预检结果（commit 前门禁）。
 */
@Data
@ApiModel("Ingest 批次 lint 预检")
public class IngestLintVo {

    @ApiModelProperty("问题总数")
    private int issueCount;

    @ApiModelProperty("阻塞 commit 的 ERROR 数（>0 不可提交）")
    private int blockingCount;

    @ApiModelProperty("是否可提交（blockingCount==0 且全部已批准）")
    private boolean commitReady;

    @ApiModelProperty("问题清单")
    private List<Item> issues;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("lint 问题")
    public static class Item {
        @ApiModelProperty("所属草稿 slug（批次级问题为空）")
        private String slug;
        @ApiModelProperty("问题类型")
        private String type;
        @ApiModelProperty("ERROR / WARN")
        private String severity;
        @ApiModelProperty("描述")
        private String message;
    }
}
