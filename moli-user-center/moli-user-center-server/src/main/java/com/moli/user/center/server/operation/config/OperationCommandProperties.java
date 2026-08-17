package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 远程命令执行参数（SVR-18）。
 *
 * <p>{@code enabled} 仍绑定 yaml 供 Environment 回落，但<strong>调用方开关判定</strong>须走
 * {@code ConfigService.getBoolean(ConfigKey.OPS_COMMAND_ENABLED)}，否则「参数设置」无法热关停。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ops.command")
public class OperationCommandProperties {

    /**
     * yaml / Environment 默认值（部署期）。运行期覆盖请走 sys_config + ConfigService，勿在业务里读本字段做门禁。
     */
    private boolean enabled = false;

    /** 单条命令最大字符数。 */
    private int maxChars = 8192;

    /** 未指定 workDir 时的默认目录。 */
    private String defaultWorkDir = "/opt/moli-project-distribute";
}
