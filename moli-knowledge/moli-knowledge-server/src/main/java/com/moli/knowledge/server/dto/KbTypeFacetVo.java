package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 体裁 facet：当前作用域（空间 + 可选分类）下各 kb_type 的已发布文档计数。
 * 供文档浏览的「体裁 chip」展示带数字的过滤项。
 */
@Data
@ApiModel("体裁 facet 计数")
public class KbTypeFacetVo {

    @ApiModelProperty("各体裁计数（仅含 count>0 的体裁，按白名单顺序）")
    private List<Item> items = new ArrayList<>();

    @ApiModelProperty("作用域内已发布文档总数")
    private long total;

    @Data
    @ApiModel("体裁 facet 项")
    public static class Item {
        @ApiModelProperty("体裁值：guide/service/concept/article/interview/output")
        private String kbType;

        @ApiModelProperty("中文展示名")
        private String label;

        @ApiModelProperty("该体裁文档数")
        private long count;

        public Item() {
        }

        public Item(String kbType, String label, long count) {
            this.kbType = kbType;
            this.label = label;
            this.count = count;
        }
    }
}
