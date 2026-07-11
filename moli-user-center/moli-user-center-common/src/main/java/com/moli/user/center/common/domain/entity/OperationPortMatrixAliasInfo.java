package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("operation_port_matrix_alias")
public class OperationPortMatrixAliasInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("operation_port_matrix.id")
    private Long matrixId;

    @ApiModelProperty("别名，全局唯一")
    private String alias;

    private Date createTime;
}
