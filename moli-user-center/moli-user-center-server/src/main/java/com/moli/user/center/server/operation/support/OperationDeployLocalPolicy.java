package com.moli.user.center.server.operation.support;

import com.moli.user.center.server.operation.config.OperationDeployProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 本机部署回退策略：仅当 {@code ops.deploy.allow-local=true} 且未指定 serverId 时允许。
 */
@Component
public class OperationDeployLocalPolicy {

    @Resource
    private OperationDeployProperties deployProperties;

    public void requireAllowLocalWhenNoServer(Long serverId) {
        if (serverId == null) {
            requireAllowLocal();
        }
    }

    public void requireAllowLocal() {
        if (!deployProperties.isAllowLocal()) {
            throw OperationBizException.localDeployDisabled();
        }
    }
}
