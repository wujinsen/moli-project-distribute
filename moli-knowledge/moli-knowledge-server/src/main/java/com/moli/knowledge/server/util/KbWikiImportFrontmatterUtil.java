package com.moli.knowledge.server.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wiki 成品导入 frontmatter 补全（T20b · 对齐 sync_to_db / lint 口径）。
 */
public final class KbWikiImportFrontmatterUtil {

    private static final Pattern H1 = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern CREATED_LINE = Pattern.compile("(?m)^created:\\s*(.+)$");
    private static final Pattern TYPE_LINE = Pattern.compile("(?m)^type:\\s*(.+)$");
    private static final Pattern TAGS_LINE = Pattern.compile("(?m)^tags:\\s*");
    private static final Pattern RELATED_LINE = Pattern.compile("(?m)^related:\\s*");

    private KbWikiImportFrontmatterUtil() {
    }

    public static String prepareImportContent(String rawMarkdown,
                                              String bareSlug,
                                              String titleOverride,
                                              String originalFileName,
                                              String preservedCreated) {
        String body = KbIngestTemplateWriter.extractBodyFromRaw(StringUtils.defaultString(rawMarkdown));
        String title = resolveTitle(rawMarkdown, body, bareSlug, titleOverride);
        String type = resolveType(rawMarkdown);
        List<String> tags = parseInlineList(rawMarkdown, "tags");
        List<String> related = parseInlineList(rawMarkdown, "related");
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String created = StringUtils.isNotBlank(preservedCreated) ? preservedCreated.trim() : today;

        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(yamlScalar(title)).append('\n');
        sb.append("slug: ").append(yamlScalar(bareSlug)).append('\n');
        sb.append("type: ").append(yamlScalar(type)).append('\n');
        sb.append("status: active\n");
        appendYamlList(sb, "tags", tags);
        appendYamlList(sb, "sources", webImportSources(originalFileName));
        appendYamlList(sb, "related", related);
        sb.append("created: ").append(created).append('\n');
        sb.append("updated: ").append(today).append('\n');
        sb.append("---\n\n");
        if (StringUtils.isNotBlank(body)) {
            sb.append(body);
            if (!body.endsWith("\n")) {
                sb.append('\n');
            }
        } else {
            sb.append("# ").append(title).append("\n\n");
        }
        return sb.toString();
    }

    public static String extractCreated(String markdown) {
        if (StringUtils.isBlank(markdown) || !markdown.startsWith("---")) {
            return null;
        }
        Matcher m = CREATED_LINE.matcher(markdown);
        return m.find() ? m.group(1).trim() : null;
    }

    private static String resolveTitle(String rawMarkdown, String body, String bareSlug, String titleOverride) {
        if (StringUtils.isNotBlank(titleOverride)) {
            return titleOverride.trim();
        }
        String fmTitle = readScalarField(rawMarkdown, "title");
        if (StringUtils.isNotBlank(fmTitle)) {
            return fmTitle;
        }
        Matcher h1 = H1.matcher(body);
        if (h1.find()) {
            return h1.group(1).trim();
        }
        return bareSlug;
    }

    private static String resolveType(String rawMarkdown) {
        Matcher m = TYPE_LINE.matcher(StringUtils.defaultString(rawMarkdown));
        if (m.find() && StringUtils.isNotBlank(m.group(1))) {
            return m.group(1).trim();
        }
        return "guide";
    }

    private static List<String> webImportSources(String originalFileName) {
        List<String> list = new ArrayList<>();
        String name = StringUtils.defaultIfBlank(originalFileName, "upload.md");
        list.add("web-import:" + name);
        return list;
    }

    private static List<String> parseInlineList(String markdown, String field) {
        List<String> out = new ArrayList<>();
        if (StringUtils.isBlank(markdown) || !markdown.startsWith("---")) {
            return out;
        }
        int end = markdown.indexOf("\n---", 3);
        if (end < 0) {
            return out;
        }
        String fm = markdown.substring(0, end + 4);
        Pattern block = Pattern.compile("(?m)^" + field + ":\\s*\\[(.*?)\\]");
        Matcher m = block.matcher(fm);
        if (m.find()) {
            for (String part : m.group(1).split(",")) {
                String item = unquote(part.trim());
                if (StringUtils.isNotBlank(item)) {
                    out.add(item);
                }
            }
        }
        return out;
    }

    private static String readScalarField(String markdown, String field) {
        if (StringUtils.isBlank(markdown) || !markdown.startsWith("---")) {
            return null;
        }
        int end = markdown.indexOf("\n---", 3);
        if (end < 0) {
            return null;
        }
        String fm = markdown.substring(0, end + 4);
        Pattern p = Pattern.compile("(?m)^" + field + ":\\s*(.+)$");
        Matcher m = p.matcher(fm);
        return m.find() ? unquote(m.group(1).trim()) : null;
    }

    private static void appendYamlList(StringBuilder sb, String field, List<String> items) {
        sb.append(field).append(":\n");
        if (items == null || items.isEmpty()) {
            sb.append("  []\n");
            return;
        }
        for (String item : items) {
            sb.append("  - ").append(yamlScalar(item)).append('\n');
        }
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

    private static String unquote(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
            return t.substring(1, t.length() - 1).trim();
        }
        return t;
    }
}
