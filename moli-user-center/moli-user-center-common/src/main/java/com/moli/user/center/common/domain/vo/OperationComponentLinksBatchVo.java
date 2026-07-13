package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class OperationComponentLinksBatchVo {

    @ApiModelProperty("各组件的 N:N 服务器关联")
    private List<OperationComponentLinksVo> items;
}
