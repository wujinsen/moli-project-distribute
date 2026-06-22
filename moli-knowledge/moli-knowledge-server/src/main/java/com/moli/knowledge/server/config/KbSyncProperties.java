package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * kb → MySQL 同步脚本触发配置（调用 {@code kb/tools/sync_to_db.py}）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.sync")
public class KbSyncProperties {

    /** 是否允许通过 API 触发同步。 */
    private boolean enabled = true;

    /** Python 可执行文件。 */
    private String python = "python";

    /** 同步脚本路径（相对项目根或绝对路径）。 */
    private String scriptPath = "moli-knowledge/kb/tools/sync_to_db.py";

    /** 默认同步的 kb_space.space_code。 */
    private String spaceCode = "enterprise-kb";

    /** 同步超时（秒）。 */
    private int timeoutSeconds = 300;

    /** 是否启用定时同步（默认关，生产按需开）。 */
    private boolean scheduleEnabled = false;

    /** 定时 cron，默认每天 02:00。 */
    private String scheduleCron = "0 0 2 * * ?";
}
