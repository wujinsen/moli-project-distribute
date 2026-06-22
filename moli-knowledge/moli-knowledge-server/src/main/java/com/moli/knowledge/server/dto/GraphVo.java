package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库关系图谱（对齐 kb/tools/serve.py 的 /api/graph）。
 * 节点 = 文档；连线 = 正文 [[标题]] 引用（links_to） + 同标签关联（same_tag）。
 */
@Data
@ApiModel("知识图谱")
public class GraphVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("节点")
    private List<Node> nodes = new ArrayList<>();

    @ApiModelProperty("连线")
    private List<Link> links = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("图谱节点")
    public static class Node implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("文档ID")
        private String id;

        @ApiModelProperty("标题")
        private String title;

        @ApiModelProperty("分组（分类名，缺省取状态/未分类）")
        private String type;

        @ApiModelProperty("度数 = 出度 + 入度")
        private int deg;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("图谱连线")
    public static class Link implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("源节点ID")
        private String source;

        @ApiModelProperty("目标节点ID")
        private String target;

        @ApiModelProperty("关系类型 links_to / same_tag")
        private String type;
    }
}
