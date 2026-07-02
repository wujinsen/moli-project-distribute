package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.dto.WikiSaveResultVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class KbWikiFileServiceImpl implements KbWikiFileService {

    private static final String DEFAULT_SPACE_CODE = "enterprise-kb";

    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public WikiPageVo readPage(String slug, Long spaceId) {
        assertEnabled();
        KbSpace space = resolveSpace(spaceId);
        kbAclService.assertCanEdit(space.getId());

        String cleanSlug = normalizeSlug(slug);
        String wikiDir = resolveWikiDir(space.getSpaceCode());
        Path file = resolveFile(wikiDir, cleanSlug);

        WikiPageVo vo = new WikiPageVo();
        vo.setSlug(cleanSlug);
        vo.setSpaceId(space.getId());
        vo.setSpaceCode(space.getSpaceCode());
        vo.setRelativePath(wikiDir + "/" + cleanSlug + ".md");

        if (!Files.exists(file)) {
            vo.setExists(false);
            vo.setContent("");
            vo.setContentHash(sha256(""));
            return vo;
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            vo.setExists(true);
            vo.setContent(content);
            vo.setContentHash(sha256(content));
            vo.setUpdatedAt(new Date(Files.getLastModifiedTime(file).toMillis()));
            return vo;
        } catch (IOException e) {
            log.error("读取 wiki 文件失败: {}", file, e);
            throw new BaseException("读取 wiki 文件失败：" + e.getMessage());
        }
    }

    @Override
    public WikiSaveResultVo writePage(WikiSaveRequest request) {
        assertEnabled();
        if (request == null || StringUtils.isBlank(request.getContent())) {
            throw new BaseException("内容不能为空");
        }
        KbSpace space = resolveSpace(request.getSpaceId());
        kbAclService.assertCanEdit(space.getId());

        String cleanSlug = normalizeSlug(request.getSlug());
        byte[] bytes = request.getContent().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > wikiProperties.getMaxBytes()) {
            throw new BaseException("内容超过大小上限（" + wikiProperties.getMaxBytes() + " 字节）");
        }

        String wikiDir = resolveWikiDir(space.getSpaceCode());
        Path file = resolveFile(wikiDir, cleanSlug);
        boolean exists = Files.exists(file);

        // 乐观锁：打开时的 baselineHash 与当前盘上内容不一致 → 拒绝，避免覆盖他人改动
        if (StringUtils.isNotBlank(request.getBaselineHash()) && exists) {
            String current;
            try {
                current = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new BaseException("读取 wiki 文件失败：" + e.getMessage());
            }
            if (!request.getBaselineHash().equals(sha256(current))) {
                throw new BaseException("文件已被其它修改更新（hash 冲突），请刷新后重试");
            }
        }

        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, bytes);
        } catch (IOException e) {
            log.error("写入 wiki 文件失败: {}", file, e);
            throw new BaseException("写入 wiki 文件失败：" + e.getMessage());
        }

        log.info("[wiki-edit] space={} slug={} created={} changeLog={}",
                space.getSpaceCode(), cleanSlug, !exists, request.getChangeLog());

        WikiSaveResultVo vo = new WikiSaveResultVo();
        vo.setSlug(cleanSlug);
        vo.setSpaceId(space.getId());
        vo.setRelativePath(wikiDir + "/" + cleanSlug + ".md");
        vo.setCreated(!exists);
        vo.setContentHash(sha256(request.getContent()));
        vo.setSavedAt(new Date());
        return vo;
    }

    @Override
    public WikiSaveResultVo movePage(Long spaceId, String fromSlug, String toSlug) {
        assertEnabled();
        KbSpace space = resolveSpace(spaceId);
        kbAclService.assertCanEdit(space.getId());

        String from = normalizeSlug(fromSlug);
        String to = normalizeSlug(toSlug);
        if (from.equals(to)) {
            throw new BaseException("源与目标分类相同，无需移动");
        }
        String wikiDir = resolveWikiDir(space.getSpaceCode());
        Path fromFile = resolveFile(wikiDir, from);
        Path toFile = resolveFile(wikiDir, to);
        if (!Files.exists(fromFile)) {
            throw new BaseException("源文件不存在: " + from);
        }
        if (Files.exists(toFile)) {
            throw new BaseException("目标分类下已存在同名文档: " + to);
        }

        try {
            if (toFile.getParent() != null) {
                Files.createDirectories(toFile.getParent());
            }
            Files.move(fromFile, toFile);
            rewriteReferences(wikiDir, from, to);
        } catch (IOException e) {
            log.error("移动 wiki 文件失败: {} -> {}", fromFile, toFile, e);
            throw new BaseException("移动 wiki 文件失败：" + e.getMessage());
        }

        log.info("[wiki-move] space={} {} -> {}", space.getSpaceCode(), from, to);
        WikiSaveResultVo vo = new WikiSaveResultVo();
        vo.setSlug(to);
        vo.setSpaceId(space.getId());
        vo.setRelativePath(wikiDir + "/" + to + ".md");
        vo.setCreated(false);
        vo.setSavedAt(new Date());
        return vo;
    }

    /** 扫描该空间所有 .md + graph/edges.jsonl，把全路径引用 from -> to 改写。 */
    private void rewriteReferences(String wikiDir, String from, String to) throws IOException {
        Path root = Paths.get(wikiProperties.getRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        Path base = root.resolve(wikiDir).normalize();
        if (!Files.exists(base)) {
            return;
        }
        String linkClose = "[[" + from + "]]";
        String linkPipe = "[[" + from + "|";
        try (java.util.stream.Stream<Path> stream = Files.walk(base)) {
            List<Path> mdFiles = stream
                    .filter(p -> p.toString().endsWith(".md"))
                    .collect(java.util.stream.Collectors.toList());
            for (Path p : mdFiles) {
                String text = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                if (!text.contains(linkClose) && !text.contains(linkPipe)) {
                    continue;
                }
                String updated = text.replace(linkClose, "[[" + to + "]]")
                        .replace(linkPipe, "[[" + to + "|");
                if (!updated.equals(text)) {
                    Files.write(p, updated.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        // graph/edges.jsonl：from/to 全路径 slug 改写
        Path edges = base.resolve("graph").resolve("edges.jsonl");
        if (Files.exists(edges)) {
            List<String> lines = Files.readAllLines(edges, StandardCharsets.UTF_8);
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String repl = line
                        .replace("\"" + from + "\"", "\"" + to + "\"");
                if (!repl.equals(line)) {
                    lines.set(i, repl);
                    changed = true;
                }
            }
            if (changed) {
                Files.write(edges, lines.stream().collect(java.util.stream.Collectors.joining("\n", "", "\n"))
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private void assertEnabled() {
        if (!wikiProperties.isEnabled()) {
            throw new BaseException("Wiki 在线编辑已禁用（kb.wiki.enabled=false）");
        }
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

    private String resolveWikiDir(String spaceCode) {
        String dir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(dir)) {
            throw new BaseException("空间未配置 wiki 目录: " + spaceCode);
        }
        return dir;
    }

    /** slug 合法性校验：去空白、统一斜杠、禁止越权路径。 */
    private String normalizeSlug(String slug) {
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

    /** 解析并校验目标文件在 wiki 目录内（防目录穿越）。 */
    private Path resolveFile(String wikiDir, String slug) {
        Path root = Paths.get(wikiProperties.getRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        Path base = root.resolve(wikiDir).normalize();
        Path file = base.resolve(slug + ".md").normalize();
        if (!file.startsWith(base)) {
            throw new BaseException("非法路径（越权）: " + slug);
        }
        return file;
    }

    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BaseException("计算 hash 失败：" + e.getMessage());
        }
    }
}
