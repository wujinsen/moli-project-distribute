package com.moli.knowledge.server.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 脚本修复 frontmatter（对齐 {@code lint.py} 的 missing_dates / slug_mismatch / missing_source）。
 */
public final class KbWikiFrontmatterFixUtil {

    private static final Pattern CREATED_LINE = Pattern.compile("(?m)^created:\\s*.+$");
    private static final Pattern UPDATED_LINE = Pattern.compile("(?m)^updated:\\s*.+$");
    private static final Pattern SLUG_LINE = Pattern.compile("(?m)^slug:\\s*.+$");
    private static final Pattern SOURCES_LINE = Pattern.compile("^sources:\\s*\\S", Pattern.MULTILINE);
    private static final Pattern RAW_REF = Pattern.compile("(?:kb/)?raw/([\\w./\\-\\u4e00-\\u9fff]+)");
    private static final Pattern README_REF = Pattern.compile("(moli-[\\w-]+/README\\.md)");

    private KbWikiFrontmatterFixUtil() {
    }

    public static String stemFromSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            return "";
        }
        String s = slug.replace('\\', '/').trim();
        int idx = s.lastIndexOf('/');
        return idx >= 0 ? s.substring(idx + 1) : s;
    }

    /** 返回修复后的全文；无 frontmatter 或无对应 kind 时可能原样返回。 */
    public static String fixContent(String content, String slug, String wikiDir,
                                    Set<String> kinds, String today) {
        if (StringUtils.isBlank(content) || kinds == null || kinds.isEmpty()) {
            return content;
        }
        String result = content;
        if (kinds.contains("missing_dates")) {
            result = fixMissingDates(result, today);
        }
        if (kinds.contains("slug_mismatch")) {
            result = fixSlugMismatch(result, stemFromSlug(slug));
        }
        if (kinds.contains("missing_source")) {
            result = fixMissingSource(result, slug, wikiDir);
        }
        return result;
    }

    public static String fixMissingDates(String content, String today) {
        if (!content.startsWith("---")) {
            return content;
        }
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return content;
        }
        String head = content.substring(0, end);
        String body = content.substring(end);
        boolean hasCreated = CREATED_LINE.matcher(head).find();
        boolean hasUpdated = UPDATED_LINE.matcher(head).find();
        if (hasCreated && hasUpdated) {
            return content;
        }
        if (!hasCreated) {
            head = head + "\ncreated: " + today;
        }
        if (!hasUpdated) {
            head = head + "\nupdated: " + today;
        }
        return head + body;
    }

    public static String fixSlugMismatch(String content, String expectedStem) {
        if (!content.startsWith("---") || StringUtils.isBlank(expectedStem)) {
            return content;
        }
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return content;
        }
        String head = content.substring(0, end);
        String body = content.substring(end);
        if (SLUG_LINE.matcher(head).find()) {
            head = SLUG_LINE.matcher(head).replaceFirst("slug: " + expectedStem);
        } else {
            head = head + "\nslug: " + expectedStem;
        }
        return head + body;
    }

    public static String fixMissingSource(String content, String slug, String wikiDir) {
        List<String> existing = KbWikiFrontmatterUtil.parseSources(content);
        if (!existing.isEmpty()) {
            return content;
        }
        List<String> inferred = inferSources(content, slug, wikiDir);
        if (inferred.isEmpty()) {
            return content;
        }
        return appendSources(content, inferred);
    }

    static List<String> inferSources(String content, String slug, String wikiDir) {
        Set<String> found = new LinkedHashSet<>();
        String body = content;
        if (content.startsWith("---")) {
            int end = content.indexOf("\n---", 3);
            if (end >= 0) {
                body = content.substring(end);
            }
        }
        Matcher rawMatcher = RAW_REF.matcher(body);
        while (rawMatcher.find()) {
            found.add("raw/" + rawMatcher.group(1).replace('\\', '/'));
        }
        Matcher readmeMatcher = README_REF.matcher(body);
        while (readmeMatcher.find()) {
            found.add(readmeMatcher.group(1));
        }
        if (found.isEmpty() && StringUtils.isNotBlank(slug)) {
            String dir = StringUtils.defaultIfBlank(wikiDir, "wiki");
            found.add("moli-knowledge/kb/" + dir + "/" + slug + ".md");
        }
        return new ArrayList<>(found);
    }

    private static String appendSources(String content, List<String> entries) {
        if (!content.startsWith("---")) {
            return content;
        }
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return content;
        }
        String head = content.substring(0, end);
        String body = content.substring(end);

        for (String entry : entries) {
            String normalized = entry.trim().replace('\\', '/');
            if (head.contains(normalized)) {
                continue;
            }
            if (SOURCES_LINE.matcher(head).find()) {
                head = head.replaceFirst("(?m)^sources:\\s*\\[(.*)]",
                        "sources: [$1, " + normalized + "]");
            } else if (head.contains("sources:\n")) {
                head = head.replaceFirst("(?m)^(sources:\\s*\\n)", "$1  - " + normalized + "\n");
            } else {
                head = head + "\nsources:\n  - " + normalized;
            }
        }
        return head + body;
    }
}
