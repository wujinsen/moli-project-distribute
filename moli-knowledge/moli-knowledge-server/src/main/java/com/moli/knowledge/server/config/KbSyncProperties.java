package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    /** 是否启用 Sync 并发锁（按 space_code，Redis SET NX）。 */
    private boolean lockEnabled = true;

    /** 锁 TTL 相对 timeout 的额外秒数（防脚本 hang 后锁永不过期）。 */
    private int lockExtraSeconds = 60;

    /**
     * 定时同步的空间 code 列表；为空时使用 {@code kb.wiki.space-dirs} 的全部 key（两空间 sync-all）。
     */
    private List<String> scheduleSpaceCodes = new ArrayList<>();

    /** KBOPS-5 · Sync 失败 webhook 告警。 */
    private Alert alert = new Alert();

    @Data
    public static class Alert {

        /** 是否启用失败告警。 */
        private boolean enabled = false;

        /**
         * Webhook 类型：{@code feishu}（默认）、{@code wecom}（企业微信）、{@code generic}（自定义 JSON）。
         */
        private String type = "feishu";

        /** 机器人 Webhook URL（飞书/企业微信群机器人）。 */
        private String webhookUrl = "";

        /** 仅定时 Sync 失败时告警（默认 true；false 时手动/编辑后失败也告警）。 */
        private boolean scheduledOnly = true;

        /** HTTP 超时（秒）。 */
        private int timeoutSeconds = 10;

        /** 告警正文是否附带脚本输出摘要。 */
        private boolean includeOutputTail = true;

        /** 输出摘要最大字符数（取末尾）。 */
        private int outputTailMaxChars = 500;
    }
}
