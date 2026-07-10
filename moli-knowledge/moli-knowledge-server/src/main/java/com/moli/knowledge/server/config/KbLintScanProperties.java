package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * KBOPS-8 · 定时 DB 健康体检（可选，默认关闭）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.lint")
public class KbLintScanProperties {

    /** 是否启用定时 scan 落库 kb_lint_issue。 */
    private boolean scheduleEnabled = false;

    /** 默认定时 cron，默认每周一 03:00。 */
    private String scheduleCron = "0 0 3 ? * MON";

    /**
     * 定时扫描的空间 ID 列表；为空时扫描全部 wiki 空间（kb_space 未删除）。
     */
    private List<Long> scheduleSpaceIds = new ArrayList<>();
}
