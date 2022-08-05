package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moli.user.center.common.domain.entity.SysUserRole;
import com.moli.user.center.server.mapper.UserRoleMapper;
import com.moli.user.center.server.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, SysUserRole> implements UserRoleService {

}
