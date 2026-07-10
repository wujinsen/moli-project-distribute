package com.moli.knowledge.server.support;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.util.KbContentHashUtil;
import com.moli.knowledge.server.util.KbWikiFrontmatterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 扫描 wiki 目录，按 sync 口径收集 slug + contentHash（KBOPS-A3）。
 */
@Slf4j
@Component
public class KbWikiDriftScanner {

    private static final int MAX_FILES = 50_000;

    @Resource
    private KbWikiProperties wikiProperties;

    public Map<String, WikiPageSnapshot> scanWikiDir(String wikiDirRelative) {
        Path base = resolveWikiBase(wikiDirRelative);
        if (!Files.isDirectory(base)) {
            throw new BaseException("wiki 目录不存在: " + base);
        }
        Map<String, WikiPageSnapshot> out = new LinkedHashMap<>();
        try (Stream<Path> stream = Files.walk(base)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".md"))
                    .limit(MAX_FILES)
                    .forEach(path -> {
                        String stem = path.getFileName().toString();
                        stem = stem.substring(0, stem.length() - 3);
                        if (KbWikiFrontmatterUtil.isMetaPageStem(stem)) {
                            return;
                        }
                        Path rel = base.relativize(path);
                        String slug = rel.toString().replace('\\', '/');
                        if (slug.endsWith(".md")) {
                            slug = slug.substring(0, slug.length() - 3);
                        }
                        try {
                            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                            WikiPageSnapshot snap = new WikiPageSnapshot();
                            snap.setSlug(slug);
                            snap.setContentHash(KbContentHashUtil.sha256(content));
                            snap.setRelativePath(wikiDirRelative + "/" + slug + ".md");
                            out.put(slug, snap);
                        } catch (IOException e) {
                            log.warn("[drift] 读取失败 {}: {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new BaseException("扫描 wiki 目录失败：" + e.getMessage());
        }
        return out;
    }

    public String resolveWikiDirForSpace(String spaceCode) {
        String dir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(dir)) {
            throw new BaseException("空间未配置 wiki 目录: " + spaceCode);
        }
        return dir;
    }

    private Path resolveWikiBase(String wikiDirRelative) {
        Path root = Paths.get(wikiProperties.getRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.resolve(wikiDirRelative).normalize();
    }
}
