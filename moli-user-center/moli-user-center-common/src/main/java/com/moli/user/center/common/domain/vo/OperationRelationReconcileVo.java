package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("运维关联主表对齐结果")
public class OperationRelationReconcileVo {

    @ApiModelProperty("修正的项目台账数")
    private int projectsFixed;

    @ApiModelProperty("修正的组件台账数")
    private int componentsFixed;

    @ApiModelProperty("修正明细（project:{id} 或 component:{id}）")
    private List<String> details = new ArrayList<>();
}
