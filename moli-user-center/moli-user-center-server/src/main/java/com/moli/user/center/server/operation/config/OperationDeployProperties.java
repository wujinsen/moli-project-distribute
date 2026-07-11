package com.moli.user.center.server.operation.config;

import com.moli.user.center.common.domain.dto.operation.OperationDeployServiceEntry;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ops.deploy")
public class OperationDeployProperties {

    /**
     * 是否允许 start/stop/restart（默认仅 status/logs 只读）。
     */
    private boolean enabled = false;

    /**
     * 部署根目录，对应 moli-service.sh 的 MOLI_DEPLOY_ROOT。
     */
    private String deployRoot = "/opt/moli-project-distribute";

    /**
     * 脚本路径；默认 ${deployRoot}/deploy/linux/moli-service.sh
     */
    private String scriptPath = "";

    /**
     * 脚本执行超时（秒）。
     */
    private int timeoutSeconds = 15;

    /**
     * 项目 deploy_running 同步方式：ssh（默认）| local | off。
     * <p>ssh：有 serverId 时 SSH 远程 status；无 serverId 时在 Linux 本机查脚本。</p>
     */
    private String statusSyncMode = OperationDeployStatusSyncMode.SSH;

    /**
     * 可部署服务列表；空则使用内置 user-center / gateway / knowledge。
     */
    private List<OperationDeployServiceEntry> services;

    /**
     * 是否允许 serverId 为空时在本机执行 moli-service.sh（默认 false，生产应关）。
     */
    private boolean allowLocal = false;
}
