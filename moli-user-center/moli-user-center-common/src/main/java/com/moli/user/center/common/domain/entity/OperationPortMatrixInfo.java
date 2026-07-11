package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("operation_port_matrix")
public class OperationPortMatrixInfo extends BaseEntity {

    @ApiModelProperty("矩阵主键，如 user-center")
    private String matrixKey;

    @ApiModelProperty("展示名")
    private String displayName;

    @ApiModelProperty("期望端口")
    private String expectedPort;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("是否启用")
    private Boolean enabled;

    @ApiModelProperty("来源说明")
    private String source;

    private String remark;
}
