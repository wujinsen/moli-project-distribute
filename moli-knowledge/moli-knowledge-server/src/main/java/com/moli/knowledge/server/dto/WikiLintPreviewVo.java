package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Wiki 保存前 lint 预检结果")
public class WikiLintPreviewVo {

    @ApiModelProperty("问题条数")
    private int issueCount;

    @ApiModelProperty("问题列表")
    private List<Item> issues = new ArrayList<>();

    @Data
    @ApiModel("预检问题项")
    public static class Item {
        @ApiModelProperty("broken_link / missing_frontmatter / empty_sources")
        private String type;
        @ApiModelProperty("说明")
        private String message;

        public Item() {
        }

        public Item(String type, String message) {
            this.type = type;
            this.message = message;
        }
    }
}
