package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 知识库浏览（/kb/index*）配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.browse")
public class KbBrowseProperties {

    /** 目录默认分组：category（分类=目录）或 type（体裁，legacy）。 */
    private String defaultGroupBy = "category";
}
