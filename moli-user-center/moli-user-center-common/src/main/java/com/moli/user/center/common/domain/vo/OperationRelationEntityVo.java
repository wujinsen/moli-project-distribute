package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationRelationEntityVo {

    @ApiModelProperty("server | project | component")
    private String entityType;
    private Long id;
    private String name;
    private Integer environment;
}
