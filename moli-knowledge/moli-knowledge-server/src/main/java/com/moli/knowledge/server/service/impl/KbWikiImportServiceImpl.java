package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.dto.WikiImportBatchFailVo;
import com.moli.knowledge.server.dto.WikiImportBatchItemVo;
import com.moli.knowledge.server.dto.WikiImportBatchResultVo;
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
import com.moli.knowledge.server.util.KbWikiAssetBundleUtil;
import com.moli.knowledge.server.util.KbWikiAssetBundleUtil.AssetBundlePlan;
import com.moli.knowledge.server.util.KbWikiImportFrontmatterUtil;
import com.moli.knowledge.server.util.KbWorkflowHints;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
                                         boolean sync,
                                         MultipartFile assetsZip) {
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

        AssetBundlePlan assetPlan = null;
        if (assetsZip != null && !assetsZip.isEmpty()) {
            try (InputStream in = assetsZip.getInputStream()) {
                assetPlan = KbWikiAssetBundleUtil.planFromZip(
                        in,
                        wikiProperties.getImportAssetsZipMaxBytes(),
                        wikiProperties.getImportAssetsMaxEntries(),
                        wikiProperties.isAllowSvg());
            } catch (BaseException e) {
                throw e;
            } catch (Exception e) {
                throw new BaseException("读取 assetsZip 失败：" + e.getMessage());
            }
            rawMarkdown = KbWikiAssetBundleUtil.rewriteMarkdownImages(
                    rawMarkdown, assetPlan.getBaseNames());
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

        List<String> assetsImported = new ArrayList<>();
        if (assetPlan != null) {
            String wikiDir = resolveWikiDir(space.getSpaceCode());
            Path wikiRoot = resolveWikiRoot();
            Path assetDir = KbWikiAssetBundleUtil.resolveWikiAssetDir(
                    wikiRoot, wikiDir, fullSlug, wikiProperties.getAssetSubdirSuffix());
            assetsImported = KbWikiAssetBundleUtil.writeAssetFiles(
                    assetDir, assetPlan, wikiProperties.isAllowSvg(), wikiProperties.getAssetMaxBytes());
            log.info("[wiki-import] assets user={} space={} slug={} count={}",
                    ShiroUtils.getUserId(), space.getSpaceCode(), fullSlug, assetsImported.size());
        }

        log.info("[wiki-import] user={} space={} slug={} created={}",
                ShiroUtils.getUserId(), space.getSpaceCode(), fullSlug, saved.isCreated());

        WikiImportResultVo result = new WikiImportResultVo();
        result.setSlug(fullSlug);
        result.setSpaceId(spaceId);
        result.setRelativePath(saved.getRelativePath());
        result.setCreated(saved.isCreated());
        result.setContentHash(saved.getContentHash());
        result.setAssetsImported(assetsImported);

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

    @Override
    public WikiImportBatchResultVo importBatch(Long spaceId,
                                               Long categoryId,
                                               List<MultipartFile> files,
                                               List<WikiImportBatchItemVo> items,
                                               String onConflict,
                                               boolean lintPreview,
                                               boolean sync) {
        assertEnabled();
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        if (files == null || files.isEmpty()) {
            throw new BaseException("请至少上传一个 .md 文件");
        }
        if (files.size() > 50) {
            throw new BaseException("单次批量导入最多 50 个文件");
        }
        kbAclService.assertCanEdit(spaceId);

        WikiImportBatchResultVo batch = new WikiImportBatchResultVo();
        String defaultConflict = normalizeOnConflict(onConflict);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            WikiImportBatchItemVo meta = findItem(items, i);
            Long itemCategoryId = meta != null && meta.getCategoryId() != null
                    ? meta.getCategoryId() : categoryId;
            if (itemCategoryId == null) {
                WikiImportBatchFailVo fail = new WikiImportBatchFailVo();
                fail.setFileName(file == null ? "file-" + i : file.getOriginalFilename());
                fail.setReason("缺少 categoryId");
                batch.getFailed().add(fail);
                continue;
            }
            String itemSlug = meta == null ? null : meta.getSlug();
            String itemTitle = meta == null ? null : meta.getTitle();
            String itemConflict = meta != null && StringUtils.isNotBlank(meta.getOnConflict())
                    ? normalizeOnConflict(meta.getOnConflict()) : defaultConflict;
            try {
                WikiImportResultVo one = importPage(
                        spaceId, itemCategoryId, file, itemSlug, itemTitle,
                        itemConflict, lintPreview, false, null);
                batch.getImported().add(one);
            } catch (Exception e) {
                WikiImportBatchFailVo fail = new WikiImportBatchFailVo();
                fail.setFileName(file == null ? "file-" + i : file.getOriginalFilename());
                fail.setReason(e.getMessage());
                batch.getFailed().add(fail);
            }
        }

        WikiImportSyncVo syncVo = new WikiImportSyncVo();
        syncVo.setTriggered(sync);
        if (sync && !batch.getImported().isEmpty()) {
            SyncTriggerVo trigger = kbSyncService.triggerAfterEdit(spaceId);
            syncVo.setSuccess(trigger.isSuccess());
            syncVo.setMessage(trigger.isSuccess() ? null
                    : StringUtils.defaultIfBlank(trigger.getOutputTail(), "Sync 失败"));
        } else if (!sync) {
            syncVo.setSuccess(false);
            syncVo.setMessage("未触发 Sync");
        } else {
            syncVo.setSuccess(false);
            syncVo.setMessage("无成功导入项，跳过 Sync");
        }
        batch.setSync(syncVo);
        return batch;
    }

    private WikiImportBatchItemVo findItem(List<WikiImportBatchItemVo> items, int index) {
        if (items == null) {
            return null;
        }
        for (WikiImportBatchItemVo item : items) {
            if (item != null && item.getFileIndex() != null && item.getFileIndex() == index) {
                return item;
            }
        }
        return items.size() > index ? items.get(index) : null;
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

    private Path resolveWikiRoot() {
        return com.moli.knowledge.server.util.KbRepoPathUtil.resolveKbRoot(wikiProperties.getRoot());
    }

    private String resolveWikiDir(String spaceCode) {
        Map<String, String> dirs = wikiProperties.getSpaceDirs();
        if (dirs == null || dirs.isEmpty()) {
            throw new BaseException("wiki space-dirs 未配置");
        }
        String wikiDir = dirs.get(spaceCode);
        if (StringUtils.isBlank(wikiDir)) {
            throw new BaseException("未知空间 wiki 目录: " + spaceCode);
        }
        return wikiDir;
    }
}
