package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.config.KbWikiGovernProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.dto.WikiAiReviseRequest;
import com.moli.knowledge.server.dto.WikiAiReviseResultVo;
import com.moli.knowledge.server.dto.WikiGovernAiBatchFixRequest;
import com.moli.knowledge.server.dto.WikiGovernAiBatchFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernAutoFixRequest;
import com.moli.knowledge.server.dto.WikiGovernAutoFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernModelVo;
import com.moli.knowledge.server.dto.WikiGovernOptionsVo;
import com.moli.knowledge.server.dto.WikiGovernPageFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernScriptFixRequest;
import com.moli.knowledge.server.dto.WikiGovernScriptFixResultVo;
import com.moli.knowledge.server.dto.WikiLintIssueVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintVo;
import com.moli.knowledge.server.service.KbSyncService;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.moli.knowledge.server.service.KbWikiGovernService;
import com.moli.knowledge.server.service.KbWikiLintService;
import com.moli.knowledge.server.dto.WikiGovernMergeHintItemVo;
import com.moli.knowledge.server.dto.WikiGovernMergeHintRequest;
import com.moli.knowledge.server.dto.WikiGovernMergeHintResultVo;
import com.moli.knowledge.server.util.KbWikiFrontmatterFixUtil;
import com.moli.knowledge.server.util.KbWikiMergeHintUtil;
import com.moli.knowledge.server.util.WikiGovernKindUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KbWikiGovernServiceImpl implements KbWikiGovernService {

    @Resource
    private KbWikiGovernProperties governProperties;
    @Resource
    private KbLlmRuntime llmRuntime;
    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbWikiAiReviseService kbWikiAiReviseService;
    @Resource
    private KbWikiLintService kbWikiLintService;
    @Resource
    private KbSyncService kbSyncService;

    @Override
    public WikiGovernOptionsVo getOptions() {
        WikiGovernOptionsVo vo = new WikiGovernOptionsVo();
        vo.setLlmAvailable(llmRuntime.usable());
        vo.setProvider(llmRuntime.getProvider());

        Set<String> ids = new LinkedHashSet<>();
        if (llmRuntime.getModel() != null) {
            ids.add(llmRuntime.getModel().trim());
        }
        if (llmRuntime.getExtraModels() != null) {
            for (String m : llmRuntime.getExtraModels()) {
                if (StringUtils.isNotBlank(m)) {
                    ids.add(m.trim());
                }
            }
        }
        if (governProperties.getModels() != null) {
            for (String m : governProperties.getModels()) {
                if (StringUtils.isNotBlank(m)) {
                    ids.add(m.trim());
                }
            }
        }

        List<WikiGovernModelVo> models = new ArrayList<>();
        for (String id : ids) {
            WikiGovernModelVo item = new WikiGovernModelVo();
            item.setId(id);
            item.setDisplayName(id);
            models.add(item);
        }
        vo.setModels(models);

        String defaultModel = llmRuntime.getModel();
        if (StringUtils.isNotBlank(defaultModel)) {
            vo.setDefaultModel(defaultModel.trim());
        } else if (!models.isEmpty()) {
            vo.setDefaultModel(models.get(0).getId());
        }
        vo.setScriptFixableKinds(new ArrayList<>(WikiGovernKindUtil.scriptFixableKinds()));
        vo.setAiFixableKinds(new ArrayList<>(WikiGovernKindUtil.aiFixableKinds()));
        vo.setManualOnlyKinds(new ArrayList<>(WikiGovernKindUtil.manualOnlyKinds()));
        return vo;
    }

    @Override
    public WikiGovernScriptFixResultVo scriptFix(WikiGovernScriptFixRequest request) {
        validateIssuesRequest(request == null ? null : request.getSpaceId(), request == null ? null : request.getIssues());
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        Long spaceId = request.getSpaceId();
        String wikiDir = resolveWikiDir(spaceId);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Map<String, Set<String>> scriptKindsByPage = groupScriptKinds(request.getIssues());
        WikiGovernScriptFixResultVo result = new WikiGovernScriptFixResultVo();
        List<WikiGovernPageFixResultVo> pages = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : scriptKindsByPage.entrySet()) {
            String slug = entry.getKey();
            Set<String> kinds = entry.getValue();
            WikiGovernPageFixResultVo pageResult = new WikiGovernPageFixResultVo();
            pageResult.setSlug(slug);
            pageResult.setKinds(new ArrayList<>(kinds));

            try {
                WikiPageVo page = kbWikiFileService.readPage(slug, spaceId);
                if (!page.isExists()) {
                    pageResult.setStatus("skipped");
                    pageResult.setMessage("wiki 文件不存在");
                    result.setSkippedPages(result.getSkippedPages() + 1);
                    pages.add(pageResult);
                    continue;
                }
                String before = page.getContent();
                String after = KbWikiFrontmatterFixUtil.fixContent(before, slug, wikiDir, kinds, today);
                if (StringUtils.equals(before, after)) {
                    pageResult.setStatus("skipped");
                    pageResult.setMessage("无需变更");
                    result.setSkippedPages(result.getSkippedPages() + 1);
                    pages.add(pageResult);
                    continue;
                }
                if (dryRun) {
                    pageResult.setStatus("ok");
                    pageResult.setPreviewContent(after);
                    result.setFixedPages(result.getFixedPages() + 1);
                } else {
                    WikiSaveRequest save = new WikiSaveRequest();
                    save.setSlug(slug);
                    save.setSpaceId(spaceId);
                    save.setContent(after);
                    save.setBaselineHash(page.getContentHash());
                    save.setChangeLog("wiki-govern script-fix: " + String.join(", ", kinds));
                    kbWikiFileService.writePage(save);
                    pageResult.setStatus("ok");
                    result.setFixedPages(result.getFixedPages() + 1);
                }
            } catch (Exception e) {
                log.warn("[wiki-govern] script-fix failed slug={}", slug, e);
                pageResult.setStatus("failed");
                pageResult.setMessage(e.getMessage());
                result.setFailedPages(result.getFailedPages() + 1);
            }
            pages.add(pageResult);
        }

        result.setPages(pages);
        return result;
    }

    @Override
    public WikiGovernAiBatchFixResultVo aiBatchFix(WikiGovernAiBatchFixRequest request) {
        validateIssuesRequest(request == null ? null : request.getSpaceId(), request == null ? null : request.getIssues());
        if (!llmRuntime.usable()) {
            throw new BaseException("kb.llm 未配置，无法 AI 批量修复");
        }
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        Long spaceId = request.getSpaceId();
        String model = resolveModel(request.getModel());

        Map<String, List<WikiLintIssueVo>> aiIssuesByPage = groupAiIssues(request.getIssues());
        WikiGovernAiBatchFixResultVo result = new WikiGovernAiBatchFixResultVo();
        result.setModel(model);
        List<WikiGovernPageFixResultVo> pages = new ArrayList<>();

        for (Map.Entry<String, List<WikiLintIssueVo>> entry : aiIssuesByPage.entrySet()) {
            String slug = entry.getKey();
            List<WikiLintIssueVo> issues = entry.getValue();
            WikiGovernPageFixResultVo pageResult = new WikiGovernPageFixResultVo();
            pageResult.setSlug(slug);
            pageResult.setKinds(issues.stream().map(WikiLintIssueVo::getKind).collect(Collectors.toList()));

            try {
                WikiPageVo page = kbWikiFileService.readPage(slug, spaceId);
                if (!page.isExists()) {
                    pageResult.setStatus("skipped");
                    pageResult.setMessage("wiki 文件不存在");
                    result.setSkippedPages(result.getSkippedPages() + 1);
                    pages.add(pageResult);
                    continue;
                }

                WikiAiReviseRequest revise = new WikiAiReviseRequest();
                revise.setSlug(slug);
                revise.setSpaceId(spaceId);
                revise.setModel(model);
                revise.setInstruction(buildAiInstruction(issues));
                revise.setBaselineContent(page.getContent());
                WikiAiReviseRequest.IssueContext ctx = new WikiAiReviseRequest.IssueContext();
                ctx.setIssueType(issues.get(0).getKind());
                ctx.setDetail(combineIssueDetails(issues));
                revise.setIssueContext(ctx);

                WikiAiReviseResultVo revised = kbWikiAiReviseService.aiRevise(revise);
                String suggested = revised.getSuggestedContent();
                if (StringUtils.isBlank(suggested)) {
                    pageResult.setStatus("failed");
                    pageResult.setMessage("LLM 未返回内容");
                    result.setFailedPages(result.getFailedPages() + 1);
                    pages.add(pageResult);
                    continue;
                }
                if (dryRun) {
                    pageResult.setStatus("ok");
                    pageResult.setPreviewContent(suggested);
                    result.setFixedPages(result.getFixedPages() + 1);
                } else {
                    WikiSaveRequest save = new WikiSaveRequest();
                    save.setSlug(slug);
                    save.setSpaceId(spaceId);
                    save.setContent(suggested);
                    save.setBaselineHash(page.getContentHash());
                    save.setChangeLog("wiki-govern ai-fix: " + pageResult.getKinds());
                    kbWikiFileService.writePage(save);
                    pageResult.setStatus("ok");
                    result.setFixedPages(result.getFixedPages() + 1);
                }
            } catch (Exception e) {
                log.warn("[wiki-govern] ai-fix failed slug={}", slug, e);
                pageResult.setStatus("failed");
                pageResult.setMessage(e.getMessage());
                result.setFailedPages(result.getFailedPages() + 1);
            }
            pages.add(pageResult);
        }

        result.setPages(pages);
        return result;
    }

    @Override
    public WikiGovernAutoFixResultVo autoFix(WikiGovernAutoFixRequest request) {
        if (request == null || request.getSpaceId() == null) {
            throw new BaseException("spaceId 不能为空");
        }
        List<WikiLintIssueVo> issues = request.getIssues() == null ? new ArrayList<>() : request.getIssues();
        boolean doScript = request.getScriptFix() == null || Boolean.TRUE.equals(request.getScriptFix());
        boolean doAi = request.getAiFix() == null || Boolean.TRUE.equals(request.getAiFix());
        boolean relintAfter = request.getRelintAfter() == null || Boolean.TRUE.equals(request.getRelintAfter());
        boolean syncAfter = Boolean.TRUE.equals(request.getSyncAfter());

        WikiGovernAutoFixResultVo vo = new WikiGovernAutoFixResultVo();
        vo.setIssuesBefore(issues.size());

        if (doScript && !issues.isEmpty()) {
            WikiGovernScriptFixRequest scriptReq = new WikiGovernScriptFixRequest();
            scriptReq.setSpaceId(request.getSpaceId());
            scriptReq.setIssues(issues);
            scriptReq.setDryRun(false);
            vo.setScriptFix(scriptFix(scriptReq));
        }

        if (doAi && !issues.isEmpty()) {
            List<WikiLintIssueVo> aiIssues = issues.stream()
                    .filter(i -> WikiGovernKindUtil.isAiFixable(i.getKind()))
                    .collect(Collectors.toList());
            if (!aiIssues.isEmpty()) {
                if (!llmRuntime.usable()) {
                    log.info("[wiki-govern] auto-fix skip ai: llm unavailable");
                } else {
                    WikiGovernAiBatchFixRequest aiReq = new WikiGovernAiBatchFixRequest();
                    aiReq.setSpaceId(request.getSpaceId());
                    aiReq.setIssues(aiIssues);
                    aiReq.setModel(request.getModel());
                    aiReq.setDryRun(false);
                    vo.setAiFix(aiBatchFix(aiReq));
                }
            }
        }

        if (relintAfter) {
            WikiSpaceLintRequest lintReq = new WikiSpaceLintRequest();
            lintReq.setSpaceId(request.getSpaceId());
            lintReq.setStrict(Boolean.TRUE.equals(request.getStrict()));
            WikiSpaceLintVo relint = kbWikiLintService.lintSpace(lintReq);
            vo.setRelint(relint);
            vo.setIssuesAfter(relint.getIssues() == null ? 0 : relint.getIssues().size());
        }

        if (syncAfter) {
            SyncTriggerVo sync = kbSyncService.triggerAfterEdit(request.getSpaceId());
            vo.setSync(sync);
        }

        return vo;
    }

    @Override
    public WikiGovernMergeHintResultVo mergeHint(WikiGovernMergeHintRequest request) {
        if (request == null || request.getSpaceId() == null) {
            throw new BaseException("spaceId 不能为空");
        }
        if (request.getIssues() == null || request.getIssues().isEmpty()) {
            throw new BaseException("issues 不能为空");
        }
        kbWikiFileService.readPage("index", request.getSpaceId());

        WikiGovernMergeHintResultVo result = new WikiGovernMergeHintResultVo();
        List<WikiGovernMergeHintItemVo> items = new ArrayList<>();
        for (WikiLintIssueVo issue : request.getIssues()) {
            if (issue == null || StringUtils.isBlank(issue.getKind())) {
                continue;
            }
            String kind = issue.getKind().trim();
            if (!WikiGovernKindUtil.isManualOnly(kind)
                    && !"dup_content".equals(kind)
                    && !"near_dup".equals(kind)) {
                continue;
            }
            items.add(KbWikiMergeHintUtil.buildHint(issue));
        }
        if (items.isEmpty()) {
            throw new BaseException("无 dup_slug/dup_content/near_dup 类 issue");
        }
        result.setItems(items);
        return result;
    }

    private void validateIssuesRequest(Long spaceId, List<WikiLintIssueVo> issues) {
        if (spaceId == null) {
            throw new BaseException("spaceId 不能为空");
        }
        if (issues == null || issues.isEmpty()) {
            throw new BaseException("issues 不能为空");
        }
    }

    private String resolveWikiDir(Long spaceId) {
        WikiPageVo probe = kbWikiFileService.readPage("index", spaceId);
        String spaceCode = probe.getSpaceCode();
        String wikiDir = wikiProperties.getSpaceDirs().get(spaceCode);
        return StringUtils.defaultIfBlank(wikiDir, "wiki");
    }

    private Map<String, Set<String>> groupScriptKinds(List<WikiLintIssueVo> issues) {
        Map<String, Set<String>> grouped = new LinkedHashMap<>();
        for (WikiLintIssueVo issue : issues) {
            if (issue == null || StringUtils.isBlank(issue.getPage()) || StringUtils.isBlank(issue.getKind())) {
                continue;
            }
            if (!WikiGovernKindUtil.isScriptFixable(issue.getKind())) {
                continue;
            }
            grouped.computeIfAbsent(issue.getPage().trim(), k -> new LinkedHashSet<>()).add(issue.getKind().trim());
        }
        return grouped;
    }

    private Map<String, List<WikiLintIssueVo>> groupAiIssues(List<WikiLintIssueVo> issues) {
        Map<String, List<WikiLintIssueVo>> grouped = new LinkedHashMap<>();
        for (WikiLintIssueVo issue : issues) {
            if (issue == null || StringUtils.isBlank(issue.getPage()) || StringUtils.isBlank(issue.getKind())) {
                continue;
            }
            if (!WikiGovernKindUtil.isAiFixable(issue.getKind())) {
                continue;
            }
            grouped.computeIfAbsent(issue.getPage().trim(), k -> new ArrayList<>()).add(issue);
        }
        return grouped;
    }

    private String resolveModel(String model) {
        if (StringUtils.isNotBlank(model)) {
            return model.trim();
        }
        if (StringUtils.isNotBlank(llmRuntime.getModel())) {
            return llmRuntime.getModel().trim();
        }
        throw new BaseException("未配置默认 LLM 模型");
    }

    private String buildAiInstruction(List<WikiLintIssueVo> issues) {
        StringBuilder sb = new StringBuilder("请修复本页 wiki 体检问题（对齐 AGENTS.md §2/§6），输出完整 markdown：\n");
        for (WikiLintIssueVo issue : issues) {
            sb.append("- [").append(issue.getKind()).append("] ")
                    .append(StringUtils.defaultString(issue.getDetail()));
            if (StringUtils.isNotBlank(issue.getSuggest())) {
                sb.append("；建议：").append(issue.getSuggest());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String combineIssueDetails(List<WikiLintIssueVo> issues) {
        return issues.stream()
                .map(i -> i.getKind() + ": " + StringUtils.defaultString(i.getDetail()))
                .collect(Collectors.joining("; "));
    }
}
