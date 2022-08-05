package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.entity.SysUser;
import org.springframework.web.bind.annotation.PathVariable;

public interface UserService{

    /**
     * 根据用户名查询用户信息
     */
    public SysUser getInfoByUserName(@PathVariable("userName") String userName);

}
