package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.dto.WikiEnrichEdgeDto;
import com.moli.knowledge.server.dto.WikiEnrichItemDto;
import com.moli.knowledge.server.dto.WikiEnrichItemResultVo;
import com.moli.knowledge.server.dto.WikiEnrichRequest;
import com.moli.knowledge.server.dto.WikiEnrichResultVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.service.KbSyncService;
import com.moli.knowledge.server.service.KbWikiEnrichService;
import com.moli.knowledge.server.service.KbWikiFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KbWikiEnrichServiceImpl implements KbWikiEnrichService {

    private static final String ENRICH_WRITER_PROMPT =
            "你是茉莉企业知识库的增量补充器（EnrichWriter）。任务：给一篇已有 wiki 页补充一个新章节。\n"
            + "硬性规则：\n"
            + "1) 只输出**要追加的 markdown 章节**（从一个 `## 标题` 开始），禁止重复已有内容、"
            + "禁止整页重写、禁止 frontmatter、禁止解释或代码围栏；\n"
            + "2) 内容忠于给定 raw 源，与已有正文不冲突；如发现冲突，在章节内用「> 注：」标注；\n"
            + "3) [[..]] 互链只用「已知 slug 列表」里的 slug。";

    private static final int RAW_SNIPPET_CHARS = 4000;
    private static final Pattern SOURCES_LINE = Pattern.compile("^sources:\\s*\\S", Pattern.MULTILINE);
    private static final Pattern UPDATED_LINE = Pattern.compile("^updated:\\s*.*$", Pattern.MULTILINE);

    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbIngestProperties ingestProperties;
    @Resource
    private KbLlmClient kbLlmClient;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbSyncService kbSyncService;

    @Override
    public WikiEnrichResultVo enrich(WikiEnrichRequest request) {
        if (request == null) {
            throw new BaseException("请求不能为空");
        }
        List<WikiEnrichItemDto> tasks = resolveTasks(request);
        if (tasks.isEmpty()) {
            throw new BaseException("至少一项 enrich 任务（slug + patch 或 rawPaths）");
        }

        Long spaceId = request.getSpaceId();
        WikiPageVo probe = kbWikiFileService.readPage(tasks.get(0).getSlug(), spaceId);
        if (!probe.isExists() && !isDryRun(request)) {
            throw new BaseException("目标页不存在: " + tasks.get(0).getSlug());
        }
        spaceId = probe.getSpaceId();
        kbAclService.assertCanEdit(spaceId);

        String batchNo = StringUtils.defaultIfBlank(request.getBatchNo(), "web");
        String topic = StringUtils.defaultIfBlank(request.getTopic(), "enrich");
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        boolean updateMeta = request.getUpdateMeta() == null || request.getUpdateMeta();
        boolean doLog = !dryRun && (request.getAppendLog() == null || request.getAppendLog());
        boolean doIndex = !dryRun && (request.getAppendIndex() == null || request.getAppendIndex());
        boolean doEdges = !dryRun && (request.getAppendEdges() == null || request.getAppendEdges());

        List<String> knownSlugs = loadKnownSlugs(spaceId);
        List<WikiEnrichItemResultVo> results = new ArrayList<>();
        List<String> appliedSlugs = new ArrayList<>();

        for (WikiEnrichItemDto task : tasks) {
            WikiEnrichItemResultVo itemVo = new WikiEnrichItemResultVo();
            itemVo.setSlug(task.getSlug().trim());
            try {
                WikiPageVo page = kbWikiFileService.readPage(task.getSlug(), spaceId);
                if (!page.isExists()) {
                    throw new BaseException("wiki 页不存在: " + task.getSlug());
                }
                String baseline = page.getContent();
                String patch = resolvePatch(task, page.getSlug(), baseline, knownSlugs);
                itemVo.setPatch(patch);
                String merged = mergeEnrich(baseline, patch);
                itemVo.setMergedPreview(tail(merged, 4000));

                if (!dryRun) {
                    if (updateMeta && task.getRawPaths() != null && !task.getRawPaths().isEmpty()) {
                        merged = updateFrontmatterMeta(merged, task.getRawPaths());
                    }
                    WikiSaveRequest save = new WikiSaveRequest();
                    save.setSlug(page.getSlug());
                    save.setSpaceId(spaceId);
                    save.setContent(merged);
                    save.setChangeLog("enrich 批次#" + batchNo);
                    kbWikiFileService.writePage(save);
                    itemVo.setApplied(true);
                    appliedSlugs.add(page.getSlug());
                } else {
                    itemVo.setApplied(false);
                }
            } catch (Exception e) {
                itemVo.setApplied(false);
                itemVo.setError(e.getMessage());
                log.warn("[wiki-enrich] slug={} failed: {}", task.getSlug(), e.getMessage());
            }
            results.add(itemVo);
        }

        WikiEnrichResultVo vo = new WikiEnrichResultVo();
        vo.setBatchNo(batchNo);
        vo.setTopic(topic);
        vo.setDryRun(dryRun);
        vo.setItems(results);
        vo.setLogAppended(false);
        vo.setIndexUpdated(false);
        vo.setEdgesAppended(0);

        if (!dryRun && !appliedSlugs.isEmpty()) {
            String spaceCode = probe.getSpaceCode();
            String marker = enrichMarker(batchNo);
            if (doLog) {
                vo.setLogAppended(appendLogBatch(spaceCode, batchNo, topic, appliedSlugs, marker));
            }
            if (doIndex) {
                vo.setIndexUpdated(appendIndexBatch(spaceCode, batchNo, appliedSlugs, marker));
            }
            if (doEdges && request.getEdges() != null && !request.getEdges().isEmpty()) {
                Set<String> touched = appliedSlugs.stream()
                        .map(this::bareSlug).collect(Collectors.toSet());
                vo.setEdgesAppended(appendEdges(spaceCode, request.getEdges(), touched));
            }
            if (Boolean.TRUE.equals(request.getSync())) {
                SyncTriggerVo sr = kbSyncService.triggerAfterEdit(spaceId);
                vo.setSyncTriggered(true);
                vo.setSyncResult(sr);
            }
        }

        log.info("[wiki-enrich] batch={} dryRun={} applied={} log={} index={} edges={}",
                batchNo, dryRun, appliedSlugs.size(), vo.getLogAppended(),
                vo.getIndexUpdated(), vo.getEdgesAppended());
        return vo;
    }

    private List<WikiEnrichItemDto> resolveTasks(WikiEnrichRequest request) {
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            return request.getItems();
        }
        if (StringUtils.isBlank(request.getSlug())) {
            return new ArrayList<>();
        }
        WikiEnrichItemDto one = new WikiEnrichItemDto();
        one.setSlug(request.getSlug());
        one.setPatch(request.getPatch());
        one.setReason(request.getReason());
        one.setRawPaths(request.getRawPaths());
        return java.util.Collections.singletonList(one);
    }

    private boolean isDryRun(WikiEnrichRequest request) {
        return Boolean.TRUE.equals(request.getDryRun());
    }

    private String resolvePatch(WikiEnrichItemDto task, String slug, String baseline,
                              List<String> knownSlugs) {
        if (StringUtils.isNotBlank(task.getPatch())) {
            return stripCodeFence(task.getPatch().trim());
        }
        if (task.getRawPaths() != null && !task.getRawPaths().isEmpty()) {
            kbLlmClient.assertUsable();
            String user = "目标页 slug：" + slug + "\n"
                    + "补充原因：" + StringUtils.defaultString(task.getReason()) + "\n"
                    + "已知 slug 列表：\n" + bulletList(knownSlugs, 60)
                    + "\n\n已有页当前全文：\n" + baseline
                    + "\n\nraw 源：\n" + readRawSnippets(task.getRawPaths());
            String raw = kbLlmClient.chat(ENRICH_WRITER_PROMPT, user);
            String patch = stripCodeFence(raw);
            if (!patch.trim().startsWith("##")) {
                patch = "## 补充\n\n" + patch;
            }
            return patch;
        }
        throw new BaseException("slug=" + task.getSlug() + " 需 patch 或 rawPaths");
    }

    private String mergeEnrich(String baseline, String patch) {
        if (StringUtils.isBlank(baseline)) {
            return StringUtils.defaultString(patch);
        }
        if (StringUtils.isBlank(patch)) {
            return baseline;
        }
        return baseline.replaceAll("\\s+$", "") + "\n\n" + patch.trim() + "\n";
    }

    private String updateFrontmatterMeta(String content, List<String> rawPaths) {
        if (!content.startsWith("---")) {
            return content;
        }
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return content;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String head = content.substring(0, end);
        String body = content.substring(end);

        if (UPDATED_LINE.matcher(head).find()) {
            head = UPDATED_LINE.matcher(head).replaceFirst("updated: " + today);
        } else {
            head = head + "\nupdated: " + today;
        }

        for (String rp : rawPaths) {
            String entry = rp.trim().replace('\\', '/');
            if (!entry.startsWith("raw/")) {
                entry = "raw/" + entry;
            }
            if (!head.contains(entry)) {
                if (SOURCES_LINE.matcher(head).find()) {
                    head = head.replaceFirst("(?m)^sources:\\s*\\[(.*)]",
                            "sources: [$1, " + entry + "]");
                } else if (head.contains("sources:\n")) {
                    head = head.replaceFirst("(?m)^(sources:\\s*\\n)",
                            "$1  - " + entry + "\n");
                } else {
                    head = head + "\nsources:\n  - " + entry;
                }
            }
        }
        return head + body;
    }

    private boolean appendLogBatch(String spaceCode, String batchNo, String topic,
                                   List<String> slugs, String marker) {
        if (readWikiRelFile(spaceCode, "log.md").contains(marker)) {
            return false;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String names = slugs.stream().map(this::bareSlug).collect(Collectors.joining(", "));
        String line = "## [" + today + "] ingest | 批次#" + batchNo + " " + topic
                + " (Web enrich); enrich " + names + " " + marker + "\n";
        appendToFile(spaceCode, "log.md", line);
        return true;
    }

    private boolean appendIndexBatch(String spaceCode, String batchNo,
                                     List<String> slugs, String marker) {
        if (readWikiRelFile(spaceCode, "index.md").contains(marker)) {
            return false;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 批次 #").append(batchNo).append("（Web Enrich ").append(today)
                .append("） ").append(marker).append("\n\n");
        for (String slug : slugs) {
            sb.append("- [[").append(bareSlug(slug)).append("]] — enrich\n");
        }
        appendToFile(spaceCode, "index.md", sb.toString());
        return true;
    }

    private int appendEdges(String spaceCode, List<WikiEnrichEdgeDto> edges, Set<String> touchedBare) {
        String existing = readWikiRelFile(spaceCode, "graph/edges.jsonl");
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (WikiEnrichEdgeDto e : edges) {
            String from = StringUtils.trimToEmpty(e.getFrom());
            String to = StringUtils.trimToEmpty(e.getTo());
            if (StringUtils.isBlank(from) || StringUtils.isBlank(to)) {
                continue;
            }
            if (!touchedBare.contains(bareSlug(from)) && !touchedBare.contains(bareSlug(to))) {
                continue;
            }
            JSONObject line = new JSONObject(true);
            line.put("from", from);
            line.put("to", to);
            line.put("type", StringUtils.defaultIfBlank(e.getType(), "relates_to"));
            line.put("evidence", StringUtils.defaultString(e.getEvidence()));
            line.put("date", today);
            String json = line.toJSONString();
            if (existing.contains(json) || sb.indexOf(json) >= 0) {
                continue;
            }
            sb.append(json).append('\n');
            count++;
        }
        if (count == 0) {
            return 0;
        }
        appendToFile(spaceCode, "graph/edges.jsonl", sb.toString());
        return count;
    }

    private String enrichMarker(String batchNo) {
        return "<!-- enrich-batch:" + batchNo + " -->";
    }

    private String readWikiRelFile(String spaceCode, String relFile) {
        Path file = resolveWikiRelFile(spaceCode, relFile);
        if (!Files.exists(file)) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private void appendToFile(String spaceCode, String relFile, String text) {
        Path file = resolveWikiRelFile(spaceCode, relFile);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, text.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new BaseException("写入 " + relFile + " 失败：" + e.getMessage());
        }
    }

    private Path resolveWikiRelFile(String spaceCode, String relFile) {
        Path base = resolveWikiBase(spaceCode);
        return base.resolve(relFile).normalize();
    }

    private Path resolveWikiBase(String spaceCode) {
        Path root = Paths.get(wikiProperties.getRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        String dir = wikiProperties.getSpaceDirs().getOrDefault(spaceCode, "wiki");
        return root.resolve(dir).normalize();
    }

    private Path resolveRawRoot() {
        Path root = Paths.get(ingestProperties.getRawRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.normalize();
    }

    private String readRawSnippets(List<String> rawPaths) {
        Path rawRoot = resolveRawRoot();
        StringBuilder sb = new StringBuilder();
        for (String rp : rawPaths) {
            String rel = rp.trim().replace('\\', '/');
            if (rel.startsWith("raw/")) {
                rel = rel.substring(4);
            }
            Path f = rawRoot.resolve(rel).normalize();
            if (!f.startsWith(rawRoot) || !Files.isRegularFile(f)) {
                sb.append("\n===== raw/").append(rel).append(" （不存在）=====\n");
                continue;
            }
            try {
                String text = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
                if (text.length() > RAW_SNIPPET_CHARS) {
                    text = text.substring(0, RAW_SNIPPET_CHARS) + "\n…（已截断）";
                }
                sb.append("\n===== raw/").append(rel).append(" =====\n").append(text).append('\n');
            } catch (IOException e) {
                sb.append("\n===== raw/").append(rel).append(" （读取失败）=====\n");
            }
        }
        return sb.length() == 0 ? "（无 raw 源）" : sb.toString();
    }

    private List<String> loadKnownSlugs(Long spaceId) {
        Set<String> slugs = new HashSet<>();
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<>();
        w.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        w.select(KbDocument::getSlug);
        if (spaceId != null) {
            w.eq(KbDocument::getSpaceId, spaceId);
        }
        for (KbDocument d : kbDocumentMapper.selectList(w)) {
            if (StringUtils.isNotBlank(d.getSlug())) {
                slugs.add(d.getSlug().trim());
            }
        }
        return slugs.stream().limit(80).collect(Collectors.toList());
    }

    private String bareSlug(String slug) {
        String s = slug.trim().replace('\\', '/');
        int slash = s.lastIndexOf('/');
        if (slash >= 0) {
            s = s.substring(slash + 1);
        }
        if (s.endsWith(".md")) {
            s = s.substring(0, s.length() - 3);
        }
        return s;
    }

    private String bulletList(List<String> items, int limit) {
        StringBuilder sb = new StringBuilder();
        int n = Math.min(items.size(), limit);
        for (int i = 0; i < n; i++) {
            sb.append("- ").append(items.get(i)).append('\n');
        }
        return sb.toString();
    }

    private String stripCodeFence(String text) {
        if (text == null) {
            return "";
        }
        String s = text.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            int lastFence = s.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                s = s.substring(firstNl + 1, lastFence).trim();
            }
        }
        return s;
    }

    private String tail(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return "…\n" + s.substring(s.length() - max);
    }
}
