package com.moli.user.center.server.operation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperationServerLinkMapper {

    @Select("SELECT project_id FROM operation_server_project WHERE server_id = #{serverId}")
    List<Long> selectProjectIdsByServerId(@Param("serverId") Long serverId);

    @Select("SELECT component_id FROM operation_server_component WHERE server_id = #{serverId}")
    List<Long> selectComponentIdsByServerId(@Param("serverId") Long serverId);

    @Select("SELECT server_id FROM operation_server_project WHERE project_id = #{projectId}")
    List<Long> selectServerIdsByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT server_id FROM operation_server_component WHERE component_id = #{componentId}")
    List<Long> selectServerIdsByComponentId(@Param("componentId") Long componentId);

    @Select("SELECT DISTINCT project_id FROM operation_server_project")
    List<Long> selectDistinctProjectIdsWithLinks();

    @Select("SELECT DISTINCT component_id FROM operation_server_component")
    List<Long> selectDistinctComponentIdsWithLinks();
}
