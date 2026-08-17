package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 运维健康探活参数。
 *
 * <p>{@code probeEnabled} 的运行期门禁走 {@code ConfigService}（{@code ConfigKey.OPS_HEALTH_PROBE_ENABLED}）；
 * cron / 并行度等调度节奏仍读本类 yaml 绑定值。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ops.health")
public class OperationHealthProperties {

    /**
     * yaml / Environment 默认值。定时任务是否执行请读 ConfigService，勿直接用本字段做门禁。
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
