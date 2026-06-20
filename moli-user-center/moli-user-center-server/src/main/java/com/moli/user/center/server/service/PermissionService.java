package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.vo.CapabilitiesVo;

import java.util.Set;

public interface PermissionService {

    Set<String> getPermissionsByUserId(Long userId, String userName);

    CapabilitiesVo buildCapabilities(Long userId, String userName);
}
