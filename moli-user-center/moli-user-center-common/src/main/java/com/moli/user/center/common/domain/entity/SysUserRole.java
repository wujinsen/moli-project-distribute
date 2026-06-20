package com.moli.user.center.common.domain.entity;

import com.moli.common.core.BaseEntity;
import lombok.Data;

@Data
public class SysUserRole {

    private Long id;

    private Long userId;

    private Long roleId;
}
