package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.dto.WikiImportResultVo;
import com.moli.knowledge.server.dto.WikiImportSyncVo;
import com.moli.knowledge.server.dto.WikiLintPreviewRequest;
import com.moli.knowledge.server.dto.WikiLintPreviewVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.dto.WikiSaveResultVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbSyncService;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.moli.knowledge.server.service.KbWikiImportService;
import com.moli.knowledge.server.service.ingest.IngestPlanPathResolver;
import com.moli.knowledge.server.util.KbRawPathUtil;
import com.moli.knowledge.server.util.KbWikiImportFrontmatterUtil;
import com.moli.knowledge.server.util.KbWorkflowHints;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KbWikiImportServiceImpl implements KbWikiImportService {

    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbCategoryMapper kbCategoryMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbWikiAiReviseService kbWikiAiReviseService;
    @Resource
    private KbSyncService kbSyncService;

    @Override
    public WikiImportResultVo importPage(Long spaceId,
                                         Long categoryId,
                                         MultipartFile file,
                                         String slug,
                                         String title,
                                         String onConflict,
                                         boolean lintPreview,
                                         boolean sync) {
        assertEnabled();
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        if (categoryId == null) {
            throw new BaseException("categoryId 不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BaseException("请上传 .md 文件");
        }
        kbAclService.assertCanEdit(spaceId);

        String originalName = KbRawPathUtil.sanitizeFileName(file.getOriginalFilename());
        if (!"md".equals(KbRawPathUtil.extension(originalName))) {
            throw new BaseException("成品 Wiki 导入仅支持 .md 文件");
        }

        KbSpace space = loadSpace(spaceId);
        KbCategory category = loadCategory(categoryId, spaceId);
        String dirSlug = StringUtils.trimToEmpty(category.getDirSlug());
        if (dirSlug.isEmpty()) {
            throw new BaseException("分类未绑定目录(dir_slug): " + category.getCategoryName());
        }

        String bareSlug = resolveBareSlug(slug, originalName);
        String fullSlug = dirSlug + "/" + bareSlug;
        String conflictMode = normalizeOnConflict(onConflict);

        WikiPageVo existing = kbWikiFileService.readPage(fullSlug, spaceId);
        if (existing.isExists() && "FAIL".equals(conflictMode)) {
            throw new BaseException("wiki 文件已存在: " + fullSlug);
        }

        String rawMarkdown;
        try {
            rawMarkdown = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BaseException("读取上传文件失败：" + e.getMessage());
        }
        if (StringUtils.isBlank(rawMarkdown)) {
            throw new BaseException("文件内容不能为空");
        }

        String preservedCreated = existing.isExists()
                ? KbWikiImportFrontmatterUtil.extractCreated(existing.getContent()) : null;
        String content = KbWikiImportFrontmatterUtil.prepareImportContent(
                rawMarkdown, bareSlug, title, originalName, preservedCreated);

        WikiSaveRequest saveRequest = new WikiSaveRequest();
        saveRequest.setSpaceId(spaceId);
        saveRequest.setSlug(fullSlug);
        saveRequest.setContent(content);
        saveRequest.setChangeLog("web-import:" + originalName);
        WikiSaveResultVo saved = kbWikiFileService.writePage(saveRequest);

        log.info("[wiki-import] user={} space={} slug={} created={}",
                ShiroUtils.getUserId(), space.getSpaceCode(), fullSlug, saved.isCreated());

        WikiImportResultVo result = new WikiImportResultVo();
        result.setSlug(fullSlug);
        result.setSpaceId(spaceId);
        result.setRelativePath(saved.getRelativePath());
        result.setCreated(saved.isCreated());
        result.setContentHash(saved.getContentHash());

        if (lintPreview) {
            WikiLintPreviewRequest lintReq = new WikiLintPreviewRequest();
            lintReq.setSpaceId(spaceId);
            lintReq.setSlug(fullSlug);
            lintReq.setContent(content);
            WikiLintPreviewVo lintVo = kbWikiAiReviseService.previewLint(lintReq);
            if (lintVo.getIssues() != null) {
                result.setLintWarnings(lintVo.getIssues().stream()
                        .map(i -> i.getType() + ": " + i.getMessage())
                        .collect(Collectors.toList()));
            }
        }

        WikiImportSyncVo syncVo = new WikiImportSyncVo();
        syncVo.setTriggered(sync);
        if (sync) {
            SyncTriggerVo trigger = kbSyncService.triggerAfterEdit(spaceId);
            syncVo.setSuccess(trigger.isSuccess());
            syncVo.setMessage(trigger.isSuccess() ? null : StringUtils.defaultIfBlank(trigger.getOutputTail(), "Sync 失败"));
            if (trigger.isSuccess()) {
                KbDocument doc = findDocument(spaceId, fullSlug);
                if (doc != null) {
                    syncVo.setDocumentId(doc.getId());
                }
                result.setNextSteps(KbWorkflowHints.afterWikiWrite(spaceId));
            }
        } else {
            syncVo.setSuccess(false);
            syncVo.setMessage("未触发 Sync");
        }
        result.setSync(syncVo);
        if (result.getNextSteps() == null || result.getNextSteps().isEmpty()) {
            result.setNextSteps(KbWorkflowHints.afterWikiWrite(spaceId));
        }
        return result;
    }

    private KbSpace loadSpace(Long spaceId) {
        KbSpace space = kbSpaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BaseException("空间不存在: " + spaceId);
        }
        return space;
    }

    private KbCategory loadCategory(Long categoryId, Long spaceId) {
        KbCategory cat = kbCategoryMapper.selectById(categoryId);
        if (cat == null || (cat.getIsDelete() != null && cat.getIsDelete() == 1)) {
            throw new BaseException("分类不存在: " + categoryId);
        }
        if (!spaceId.equals(cat.getSpaceId())) {
            throw new BaseException("分类不属于当前空间");
        }
        return cat;
    }

    private String resolveBareSlug(String slug, String originalName) {
        if (StringUtils.isNotBlank(slug)) {
            return IngestPlanPathResolver.sanitizeBareSlug(slug);
        }
        String stem = originalName;
        if (stem.toLowerCase(Locale.ROOT).endsWith(".md")) {
            stem = stem.substring(0, stem.length() - 3);
        }
        return IngestPlanPathResolver.sanitizeBareSlug(stem);
    }

    private KbDocument findDocument(Long spaceId, String fullSlug) {
        return kbDocumentMapper.selectOne(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getSpaceId, spaceId)
                .eq(KbDocument::getSlug, fullSlug)
                .eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE)
                .last("LIMIT 1"));
    }

    private static String normalizeOnConflict(String onConflict) {
        String mode = StringUtils.defaultIfBlank(onConflict, "FAIL").trim().toUpperCase(Locale.ROOT);
        if (!"FAIL".equals(mode) && !"OVERWRITE".equals(mode)) {
            throw new BaseException("onConflict 非法: " + onConflict);
        }
        return mode;
    }

    private void assertEnabled() {
        if (!wikiProperties.isEnabled()) {
            throw new BaseException("Wiki 在线编辑未启用");
        }
    }
}
