package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("kb_agentic_trace")
@ApiModel("Agentic RAG 编排 trace")
public class KbAgenticTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("关联 kb_qa_log.id")
    private Long qaLogId;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("提问用户ID")
    private Long userId;

    @ApiModelProperty("原始问题")
    private String question;

    @ApiModelProperty("改写后主问")
    private String rewritten;

    @ApiModelProperty("拆解子问题 JSON")
    private String subQuestionsJson;

    @ApiModelProperty("实际轮次")
    private Integer rounds;

    @ApiModelProperty("每轮步骤 JSON")
    private String stepsJson;

    @ApiModelProperty("末轮 coverage")
    private Double coverage;

    @ApiModelProperty("是否降级")
    private Boolean degraded;

    @ApiModelProperty("总耗时毫秒")
    private Long latencyMs;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
