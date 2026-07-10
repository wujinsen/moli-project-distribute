package com.moli.user.center.server.operation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 运维异步任务参数（SVR-14）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ops.task")
public class OperationTaskProperties {

    /** 线程池大小。 */
    private int poolSize = 2;

    /** 单任务日志最大字符数（超出截断头部）。 */
    private int logMaxChars = 1_000_000;

    /** 任务记录保留天数（清理用，暂留配置）。 */
    private int retentionDays = 7;
}
