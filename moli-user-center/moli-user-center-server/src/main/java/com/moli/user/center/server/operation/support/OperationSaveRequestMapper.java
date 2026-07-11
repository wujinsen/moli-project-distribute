package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.dto.operation.OperationComponentSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationPlatformSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationProjectSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationServerSaveRequest;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;

/**
 * SaveRequest → Entity 字段映射（不含密码加密 / server 绑定）。
 */
public final class OperationSaveRequestMapper {

    private OperationSaveRequestMapper() {
    }

    public static OperationPlatformInfo toEntity(OperationPlatformSaveRequest req) {
        OperationPlatformInfo row = new OperationPlatformInfo();
        row.setId(req.getId());
        row.setPlatformName(trim(req.getPlatformName()));
        row.setUrl(trim(req.getUrl()));
        row.setAccount(trim(req.getAccount()));
        row.setEnvironment(req.getEnvironment());
        row.setRemark(trim(req.getRemark()));
        return row;
    }

    public static OperationServerInfo toEntity(OperationServerSaveRequest req) {
        OperationServerInfo row = new OperationServerInfo();
        row.setId(req.getId());
        row.setServerName(trim(req.getServerName()));
        row.setIp(trim(req.getIp()));
        row.setInnerIp(trim(req.getInnerIp()));
        row.setPort(trim(req.getPort()));
        row.setEnvironment(req.getEnvironment());
        row.setRemark(trim(req.getRemark()));
        return row;
    }

    public static OperationProjectDeployInfo toEntity(OperationProjectSaveRequest req) {
        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setId(req.getId());
        row.setServerId(req.getServerId());
        row.setServerIp(trim(req.getServerIp()));
        row.setInnerIp(trim(req.getInnerIp()));
        row.setUrl(trim(req.getUrl()));
        row.setProjectName(trim(req.getProjectName()));
        row.setDeployPath(trim(req.getDeployPath()));
        row.setPort(trim(req.getPort()));
        row.setEnvironment(req.getEnvironment());
        row.setRemark(trim(req.getRemark()));
        return row;
    }

    public static OperationComponentDeployInfo toEntity(OperationComponentSaveRequest req) {
        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setId(req.getId());
        row.setServerId(req.getServerId());
        row.setServerIp(trim(req.getServerIp()));
        row.setComponentName(trim(req.getComponentName()));
        row.setAccount(trim(req.getAccount()));
        row.setDeployPath(trim(req.getDeployPath()));
        row.setPort(trim(req.getPort()));
        row.setVersion(trim(req.getVersion()));
        row.setEnvironment(req.getEnvironment());
        row.setRemark(trim(req.getRemark()));
        return row;
    }

    public static String password(OperationPlatformSaveRequest req) {
        return req.getPassword();
    }

    public static String password(OperationComponentSaveRequest req) {
        return req.getPassword();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
