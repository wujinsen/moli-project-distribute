package com.moli.user.center.api;

import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.SysUser;

import java.util.Set;

/**
 * 用户中心 Dubbo 契约。
 * <p>
 * 单点登录在 user-center-server 完成；业务服务（order/bi）仅消费本接口做会话校验与授权，不在本地登录。
 */
public interface UserCenterServer {

    /**
     * 根据用户名查用户（由 user-center-server 登录流程内部使用，业务服务勿用于 subject.login）。
     */
    MoliResult<SysUser> getInfoByUserName(String userName);

    /**
     * 根据用户 ID 查用户（业务服务每次请求校验账号是否仍有效）。
     */
    MoliResult<SysUser> getUserById(Long userId);

    /**
     * 获取用户权限标识（业务服务 Shiro 授权）。
     */
    MoliResult<Set<String>> getPermissionsByUserId(Long userId, String userName);
}
