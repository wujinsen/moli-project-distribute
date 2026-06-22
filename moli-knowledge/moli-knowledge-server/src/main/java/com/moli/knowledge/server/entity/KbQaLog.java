package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("kb_qa_log")
@ApiModel("知识问答日志")
public class KbQaLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("提问用户ID")
    private Long userId;

    @ApiModelProperty("问题")
    private String question;

    @ApiModelProperty("答案")
    private String answer;

    @ApiModelProperty("引用来源 JSON [{docId,title,slug,snippet}]")
    private String citations;

    @ApiModelProperty("识别的检索作用域 type/tags")
    private String scope;

    @ApiModelProperty("LLM 提供方 deepseek/qwen/glm")
    private String provider;

    @ApiModelProperty("模型名")
    private String model;

    @ApiModelProperty("输入 token")
    private Integer promptTokens;

    @ApiModelProperty("输出 token")
    private Integer completionTokens;

    @ApiModelProperty("反馈 1有用 0无用 NULL未评")
    private Integer useful;

    @ApiModelProperty("提问时间")
    private Date createTime;
}
