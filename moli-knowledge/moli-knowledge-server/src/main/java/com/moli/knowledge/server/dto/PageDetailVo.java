package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("知识页详情（按 slug）")
public class PageDetailVo {

    @ApiModelProperty("文档ID")
    private Long docId;

    @ApiModelProperty("slug")
    private String slug;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("摘要")
    private String summary;

    @ApiModelProperty("正文 markdown")
    private String content;

    @ApiModelProperty("知识类型")
    private String kbType;

    @ApiModelProperty("领域")
    private String domain;

    @ApiModelProperty("状态 0草稿 1已发布 2已归档")
    private Integer status;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("标签名")
    private List<String> tags = new ArrayList<>();

    @ApiModelProperty("出链（本页 → 其它页）")
    private List<Ref> outLinks = new ArrayList<>();

    @ApiModelProperty("入链（其它页 → 本页）")
    private List<Ref> backLinks = new ArrayList<>();

    @Data
    @ApiModel("链接引用")
    public static class Ref {
        @ApiModelProperty("文档ID")
        private Long docId;
        @ApiModelProperty("slug")
        private String slug;
        @ApiModelProperty("标题")
        private String title;
        @ApiModelProperty("关系类型")
        private String relationType;

        public Ref(Long docId, String slug, String title, String relationType) {
            this.docId = docId;
            this.slug = slug;
            this.title = title;
            this.relationType = relationType;
        }
    }
}
