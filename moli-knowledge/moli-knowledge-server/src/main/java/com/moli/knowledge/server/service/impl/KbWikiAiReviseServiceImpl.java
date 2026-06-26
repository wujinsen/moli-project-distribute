package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.WikiAiReviseRequest;
import com.moli.knowledge.server.dto.WikiAiReviseResultVo;
import com.moli.knowledge.server.dto.WikiLintPreviewRequest;
import com.moli.knowledge.server.dto.WikiLintPreviewVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KbWikiAiReviseServiceImpl implements KbWikiAiReviseService {

    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final Pattern SOURCES_LINE = Pattern.compile("^sources:\\s*\\S", Pattern.MULTILINE);

    private static final String SYSTEM_PROMPT =
            "你是茉莉企业知识库的 Wiki 审校助手。任务：按用户指令修改一篇 markdown wiki 页。\n"
            + "硬性规则（对齐 AGENTS.md §2 与 AI 审校场景 B）：\n"
            + "1) 输出必须是**完整** markdown（含 YAML frontmatter），不要解释、不要代码围栏、不要前缀后缀说明；\n"
            + "2) frontmatter 必填：title、slug、type、status、tags、sources、related、created、updated；\n"
            + "3) 正文 [[..]] 互链：目标 slug 须存在于「已知 slug 列表」或保持为合理的新 slug；禁止乱造不存在的页名；\n"
            + "4) sources 非空，写可追溯 raw 或模块 README 路径；\n"
            + "5) 只改当前页内容，不要动 raw/；事实与给定上下文一致；\n"
            + "6) 若附带体检 issue，优先修复该问题。";

    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbLlmClient kbLlmClient;

    @Override
    public WikiAiReviseResultVo aiRevise(WikiAiReviseRequest request) {
        if (request == null || StringUtils.isBlank(request.getSlug()) || StringUtils.isBlank(request.getInstruction())) {
            throw new BaseException("slug 与 instruction 不能为空");
        }
        kbLlmClient.assertUsable();

        String slug = request.getSlug().trim();
        WikiPageVo ref = kbWikiFileService.readPage(slug, request.getSpaceId());
        Long spaceId = ref.getSpaceId();

        String baseline = request.getBaselineContent();
        if (StringUtils.isBlank(baseline)) {
            baseline = ref.getContent();
        }

        List<String> knownSlugs = loadKnownSlugs(spaceId, slug);
        String userPrompt = buildUserPrompt(slug, request.getInstruction().trim(), baseline,
                request.getIssueContext(), knownSlugs);

        String raw = kbLlmClient.chat(SYSTEM_PROMPT, userPrompt);
        String suggested = stripCodeFence(raw);

        WikiAiReviseResultVo vo = new WikiAiReviseResultVo();
        vo.setSuggestedContent(suggested);
        vo.setProvider(kbLlmClient.getProvider());
        vo.setModel(kbLlmClient.getModel());
        vo.setNotes(extractNotes(raw, suggested));
        return vo;
    }

    @Override
    public WikiLintPreviewVo previewLint(WikiLintPreviewRequest request) {
        if (request == null || StringUtils.isBlank(request.getSlug()) || request.getContent() == null) {
            throw new BaseException("slug 与 content 不能为空");
        }
        WikiPageVo ref = kbWikiFileService.readPage(request.getSlug(), request.getSpaceId());
        Long spaceId = ref.getSpaceId();

        WikiLintPreviewVo vo = new WikiLintPreviewVo();
        List<WikiLintPreviewVo.Item> items = new ArrayList<>();
        String content = request.getContent();

        if (!content.startsWith("---")) {
            items.add(new WikiLintPreviewVo.Item("missing_frontmatter", "缺少 YAML frontmatter（应以 --- 开头）"));
        } else {
            int end = content.indexOf("\n---", 3);
            if (end < 0) {
                items.add(new WikiLintPreviewVo.Item("missing_frontmatter", "frontmatter 未闭合（缺少第二个 ---）"));
            } else {
                String fm = content.substring(0, end + 4);
                if (!fm.contains("title:")) {
                    items.add(new WikiLintPreviewVo.Item("missing_frontmatter", "frontmatter 缺少 title"));
                }
                if (!fm.contains("slug:")) {
                    items.add(new WikiLintPreviewVo.Item("missing_frontmatter", "frontmatter 缺少 slug"));
                }
                if (!fm.contains("type:")) {
                    items.add(new WikiLintPreviewVo.Item("missing_frontmatter", "frontmatter 缺少 type"));
                }
                if (!SOURCES_LINE.matcher(fm).find()) {
                    items.add(new WikiLintPreviewVo.Item("empty_sources", "frontmatter sources 为空或缺失"));
                }
            }
        }

        Set<String> slugIndex = new HashSet<>();
        Set<String> titleIndex = new HashSet<>();
        indexDocuments(spaceId, slugIndex, titleIndex);

        Matcher m = WIKILINK.matcher(content);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String target = m.group(1).split("\\|")[0].trim();
            if (target.isEmpty() || !seen.add(target)) {
                continue;
            }
            if (!resolvesLink(target, slugIndex, titleIndex)) {
                items.add(new WikiLintPreviewVo.Item("broken_link",
                        "断链：`[[" + target + "]]` 在当前空间未找到对应页（slug/标题）"));
            }
        }

        vo.setIssues(items);
        vo.setIssueCount(items.size());
        return vo;
    }

    private List<String> loadKnownSlugs(Long spaceId, String currentSlug) {
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<>();
        w.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        w.select(KbDocument::getSlug, KbDocument::getKbType);
        if (spaceId != null) {
            w.eq(KbDocument::getSpaceId, spaceId);
        } else {
            List<Long> accessible = kbAclService.accessibleSpaceIds();
            if (accessible.isEmpty()) {
                return Collections.singletonList(currentSlug);

            }
            w.in(KbDocument::getSpaceId, accessible);
        }
        String currentType = null;
        List<String> slugs = new ArrayList<>();
        for (KbDocument d : kbDocumentMapper.selectList(w)) {
            if (StringUtils.isNotBlank(d.getSlug())) {
                slugs.add(d.getSlug());
            }
            if (currentSlug.equals(d.getSlug())) {
                currentType = d.getKbType();
            }
        }
        if (currentType != null && spaceId != null) {
            List<String> sameGroup = kbDocumentMapper.selectList(new LambdaQueryWrapper<KbDocument>()
                            .eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE)
                            .eq(KbDocument::getSpaceId, spaceId)
                            .eq(KbDocument::getKbType, currentType)
                            .select(KbDocument::getSlug)
                            .last("limit 30"))
                    .stream()
                    .map(KbDocument::getSlug)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            slugs.addAll(sameGroup);
        }
        return slugs.stream().distinct().limit(50).collect(Collectors.toList());
    }

    private void indexDocuments(Long spaceId, Set<String> slugIndex, Set<String> titleIndex) {
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<>();
        w.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        w.select(KbDocument::getSlug, KbDocument::getTitle);
        if (spaceId != null) {
            w.eq(KbDocument::getSpaceId, spaceId);
        } else {
            List<Long> accessible = kbAclService.accessibleSpaceIds();
            if (accessible.isEmpty()) {
                return;
            }
            w.in(KbDocument::getSpaceId, accessible);
        }
        for (KbDocument d : kbDocumentMapper.selectList(w)) {
            if (StringUtils.isNotBlank(d.getSlug())) {
                slugIndex.add(d.getSlug().trim().toLowerCase(Locale.ROOT));
            }
            if (StringUtils.isNotBlank(d.getTitle())) {
                titleIndex.add(d.getTitle().trim().toLowerCase(Locale.ROOT));
            }
        }
    }

    private boolean resolvesLink(String target, Set<String> slugIndex, Set<String> titleIndex) {
        String t = target.toLowerCase(Locale.ROOT);
        if (slugIndex.contains(t)) {
            return true;
        }
        if (titleIndex.contains(t)) {
            return true;
        }
        // slug 末段匹配（如 [[用户中心]] 对 services/用户中心）
        int slash = t.lastIndexOf('/');
        if (slash >= 0 && slugIndex.contains(t.substring(slash + 1))) {
            return true;
        }
        for (String slug : slugIndex) {
            if (slug.endsWith("/" + t) || slug.equals(t)) {
                return true;
            }
        }
        return false;
    }

    private String buildUserPrompt(String slug, String instruction, String baseline,
                                   WikiAiReviseRequest.IssueContext issueContext,
                                   List<String> knownSlugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前页 slug：").append(slug).append("\n\n");
        sb.append("用户指令：").append(instruction).append("\n\n");
        if (issueContext != null && (StringUtils.isNotBlank(issueContext.getIssueType())
                || StringUtils.isNotBlank(issueContext.getDetail()))) {
            sb.append("体检问题上下文：\n");
            if (StringUtils.isNotBlank(issueContext.getIssueType())) {
                sb.append("- issueType: ").append(issueContext.getIssueType()).append("\n");
            }
            if (StringUtils.isNotBlank(issueContext.getDetail())) {
                sb.append("- detail: ").append(issueContext.getDetail()).append("\n");
            }
            sb.append("\n");
        }
        if (!knownSlugs.isEmpty()) {
            sb.append("已知 slug 列表（互链请优先使用）：\n");
            for (String s : knownSlugs) {
                sb.append("- ").append(s).append("\n");
            }
            sb.append("\n");
        }
        sb.append("当前全文（请在此基础上修改并输出完整新版）：\n\n").append(baseline);
        return sb.toString();
    }

    /** 去掉模型可能包裹的 ```markdown 围栏。 */
    private String stripCodeFence(String text) {
        if (text == null) {
            return "";
        }
        String s = text.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl > 0) {
                s = s.substring(firstNl + 1);
            }
            if (s.endsWith("```")) {
                s = s.substring(0, s.length() - 3).trim();
            }
        }
        return s;
    }

    /** 若模型在围栏外加了说明，记入 notes。 */
    private String extractNotes(String raw, String suggested) {
        if (raw == null || suggested == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.equals(suggested.trim())) {
            return null;
        }
        if (trimmed.startsWith("```") && trimmed.contains(suggested.trim())) {
            String after = trimmed.substring(trimmed.lastIndexOf("```") + 3).trim();
            return after.isEmpty() ? null : after.substring(0, Math.min(500, after.length()));
        }
        return null;
    }
}
