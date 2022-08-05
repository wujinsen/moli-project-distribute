package com.moli.user.center.server.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.user.center.common.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BaseMapper<SysUserRole> {
}
