package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("operation_server_project")
public class OperationServerProject {

    @TableId
    private Long id;
    private Long serverId;
    private Long projectId;
}
