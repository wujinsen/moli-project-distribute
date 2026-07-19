package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.knowledge.server.config.KbSearchProperties;
import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.dto.KbChunkAskRow;
import com.moli.knowledge.server.dto.retrieval.RerankCandidateDto;
import com.moli.knowledge.server.dto.retrieval.RerankHitDto;
import com.moli.knowledge.server.dto.retrieval.RerankResponseDto;
import com.moli.knowledge.server.dto.retrieval.VectorSearchHit;
import com.moli.knowledge.server.support.KbGraphExpandConfig;
import com.moli.knowledge.server.support.KbGraphExpandSupport;
import com.moli.knowledge.server.support.KbGraphMergeSupport;
import com.moli.knowledge.server.support.KbHybridRrfSupport;
import com.moli.knowledge.server.support.KbRetrievalClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.QaHistoryVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbQaLog;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbDocumentChunkMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbQaLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbAskService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.guard.InputGuardOutcome;
import com.moli.knowledge.server.guard.KbInputGuardService;
import com.moli.knowledge.server.guard.KbOutputGroundingService;
import com.moli.knowledge.server.support.KbLlmCallScenes;
import com.moli.knowledge.server.util.ShiroUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 知识库问答（Query）。流程对齐 kb/AGENTS.md §5 与 kb/tools/serve.py：
 * 定作用域 → 选 ≤topK 页 → 有 key 调 LLM 带引用作答 / 无 key 检索式 → 记 kb_qa_log。
 */
@Service
public class KbAskServiceImpl implements KbAskService {

    private static final Logger log = LoggerFactory.getLogger(KbAskServiceImpl.class);

    static final String SYSTEM_PROMPT =
            "你是茉莉企业知识库的问答助手。只能依据用户提供的【知识库页】内容作答，严禁编造。\n"
            + "要求：\n"
            + "1) 用中文、条理清晰，先给结论再给要点；\n"
            + "2) 每个关键结论后用 [[页slug]] 标注来源；\n"
            + "3) 若所给页无法回答，明说「知识库暂无相关内容」并建议应补充哪些资料；\n"
            + "4) 提炼要点，不要整页复述。";

    private static final Pattern LATIN = Pattern.compile("[a-z0-9]+");
    private static final Pattern CJK = Pattern.compile("[\\u4e00-\\u9fff]+");

    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbDocumentChunkMapper kbDocumentChunkMapper;
    @Resource
    private KbQaLogMapper kbQaLogMapper;
    @Resource
    private KbLlmClient kbLlmClient;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private com.moli.knowledge.server.config.KbSearchProperties kbSearchProperties;
    @Resource
    private com.moli.knowledge.server.config.KbAskProperties kbAskProperties;
    @Resource
    private KbRetrievalClient kbRetrievalClient;
    @Resource
    private KbGraphExpandSupport kbGraphExpandSupport;
    @Resource
    private KbInputGuardService inputGuardService;
    @Resource
    private KbOutputGroundingService outputGroundingService;

    @Override
    public AskResponse ask(AskRequest request) {
        return executeAsk(request, null);
    }

    /**
     * @param preGuard 非 null 时跳过重复检测（Agentic BLOCK 路径）
     */
    AskResponse executeAsk(AskRequest request, InputGuardOutcome preGuard) {
        String rawQuestion = request.getQuestion().trim();
        int citationTopK = request.getTopK() == null || request.getTopK() <= 0
                ? kbAskProperties.normalizedCitationTopK()
                : request.getTopK();
        int llmContextTopK = request.getLlmContextTopK() == null || request.getLlmContextTopK() <= 0
                ? kbAskProperties.normalizedLlmContextTopK()
                : request.getLlmContextTopK();
        int llmContextMaxChars = kbAskProperties.normalizedLlmContextMaxChars();

        List<Long> scopeSpaces = kbAclService.resolveReadableSpaceIds(
                request.getSpaceId(), request.getSpaceIds());
        if (scopeSpaces.isEmpty()) {
            AskResponse empty = new AskResponse();
            empty.setAnswer("无可访问的知识空间。");
            empty.setMode("retrieval");
            empty.setScope("全部类型");
            empty.setScopeReason("");
            empty.setProvider(kbLlmClient.getProvider());
            empty.setModel(kbLlmClient.getModel());
            return empty;
        }

        InputGuardOutcome guardOutcome = preGuard != null ? preGuard : inputGuardService.process(rawQuestion);
        String question = guardOutcome.getQuestionForProcessing();
        if (guardOutcome.isPiiOnlyReject()) {
            return buildGuardRejectResponse(request, guardOutcome, scopeSpaces, question);
        }

        Scope scope = detectScope(question);
        List<String> terms = buildTerms(question);
        int candidateLimit = kbSearchProperties.normalizedAskCandidateLimit();
        List<AskResponse.Citation> citations;
        String llmContext;

        if (kbSearchProperties.isChunkEnabled()) {
            String strategy = resolveRetrievalStrategy(request);
            ChunkRecallResult chunkResult;
            if (kbSearchProperties.isNgramStrategy(strategy)) {
                chunkResult = recallAndScoreChunks(scopeSpaces, scope, question, terms, candidateLimit);
            } else {
                chunkResult = recallHybridChunks(scopeSpaces, scope, question, terms, candidateLimit, strategy,
                        request);
            }
            DocumentRecallResult docResult = recallAndScoreDocuments(
                    scopeSpaces, scope, question, terms, candidateLimit);
            if (chunkResult != null && !chunkResult.scored.isEmpty()) {
                citations = buildMergedCitations(chunkResult.scored, docResult.scored, terms, citationTopK);
                llmContext = buildChunkContext(chunkResult.scored, llmContextTopK, llmContextMaxChars);
            } else {
                citations = buildDocumentCitations(docResult.scored, terms, citationTopK);
                llmContext = buildContext(docResult.scored, llmContextTopK, llmContextMaxChars);
            }
        } else {
            DocumentRecallResult docResult = recallAndScoreDocuments(
                    scopeSpaces, scope, question, terms, candidateLimit);
            citations = buildDocumentCitations(docResult.scored, terms, citationTopK);
            llmContext = buildContext(docResult.scored, llmContextTopK, llmContextMaxChars);
        }

        AskResponse resp = new AskResponse();
        resp.setScope(scope.include.isEmpty() ? "全部类型" : scope.include.toString());
        resp.setScopeReason(scope.reason);
        resp.setProvider(kbLlmClient.getProvider());
        resp.setModel(kbLlmClient.getModel());

        resp.setCitations(citations);
        if (guardOutcome.toVo() != null) {
            resp.setGuard(guardOutcome.toVo());
        }

        boolean injectBlocked = guardOutcome.isBlocked();
        if (injectBlocked) {
            String tail = citations.isEmpty() ? null : retrievalAnswer(question, citations);
            resp.setAnswer(inputGuardService.mergeBlockedAnswer(tail));
            resp.setMode("retrieval");
        } else if (shouldUseLlm(request) && !citations.isEmpty()) {
            try {
                Long askSpaceId = request.getSpaceId() != null ? request.getSpaceId()
                        : (scopeSpaces.size() == 1 ? scopeSpaces.get(0) : null);
                String answer = kbLlmClient.chat(KbLlmCallScenes.ASK, askSpaceId, SYSTEM_PROMPT,
                        "问题：" + question + "\n\n可用知识库页（只能依据这些作答）：\n\n" + llmContext);
                resp.setAnswer(answer);
                resp.setMode("generative");
                outputGroundingService.applyGrounding(resp, askSpaceId, guardOutcome);
            } catch (Exception e) {                       // noqa
                log.warn("LLM 调用失败，降级检索式: {}", e.getMessage());
                resp.setAnswer("> 调用 " + kbLlmClient.getProvider() + " 失败（" + e.getMessage()
                        + "），已回退检索式。\n\n" + retrievalAnswer(question, citations));
                resp.setMode("retrieval");
            }
        } else {
            String note = "";
            if (!Boolean.TRUE.equals(request.getUseLlm())) {
                note = "> 本次未启用 LLM 生成式，当前为检索式。\n\n";
            } else if (!kbLlmClient.usable()) {
                note = "> 后端未配置 LLM（平台 LLM 设置或 kb.llm.enabled/api-key），当前为检索式。\n\n";
            }
            resp.setAnswer(note + retrievalAnswer(question, citations));
            resp.setMode("retrieval");
        }

        Long qaLogId = saveLog(request, resp, scopeSpaces, question);
        resp.setQaLogId(qaLogId);
        return resp;
    }

