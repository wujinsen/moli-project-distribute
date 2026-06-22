package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库体检报告（对齐 kb/tools/serve.py 的 /api/lint）。
 * broken：正文 [[..]] 指向不存在的文档；orphans：无任何入链（含同标签）；noSummary：缺摘要。
 */
@Data
@ApiModel("知识库体检报告")
public class LintVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("断链：正文 [[..]] 指向不存在的文档")
    private List<Broken> broken = new ArrayList<>();

    @ApiModelProperty("孤儿页：无任何入链（含同标签关联）")
    private List<Ref> orphans = new ArrayList<>();

    @ApiModelProperty("缺摘要：summary 为空")
    private List<Ref> noSummary = new ArrayList<>();

    @ApiModelProperty("统计：pages/broken/orphans/noSummary")
    private Map<String, Integer> counts = new LinkedHashMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("文档引用")
    public static class Ref implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("文档ID")
        private String slug;

        @ApiModelProperty("标题")
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("断链项")
    public static class Broken implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("源文档ID")
        private String page;

        @ApiModelProperty("源文档标题")
        private String title;

        @ApiModelProperty("未能解析的目标（[[..]] 内文本）")
        private String target;
    }
}
