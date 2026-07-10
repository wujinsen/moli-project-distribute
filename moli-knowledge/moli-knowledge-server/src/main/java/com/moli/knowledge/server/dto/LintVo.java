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

    @ApiModelProperty("slug 裸名歧义（duplicate）")
    private List<Duplicate> duplicates = new ArrayList<>();

    @ApiModelProperty("过时/被取代仍发布（stale）")
    private List<Stale> stale = new ArrayList<>();

    @ApiModelProperty("内容 hash 重复（conflict）")
    private List<Conflict> conflicts = new ArrayList<>();

    @ApiModelProperty("缺 sources（missing_source）")
    private List<IssueItem> missingSources = new ArrayList<>();

    @ApiModelProperty("type 非法（bad_type）")
    private List<IssueItem> badTypes = new ArrayList<>();

    @ApiModelProperty("缺标题（missing_title）")
    private List<IssueItem> missingTitles = new ArrayList<>();

    @ApiModelProperty("slug 与文件名不一致（slug_mismatch）")
    private List<IssueItem> slugMismatches = new ArrayList<>();

    @ApiModelProperty("缺 created/updated（missing_dates）")
    private List<IssueItem> missingDates = new ArrayList<>();

    @ApiModelProperty("缺概念页（missing_concept）")
    private List<IssueItem> missingConcepts = new ArrayList<>();

    @ApiModelProperty("统计汇总")
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("slug 歧义项")
    public static class Duplicate implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("裸 slug 名")
        private String stem;

        @ApiModelProperty("代表文档 ID")
        private String page;

        @ApiModelProperty("代表文档标题")
        private String title;

        @ApiModelProperty("冲突的全路径 slug 列表")
        private List<String> slugs = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("过时项")
    public static class Stale implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("文档 ID")
        private String slug;

        @ApiModelProperty("标题")
        private String title;

        @ApiModelProperty("原因说明")
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("内容冲突项")
    public static class Conflict implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("代表文档 ID")
        private String page;

        @ApiModelProperty("标题")
        private String title;

        @ApiModelProperty("同 hash 的 slug 列表")
        private List<String> slugs = new ArrayList<>();

        @ApiModelProperty("详情")
        private String detail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("通用体检项（KBOPS-10 frontmatter / concept）")
    public static class IssueItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("文档 ID（缺概念页可为空）")
        private String page;

        @ApiModelProperty("标题或摘要")
        private String title;

        @ApiModelProperty("详情")
        private String detail;
    }
}
