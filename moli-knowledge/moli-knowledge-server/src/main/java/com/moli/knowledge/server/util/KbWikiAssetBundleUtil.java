package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * T20e · Wiki 成品导入附带 {@code .assets} zip：解压插图并重写 markdown 相对路径。
 */
public final class KbWikiAssetBundleUtil {

    private static final Pattern MARKDOWN_IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
    private static final Set<String> ALLOWED_EXT = allowedExtensions();

    private static Set<String> allowedExtensions() {
        Set<String> set = new HashSet<>();
        set.add("png");
        set.add("jpg");
        set.add("jpeg");
        set.add("gif");
        set.add("webp");
        set.add("svg");
        return set;
    }

    private KbWikiAssetBundleUtil() {
    }

    public static AssetBundlePlan planFromZip(InputStream zipStream,
                                              long maxZipBytes,
                                              int maxEntries,
                                              boolean allowSvg) {
        if (zipStream == null) {
            throw new BaseException("assetsZip 不能为空");
        }
        Map<String, byte[]> byBaseName = new LinkedHashMap<>();
        long totalBytes = 0;
        int entries = 0;
        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = normalizeZipEntryName(entry.getName());
                if (name == null) {
                    continue;
                }
                entries++;
                if (entries > maxEntries) {
                    throw new BaseException("assetsZip 文件数超过上限（" + maxEntries + "）");
                }
                byte[] data = readEntry(zis, maxZipBytes - totalBytes);
                totalBytes += data.length;
                if (totalBytes > maxZipBytes) {
                    throw new BaseException("assetsZip 解压后总大小超过上限（" + maxZipBytes + " 字节）");
                }
                String ext = extension(name);
                assertAllowedImageExt(ext, allowSvg);
                byBaseName.putIfAbsent(name, data);
            }
        } catch (IOException e) {
            throw new BaseException("读取 assetsZip 失败：" + e.getMessage());
        }
        if (byBaseName.isEmpty()) {
            throw new BaseException("assetsZip 内未找到可用图片（支持 png/jpg/gif/webp/svg）");
        }
        AssetBundlePlan plan = new AssetBundlePlan();
        plan.filesByBaseName = byBaseName;
        plan.baseNames = new LinkedHashSet<>(byBaseName.keySet());
        return plan;
    }

    public static String rewriteMarkdownImages(String markdown, Set<String> availableBaseNames) {
        if (StringUtils.isBlank(markdown) || availableBaseNames == null || availableBaseNames.isEmpty()) {
            return markdown;
        }
        Matcher matcher = MARKDOWN_IMAGE.matcher(markdown);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String alt = matcher.group(1);
            String target = matcher.group(2).trim();
            String replacement = matcher.group(0);
            if (!shouldRewriteImageTarget(target)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                continue;
            }
            String baseName = basenameFromImageTarget(target);
            if (availableBaseNames.contains(baseName)) {
                replacement = "![" + alt + "](assets/" + baseName + ")";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> writeAssetFiles(Path assetDir,
                                               AssetBundlePlan plan,
                                               boolean allowSvg,
                                               long maxFileBytes) {
        try {
            Files.createDirectories(assetDir);
        } catch (IOException e) {
            throw new BaseException("创建插图目录失败：" + e.getMessage());
        }
        List<String> imported = new ArrayList<>();
        for (Map.Entry<String, byte[]> e : plan.filesByBaseName.entrySet()) {
            String baseName = e.getKey();
            byte[] data = e.getValue();
            if (data.length > maxFileBytes) {
                throw new BaseException("插图超过大小上限: " + baseName);
            }
            assertAllowedImageExt(extension(baseName), allowSvg);
            Path target = assetDir.resolve(baseName).normalize();
            if (!target.startsWith(assetDir.normalize())) {
                throw new BaseException("非法插图路径: " + baseName);
            }
            try {
                Files.write(target, data);
                imported.add("assets/" + baseName);
            } catch (IOException ex) {
                throw new BaseException("写入插图失败：" + baseName + " — " + ex.getMessage());
            }
        }
        return imported;
    }

    public static Path resolveWikiAssetDir(Path wikiRoot, String wikiDir, String fullSlug, String assetSubdirSuffix) {
        Path base = wikiRoot.resolve(wikiDir).normalize();
        String suffix = StringUtils.defaultIfBlank(assetSubdirSuffix, ".assets");
        Path assetDir = base.resolve(fullSlug + suffix).normalize();
        if (!assetDir.startsWith(base)) {
            throw new BaseException("非法 slug（越权）: " + fullSlug);
        }
        return assetDir;
    }

    private static boolean shouldRewriteImageTarget(String target) {
        String t = target.trim();
        if (t.startsWith("http://") || t.startsWith("https://") || t.startsWith("data:")) {
            return false;
        }
        if (t.startsWith("/kb/") || t.contains("/kb/raw/asset") || t.contains("/kb/wiki")) {
            return false;
        }
        return true;
    }

    private static String basenameFromImageTarget(String target) {
        String t = target.trim().replace('\\', '/');
        while (t.startsWith("./")) {
            t = t.substring(2);
        }
        if (t.startsWith("assets/")) {
            t = t.substring("assets/".length());
        }
        int slash = t.lastIndexOf('/');
        return slash >= 0 ? t.substring(slash + 1) : t;
    }

    static String normalizeZipEntryName(String entryName) {
        if (StringUtils.isBlank(entryName)) {
            return null;
        }
        String name = entryName.trim().replace('\\', '/');
        if (name.startsWith("__MACOSX/") || name.endsWith(".DS_Store") || name.contains("/.")) {
            return null;
        }
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (name.contains("..") || name.contains(":")) {
            throw new BaseException("assetsZip 含非法路径: " + entryName);
        }
        int slash = name.lastIndexOf('/');
        name = slash >= 0 ? name.substring(slash + 1) : name;
        if (name.isEmpty() || !name.contains(".")) {
            return null;
        }
        return name;
    }

    private static byte[] readEntry(InputStream in, long maxRemaining) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        long total = 0;
        while ((len = in.read(buf)) != -1) {
            total += len;
            if (total > maxRemaining) {
                throw new BaseException("assetsZip 解压后总大小超过上限");
            }
            out.write(buf, 0, len);
        }
        return out.toByteArray();
    }

    private static String extension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) {
            return "";
        }
        return lower.substring(dot + 1);
    }

    private static void assertAllowedImageExt(String ext, boolean allowSvg) {
        if ("svg".equals(ext) && !allowSvg) {
            throw new BaseException("不支持的图片类型: svg");
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BaseException("不支持的图片类型: " + ext);
        }
    }

    public static final class AssetBundlePlan {
        private Map<String, byte[]> filesByBaseName;
        private Set<String> baseNames;

        public Map<String, byte[]> getFilesByBaseName() {
            return filesByBaseName;
        }

        public Set<String> getBaseNames() {
            return baseNames;
        }
    }
}
