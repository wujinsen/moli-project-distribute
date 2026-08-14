package com.moli.knowledge.server.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ingest 模板模式：raw 直贴 wiki（不调 LLM 写正文）。
 */
public final class KbIngestTemplateWriter {

    private KbIngestTemplateWriter() {
    }

    public static String buildCreatePage(String bareSlug,
                                         String title,
                                         String type,
                                         List<String> sources,
                                         List<String> related,
                                         String rawBody,
                                         String today) {
        String safeTitle = StringUtils.defaultIfBlank(title, bareSlug);
        String safeType = StringUtils.defaultIfBlank(type, "article");
        String body = extractBodyFromRaw(StringUtils.defaultString(rawBody));

        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(yamlScalar(safeTitle)).append('\n');
        sb.append("slug: ").append(yamlScalar(bareSlug)).append('\n');
        sb.append("type: ").append(yamlScalar(safeType)).append('\n');
        sb.append("status: active\n");
        sb.append("tags: []\n");
        sb.append("sources:\n");
        for (String src : normalizeSources(sources)) {
            sb.append("  - ").append(yamlScalar(src)).append('\n');
        }
        sb.append("related:\n");
        List<String> rel = related == null ? new ArrayList<>() : related;
        if (rel.isEmpty()) {
            sb.append("  []\n");
        } else {
            for (String r : rel) {
                sb.append("  - ").append(yamlScalar(r)).append('\n');
            }
        }
        sb.append("created: ").append(today).append('\n');
        sb.append("updated: ").append(today).append('\n');
        sb.append("---\n\n");
        if (StringUtils.isNotBlank(body)) {
            sb.append(body);
            if (!body.endsWith("\n")) {
                sb.append('\n');
            }
        } else {
            sb.append("# ").append(safeTitle).append("\n\n（模板模式：raw 源为空，请人工补充正文）\n");
        }
        return sb.toString();
    }

    public static String buildEnrichPatch(String sectionTitle, String rawBody) {
        String title = StringUtils.defaultIfBlank(sectionTitle, "补充");
        String body = extractBodyFromRaw(StringUtils.defaultString(rawBody));
        if (StringUtils.isBlank(body)) {
            body = "（模板模式：无 raw 正文，请人工编辑）";
        }
        return "## " + title + "\n\n" + body.trim() + "\n";
    }

    public static String extractBodyFromRaw(String rawContent) {
        if (StringUtils.isBlank(rawContent)) {
            return "";
        }
        String s = rawContent.trim();
        if (s.startsWith("---")) {
            int end = s.indexOf("\n---", 3);
            if (end >= 0) {
                return s.substring(end + 4).trim();
            }
        }
        return s;
    }

    private static List<String> normalizeSources(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            List<String> fallback = new ArrayList<>();
            fallback.add("raw/（待补充）");
            return fallback;
        }
        return sources.stream()
                .filter(StringUtils::isNotBlank)
                .map(s -> {
                    String t = s.trim().replace('\\', '/');
                    if (!t.startsWith("raw/") && !t.contains("/README.md")) {
                        if (t.startsWith("kb/raw/")) {
                            t = t.substring(3);
                        } else if (!t.startsWith("raw/")) {
                            t = "raw/" + t;
                        }
                    }
                    return t;
                })
                .collect(Collectors.toList());
    }

    private static String yamlScalar(String value) {
        if (value == null) {
            return "\"\"";
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return "\"\"";
        }
        if (v.contains(":") || v.contains("#") || v.contains("\"") || v.contains("'")
                || v.contains("\n") || v.contains("[") || v.contains("]")) {
            return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return v;
    }
}
