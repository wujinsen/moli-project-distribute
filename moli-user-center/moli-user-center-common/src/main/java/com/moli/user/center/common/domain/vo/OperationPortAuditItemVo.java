package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationPortAuditItemVo {

    private Long id;

    @ApiModelProperty("project | component")
    private String recordType;

    private String name;
    private String actualPort;
    private String expectedPort;
    private String matrixKey;

    @ApiModelProperty("0未映射 1一致 2不符 3跳过")
    private Integer portMatchStatus;

    private String message;
    private Integer environment;
}
