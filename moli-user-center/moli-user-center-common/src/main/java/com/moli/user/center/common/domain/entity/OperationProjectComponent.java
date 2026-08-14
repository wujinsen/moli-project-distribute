package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("operation_project_component")
public class OperationProjectComponent {

    @TableId
    private Long id;
    private Long projectId;
    private Long componentId;
    private String remark;
}
