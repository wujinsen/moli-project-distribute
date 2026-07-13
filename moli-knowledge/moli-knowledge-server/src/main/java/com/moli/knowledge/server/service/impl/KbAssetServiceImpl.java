package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.KbWikiAssetUploadVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbAssetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class KbAssetServiceImpl implements KbAssetService {

    private static final String DEFAULT_SPACE_CODE = "enterprise-kb";

    private static final Map<String, String> EXT_TO_MIME;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("png", "image/png");
        map.put("jpg", "image/jpeg");
        map.put("jpeg", "image/jpeg");
        map.put("gif", "image/gif");
        map.put("webp", "image/webp");
        map.put("svg", "image/svg+xml");
        EXT_TO_MIME = Collections.unmodifiableMap(map);
    }

    @Resource
    private KbIngestProperties ingestProperties;
    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public void serveRawAsset(Long spaceId, String path, HttpServletResponse response) {
        assertIngestEnabled();
        KbSpace space = resolveSpace(spaceId);
        kbAclService.assertCanRead(space.getId());

        if (StringUtils.isBlank(path)) {
            throw new BaseException("path 不能为空");
        }
        Path root = resolveRawRoot();
        Path file = normalizeUnder(root, cleanRawRelative(path));
        assertImageFile(file);

        streamFile(file, response);
    }

    @Override
    public void serveWikiAsset(Long spaceId, String slug, String rel, HttpServletResponse response) {
        assertWikiEnabled();
        KbSpace space = resolveSpace(spaceId);
        kbAclService.assertCanRead(space.getId());

        String cleanSlug = normalizeSlug(slug);
        if (StringUtils.isBlank(rel)) {
            throw new BaseException("rel 不能为空");
        }
        String cleanRel = normalizeAssetRel(rel);

        String wikiDir = resolveWikiDir(space.getSpaceCode());
        Path assetDir = resolveWikiAssetDir(wikiDir, cleanSlug);
        Path file = normalizeUnder(assetDir, cleanRel);
        assertImageFile(file);

        streamFile(file, response);
    }

    @Override
    public KbWikiAssetUploadVo uploadWikiAsset(Long spaceId, String slug, MultipartFile file) {
        assertWikiEnabled();
        KbSpace space = resolveSpace(spaceId);
        kbAclService.assertCanEdit(space.getId());

        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }
        if (file.getSize() > wikiProperties.getAssetMaxBytes()) {
            throw new BaseException("图片超过大小上限（" + wikiProperties.getAssetMaxBytes() + " 字节）");
        }

        String cleanSlug = normalizeSlug(slug);
        String wikiDir = resolveWikiDir(space.getSpaceCode());
        Path mdFile = resolveWikiMdFile(wikiDir, cleanSlug);
        if (!Files.isRegularFile(mdFile)) {
            throw new BaseException("wiki 文件不存在，请先保存页面后再上传插图");
        }

        String ext = resolveUploadExtension(file);
        assertAllowedImageExt(ext, wikiProperties.isAllowSvg());
        String storedName = buildStoredAssetFileName(ext);
        Path assetDir = resolveWikiAssetDir(wikiDir, cleanSlug);
        try {
            Files.createDirectories(assetDir);
        } catch (IOException e) {
            throw new BaseException("创建插图目录失败：" + e.getMessage());
        }
        Path target = normalizeUnder(assetDir, storedName);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("保存 wiki asset 失败: {}", target, e);
            throw new BaseException("保存图片失败：" + e.getMessage());
        }

        String rel = "assets/" + storedName;
        KbWikiAssetUploadVo vo = new KbWikiAssetUploadVo();
        vo.setRel(rel);
        vo.setFileName(storedName);
        vo.setFileSize(file.getSize());
        vo.setContentType(resolveContentType(target, wikiProperties.isAllowSvg()));
        vo.setMarkdown("![" + buildImageAlt(file.getOriginalFilename()) + "](" + rel + ")");
        return vo;
    }

    static String buildStoredAssetFileName(String ext) {
        String hex = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "img-" + System.currentTimeMillis() + "-" + hex + "." + ext;
    }

    static String buildImageAlt(String originalName) {
        if (StringUtils.isBlank(originalName)) {
            return "image";
        }
        String name = originalName.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        name = name.trim();
        return StringUtils.isBlank(name) ? "image" : name;
    }

    static String resolveUploadExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (StringUtils.isNotBlank(original)) {
            String name = original.replace('\\', '/');
            int slash = name.lastIndexOf('/');
            if (slash >= 0) {
                name = name.substring(slash + 1);
            }
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot < name.length() - 1) {
                return name.substring(dot + 1).toLowerCase(Locale.ROOT);
            }
        }
        String contentType = file.getContentType();
        if (StringUtils.isNotBlank(contentType)) {
            if (contentType.contains("png")) {
                return "png";
            }
            if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                return "jpeg";
            }
            if (contentType.contains("gif")) {
                return "gif";
            }
            if (contentType.contains("webp")) {
                return "webp";
            }
            if (contentType.contains("svg")) {
                return "svg";
            }
        }
        throw new BaseException("不支持的图片类型");
    }

    static void assertAllowedImageExt(String ext, boolean allowSvg) {
        if ("svg".equals(ext) && !allowSvg) {
            throw new BaseException("不支持的图片类型: svg");
        }
        if (!EXT_TO_MIME.containsKey(ext)) {
            throw new BaseException("不支持的图片类型: " + ext);
        }
    }

    private void assertImageExtension(String fileName) {
        assertAllowedImageExt(extension(Paths.get(fileName)), wikiProperties.isAllowSvg());
    }

    private Path resolveWikiMdFile(String wikiDir, String slug) {
        Path root = resolveWikiRoot();
        Path base = root.resolve(wikiDir).normalize();
        Path file = base.resolve(slug + ".md").normalize();
        if (!file.startsWith(base)) {
            throw new BaseException("非法 slug（越权）: " + slug);
        }
        return file;
    }

    /** 去掉可选 {@code raw/} 前缀。 */
    static String cleanRawRelative(String path) {
        String p = path.trim().replace('\\', '/');
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (p.startsWith("raw/")) {
            p = p.substring(4);
        }
        return p;
    }

    static String normalizeAssetRel(String rel) {
        String r = rel.trim().replace('\\', '/');
        while (r.startsWith("/")) {
            r = r.substring(1);
        }
        if (r.startsWith("./")) {
            r = r.substring(2);
        }
        if (r.startsWith("assets/")) {
            r = r.substring("assets/".length());
        }
        if (StringUtils.isBlank(r) || r.contains("..") || r.contains(":")) {
            throw new BaseException("非法 rel: " + rel);
        }
        return r;
    }

    static String normalizeSlug(String slug) {
        if (StringUtils.isBlank(slug)) {
            throw new BaseException("slug 不能为空");
        }
        String s = slug.trim().replace('\\', '/');
        if (s.startsWith("/") || s.contains("..") || s.contains(":")) {
            throw new BaseException("非法 slug: " + slug);
        }
        if (s.endsWith(".md")) {
            s = s.substring(0, s.length() - 3);
        }
        return s;
    }

    static Path normalizeUnder(Path root, String relative) {
        if (StringUtils.isBlank(relative)) {
            throw new BaseException("非法路径（越权）: " + relative);
        }
        String rel = relative.trim().replace('\\', '/');
        if (rel.startsWith("/") || rel.contains("..") || rel.contains(":")) {
            throw new BaseException("非法路径（越权）: " + relative);
        }
        Path target = root.resolve(rel).normalize();
        if (!target.startsWith(root)) {
            throw new BaseException("非法路径（越权）: " + relative);
        }
        return target;
    }

    static String resolveContentType(Path file, boolean allowSvg) {
        String ext = extension(file);
        if ("svg".equals(ext) && !allowSvg) {
            throw new BaseException("不支持的图片类型: svg");
        }
        String mapped = EXT_TO_MIME.get(ext);
        if (mapped == null) {
            throw new BaseException("不支持的图片类型: " + ext);
        }
        return mapped;
    }

    static String extension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void assertImageFile(Path file) {
        if (!Files.isRegularFile(file)) {
            throw new BaseException("资源不存在");
        }
        resolveContentType(file, wikiProperties.isAllowSvg());
    }

    private void streamFile(Path file, HttpServletResponse response) {
        String contentType = resolveContentType(file, wikiProperties.isAllowSvg());
        try {
            long size = Files.size(file);
            String fileName = file.getFileName().toString();
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedName);
            response.setHeader("Cache-Control", "private, max-age=" + wikiProperties.getAssetCacheMaxAgeSeconds());
            if (size > 0) {
                response.setContentLengthLong(size);
            }
            try (InputStream in = Files.newInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
        } catch (IOException e) {
            log.error("读取 asset 失败: {}", file, e);
            throw new BaseException("读取资源失败：" + e.getMessage());
        }
    }

    private Path resolveWikiAssetDir(String wikiDir, String slug) {
        Path root = resolveWikiRoot();
        Path base = root.resolve(wikiDir).normalize();
        String suffix = StringUtils.defaultIfBlank(wikiProperties.getAssetSubdirSuffix(), ".assets");
        Path assetDir = base.resolve(slug + suffix).normalize();
        if (!assetDir.startsWith(base)) {
            throw new BaseException("非法 slug（越权）: " + slug);
        }
        return assetDir;
    }

    private Path resolveRawRoot() {
        return com.moli.knowledge.server.util.KbRepoPathUtil.resolveRawRoot(ingestProperties.getRawRoot());
    }

    private Path resolveWikiRoot() {
        return com.moli.knowledge.server.util.KbRepoPathUtil.resolveKbRoot(wikiProperties.getRoot());
    }

    private String resolveWikiDir(String spaceCode) {
        String dir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(dir)) {
            throw new BaseException("空间未配置 wiki 目录: " + spaceCode);
        }
        return dir;
    }

    private KbSpace resolveSpace(Long spaceId) {
        if (spaceId != null) {
            KbSpace space = kbSpaceMapper.selectById(spaceId);
            if (space == null) {
                throw new BaseException("空间不存在");
            }
            return space;
        }
        KbSpace space = kbSpaceMapper.selectOne(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getSpaceCode, DEFAULT_SPACE_CODE)
                .last("limit 1"));
        if (space == null) {
            throw new BaseException("默认空间不存在: " + DEFAULT_SPACE_CODE);
        }
        return space;
    }

    private void assertIngestEnabled() {
        if (!ingestProperties.isEnabled()) {
            throw new BaseException("Ingest 已禁用（kb.ingest.enabled=false）");
        }
    }

    private void assertWikiEnabled() {
        if (!wikiProperties.isEnabled()) {
            throw new BaseException("Wiki 已禁用（kb.wiki.enabled=false）");
        }
    }
}
