package com.moli.user.center.common.domain.vo;

import com.moli.user.center.common.domain.entity.SysRole;
import com.moli.user.center.common.domain.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleVo {

    private SysUser user;

    private Long UserId;

    private List<SysRole> sysRoleList;

}
