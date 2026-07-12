package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationRelationComponentItemVo {

    private Long id;
    private String componentName;
    private String port;
    private String version;
    private Integer environment;
    private Integer status;

    @ApiModelProperty("0未映射 1一致 2不符 3跳过")
    private Integer portMatchStatus;
}
