package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.vo.RoleAuthVo;

import java.util.List;

public interface RoleAuthService {

    void assignRoleAuth(Long roleId, List<Long> menuIds, List<String> actionCodes);

    RoleAuthVo getRoleAuth(Long roleId);
}
