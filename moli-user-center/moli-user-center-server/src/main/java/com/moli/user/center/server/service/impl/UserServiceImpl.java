package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.server.mapper.UserMapper;
import com.moli.user.center.server.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 根据用户名查询用户信息
     */
    public SysUser getInfoByUserName(@PathVariable("userName") String userName) {
        return userMapper.selectOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName, userName).eq(SysUser::getIsDelete, CommonConstant.UN_DELETE));
    }

}
