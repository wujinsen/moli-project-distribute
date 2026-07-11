package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 部署任务与项目台账绑定：校验 projectId ↔ serviceKey ↔ serverId。
 */
@Component
public class OperationDeployTaskProjectSupport {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationDeployServiceRegistry deployServiceRegistry;

    public DeployTaskBinding resolve(OperationDeployTaskRequest request) {
        String key = deployServiceRegistry.requireKnownKey(request.getServiceKey());
        Long serverId = request.getServerId();
        Long projectId = request.getProjectId();
        if (projectId == null) {
            return new DeployTaskBinding(serverId, null, key);
        }

        OperationProjectDeployInfo project = operationProjectDeployInfoMapper.selectById(projectId);
        if (project == null) {
            throw OperationBizException.notFound("项目", projectId);
        }

        String mappedKey = deployServiceRegistry.resolveProjectName(project.getProjectName());
        if (mappedKey == null) {
            throw OperationBizException.params("项目「" + project.getProjectName() + "」无法映射到可部署 serviceKey");
        }
        if (!mappedKey.equals(key)) {
            throw OperationBizException.params(
                    "projectId 对应 serviceKey 为 " + mappedKey + "，与请求 " + key + " 不一致");
        }

        if (project.getServerId() != null) {
            if (serverId != null && !project.getServerId().equals(serverId)) {
                throw OperationBizException.params("serverId 与项目绑定的服务器不一致");
            }
            serverId = project.getServerId();
        } else if (serverId == null) {
            throw OperationBizException.params("项目未绑定服务器，请指定 serverId");
        }

        return new DeployTaskBinding(serverId, projectId, key);
    }

    @Getter
    public static final class DeployTaskBinding {
        private final Long serverId;
        private final Long projectId;
        private final String serviceKey;

        DeployTaskBinding(Long serverId, Long projectId, String serviceKey) {
            this.serverId = serverId;
            this.projectId = projectId;
            this.serviceKey = serviceKey;
        }
    }
}
