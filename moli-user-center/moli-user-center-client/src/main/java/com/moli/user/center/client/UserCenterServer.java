package com.moli.user.center.client;

import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.SysUser;

public interface UserCenterServer {

    /**
     * 根据用户名获取用户
     */
    MoliResult<SysUser> getInfoByUserName(String userName);

}
