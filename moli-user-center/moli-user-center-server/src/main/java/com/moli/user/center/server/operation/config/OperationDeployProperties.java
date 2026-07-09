package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
}
