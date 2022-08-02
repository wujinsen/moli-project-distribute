package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.MoliResult;
import com.moli.common.domain.entity.User;
import com.moli.user.center.server.mapper.UserMapper;
import com.moli.user.center.server.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 根据用户名查询用户信息
     */
    public User getInfoByUserName(@PathVariable("userName") String userName) {
        return userMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUserName, userName).eq(User::getIsDelete, CommonConstant.UN_DELETE));
    }

}
