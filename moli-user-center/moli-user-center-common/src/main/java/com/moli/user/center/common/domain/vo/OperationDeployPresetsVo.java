package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 部署中心预设（SVR-20）：常用路径与快捷后置动作。
 */
@Data
public class OperationDeployPresetsVo {

    @ApiModelProperty("常用上传目标路径")
    private List<String> pathPresets;

    @ApiModelProperty("快捷后置动作")
    private List<OperationDeployPresetItemVo> actionPresets;

    @ApiModelProperty("可部署服务（与 ops.deploy.services 一致）")
    private List<OperationDeployServiceOptionVo> serviceKeys;
}
