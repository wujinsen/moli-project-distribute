package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel("空间级文件 Lint 结果（文件真值）")
public class WikiSpaceLintVo {

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("wiki 子目录（lint.py --wiki-dir）")
    private String wikiDir;

    @ApiModelProperty("统计：pages / issues / errors / warnings / infos / by_kind")
    private Map<String, Object> stats;

    @ApiModelProperty("分级问题清单（page 字段即 slug）")
    private List<WikiLintIssueVo> issues;

    @ApiModelProperty("脚本退出码（0=无阻断；非 0 表示有 error 或 strict 下的 warn，非接口失败）")
    private Integer exitCode;

    @ApiModelProperty("脚本输出尾部（排障用）")
    private String outputTail;
}
