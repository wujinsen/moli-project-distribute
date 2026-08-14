package com.moli.knowledge.server.service.ingest;

import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbCategory;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ingest Plan create 项落盘路径解析（T17a）。
 * <p>
 * 有 {@code categoryId} → {@code {dir_slug}/{bareSlug}}；无则 legacy {@code typeDir(type)/bareSlug}；
 * {@code slug} 已含 {@code /} 时作完整相对路径，不再叠 {@code typeDir}。
 */
public final class IngestPlanPathResolver {

    private static final Map<String, String> TYPE_DIRS = buildTypeDirs();

    /**
     * raw 首段目录 → 空间分类 {@code dir_slug} 别名（ops 等业务分类与 raw 目录名不一致时使用）。
     */
    private static final Map<String, String> RAW_DIR_ALIASES = buildRawDirAliases();

    private IngestPlanPathResolver() {
    }

    private static Map<String, String> buildRawDirAliases() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("prd", "product");
        m.put("design", "develop");
        m.put("api", "develop");
        m.put("docs", "develop");
        return m;
    }

    private static Map<String, String> buildTypeDirs() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("guide", "guides");
        m.put("service", "services");
        m.put("concept", "concepts");
        m.put("article", "articles");
        m.put("interview", "interview");
        m.put("output", "outputs");
        m.put("exam", "exams");
        return m;
    }

    /**
     * 从 Plan create 项解析 wiki 相对路径（无 {@code .md} 后缀）。
     */
    public static String resolveCreateRelPath(JSONObject item, KbCategory category) {
        if (item == null) {
            throw new BaseException("create 项不能为空");
        }
        String slugInput = StringUtils.trimToEmpty(item.getString("slug"));
        if (slugInput.isEmpty()) {
            throw new BaseException("create 项缺少 slug，请先在 Plan 补全");
        }

        Long categoryId = parseCategoryId(item);
        if (categoryId != null) {
            if (category == null) {
                throw new BaseException("分类不存在或未加载: " + categoryId);
            }
            if (slugInput.contains("/") || slugInput.contains("\\")) {
                throw new BaseException("已选分类时 slug 必须为裸文件名（不能含 /）: " + slugInput);
            }
            String dirSlug = StringUtils.trimToEmpty(category.getDirSlug());
            if (dirSlug.isEmpty()) {
                throw new BaseException("分类未绑定目录(dir_slug): "
                        + StringUtils.defaultString(category.getCategoryName()));
            }
            return dirSlug + "/" + sanitizeBareSlug(slugInput);
        }

        if (slugInput.contains("/") || slugInput.contains("\\")) {
            return normalizeFullRelPath(slugInput);
        }

        String type = StringUtils.defaultIfBlank(item.getString("type"), "article");
        return typeDir(type) + "/" + sanitizeBareSlug(slugInput);
    }

    /**
     * 裸 slug：去空白、去 {@code .md}，禁止路径分隔符与 {@code ..}。
     */
    public static String sanitizeBareSlug(String raw) {
        String s = StringUtils.trimToEmpty(raw);
        if (s.isEmpty()) {
            throw new BaseException("slug 不能为空");
        }
        if (s.contains("..") || s.contains("/") || s.contains("\\")) {
            throw new BaseException("slug 非法：不能包含路径分隔符或 ..");
        }
        if (s.endsWith(".md")) {
            s = s.substring(0, s.length() - 3);
            if (s.isEmpty()) {
                throw new BaseException("slug 不能为空");
            }
        }
        return s;
    }

    /**
     * legacy：slug 已含目录时整段作相对路径。
     */
    public static String normalizeFullRelPath(String slugInput) {
        String path = slugInput.trim().replace('\\', '/');
        if (path.startsWith("/") || path.contains("..") || path.contains(":")) {
            throw new BaseException("非法 slug 路径: " + slugInput);
        }
        if (path.endsWith(".md")) {
            path = path.substring(0, path.length() - 3);
        }
        if (path.isEmpty()) {
            throw new BaseException("slug 不能为空");
        }
        return path;
    }

    public static Long parseCategoryId(JSONObject item) {
        if (item == null || !item.containsKey("categoryId")) {
            return null;
        }
        Object v = item.get("categoryId");
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            long id = ((Number) v).longValue();
            return id <= 0 ? null : id;
        }
        String s = StringUtils.trimToEmpty(String.valueOf(v));
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        try {
            long id = Long.parseLong(s);
            return id <= 0 ? null : id;
        } catch (NumberFormatException e) {
            throw new BaseException("categoryId 非法: " + v);
        }
    }

    public static String typeDir(String type) {
        if (StringUtils.isBlank(type)) {
            return "articles";
        }
        return TYPE_DIRS.getOrDefault(type.trim().toLowerCase(Locale.ROOT), "articles");
    }

    /**
     * 从 raw 相对路径取文件名 stem（去 {@code .md}）。
     */
    public static String stemFromRawPath(String rawPath) {
        if (StringUtils.isBlank(rawPath)) {
            return "";
        }
        String name = rawPath.trim().replace('\\', '/');
        int slash = name.lastIndexOf('/');
        name = slash >= 0 ? name.substring(slash + 1) : name;
        if (name.endsWith(".md")) {
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }

    /**
     * 从 {@code raw/} 之后的路径取用于匹配 {@code dir_slug} 的首段。
     * <ul>
     *   <li>{@code school/fe/foo.md} → {@code fe}</li>
     *   <li>{@code school/ap/foo.md} → {@code ap}</li>
     *   <li>{@code fe/foo.md} → {@code fe}（历史路径，仍兼容）</li>
     * </ul>
     */
    static String dirSlugSegmentFromPathAfterRaw(String pathAfterRaw) {
        if (StringUtils.isBlank(pathAfterRaw)) {
            return null;
        }
        String p = pathAfterRaw.trim().replace('\\', '/');
        if (p.startsWith("school/")) {
            p = p.substring("school/".length());
        }
        int slash = p.indexOf('/');
        if (slash <= 0) {
            return null;
        }
        String seg = p.substring(0, slash);
        if (seg.contains(".")) {
            return null;
        }
        return seg;
    }

    /**
     * {@code raw/school/fe/foo.md}、{@code raw/fe/foo.md}、{@code raw/prd/foo.md} 等 → 匹配 {@code dir_slug}。
     */
    public static KbCategory inferCategoryFromRawSource(String rawSource, Map<String, KbCategory> categoriesByDirSlug) {
        String dirSlug = suggestDirSlugFromRawSource(rawSource);
        if (dirSlug == null || categoriesByDirSlug == null || categoriesByDirSlug.isEmpty()) {
            return null;
        }
        return categoriesByDirSlug.get(dirSlug);
    }

    /**
     * 从 raw 路径推断目标分类 {@code dir_slug}（未匹配已存在分类时，可用于新建分类）。
     */
    public static String suggestDirSlugFromRawSource(String rawSource) {
        if (StringUtils.isBlank(rawSource)) {
            return null;
        }
        String path = rawSource.trim().replace('\\', '/');
        if (path.startsWith("raw/")) {
            path = path.substring(4);
        }
        String seg = dirSlugSegmentFromPathAfterRaw(path);
        if (seg == null) {
            return null;
        }
        return RAW_DIR_ALIASES.getOrDefault(seg, seg);
    }

    public static Map<String, KbCategory> indexCategoriesByDirSlug(Iterable<KbCategory> categories) {
        Map<String, KbCategory> map = new LinkedHashMap<>();
        if (categories == null) {
            return map;
        }
        for (KbCategory cat : categories) {
            if (cat == null) {
                continue;
            }
            String dir = StringUtils.trimToEmpty(cat.getDirSlug());
            if (!dir.isEmpty() && !map.containsKey(dir)) {
                map.put(dir, cat);
            }
        }
        return map;
    }
}
