package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 远程命令执行参数（SVR-18）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ops.command")
public class OperationCommandProperties {

    /** 是否允许通过系统执行远程 shell 命令。 */
    private boolean enabled = false;

    /** 单条命令最大字符数。 */
    private int maxChars = 8192;

    /** 未指定 workDir 时的默认目录。 */
    private String defaultWorkDir = "/opt/moli-project-distribute";
}
