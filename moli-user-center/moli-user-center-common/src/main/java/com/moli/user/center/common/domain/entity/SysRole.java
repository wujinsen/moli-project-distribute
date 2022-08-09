package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SysRole extends BaseEntity {


    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @ApiModelProperty(value = "排序")
    private String orderNum;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("角色状态（1正常 0停用）")
    private Integer status;

}
