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

    /**
     * TCP 探活并行度（有界线程池大小）。
     */
    private int probeParallelism = 8;

    /**
     * 单次 probe-all 等待全部 TCP 探测完成的最长时间（秒）。
     */
    private int probeTimeoutSeconds = 120;
}
