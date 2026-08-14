package com.moli.user.center.server.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperationProjectComponentLinkMapper extends BaseMapper<OperationProjectComponent> {

    @Select("SELECT component_id FROM operation_project_component WHERE project_id = #{projectId}")
    List<Long> selectComponentIdsByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT project_id FROM operation_project_component WHERE component_id = #{componentId}")
    List<Long> selectProjectIdsByComponentId(@Param("componentId") Long componentId);
}
