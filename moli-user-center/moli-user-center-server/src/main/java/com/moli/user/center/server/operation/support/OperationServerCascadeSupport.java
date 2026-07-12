package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerComponent;
import com.moli.user.center.common.domain.entity.OperationServerProject;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 删除服务器/项目/组件时的 N:N 与 server_id 级联清理。
 */
@Component
public class OperationServerCascadeSupport {

    @Resource
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Resource
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;
    @Resource
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    public void onDeleteServer(Long serverId) {
        if (serverId == null) {
            return;
        }
        LambdaQueryWrapper<OperationServerProject> deleteProjects = new LambdaQueryWrapper<>();
        deleteProjects.eq(OperationServerProject::getServerId, serverId);
        operationServerProjectLinkMapper.delete(deleteProjects);

        LambdaQueryWrapper<OperationServerComponent> deleteComponents = new LambdaQueryWrapper<>();
        deleteComponents.eq(OperationServerComponent::getServerId, serverId);
        operationServerComponentLinkMapper.delete(deleteComponents);

        UpdateWrapper<OperationProjectDeployInfo> clearProjects = new UpdateWrapper<>();
        clearProjects.eq("server_id", serverId).set("server_id", null);
        operationProjectDeployInfoMapper.update(null, clearProjects);

        UpdateWrapper<OperationComponentDeployInfo> clearComponents = new UpdateWrapper<>();
        clearComponents.eq("server_id", serverId).set("server_id", null);
        operationComponentDeployInfoMapper.update(null, clearComponents);
    }

    public void onDeleteProject(Long projectId) {
        if (projectId == null) {
            return;
        }
        LambdaQueryWrapper<OperationServerProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationServerProject::getProjectId, projectId);
        operationServerProjectLinkMapper.delete(wrapper);

        LambdaQueryWrapper<OperationProjectComponent> pc = new LambdaQueryWrapper<>();
        pc.eq(OperationProjectComponent::getProjectId, projectId);
        operationProjectComponentLinkMapper.delete(pc);
    }

    public void onDeleteComponent(Long componentId) {
        if (componentId == null) {
            return;
        }
        LambdaQueryWrapper<OperationServerComponent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationServerComponent::getComponentId, componentId);
        operationServerComponentLinkMapper.delete(wrapper);

        LambdaQueryWrapper<OperationProjectComponent> pc = new LambdaQueryWrapper<>();
        pc.eq(OperationProjectComponent::getComponentId, componentId);
        operationProjectComponentLinkMapper.delete(pc);
    }
}
