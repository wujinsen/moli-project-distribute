package com.moli.user.center.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.user.center.common.domain.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeptMapper extends BaseMapper<SysDept> {
}
