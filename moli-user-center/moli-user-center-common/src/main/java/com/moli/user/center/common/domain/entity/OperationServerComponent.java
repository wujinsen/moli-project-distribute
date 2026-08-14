package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("operation_server_component")
public class OperationServerComponent {

    @TableId
    private Long id;
    @ApiModelProperty("服务器ID")
    private Long serverId;
    @ApiModelProperty("组件ID")
    private Long componentId;
}
