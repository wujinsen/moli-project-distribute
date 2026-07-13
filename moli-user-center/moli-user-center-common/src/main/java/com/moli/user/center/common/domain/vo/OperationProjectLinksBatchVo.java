package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class OperationProjectLinksBatchVo {

    @ApiModelProperty("各项目的 N:N 服务器关联")
    private List<OperationProjectLinksVo> items;
}
