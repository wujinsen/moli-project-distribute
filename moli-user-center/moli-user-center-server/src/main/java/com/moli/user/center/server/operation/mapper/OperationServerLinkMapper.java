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
}
