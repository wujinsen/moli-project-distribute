package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationRelationProjectItemVo {

    private Long id;
    private String projectName;
    private String port;
    private Integer environment;

    @ApiModelProperty("部署进程是否运行")
    private Boolean deployRunning;

    @ApiModelProperty("0未映射 1一致 2不符 3跳过")
    private Integer portMatchStatus;
}
