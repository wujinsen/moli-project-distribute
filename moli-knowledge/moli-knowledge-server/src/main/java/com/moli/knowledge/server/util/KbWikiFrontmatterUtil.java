package com.moli.knowledge.server.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 轻量 frontmatter 解析（与 {@code kb/tools/sync_to_db.py} / {@code lint.py} 口径接近）。
 */
public final class KbWikiFrontmatterUtil {

    private static final Pattern META_STEM = Pattern.compile("^(index|log)(-.*)?$", Pattern.CASE_INSENSITIVE);

    private KbWikiFrontmatterUtil() {
    }

    /** 是否为 index/log 类 meta 页（不参与 coverage 索引）。 */
    public static boolean isMetaPageStem(String stem) {
        return stem != null && META_STEM.matcher(stem).matches();
    }

    /**
     * 从 markdown 全文提取 frontmatter {@code sources} 列表项（原始字符串，未归一化）。
     */
    public static List<String> parseSources(String markdown) {
        List<String> out = new ArrayList<>();
        if (StringUtils.isBlank(markdown) || !markdown.startsWith("---")) {
            return out;
        }
        int end = markdown.indexOf("\n---", 3);
        if (end < 0) {
            return out;
        }
        String fm = markdown.substring(0, end + 4);
        // inline: sources: [a, b]
        int inlineIdx = fm.indexOf("sources:");
        if (inlineIdx < 0) {
            return out;
        }
        String after = fm.substring(inlineIdx + "sources:".length()).trim();
        if (after.startsWith("[")) {
            int close = after.indexOf(']');
            if (close > 0) {
                String inner = after.substring(1, close);
                for (String part : inner.split(",")) {
                    String item = unquote(part.trim());
                    if (StringUtils.isNotBlank(item)) {
                        out.add(item);
                    }
                }
            }
            return out;
        }
        // block list
        String[] lines = fm.split("\n", -1);
        boolean inSources = false;
        for (String line : lines) {
            if (line.startsWith("sources:")) {
                inSources = true;
                String rest = line.substring("sources:".length()).trim();
                if (StringUtils.isNotBlank(rest) && !rest.startsWith("[")) {
                    out.add(unquote(rest));
                }
                continue;
            }
            if (!inSources) {
                continue;
            }
            if (line.matches("^\\s+-\\s+.+")) {
                out.add(unquote(line.replaceFirst("^\\s+-\\s+", "").trim()));
                continue;
            }
            if (!line.startsWith(" ") && !line.startsWith("\t") && line.contains(":")) {
                break;
            }
        }
        return out;
    }

    /**
     * 将 frontmatter sources 条目归一化为 raw 根下的相对路径；非 raw 源返回 {@code null}。
     */
    public static String normalizeRawSourcePath(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        String s = unquote(source.trim()).replace('\\', '/');
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return null;
        }
        int idx = s.indexOf("raw/");
        if (idx >= 0) {
            s = s.substring(idx + 4);
        } else if (s.startsWith("kb/raw/")) {
            s = s.substring("kb/raw/".length());
        } else {
            return null;
        }
        while (s.startsWith("/")) {
            s = s.substring(1);
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return StringUtils.isBlank(s) ? null : s;
    }

    /** 判断归一化 raw 路径是否为目录级引用（末段无扩展名）。 */
    public static boolean isDirectoryLikeRawPath(String normalizedRawPath) {
        if (StringUtils.isBlank(normalizedRawPath)) {
            return false;
        }
        String name = normalizedRawPath;
        int slash = normalizedRawPath.lastIndexOf('/');
        if (slash >= 0) {
            name = normalizedRawPath.substring(slash + 1);
        }
        return !name.contains(".");
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

    /** 提取 frontmatter 块（含首尾 ---）；无 frontmatter 返回 null。 */
    public static String extractFrontmatter(String markdown) {
        if (StringUtils.isBlank(markdown) || !markdown.startsWith("---")) {
            return null;
        }
        int end = markdown.indexOf("\n---", 3);
        if (end < 0) {
            return null;
        }
        return markdown.substring(0, end + 4);
    }

    /** 读取 frontmatter 单行字段（如 slug / created / updated / type）。 */
    public static String readField(String markdown, String key) {
        String fm = extractFrontmatter(markdown);
        if (fm == null || StringUtils.isBlank(key)) {
            return null;
        }
        String prefix = key + ":";
        for (String line : fm.split("\n", -1)) {
            if (line.startsWith(prefix)) {
                String val = line.substring(prefix.length()).trim();
                return StringUtils.isBlank(val) ? null : unquote(val);
            }
        }
        return null;
    }

    /** 正文是否存在 H1（`# 标题`，非 `##`）。 */
    public static boolean hasH1(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return false;
        }
        String body = markdown;
        if (markdown.startsWith("---")) {
            int end = markdown.indexOf("\n---", 3);
            if (end >= 0) {
                body = markdown.substring(end + 4);
            }
        }
        for (String line : body.split("\n", -1)) {
            String t = line.trim();
            if (t.startsWith("# ") && !t.startsWith("##")) {
                return true;
            }
        }
        return false;
    }
}
