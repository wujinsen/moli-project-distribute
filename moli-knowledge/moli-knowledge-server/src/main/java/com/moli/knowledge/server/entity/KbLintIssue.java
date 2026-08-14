package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("kb_lint_issue")
@ApiModel("知识体检问题")
public class KbLintIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("相关文档ID")
    private Long documentId;

    @ApiModelProperty("问题类型 broken_link/orphan/no_summary/duplicate/stale/conflict")
    private String issueType;

    @ApiModelProperty("问题详情")
    private String detail;

    @ApiModelProperty("0待处理 1已忽略 2已修复")
    private Integer status;

    @ApiModelProperty("处理人用户ID")
    private Long assigneeId;

    @ApiModelProperty("0普通 1高 2紧急")
    private Integer priority;

    @ApiModelProperty("扫描时间")
    private Date scanTime;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
