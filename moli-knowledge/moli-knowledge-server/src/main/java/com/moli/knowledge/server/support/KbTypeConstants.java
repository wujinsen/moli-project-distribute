package com.moli.knowledge.server.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 体裁（kb_type）白名单 · 单一来源（Java 侧）。
 *
 * <p>真相仍是 wiki frontmatter 的 {@code type:} 行，经 {@code sync_to_db.py} 写入
 * {@code kb_document.kb_type}。本常量与 {@code kb/tools/sync_to_db.py} 的 {@code KB_TYPES}
 * 必须保持一致；新增体裁需两处同步修改。
 *
 * <p>用途：浏览/搜索体裁过滤入参校验、{@code /kb/meta/kb-types} 下拉数据源。
 * 注意：Sync 才是最终校验（非法值会被强制改为 {@code concept}），此处仅做接口层早失败。
 */
public final class KbTypeConstants {

    public static final String GUIDE = "guide";
    public static final String SERVICE = "service";
    public static final String CONCEPT = "concept";
    public static final String ARTICLE = "article";
    public static final String INTERVIEW = "interview";
    public static final String OUTPUT = "output";

    /** 有序白名单（前端下拉按此顺序展示）。 */
    public static final List<String> ALL = Collections.unmodifiableList(
            Arrays.asList(GUIDE, SERVICE, CONCEPT, ARTICLE, INTERVIEW, OUTPUT));

    private static final Set<String> SET = new LinkedHashSet<>(ALL);

    /** 体裁 → 中文展示名（前端无 i18n 时兜底）。 */
    private static final Map<String, String> LABELS = buildLabels();

    private static Map<String, String> buildLabels() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(GUIDE, "操作指南");
        m.put(SERVICE, "服务实体");
        m.put(CONCEPT, "概念");
        m.put(ARTICLE, "文章");
        m.put(INTERVIEW, "面试题");
        m.put(OUTPUT, "汇总");
        return Collections.unmodifiableMap(m);
    }

    private KbTypeConstants() {
    }

    /** 体裁中文展示名；未知体裁原样返回。 */
    public static String label(String kbType) {
        if (StringUtils.isBlank(kbType)) {
            return "";
        }
        return LABELS.getOrDefault(kbType.trim().toLowerCase(Locale.ROOT), kbType);
    }

    /** 是否合法体裁（大小写不敏感，trim 后比较）。 */
    public static boolean isValid(String kbType) {
        if (StringUtils.isBlank(kbType)) {
            return false;
        }
        return SET.contains(kbType.trim().toLowerCase(Locale.ROOT));
    }

    /** 归一化：trim + 小写；非法返回 null（调用方按需忽略或报错）。 */
    public static String normalize(String kbType) {
        if (StringUtils.isBlank(kbType)) {
            return null;
        }
        String v = kbType.trim().toLowerCase(Locale.ROOT);
        return SET.contains(v) ? v : null;
    }
}
