package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ops.health")
public class OperationHealthProperties {

    /**
     * 是否启用定时探活与部署状态同步。
     */
    private boolean probeEnabled = true;

    /**
     * Spring cron，默认每 15 分钟。
     */
    private String probeCron = "0 0/15 * * * ?";
}
