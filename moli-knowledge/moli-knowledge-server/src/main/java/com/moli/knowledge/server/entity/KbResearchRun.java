package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("kb_research_run")
@ApiModel("DeepResearch 运行 trace")
public class KbResearchRun implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("对外 runId")
    private String runId;

    @ApiModelProperty("用户 ID")
    private Long userId;

    @ApiModelProperty("ACL 空间 JSON")
    private String spaceIdsJson;

    @ApiModelProperty("脱敏主题")
    private String topic;

    @ApiModelProperty("PENDING|RUNNING|SUCCEEDED|FAILED|DEGRADED")
    private String status;

    private Boolean degraded;
    private String degradeReason;

    private String outlineJson;
    private String sectionsJson;
    private String citationsJson;
    private Double coverage;
    private String reportMd;

    private Long ingestJobId;
    private Long latencyMs;

    private Date createTime;
    private Date updateTime;
}
