package com.moli.user.center.server.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.vo.UserVo;
import com.moli.common.page.PageRes;

public interface UserService{

    PageRes<UserVo> list(UserVo userVo);

    SysUser resolveCurrentUser();

    void applyPrivilegedUserVisibility(LambdaQueryWrapper<SysUser> wrapper);

    boolean canViewUser(SysUser target);

    SysUser getInfoByUserName(String userName);

}
