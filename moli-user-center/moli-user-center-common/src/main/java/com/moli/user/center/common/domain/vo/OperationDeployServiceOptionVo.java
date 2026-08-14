package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationDeployServiceOptionVo {

    @ApiModelProperty("moli-service.sh serviceKey")
    private String key;

    @ApiModelProperty("展示名")
    private String label;
}
