package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("问答历史条目")
public class QaHistoryVo {

    @ApiModelProperty("日志ID")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("问题")
    private String question;

    @ApiModelProperty("答案")
    private String answer;

    @ApiModelProperty("模式 generative/retrieval")
    private String mode;

    @ApiModelProperty("作用域")
    private String scope;

    @ApiModelProperty("LLM 提供方")
    private String provider;

    @ApiModelProperty("模型")
    private String model;

    @ApiModelProperty("引用")
    private List<AskResponse.Citation> citations = new ArrayList<>();

    @ApiModelProperty("反馈 1有用 0无用 null未评")
    private Integer useful;

    @ApiModelProperty("提问时间")
    private Date createTime;
}
