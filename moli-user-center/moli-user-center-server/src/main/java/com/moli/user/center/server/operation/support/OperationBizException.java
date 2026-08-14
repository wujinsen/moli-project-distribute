package com.moli.user.center.server.operation.support;

import com.moli.common.enums.ResponseCodeEnums;
import com.moli.common.exception.BaseException;

/**
 * 运维模块业务异常（带稳定 HTTP 业务码）。
 */
public final class OperationBizException {

    public static final int CODE_DUPLICATE_IP = 10101;
    public static final int CODE_SERVER_NOT_FOUND = 10102;
    public static final int CODE_ENTITY_NOT_FOUND = 10103;
    public static final int CODE_MISSING_ID = 10104;
    public static final int CODE_SSH_NOT_CONFIGURED = 10105;
    public static final int CODE_DEPLOY_DISABLED = 10106;
    public static final int CODE_SERVER_TASK_RUNNING = 10107;
    public static final int CODE_UPLOAD_DISABLED = 10108;
    public static final int CODE_LOCAL_DEPLOY_DISABLED = 10109;

    private OperationBizException() {
    }

    public static BaseException duplicateIp(String ip, Integer environment) {
        return new BaseException(CODE_DUPLICATE_IP,
                "OPERATION_DUPLICATE_IP: 环境 " + environment + " 下 IP 已存在: " + ip);
    }

    public static BaseException serverNotFound(Long serverId) {
        return new BaseException(CODE_SERVER_NOT_FOUND,
                "OPERATION_SERVER_NOT_FOUND: 服务器不存在: " + serverId);
    }

    public static BaseException missingId() {
        return new BaseException(CODE_MISSING_ID, "OPERATION_MISSING_ID: 更新时 id 不能为空");
    }

    public static BaseException notFound(String entity, Long id) {
        return new BaseException(CODE_ENTITY_NOT_FOUND, entity + "不存在: " + id);
    }

    public static BaseException params(String message) {
        return new BaseException(ResponseCodeEnums.PARAMS_ERROR_CODE.getCode(), message);
    }

    public static BaseException sshNotConfigured(Long serverId) {
        return new BaseException(CODE_SSH_NOT_CONFIGURED,
                "OPERATION_SSH_NOT_CONFIGURED: 服务器 " + serverId + " 未配置 SSH 认证方式");
    }

    public static BaseException deployDisabled() {
        return new BaseException(CODE_DEPLOY_DISABLED,
                "OPERATION_DEPLOY_DISABLED: 部署变更动作未启用，请配置 ops.deploy.enabled=true");
    }

    public static BaseException uploadDisabled() {
        return new BaseException(CODE_UPLOAD_DISABLED,
                "OPERATION_UPLOAD_DISABLED: 文件上传发布未启用，请配置 ops.upload.enabled=true");
    }

    public static BaseException serverTaskRunning(Long serverId) {
        return new BaseException(CODE_SERVER_TASK_RUNNING,
                "OPERATION_SERVER_TASK_RUNNING: 服务器 " + serverId + " 仍有进行中的运维任务");
    }

    public static BaseException localDeployDisabled() {
        return new BaseException(CODE_LOCAL_DEPLOY_DISABLED,
                "OPERATION_LOCAL_DEPLOY_DISABLED: 本机部署未启用，请指定 serverId 或配置 ops.deploy.allow-local=true");
    }
}
