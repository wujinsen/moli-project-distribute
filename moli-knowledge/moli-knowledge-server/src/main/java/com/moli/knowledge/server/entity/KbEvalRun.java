package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("kb_eval_run")
@ApiModel("知识库评测回归记录")
public class KbEvalRun implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("报告 time")
    private Date runAt;

    @ApiModelProperty("ngram/hybrid/hybrid-rerank")
    private String strategy;

    @ApiModelProperty("检索式0/生成式1")
    private Integer useLlm;

    @ApiModelProperty("golden 题数")
    private Integer goldenTotal;

    @ApiModelProperty("可答题数")
    private Integer answerableTotal;

    @ApiModelProperty("negative 题数")
    private Integer negativeTotal;

    @ApiModelProperty("HTTP/请求错误数")
    private Integer errors;

    private BigDecimal hit1;
    private BigDecimal hit3;
    private BigDecimal hit5;
    private BigDecimal hit8;
    private BigDecimal mrr;
    private BigDecimal coverage;
    private BigDecimal refusalAccuracy;

    @ApiModelProperty("P95 延迟毫秒")
    private Integer p95Ms;

    @ApiModelProperty("by_difficulty JSON")
    private String byDifficultyJson;

    @ApiModelProperty("报告相对路径")
    private String reportPath;

    @ApiModelProperty("关联 git 提交")
    private String gitSha;

    @ApiModelProperty("门禁是否通过")
    private Integer gatePass;

    @ApiModelProperty("落库时间")
    private Date createTime;
}
