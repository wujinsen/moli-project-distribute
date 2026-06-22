package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("知识库目录树（按知识类型分组）")
public class IndexTreeVo {

    @ApiModelProperty("文档总数")
    private int total;

    @ApiModelProperty("按类型分组")
    private List<Group> groups = new ArrayList<>();

    @Data
    @ApiModel("类型分组")
    public static class Group {
        @ApiModelProperty("类型 guide/service/concept/article/interview/output")
        private String type;
        @ApiModelProperty("类型中文名")
        private String label;
        @ApiModelProperty("该类型下文档")
        private List<Item> items = new ArrayList<>();

        public Group(String type, String label) {
            this.type = type;
            this.label = label;
        }
    }

    @Data
    @ApiModel("目录项")
    public static class Item {
        @ApiModelProperty("文档ID")
        private Long id;
        @ApiModelProperty("slug")
        private String slug;
        @ApiModelProperty("标题")
        private String title;
        @ApiModelProperty("摘要")
        private String summary;

        public Item(Long id, String slug, String title, String summary) {
            this.id = id;
            this.slug = slug;
            this.title = title;
            this.summary = summary;
        }
    }
}
