package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSH 远程运维连接参数（SVR-13~16）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ops.ssh")
public class OperationSshProperties {

    /** 连接超时（毫秒）。 */
    private int connectTimeoutMs = 5000;

    /** 单条命令执行超时（秒）。 */
    private int commandTimeoutSeconds = 120;

    /** 默认 SSH 端口。 */
    private int defaultPort = 22;

    /** 默认登录用户。 */
    private String defaultUser = "ubuntu";

    /** 是否跳过 host key 校验（内网自建服务器默认 true）。 */
    private boolean strictHostKeyChecking = false;
}
