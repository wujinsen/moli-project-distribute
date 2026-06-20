package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moli.user.center.common.domain.entity.SysUserRole;
import com.moli.user.center.server.mapper.SysUserRoleMapper;
import com.moli.user.center.server.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements UserRoleService {

}
