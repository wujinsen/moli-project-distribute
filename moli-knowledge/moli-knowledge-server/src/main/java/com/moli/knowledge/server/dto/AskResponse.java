package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("知识库提问响应")
public class AskResponse {

    @ApiModelProperty("答案（生成式或检索式）")
    private String answer;

    @ApiModelProperty("模式 generative/retrieval")
    private String mode;

    @ApiModelProperty("识别的检索作用域，如 [article, concept] 或 全部类型")
    private String scope;

    @ApiModelProperty("作用域判定理由")
    private String scopeReason;

    @ApiModelProperty("LLM 提供方")
    private String provider;

    @ApiModelProperty("模型名")
    private String model;

    @ApiModelProperty("引用来源")
    private List<Citation> citations = new ArrayList<>();

    @ApiModelProperty("本次问答日志ID（用于提交反馈）")
    private Long qaLogId;

    @Data
    @ApiModel("引用")
    public static class Citation {
        @ApiModelProperty("文档ID")
        private Long docId;
        @ApiModelProperty("所属空间ID")
        private Long spaceId;
        @ApiModelProperty("slug")
        private String slug;
        @ApiModelProperty("标题")
        private String title;
        @ApiModelProperty("知识类型")
        private String kbType;
        @ApiModelProperty("命中片段")
        private String snippet;

        public Citation() {
        }

        public Citation(Long docId, Long spaceId, String slug, String title, String kbType, String snippet) {
            this.docId = docId;
            this.spaceId = spaceId;
            this.slug = slug;
            this.title = title;
            this.kbType = kbType;
            this.snippet = snippet;
        }
    }
}
