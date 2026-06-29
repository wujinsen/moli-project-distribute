package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Web Wiki 在线编辑（T14）读写 markdown 文件的配置。
 *
 * <p>权威源是 {@code kb/} 下的 markdown 目录（与 {@code sync_to_db.py} 同源）：
 * <ul>
 *   <li>{@code root}：{@code kb/} 根目录（相对进程 cwd 或绝对路径）。</li>
 *   <li>{@code spaceDirs}：space_code → 该空间 wiki 子目录（相对 {@code root}）。</li>
 * </ul>
 * 文件路径 = {@code root/{spaceDir}/{slug}.md}，与 sync 的 slug 规则一致。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.wiki")
public class KbWikiProperties {

    /** 是否允许通过 API 读写 wiki 文件。 */
    private boolean enabled = true;

    /** kb/ 根目录（相对项目根或绝对路径）。 */
    private String root = "moli-knowledge/kb";

    /** space_code → wiki 子目录（相对 root）。 */
    private Map<String, String> spaceDirs = defaultSpaceDirs();

    /** 单文件大小上限（字节），防止误传超大内容。 */
    private long maxBytes = 2 * 1024 * 1024;

    /** 文件级空间体检脚本 lint.py 路径（相对项目根或绝对路径）。 */
    private String lintScriptPath = "moli-knowledge/kb/tools/lint.py";

    /** 文件级体检脚本超时（秒）。 */
    private int lintTimeoutSeconds = 120;

    private static Map<String, String> defaultSpaceDirs() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("enterprise-kb", "wiki");
        map.put("moli-ops-manual", "wiki-moli");
        map.put("jp-fe-ap-exam", "wiki-jp-exam");
        return map;
    }
}
