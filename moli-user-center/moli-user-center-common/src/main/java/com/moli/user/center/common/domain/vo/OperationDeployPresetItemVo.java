package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationDeployPresetItemVo {

    @ApiModelProperty("postAction 值")
    private String value;

    @ApiModelProperty("展示名称")
    private String label;
}
