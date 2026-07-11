package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OperationProjectVo {

    private Long id;
    private Long createId;
    private Date createTime;
    private Long updateId;
    private Date updateTime;

    private Long serverId;
    /** N:N 关联的服务器 ID（含主 serverId） */
    private List<Long> serverIds;
    private String serverIp;
    private String innerIp;
    private String url;
    private String projectName;
    private String deployPath;
    private String port;
    private Integer environment;
    private String remark;

    @ApiModelProperty("矩阵期望端口（可映射时）")
    private String expectedPort;
    @ApiModelProperty("0未映射 1一致 2不符 3跳过")
    private Integer portMatchStatus;

    @ApiModelProperty("部署进程是否运行（可映射 serviceKey 时由定时任务更新）")
    private Boolean deployRunning;
    private Date lastDeployCheckTime;
}
