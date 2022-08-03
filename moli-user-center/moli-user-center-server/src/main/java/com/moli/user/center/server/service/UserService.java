package com.moli.user.center.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface UserService{

    /**
     * 根据用户名查询用户信息
     */
    public User getInfoByUserName(@PathVariable("userName") String userName);

}
