package com.moli.user.center.server.provider;


import com.moli.common.core.MoliResult;
import com.moli.user.center.api.UserCenterServer;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.server.service.PermissionService;
import com.moli.user.center.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.Set;

@DubboService(version = "1.0.0", group = "moli", protocol = "dubbo")
@Slf4j
public class UserServerProvider implements UserCenterServer {

    @Resource
    private UserService userService;

    @Resource
    private PermissionService permissionService;

    @Override
    public MoliResult<SysUser> getInfoByUserName(String userName) {
        return MoliResult.success(userService.getInfoByUserName(userName));
    }

    @Override
    public MoliResult<SysUser> getUserById(Long userId) {
        return MoliResult.success(userService.getUserById(userId));
    }

    @Override
    public MoliResult<Set<String>> getPermissionsByUserId(Long userId, String userName) {
        return MoliResult.success(permissionService.getPermissionsByUserId(userId, userName));
    }
}