    private AskResponse buildGuardRejectResponse(AskRequest request, InputGuardOutcome guardOutcome,
                                                 List<Long> scopeSpaces, String questionForLog) {
        AskResponse resp = new AskResponse();
        resp.setAnswer(InputGuardOutcome.PII_ONLY_ANSWER);
        resp.setMode("retrieval");
        resp.setScope("全部类型");
        resp.setScopeReason("");
        resp.setProvider(kbLlmClient.getProvider());
        resp.setModel(kbLlmClient.getModel());
        resp.setGuard(guardOutcome.toVo());
        Long qaLogId = saveLog(request, resp, scopeSpaces, questionForLog);
        resp.setQaLogId(qaLogId);
        return resp;
    }

    @Override
    public Page<QaHistoryVo> history(Long spaceId, int pageNum, int pageSize) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        LambdaQueryWrapper<KbQaLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbQaLog::getUserId, userId);
        if (spaceId != null) {
            kbAclService.assertCanRead(spaceId);
            wrapper.eq(KbQaLog::getSpaceId, spaceId);
        } else {
            List<Long> accessible = kbAclService.accessibleSpaceIds();
            if (accessible.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            wrapper.and(w -> w.in(KbQaLog::getSpaceId, accessible).or().isNull(KbQaLog::getSpaceId));
        }
        wrapper.orderByDesc(KbQaLog::getCreateTime);
        Page<KbQaLog> page = kbQaLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<QaHistoryVo> result = new Page<>(pageNum, pageSize, page.getTotal());
        List<QaHistoryVo> rows = new ArrayList<>();
        for (KbQaLog row : page.getRecords()) {
            rows.add(toHistoryVo(row));
        }
        result.setRecords(rows);
        return result;
    }

    @Override
    public void feedback(Long id, Integer useful) {
        if (id == null || useful == null) {
            throw new BaseException("日志ID与反馈值不能为空");
        }
        if (useful != 0 && useful != 1) {
            throw new BaseException("反馈值只能为 0 或 1");
        }
        KbQaLog row = kbQaLogMapper.selectById(id);
        if (row == null) {
            throw new BaseException("问答记录不存在");
        }
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            throw new BaseException("请先登录");
        }
        if (!userId.equals(row.getUserId()) && !kbAclService.isAdmin()) {
            throw new BaseException("无权评价该问答");
        }
        row.setUseful(useful);
        kbQaLogMapper.updateById(row);
    }

    private QaHistoryVo toHistoryVo(KbQaLog row) {
        QaHistoryVo vo = new QaHistoryVo();
        vo.setId(row.getId());
        vo.setSpaceId(row.getSpaceId());
        vo.setQuestion(row.getQuestion());
        vo.setAnswer(row.getAnswer());
        vo.setProvider(row.getProvider());
        vo.setModel(row.getModel());
        vo.setUseful(row.getUseful());
        vo.setCreateTime(row.getCreateTime());
        unpackScope(row.getScope(), vo);
        if (StringUtils.isNotBlank(row.getCitations())) {
            try {
                vo.setCitations(JSON.parseArray(row.getCitations(), AskResponse.Citation.class));
            } catch (Exception ignored) {
                vo.setCitations(new ArrayList<>());
            }
        }
        return vo;
    }

    private String packScope(String mode, String scope) {
        return (mode == null ? "retrieval" : mode) + "|" + (scope == null ? "" : scope);
    }

    private void unpackScope(String packed, QaHistoryVo vo) {
        if (StringUtils.isBlank(packed)) {
            vo.setMode("retrieval");
            return;
        }
        int idx = packed.indexOf('|');
        if (idx > 0 && ("generative".equals(packed.substring(0, idx)) || "retrieval".equals(packed.substring(0, idx)))) {
            vo.setMode(packed.substring(0, idx));
            vo.setScope(packed.substring(idx + 1));
        } else {
            vo.setMode("retrieval");
            vo.setScope(packed);
        }
    }

    // ------------------------------------------------------------------
    // 作用域识别
    // ------------------------------------------------------------------

    static class Scope {
        List<String> include = new ArrayList<>();
        Set<String> exclude = new LinkedHashSet<>();
        String reason;
    }

    private Scope detectScope(String question) {
        String q = question.toLowerCase();
        Scope s = new Scope();
        if (find(q, "(不要|别|排除).{0,4}(面试|八股)")) {
            s.exclude.add("interview");
        }
        if (find(q, "面试|八股|突击|怎么答")) {
            s.include.add("interview");
            s.reason = "命中『面试题』意图 → 限 interview";
            return s;
        }
        // 勿单独匹配「排查」——会把 ops guide（故障排查指南/监控与日志）滤掉（AI-5 M28）
        if (find(q, "方案|解决|最佳实践|优化|调优|排查方案|根因分析")) {
            s.include.add("article");
            s.include.add("concept");
            s.reason = "命中『方案/最佳实践』意图 → 限 article + concept";
            return s;
        }
        // 监控告警 + 故障排查联读 → guide（运维手册）
        if (find(q, "监控告警|告警.*排查|排查.*文档|监控与日志|故障排查")) {
            s.include.add("guide");
            s.include.add("service");
            s.reason = "命中『监控/排障文档』意图 → 限 guide + service";
            return s;
        }
        // 设计/原理类（概要设计、RBAC 模型怎么设计、JVM 怎么工作）→ 概念/文章，勿落 guide
        if (find(q, "是怎么设计|如何设计|怎样设计|怎么设计|设计思路|概要设计|架构设计|"
                + "是怎么工作|如何工作|怎样工作|怎么工作|工作原理|什么原理|怎么实现|如何实现")) {
            s.include.add("concept");
            s.include.add("article");
            s.include.add("service");
            s.reason = "命中『设计/原理』意图 → 限 concept + article + service";
            return s;
        }
        // 「三操作」是知识库产品术语，勿与「怎么操作」意图混淆
        if (!find(q, "三操作")
                && find(q, "怎么启动|如何启动|怎么部署|如何部署|怎么配置|如何配置|"
                + "怎么登录|如何登录|怎么操作|如何操作|操作步骤|怎么用|如何使用|怎么使用|"
                + "怎么跑|如何跑|怎么开通|如何开通|怎么提问|如何提问|怎么查询|如何查询|"
                + "怎么向|如何向|怎么同步|如何同步|怎么初始化|如何初始化|"
                + "体检怎么|提问怎么|查询怎么|同步怎么|启动|部署|配置|登录|步骤|开通|体检")) {
            s.include.add("guide");
            s.include.add("service");
            s.reason = "命中『怎么操作』意图 → 限 guide + service";
            return s;
        }
        s.reason = "未识别明确类型 → 全库检索";
        return s;
    }

    private boolean find(String text, String regex) {
        return Pattern.compile(regex).matcher(text).find();
    }

    // ------------------------------------------------------------------
    // 中文友好分词 + 打分 + 片段（对齐 serve.py）
    // ------------------------------------------------------------------

    private List<String> buildTerms(String query) {
        String q = query.trim().toLowerCase();
        Set<String> terms = new LinkedHashSet<>();
        if (q.length() >= 2) {
            terms.add(q);
        }
        Matcher lm = LATIN.matcher(q);
        while (lm.find()) {
            if (lm.group().length() >= 2) {
                terms.add(lm.group());
            }
        }
        Matcher cm = CJK.matcher(q);
        while (cm.find()) {
            String seg = cm.group();
            if (seg.length() == 1) {
                terms.add(seg);
            }
            for (int i = 0; i < seg.length() - 1; i++) {
                terms.add(seg.substring(i, i + 2));
            }
        }
        return new ArrayList<>(terms);
    }

    private static final int BODY_TERM_HIT_CAP = 8;

    private int score(KbDocument d, List<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        String title = lower(d.getTitle());
        String summary = lower(d.getSummary());
        String body = lower(d.getContent());
        String slug = lower(d.getSlug());
        int score = 0;
        for (String t : terms) {
            score += count(title, t) * 5;
            score += count(summary, t) * 3;
            score += Math.min(count(body, t), BODY_TERM_HIT_CAP);
            score += count(slug, t) * 4;
        }
        return finalizeRecallScore(score, slug);
    }

    private String snippet(KbDocument d, List<String> terms) {
        String body = d.getContent() == null ? "" : d.getContent();
        String low = body.toLowerCase();
        int pos = -1;
        for (String t : terms) {
            int i = low.indexOf(t);
            if (i != -1 && (pos == -1 || i < pos)) {
                pos = i;
            }
        }
        int width = 140;
        String snip;
        if (pos == -1) {
            snip = body.substring(0, Math.min(width, body.length()));
        } else {
            int start = Math.max(0, pos - width / 3);
            snip = body.substring(start, Math.min(start + width, body.length()));
        }
        return snip.replaceAll("\\s+", " ").trim();
    }

    private int count(String haystack, String needle) {
        if (StringUtils.isEmpty(haystack) || StringUtils.isEmpty(needle)) {
            return 0;
        }
        int c = 0, idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            c++;
            idx += needle.length();
        }
        return c;
    }

    private String lower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    // ------------------------------------------------------------------
    // 候选召回（chunk / 整页）
    // ------------------------------------------------------------------

    private static class ChunkRecallResult {
        final List<ChunkScored> scored;

        ChunkRecallResult(List<ChunkScored> scored) {
            this.scored = scored;
        }
    }

    private static class DocumentRecallResult {
        final List<Scored> scored;

        DocumentRecallResult(List<Scored> scored) {
            this.scored = scored;
        }
    }

    private ChunkRecallResult recallAndScoreChunks(List<Long> scopeSpaces, Scope scope, String question,
                                                   List<String> terms, int candidateLimit) {
        List<KbChunkAskRow> candidates = null;
        boolean usedFullText = false;
        if (kbSearchProperties.fullTextEnabled() && StringUtils.isNotBlank(question)) {
            try {
                candidates = kbDocumentChunkMapper.searchAskChunkCandidates(
                        scopeSpaces,
                        DocumentStatus.PUBLISHED.getCode(),
                        scope.include.isEmpty() ? null : scope.include,
                        scope.exclude.isEmpty() ? null : new ArrayList<>(scope.exclude),
                        question,
                        candidateLimit);
                usedFullText = candidates != null && !candidates.isEmpty();
                if (!usedFullText) {
                    candidates = kbDocumentChunkMapper.searchAskChunkCandidatesLike(
                            scopeSpaces,
                            DocumentStatus.PUBLISHED.getCode(),
                            scope.include.isEmpty() ? null : scope.include,
                            scope.exclude.isEmpty() ? null : new ArrayList<>(scope.exclude),
                            question,
                            candidateLimit);
                    usedFullText = candidates != null && !candidates.isEmpty();
                }
            } catch (Exception e) {
                log.warn("ask chunk 全文召回失败: {}", e.getMessage());
                candidates = null;
            }
        }
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        List<ChunkScored> scored = new ArrayList<>();
        for (KbChunkAskRow row : candidates) {
            if (row.getKbType() != null && scope.exclude.contains(row.getKbType())) {
                continue;
            }
            int s = scoreChunk(row, terms);
            if (s > 0 || usedFullText) {
                scored.add(new ChunkScored(row, s));
            }
        }
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        return scored.isEmpty() ? null : new ChunkRecallResult(scored);
    }

    private String resolveRetrievalStrategy(AskRequest request) {
        if (StringUtils.isNotBlank(request.getRetrievalStrategy())) {
            return KbSearchProperties.normalizeStrategy(request.getRetrievalStrategy());
        }
        return kbSearchProperties.normalizedRetrievalStrategy();
    }

    /** G-INV-1：ngram 忽略 graph；请求 graphExpand 覆盖配置默认。 */
    private boolean resolveGraphExpand(AskRequest request, String strategy) {
        if (kbSearchProperties.isNgramStrategy(strategy)) {
            return false;
        }
        if (request != null && request.getGraphExpand() != null) {
            return request.getGraphExpand();
        }
        return kbSearchProperties.graphEnabled();
    }

    /**
     * AI-2 hybrid：ngram + 向量 RRF 融合；AI-5 graph 在 rerank 前扩跳；sidecar 失败时仍有 ngram 候选。
     */
    private ChunkRecallResult recallHybridChunks(List<Long> scopeSpaces, Scope scope, String question,
                                                 List<String> terms, int candidateLimit, String strategy,
                                                 AskRequest request) {
        ChunkRecallResult ngramResult = recallAndScoreChunks(scopeSpaces, scope, question, terms, candidateLimit);
        List<ChunkScored> ngramChunks = ngramResult == null
                ? Collections.emptyList() : ngramResult.scored;

        try {
            List<String> includeTypes = scope.include.isEmpty() ? null : new ArrayList<>(scope.include);
            List<String> excludeTypes = scope.exclude.isEmpty() ? null : new ArrayList<>(scope.exclude);
            List<VectorSearchHit> vectorHits = kbRetrievalClient.search(
                    question, scopeSpaces, includeTypes, excludeTypes);
            vectorHits = filterVectorHitsAcl(vectorHits, scopeSpaces, scope);

            if (ngramChunks.isEmpty() && vectorHits.isEmpty()) {
                return ngramResult;
            }

            Map<Long, String> slugByChunkId = buildChunkSlugIndex(ngramChunks, vectorHits);
            List<Long> ngramIds = new ArrayList<>();
            for (ChunkScored cs : ngramChunks) {
                if (cs.row.getChunkId() != null) {
                    ngramIds.add(cs.row.getChunkId());
                }
            }
            Map<Long, Double> fused = KbHybridRrfSupport.rrfFuse(
                    ngramIds, vectorHits, kbSearchProperties.normalizedRrfK());
            fused = KbHybridRrfSupport.applyAnnexFusionPenalty(fused, slugByChunkId);
            List<Long> ordered = KbHybridRrfSupport.sortChunkIdsByRrf(fused);

            HybridRecallState state = new HybridRecallState(ordered, fused, ngramChunks);
            if (resolveGraphExpand(request, strategy)) {
                state = graphExpandAndMerge(state, scopeSpaces, scope, question, terms);
            }
            if (kbSearchProperties.isHybridRerankStrategy(strategy)) {
                state.ordered = applyRerank(question, state.ordered);
            }
            List<ChunkScored> scored = materializeHybridRecall(state, terms, candidateLimit);
            return scored.isEmpty() ? ngramResult : new ChunkRecallResult(scored);
        } catch (Exception e) {
            log.warn("hybrid 召回异常，降级 ngram: {}", e.getMessage());
            return ngramResult;
        }
    }

    /**
     * AI-5 §1.3 Step 1–4：BFS graphBoost + 邻居 chunk 注入/强化；G-INV-7 异常回退原 state。
     */
    private HybridRecallState graphExpandAndMerge(HybridRecallState state,
                                                  List<Long> scopeSpaces,
                                                  Scope scope,
                                                  String question,
                                                  List<String> terms) {
        if (state == null || state.ordered == null || state.ordered.isEmpty()) {
            return state;
        }
        KbGraphExpandConfig cfg = KbGraphExpandConfig.from(kbSearchProperties).withEnabled(true);
        try {
            Map<Long, KbChunkAskRow> rowByChunkId = buildRowMapForOrdered(state.ordered, state.ngramChunks);
            Map<Long, Integer> chunkScores = computeHybridChunkScores(state.ordered, state.fused, rowByChunkId, terms);
            Map<Long, Integer> docMax = KbGraphMergeSupport.docMaxScores(chunkScores, rowByChunkId);
            Map<Long, Double> entryNorm = KbGraphMergeSupport.buildEntryDocScoreNorm(docMax, cfg.getEntryTopE());
            if (entryNorm.isEmpty()) {
                return state;
            }

            Map<Long, Double> graphBoost = kbGraphExpandSupport.expand(entryNorm, scopeSpaces, cfg);
            if (graphBoost.isEmpty()) {
                return state;
            }

            Set<Long> baseDocIds = docMax.keySet();
            List<Long> neighborDocIds = graphBoost.keySet().stream()
                    .filter(d -> d != null && !baseDocIds.contains(d))
                    .collect(Collectors.toList());
            if (!neighborDocIds.isEmpty()) {
                List<String> includeTypes = scope.include.isEmpty() ? null : new ArrayList<>(scope.include);
                List<KbChunkAskRow> neighborRows = kbDocumentChunkMapper.selectAskChunksByDocumentIds(
                        neighborDocIds,
                        scopeSpaces,
                        DocumentStatus.PUBLISHED.getCode(),
                        question,
                        cfg.getChunksPerNeighbor() <= 0 ? 2 : cfg.getChunksPerNeighbor());
                for (KbChunkAskRow row : neighborRows) {
                    if (row.getChunkId() != null
                            && KbGraphMergeSupport.passesScope(row, scopeSpaces, includeTypes, scope.exclude)) {
                        rowByChunkId.put(row.getChunkId(), row);
                    }
                }
            }

            KbGraphMergeSupport.mergeGraphIntoPool(
                    state.ordered,
                    chunkScores,
                    rowByChunkId,
                    graphBoost,
                    cfg,
                    row -> scoreChunk(row, terms));

            state.chunkScores = chunkScores;
            return state;
        } catch (Exception ex) {
            log.warn("graph expand merge failed, degrade hybrid: {}", ex.getMessage());
            return state;
        }
    }

    private Map<Long, KbChunkAskRow> buildRowMapForOrdered(List<Long> ordered, List<ChunkScored> ngramChunks) {
        Map<Long, KbChunkAskRow> rowByChunkId = new HashMap<>();
        for (ChunkScored cs : ngramChunks) {
            if (cs.row.getChunkId() != null) {
                rowByChunkId.put(cs.row.getChunkId(), cs.row);
            }
        }
        List<Long> needLoad = new ArrayList<>();
        for (Long chunkId : ordered) {
            if (!rowByChunkId.containsKey(chunkId)) {
                needLoad.add(chunkId);
            }
        }
        if (!needLoad.isEmpty()) {
            List<KbChunkAskRow> loaded = kbDocumentChunkMapper.selectAskChunksByIds(
                    needLoad, DocumentStatus.PUBLISHED.getCode());
            for (KbChunkAskRow row : loaded) {
                if (row.getChunkId() != null) {
                    rowByChunkId.put(row.getChunkId(), row);
                }
            }
        }
        return rowByChunkId;
    }

    private Map<Long, Integer> computeHybridChunkScores(List<Long> ordered,
                                                        Map<Long, Double> fused,
                                                        Map<Long, KbChunkAskRow> rowByChunkId,
                                                        List<String> terms) {
        Map<Long, Integer> scores = new HashMap<>();
        for (Long chunkId : ordered) {
            KbChunkAskRow row = rowByChunkId.get(chunkId);
            if (row == null) {
                continue;
            }
            double rrf = fused.getOrDefault(chunkId, 0.0);
            scores.put(chunkId, (int) (rrf * 10_000) + scoreChunk(row, terms));
        }
        return scores;
    }

    private List<ChunkScored> materializeHybridRecall(HybridRecallState state,
                                                      List<String> terms,
                                                      int candidateLimit) {
        if (state.chunkScores != null && !state.chunkScores.isEmpty()) {
            Map<Long, KbChunkAskRow> rowByChunkId = buildRowMapForOrdered(state.ordered, state.ngramChunks);
            List<ChunkScored> scored = new ArrayList<>();
            int limit = Math.min(candidateLimit, state.ordered.size());
            for (int i = 0; i < limit; i++) {
                Long chunkId = state.ordered.get(i);
                KbChunkAskRow row = rowByChunkId.get(chunkId);
                if (row == null) {
                    continue;
                }
                int score = state.chunkScores.getOrDefault(chunkId, scoreChunk(row, terms));
                scored.add(new ChunkScored(row, score));
            }
            return scored;
        }
        return materializeFusedChunks(state.ordered, state.fused, state.ngramChunks, terms, candidateLimit);
    }

    private Map<Long, String> buildChunkSlugIndex(List<ChunkScored> ngramChunks, List<VectorSearchHit> vectorHits) {
        Map<Long, String> slugByChunkId = new HashMap<>();
        for (ChunkScored cs : ngramChunks) {
            if (cs.row.getChunkId() != null && StringUtils.isNotBlank(cs.row.getSlug())) {
                slugByChunkId.putIfAbsent(cs.row.getChunkId(), cs.row.getSlug());
            }
        }
        for (VectorSearchHit hit : vectorHits) {
            if (hit.getChunkId() != null && StringUtils.isNotBlank(hit.getSlug())) {
                slugByChunkId.putIfAbsent(hit.getChunkId(), hit.getSlug());
            }
        }
        return slugByChunkId;
    }

    private List<VectorSearchHit> filterVectorHitsAcl(List<VectorSearchHit> hits,
                                                      List<Long> scopeSpaces, Scope scope) {
        if (hits == null || hits.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> allowed = new HashSet<>(scopeSpaces);
        List<VectorSearchHit> out = new ArrayList<>();
        for (VectorSearchHit hit : hits) {
            if (hit.getChunkId() == null || hit.getSpaceId() == null) {
                continue;
            }
            if (!allowed.contains(hit.getSpaceId())) {
                continue;
            }
            if (hit.getKbType() != null && scope.exclude.contains(hit.getKbType())) {
                continue;
            }
            if (!scope.include.isEmpty() && hit.getKbType() != null
                    && !scope.include.contains(hit.getKbType())) {
                continue;
            }
            out.add(hit);
        }
        return out;
    }

    private List<Long> applyRerank(String question, List<Long> ordered) {
        if (ordered == null || ordered.isEmpty()) {
            return ordered;
        }
        List<Long> fusionOrder = new ArrayList<>(ordered);
        try {
            int poolSize = kbSearchProperties.normalizedRerankPool();
            List<Long> poolIds = fusionOrder.subList(0, Math.min(poolSize, fusionOrder.size()));
            List<KbChunkAskRow> rows = kbDocumentChunkMapper.selectAskChunksByIds(
                    new ArrayList<>(poolIds), DocumentStatus.PUBLISHED.getCode());
            Map<Long, KbChunkAskRow> rowMap = new HashMap<>();
            for (KbChunkAskRow row : rows) {
                rowMap.put(row.getChunkId(), row);
            }
            List<RerankCandidateDto> candidates = new ArrayList<>();
            for (Long chunkId : poolIds) {
                KbChunkAskRow row = rowMap.get(chunkId);
                if (row == null) {
                    continue;
                }
                RerankCandidateDto dto = new RerankCandidateDto();
                dto.setChunkId(chunkId);
                dto.setText(chunkPlainText(row));
                candidates.add(dto);
            }
            RerankResponseDto resp = kbRetrievalClient.rerank(
                    question, candidates, kbSearchProperties.normalizedRerankTopM());
            if (resp == null || resp.getResults() == null || resp.getResults().isEmpty()) {
                return fusionOrder;
            }
            List<Long> reordered = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
            for (RerankHitDto hit : resp.getResults()) {
                if (hit.getChunkId() != null && poolIds.contains(hit.getChunkId()) && seen.add(hit.getChunkId())) {
                    reordered.add(hit.getChunkId());
                }
            }
            for (Long chunkId : poolIds) {
                if (seen.add(chunkId)) {
                    reordered.add(chunkId);
                }
            }
            if (fusionOrder.size() > poolSize) {
                reordered.addAll(fusionOrder.subList(poolSize, fusionOrder.size()));
            }
            return reordered;
        } catch (Exception e) {
            log.warn("rerank 异常，回退融合序: {}", e.getMessage());
            return fusionOrder;
        }
    }

    private List<ChunkScored> materializeFusedChunks(List<Long> ordered, Map<Long, Double> fused,
                                                     List<ChunkScored> ngramChunks, List<String> terms,
                                                     int candidateLimit) {
        Map<Long, KbChunkAskRow> ngramRowMap = new HashMap<>();
        for (ChunkScored cs : ngramChunks) {
            if (cs.row.getChunkId() != null) {
                ngramRowMap.put(cs.row.getChunkId(), cs.row);
            }
        }
        List<Long> needLoad = new ArrayList<>();
        for (Long chunkId : ordered) {
            if (!ngramRowMap.containsKey(chunkId)) {
                needLoad.add(chunkId);
            }
        }
        if (!needLoad.isEmpty()) {
            List<KbChunkAskRow> loaded = kbDocumentChunkMapper.selectAskChunksByIds(
                    needLoad, DocumentStatus.PUBLISHED.getCode());
            for (KbChunkAskRow row : loaded) {
                ngramRowMap.put(row.getChunkId(), row);
            }
        }
        List<ChunkScored> scored = new ArrayList<>();
        int limit = Math.min(candidateLimit, ordered.size());
        for (int i = 0; i < limit; i++) {
            Long chunkId = ordered.get(i);
            KbChunkAskRow row = ngramRowMap.get(chunkId);
            if (row == null) {
                continue;
            }
            double rrf = fused.getOrDefault(chunkId, 0.0);
            int score = (int) (rrf * 10_000) + scoreChunk(row, terms);
            scored.add(new ChunkScored(row, score));
        }
        return scored;
    }

    private String chunkPlainText(KbChunkAskRow row) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(row.getHeading())) {
            sb.append(row.getHeading().trim());
        }
        if (StringUtils.isNotBlank(row.getContent())) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(row.getContent().trim());
        }
        return sb.toString();
    }

    private DocumentRecallResult recallAndScoreDocuments(List<Long> scopeSpaces, Scope scope, String question,
                                                         List<String> terms, int candidateLimit) {
        List<KbDocument> candidates = null;
        boolean usedFullText = false;
        if (kbSearchProperties.fullTextEnabled() && StringUtils.isNotBlank(question)) {
            try {
                candidates = kbDocumentMapper.searchAskCandidates(
                        scopeSpaces,
                        DocumentStatus.PUBLISHED.getCode(),
                        scope.include.isEmpty() ? null : scope.include,
                        scope.exclude.isEmpty() ? null : new ArrayList<>(scope.exclude),
                        question,
                        candidateLimit);
                usedFullText = candidates != null && !candidates.isEmpty();
            } catch (Exception e) {
                log.warn("ask 全文召回失败，回退全量扫描: {}", e.getMessage());
                candidates = null;
            }
        }
        if (candidates == null || candidates.isEmpty()) {
            LambdaQueryWrapper<KbDocument> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
            wrapper.eq(KbDocument::getStatus, DocumentStatus.PUBLISHED.getCode());
            if (scopeSpaces.size() == 1) {
                wrapper.eq(KbDocument::getSpaceId, scopeSpaces.get(0));
            } else {
                wrapper.in(KbDocument::getSpaceId, scopeSpaces);
            }
            if (!scope.include.isEmpty()) {
                wrapper.in(KbDocument::getKbType, scope.include);
            }
            candidates = kbDocumentMapper.selectList(wrapper);
            usedFullText = false;
        }
        List<Scored> scored = new ArrayList<>();
        for (KbDocument d : candidates) {
            if (d.getKbType() != null && scope.exclude.contains(d.getKbType())) {
                continue;
            }
            int s = score(d, terms);
            if (s > 0 || usedFullText) {
                scored.add(new Scored(d, s));
            }
        }
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        return new DocumentRecallResult(scored);
    }

    private List<AskResponse.Citation> buildDocumentCitations(List<Scored> scored, List<String> terms, int topK) {
        List<AskResponse.Citation> citations = new ArrayList<>();
        for (int i = 0; i < scored.size() && i < topK; i++) {
            KbDocument d = scored.get(i).doc;
            citations.add(new AskResponse.Citation(d.getId(), d.getSpaceId(), d.getSlug(), d.getTitle(),
                    d.getKbType(), snippet(d, terms)));
        }
        return citations;
    }

    private List<AskResponse.Citation> buildMergedCitations(List<ChunkScored> chunkScored,
                                                            List<Scored> docScored,
                                                            List<String> terms, int topK) {
        Map<Long, MergedCitation> merged = new HashMap<>();
        for (ChunkScored cs : chunkScored) {
            mergeCitation(merged, cs.row.getDocumentId(), cs.score, cs.row, null);
        }
        for (Scored s : docScored) {
            mergeCitation(merged, s.doc.getId(), s.score, null, s.doc);
        }
        List<MergedCitation> rows = new ArrayList<>(merged.values());
        rows.sort((a, b) -> Integer.compare(b.score, a.score));
        List<AskResponse.Citation> citations = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < topK; i++) {
            MergedCitation m = rows.get(i);
            if (m.chunk != null) {
                KbChunkAskRow r = m.chunk;
                citations.add(new AskResponse.Citation(r.getDocumentId(), r.getSpaceId(), r.getSlug(),
                        r.getTitle(), r.getKbType(), snippetChunk(r, terms)));
            } else if (m.doc != null) {
                KbDocument d = m.doc;
                citations.add(new AskResponse.Citation(d.getId(), d.getSpaceId(), d.getSlug(), d.getTitle(),
                        d.getKbType(), snippet(d, terms)));
            }
        }
        return citations;
    }

    private void mergeCitation(Map<Long, MergedCitation> merged, Long docId, int score,
                               KbChunkAskRow chunk, KbDocument doc) {
        MergedCitation existing = merged.get(docId);
        if (existing == null) {
            merged.put(docId, new MergedCitation(score, chunk, doc));
            return;
        }
        if (score > existing.score) {
            existing.score = score;
            if (chunk != null) {
                existing.chunk = chunk;
            }
            if (doc != null) {
                existing.doc = doc;
            }
        } else {
            if (chunk != null && existing.chunk == null) {
                existing.chunk = chunk;
            }
            if (doc != null && existing.doc == null) {
                existing.doc = doc;
            }
        }
    }

    private List<AskResponse.Citation> buildChunkCitations(List<ChunkScored> scored, List<String> terms, int topK) {
        Set<Long> seen = new LinkedHashSet<>();
        List<AskResponse.Citation> citations = new ArrayList<>();
        for (ChunkScored cs : scored) {
            Long docId = cs.row.getDocumentId();
            if (seen.contains(docId)) {
                continue;
            }
            seen.add(docId);
            KbChunkAskRow r = cs.row;
            citations.add(new AskResponse.Citation(r.getDocumentId(), r.getSpaceId(), r.getSlug(), r.getTitle(),
                    r.getKbType(), snippetChunk(r, terms)));
            if (citations.size() >= topK) {
                break;
            }
        }
        return citations;
    }

    private int scoreChunk(KbChunkAskRow c, List<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        String heading = lower(c.getHeading());
        String title = lower(c.getTitle());
        String summary = lower(c.getSummary());
        String slug = lower(c.getSlug());
        String body = lower(c.getContent());
        int score = 0;
        for (String t : terms) {
            score += count(heading, t) * 8;
            score += count(title, t) * 6;
            score += count(slug, t) * 5;
            score += count(summary, t) * 3;
            score += Math.min(count(body, t), BODY_TERM_HIT_CAP);
        }
        return finalizeRecallScore(score, slug);
    }

    /** 压低 annex 归档长文的正文刷分，避免盖过精炼概念页。 */
    private int finalizeRecallScore(int score, String slug) {
        if (score <= 0 || StringUtils.isBlank(slug)) {
            return score;
        }
        if (slug.toLowerCase().contains("/annex-")) {
            return score / 3;
        }
        return score;
    }

    private String snippetChunk(KbChunkAskRow c, List<String> terms) {
        String body = c.getContent() == null ? "" : c.getContent();
        String low = body.toLowerCase();
        int pos = -1;
        for (String t : terms) {
            int i = low.indexOf(t);
            if (i != -1 && (pos == -1 || i < pos)) {
                pos = i;
            }
        }
        int width = 140;
        String snip;
        if (pos == -1) {
            snip = body.substring(0, Math.min(width, body.length()));
        } else {
            int start = Math.max(0, pos - width / 3);
            snip = body.substring(start, Math.min(start + width, body.length()));
        }
        snip = snip.replaceAll("\\s+", " ").trim();
        if (StringUtils.isNotBlank(c.getHeading())) {
            return c.getHeading() + " · " + snip;
        }
        return snip;
    }

    private String buildChunkContext(List<ChunkScored> scored, int topK, int maxChars) {
        int budget = maxChars;
        StringBuilder sb = new StringBuilder();
        int used = 0;
        Set<Long> docsInContext = new LinkedHashSet<>();
        Map<Long, Integer> perDoc = new HashMap<>();
        for (ChunkScored cs : scored) {
            Long docId = cs.row.getDocumentId();
            if (!docsInContext.contains(docId)) {
                if (docsInContext.size() >= topK) {
                    continue;
                }
                docsInContext.add(docId);
            }
            int n = perDoc.getOrDefault(docId, 0);
            if (n >= 2) {
                continue;
            }
            perDoc.put(docId, n + 1);
            KbChunkAskRow r = cs.row;
            String head = r.getHeading() == null ? "" : r.getHeading();
            String chunk = "## 节：[[" + r.getSlug() + "]]（" + r.getTitle() + "）\n" + head + "\n"
                    + (r.getContent() == null ? "" : r.getContent().trim()) + "\n";
            if (used + chunk.length() > budget) {
                chunk = chunk.substring(0, Math.max(0, budget - used));
            }
            sb.append(chunk).append("\n");
            used += chunk.length();
            if (used >= budget) {
                break;
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // 检索式答案 / LLM 上下文 / LLM 调用
    // ------------------------------------------------------------------

    /** 前端显式 useLlm=true 且后端 kb.llm 已就绪时才调 LLM。 */
    private boolean shouldUseLlm(AskRequest request) {
        return Boolean.TRUE.equals(request.getUseLlm()) && kbLlmClient.usable();
    }

    private String retrievalAnswer(String question, List<AskResponse.Citation> citations) {
        if (citations.isEmpty()) {
            return "知识库暂无相关内容。建议先 ingest 相关源后重试。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("围绕「").append(question).append("」，检索到 ")
                .append(citations.size()).append(" 个相关页（按相关度排序）：\n");
        int i = 1;
        for (AskResponse.Citation c : citations) {
            sb.append(i++).append(". [[").append(c.getSlug()).append("]]（")
                    .append(c.getKbType()).append("）\n");
        }
        sb.append("\n> 当前为检索式结果；配置 kb.llm 后将变为基于以上页的生成式带引用答案。");
        return sb.toString();
    }

    private String buildContext(List<Scored> scored, int topK, int maxChars) {
        int budget = maxChars;
        StringBuilder sb = new StringBuilder();
        int used = 0;
        for (int i = 0; i < scored.size() && i < topK; i++) {
            KbDocument d = scored.get(i).doc;
            String body = d.getContent() == null ? "" : d.getContent().trim();
            String chunk = "## 页：[[" + d.getSlug() + "]]（" + d.getTitle() + "）\n" + body + "\n";
            if (used + chunk.length() > budget) {
                chunk = chunk.substring(0, Math.max(0, budget - used));
            }
            sb.append(chunk).append("\n");
            used += chunk.length();
            if (used >= budget) {
                break;
            }
        }
        return sb.toString();
    }

    private Long saveLog(AskRequest request, AskResponse resp, List<Long> scopeSpaces, String questionForLog) {
        try {
            KbQaLog qa = new KbQaLog();
            Long id = com.moli.common.core.IdGenerator.getId();
            qa.setId(id);
            qa.setSpaceId(scopeSpaces.size() == 1 ? scopeSpaces.get(0) : null);
            qa.setUserId(ShiroUtils.getUserId());
            qa.setQuestion(questionForLog);
            qa.setAnswer(resp.getAnswer());
            qa.setCitations(JSON.toJSONString(resp.getCitations()));
            qa.setScope(packScope(resp.getMode(), resp.getScope()));
            qa.setProvider(resp.getProvider());
            qa.setModel(resp.getModel());
            qa.setCreateTime(new Date());
            kbQaLogMapper.insert(qa);
            return id;
        } catch (Exception e) {                            // noqa: 记录日志失败不影响问答
            log.warn("写 kb_qa_log 失败: {}", e.getMessage());
            return null;
        }
    }

    private static class MergedCitation {
        int score;
        KbChunkAskRow chunk;
        KbDocument doc;

        MergedCitation(int score, KbChunkAskRow chunk, KbDocument doc) {
            this.score = score;
            this.chunk = chunk;
            this.doc = doc;
        }
    }

    private static class HybridRecallState {
        List<Long> ordered;
        Map<Long, Double> fused;
        List<ChunkScored> ngramChunks;
        Map<Long, Integer> chunkScores;

        HybridRecallState(List<Long> ordered, Map<Long, Double> fused, List<ChunkScored> ngramChunks) {
            this.ordered = ordered;
            this.fused = fused;
            this.ngramChunks = ngramChunks;
        }
    }

    static class ChunkScored {
        final KbChunkAskRow row;
        final int score;

        ChunkScored(KbChunkAskRow row, int score) {
            this.row = row;
            this.score = score;
        }
    }

    private static class Scored {
        final KbDocument doc;
        final int score;

        Scored(KbDocument doc, int score) {
            this.doc = doc;
            this.score = score;
        }
    }

    // ------------------------------------------------------------------
    // AI-7 Agentic：package-private 召回/合并/生成（复用 AI-2/AI-5，A-INV-5）
    // ------------------------------------------------------------------

    Scope detectScopeForAgentic(String question) {
        return detectScope(question);
    }

    List<String> buildTermsForAgentic(String question) {
        return buildTerms(question);
    }

    static final class QueryRecallResult {
        final String query;
        final Scope scope;
        final List<String> terms;
        final List<ChunkScored> chunkScored;
        final List<Scored> docScored;

        QueryRecallResult(String query, Scope scope, List<String> terms,
                          List<ChunkScored> chunkScored, List<Scored> docScored) {
            this.query = query;
            this.scope = scope;
            this.terms = terms;
            this.chunkScored = chunkScored;
            this.docScored = docScored;
        }
    }

    static final class MergedPool {
        final List<ChunkScored> mergedChunks;
        final List<Scored> mergedDocs;
        final Set<String> poolSlugs;

        MergedPool(List<ChunkScored> mergedChunks, List<Scored> mergedDocs, Set<String> poolSlugs) {
            this.mergedChunks = mergedChunks;
            this.mergedDocs = mergedDocs;
            this.poolSlugs = poolSlugs;
        }
    }

    QueryRecallResult recallForQuery(AskRequest request, String query, List<Long> scopeSpaces) {
        Scope scope = detectScope(query);
        List<String> terms = buildTerms(query);
        int candidateLimit = kbSearchProperties.normalizedAskCandidateLimit();
        List<ChunkScored> chunkScored = null;
        List<Scored> docScored;
        if (kbSearchProperties.isChunkEnabled()) {
            String strategy = resolveRetrievalStrategy(request);
            ChunkRecallResult chunkResult;
            if (kbSearchProperties.isNgramStrategy(strategy)) {
                chunkResult = recallAndScoreChunks(scopeSpaces, scope, query, terms, candidateLimit);
            } else {
                chunkResult = recallHybridChunks(scopeSpaces, scope, query, terms, candidateLimit, strategy,
                        request);
            }
            DocumentRecallResult docResult = recallAndScoreDocuments(
                    scopeSpaces, scope, query, terms, candidateLimit);
            chunkScored = chunkResult != null ? chunkResult.scored : null;
            docScored = docResult.scored;
        } else {
            DocumentRecallResult docResult = recallAndScoreDocuments(
                    scopeSpaces, scope, query, terms, candidateLimit);
            docScored = docResult.scored;
        }
        return new QueryRecallResult(query, scope, terms, chunkScored, docScored);
    }

    MergedPool mergeQueryRecalls(List<QueryRecallResult> recalls) {
        Map<Long, ChunkScored> byChunkId = new HashMap<>();
        Map<Long, Scored> byDocId = new HashMap<>();
        Set<String> poolSlugs = new LinkedHashSet<>();
        for (QueryRecallResult qr : recalls) {
            if (qr.chunkScored != null) {
                for (ChunkScored cs : qr.chunkScored) {
                    Long chunkId = cs.row.getChunkId();
                    if (chunkId != null) {
                        byChunkId.merge(chunkId, cs, (a, b) -> a.score >= b.score ? a : b);
                        if (StringUtils.isNotBlank(cs.row.getSlug())) {
                            poolSlugs.add(cs.row.getSlug());
                        }
                    } else if (cs.row.getDocumentId() != null) {
                        byDocId.merge(cs.row.getDocumentId(),
                                new Scored(null, cs.score),
                                (a, b) -> a.score >= b.score ? a : b);
                        if (StringUtils.isNotBlank(cs.row.getSlug())) {
                            poolSlugs.add(cs.row.getSlug());
                        }
                    }
                }
            }
            if (qr.docScored != null) {
                for (Scored s : qr.docScored) {
                    if (s.doc != null && s.doc.getId() != null) {
                        byDocId.merge(s.doc.getId(), s, (a, b) -> a.score >= b.score ? a : b);
                        if (StringUtils.isNotBlank(s.doc.getSlug())) {
                            poolSlugs.add(s.doc.getSlug());
                        }
                    }
                }
            }
        }
        List<ChunkScored> mergedChunks = new ArrayList<>(byChunkId.values());
        mergedChunks.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Scored> mergedDocs = new ArrayList<>();
        for (Scored s : byDocId.values()) {
            if (s.doc != null) {
                mergedDocs.add(s);
            }
        }
        mergedDocs.sort((a, b) -> Integer.compare(b.score, a.score));
        return new MergedPool(mergedChunks, mergedDocs, poolSlugs);
    }

    List<String> slugsFromRecall(QueryRecallResult recall) {
        Set<String> slugs = new LinkedHashSet<>();
        if (recall.chunkScored != null) {
            for (ChunkScored cs : recall.chunkScored) {
                if (StringUtils.isNotBlank(cs.row.getSlug())) {
                    slugs.add(cs.row.getSlug());
                }
            }
        }
        if (recall.docScored != null) {
            for (Scored s : recall.docScored) {
                if (s.doc != null && StringUtils.isNotBlank(s.doc.getSlug())) {
                    slugs.add(s.doc.getSlug());
                }
            }
        }
        return new ArrayList<>(slugs);
    }

    AskResponse generateFromPool(AskRequest request, String question, MergedPool pool,
                                 Scope displayScope, List<String> snippetTerms,
                                 int citationTopK, int llmContextTopK, int llmContextMaxChars,
                                 List<Long> scopeSpaces, boolean useLlm) {
        List<AskResponse.Citation> citations;
        String llmContext;
        if (pool.mergedChunks != null && !pool.mergedChunks.isEmpty()) {
            citations = buildMergedCitations(pool.mergedChunks, pool.mergedDocs, snippetTerms, citationTopK);
            llmContext = buildChunkContext(pool.mergedChunks, llmContextTopK, llmContextMaxChars);
        } else {
            citations = buildDocumentCitations(pool.mergedDocs, snippetTerms, citationTopK);
            llmContext = buildContext(pool.mergedDocs, llmContextTopK, llmContextMaxChars);
        }
        citations = filterCitationsToPool(citations, pool.poolSlugs);

        AskResponse resp = new AskResponse();
        resp.setScope(displayScope.include.isEmpty() ? "全部类型" : displayScope.include.toString());
        resp.setScopeReason(displayScope.reason);
        resp.setProvider(kbLlmClient.getProvider());
        resp.setModel(kbLlmClient.getModel());
        resp.setCitations(citations);

        if (useLlm && kbLlmClient.usable() && !citations.isEmpty()) {
            try {
                Long askSpaceId = request.getSpaceId() != null ? request.getSpaceId()
                        : (scopeSpaces.size() == 1 ? scopeSpaces.get(0) : null);
                String answer = kbLlmClient.chat(KbLlmCallScenes.ASK, askSpaceId, SYSTEM_PROMPT,
                        "问题：" + question + "\n\n可用知识库页（只能依据这些作答）：\n\n" + llmContext);
                resp.setAnswer(answer);
                resp.setMode("generative");
            } catch (Exception e) {
                log.warn("Agentic LLM 调用失败，降级检索式: {}", e.getMessage());
                resp.setAnswer("> 调用 " + kbLlmClient.getProvider() + " 失败（" + e.getMessage()
                        + "），已回退检索式。\n\n" + retrievalAnswer(question, citations));
                resp.setMode("retrieval");
            }
        } else {
            String note = "";
            if (!useLlm) {
                note = "> 本次未启用 LLM 生成式，当前为检索式。\n\n";
            } else if (!kbLlmClient.usable()) {
                note = "> 后端未配置 LLM（平台 LLM 设置或 kb.llm.enabled/api-key），当前为检索式。\n\n";
            }
            resp.setAnswer(note + retrievalAnswer(question, citations));
            resp.setMode("retrieval");
        }
        return resp;
    }

    static List<AskResponse.Citation> filterCitationsToPool(List<AskResponse.Citation> citations,
                                                            Set<String> poolSlugs) {
        if (citations == null || citations.isEmpty()) {
            return citations == null ? new ArrayList<>() : citations;
        }
        if (poolSlugs == null || poolSlugs.isEmpty()) {
            return citations;
        }
        List<AskResponse.Citation> out = new ArrayList<>();
        for (AskResponse.Citation c : citations) {
            if (c.getSlug() != null && poolSlugs.contains(c.getSlug())) {
                out.add(c);
            }
        }
        return out;
    }

    Long saveQaLog(AskRequest request, AskResponse resp, List<Long> scopeSpaces) {
        return saveLog(request, resp, scopeSpaces, request.getQuestion());
    }
}
