package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.knowledge.server.config.KbAgenticProperties;
import com.moli.knowledge.server.config.KbAskProperties;
import com.moli.knowledge.server.dto.AgenticAskRequest;
import com.moli.knowledge.server.dto.AgenticAskVo;
import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.entity.KbAgenticTrace;
import com.moli.knowledge.server.mapper.KbAgenticTraceMapper;
import com.moli.knowledge.server.service.KbAgenticAskService;
import com.moli.knowledge.server.service.KbAskService;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.guard.AskGuardVo;
import com.moli.knowledge.server.guard.InputGuardOutcome;
import com.moli.knowledge.server.guard.KbGroundingSelfCheckSupport;
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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * AI-7 Agentic RAG 编排（S0–S5 · Phase B：自检/回补/trace）。
 */
@Service
public class KbAgenticAskServiceImpl implements KbAgenticAskService {

    private static final Logger log = LoggerFactory.getLogger(KbAgenticAskServiceImpl.class);

    private static final String REWRITE_DECOMPOSE_SYSTEM =
            "你是知识库检索规划器。给定用户问题，做两件事：\n"
            + "1) 改写：修正错别字/口语/中英混写，产出适合全文+向量检索的规范中文查询；不改变原意、不臆造实体。\n"
            + "2) 判定是否“多跳”（需综合 2+ 主题/页才能答）。是→拆成 2~{maxSub} 个彼此独立、可各自检索的子问题；否→子问题为空。\n"
            + "只输出 JSON：{\"rewritten\":\"...\",\"multiHop\":true|false,\"subQuestions\":[\"...\",\"...\"]}";

    @Resource
    private KbAskService kbAskService;
    @Resource
    private KbAskServiceImpl kbAskServiceImpl;
    @Resource
    private KbAgenticProperties kbAgenticProperties;
    @Resource
    private KbAskProperties kbAskProperties;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbLlmClient kbLlmClient;
    @Resource
    private KbAgenticTraceMapper kbAgenticTraceMapper;
    @Resource
    private KbInputGuardService inputGuardService;
    @Resource
    private KbGroundingSelfCheckSupport groundingSelfCheckSupport;
    @Resource
    private KbOutputGroundingService outputGroundingService;

    @Override
    public AgenticAskVo agenticAsk(AgenticAskRequest request) {
        String question = request.getQuestion().trim();
        boolean effectiveAgentic = request.getAgentic() != null
                ? request.getAgentic()
                : kbAgenticProperties.isEnabled();
        boolean useLlm = request.getUseLlm() != null ? request.getUseLlm() : true;

        if (!effectiveAgentic) {
            return degradeSingleTurn(request, false, false, false);
        }
        if (!useLlm) {
            return degradeSingleTurn(request, false, true, true);
        }
        if (!kbLlmClient.usable()) {
            return degradeSingleTurn(request, false, true, true);
        }

        List<Long> scopeSpaces = kbAclService.resolveReadableSpaceIds(
                request.getSpaceId(), request.getSpaceIds());
        if (scopeSpaces.isEmpty()) {
            return emptyScopeVo();
        }

        InputGuardOutcome guardOutcome = inputGuardService.process(question);
        if (guardOutcome.isPiiOnlyReject()) {
            return blockedAgenticVo(request, guardOutcome);
        }
        if (guardOutcome.isBlocked()) {
            return blockedAgenticVo(request, guardOutcome);
        }
        question = guardOutcome.getQuestionForProcessing();

        long startMs = System.currentTimeMillis();
        AskRequest askTemplate = request.toAskRequest(true);
        int citationTopK = normalizedCitationTopK(request);
        int llmContextTopK = normalizedLlmContextTopK(request);
        int llmContextMaxChars = kbAskProperties.normalizedLlmContextMaxChars();
        KbAskServiceImpl.Scope displayScope = kbAskServiceImpl.detectScopeForAgentic(question);
        List<String> snippetTerms = kbAskServiceImpl.buildTermsForAgentic(question);
        Long askSpaceId = resolveAskSpaceId(request.getSpaceId(), scopeSpaces);

        RewriteDecomposeResult rewrite = rewriteDecompose(question, askSpaceId);
        if (budgetExceeded(startMs)) {
            return finalizeBudgetExceeded(request, question, rewrite, scopeSpaces, askTemplate,
                    displayScope, snippetTerms, citationTopK, llmContextTopK, llmContextMaxChars,
                    startMs, new ArrayList<>(), buildQueries(rewrite), guardOutcome);
        }

        int maxRounds = kbAgenticProperties.normalizedMaxRounds();
        double coverageThreshold = kbAgenticProperties.getCoverageThreshold() > 0
                ? kbAgenticProperties.getCoverageThreshold() : 0.8;
        boolean selfCheckEnabled = kbAgenticProperties.isSelfCheck();

        List<String> queries = buildQueries(rewrite);
        List<KbAskServiceImpl.QueryRecallResult> accumulatedRecalls = new ArrayList<>();
        List<List<String>> slugsPerRound = new ArrayList<>();
        List<RoundStep> steps = new ArrayList<>();

        AskResponse generated = null;
        Double lastCoverage = null;
        List<String> lastUnsupported = new ArrayList<>();
        boolean selfCheckDegraded = false;
        int round = 1;

        while (round <= maxRounds) {
            if (budgetExceeded(startMs)) {
                break;
            }

            List<KbAskServiceImpl.QueryRecallResult> roundRecalls = new ArrayList<>();
            List<String> roundSlugs = new ArrayList<>();
            for (String q : queries) {
                if (budgetExceeded(startMs)) {
                    break;
                }
                KbAskServiceImpl.QueryRecallResult recall =
                        kbAskServiceImpl.recallForQuery(askTemplate, q, scopeSpaces);
                roundRecalls.add(recall);
                accumulatedRecalls.add(recall);
                roundSlugs.addAll(kbAskServiceImpl.slugsFromRecall(recall));
            }
            slugsPerRound.add(new ArrayList<>(new LinkedHashSet<>(roundSlugs)));

            KbAskServiceImpl.MergedPool pool = kbAskServiceImpl.mergeQueryRecalls(accumulatedRecalls);
            generated = kbAskServiceImpl.generateFromPool(
                    askTemplate, question, pool, displayScope, snippetTerms,
                    citationTopK, llmContextTopK, llmContextMaxChars, scopeSpaces, true);
            generated.setCitations(
                    KbAskServiceImpl.filterCitationsToPool(generated.getCitations(), pool.poolSlugs));

            if (!selfCheckEnabled) {
                steps.add(RoundStep.of(round, queries, slugsPerRound.get(slugsPerRound.size() - 1),
                        null, new ArrayList<>(), stepLatency(startMs)));
                break;
            }
            // A-INV-8：自检/回补前若已超预算则提前 finalize（不发起额外 LLM）
            if (budgetExceeded(startMs)) {
                steps.add(RoundStep.of(round, queries, slugsPerRound.get(slugsPerRound.size() - 1),
                        null, new ArrayList<>(), stepLatency(startMs)));
                break;
            }

            SelfCheckResult check = selfCheck(generated, askSpaceId);
            lastCoverage = check.coverage;
            lastUnsupported = check.unsupported;
            steps.add(RoundStep.of(round, queries, slugsPerRound.get(slugsPerRound.size() - 1),
                    check.coverage, check.unsupported, stepLatency(startMs)));

            if (check.parseFailed) {
                selfCheckDegraded = true;
                break;
            }
            if (check.coverage >= coverageThreshold || round >= maxRounds || budgetExceeded(startMs)) {
                break;
            }

            queries = buildBackfillQueries(check);
            if (queries.isEmpty() || budgetExceeded(startMs)) {
                break;
            }
            round++;
        }

        if (generated == null) {
            generated = emptyGenerated(question, displayScope);
        }

        Long qaLogId = kbAskServiceImpl.saveQaLog(askTemplate, generated, scopeSpaces);
        generated.setQaLogId(qaLogId);

        long latencyMs = System.currentTimeMillis() - startMs;
        AgenticAskVo vo = toAgenticVo(generated);
        vo.setAgentic(true);
        vo.setRounds(round);
        vo.setRewrittenQuery(rewrite.rewritten);
        vo.setSubQuestions(rewrite.subQuestions);
        vo.setCoverage(selfCheckEnabled ? lastCoverage : null);
        vo.setUnsupportedStatements(lastUnsupported != null ? lastUnsupported : new ArrayList<>());
        vo.setRetrievedSlugsPerRound(slugsPerRound);
        vo.setDegraded(selfCheckDegraded || budgetExceeded(startMs));
        vo.setGuard(outputGroundingService.mergeAgenticGuard(
                guardOutcome, selfCheckEnabled, lastCoverage, lastUnsupported));

        saveTrace(request, rewrite, vo, steps, latencyMs, scopeSpaces);
        return vo;
    }

    private AgenticAskVo finalizeBudgetExceeded(AgenticAskRequest request, String question,
                                              RewriteDecomposeResult rewrite, List<Long> scopeSpaces,
                                              AskRequest askTemplate, KbAskServiceImpl.Scope displayScope,
                                              List<String> snippetTerms, int citationTopK,
                                              int llmContextTopK, int llmContextMaxChars,
                                              long startMs, List<RoundStep> steps, List<String> queries,
                                              InputGuardOutcome guardOutcome) {
        List<KbAskServiceImpl.QueryRecallResult> accumulatedRecalls = new ArrayList<>();
        List<List<String>> slugsPerRound = new ArrayList<>();
        List<String> roundSlugs = new ArrayList<>();
        for (String q : queries) {
            KbAskServiceImpl.QueryRecallResult recall =
                    kbAskServiceImpl.recallForQuery(askTemplate, q, scopeSpaces);
            accumulatedRecalls.add(recall);
            roundSlugs.addAll(kbAskServiceImpl.slugsFromRecall(recall));
        }
        slugsPerRound.add(new ArrayList<>(new LinkedHashSet<>(roundSlugs)));
        steps.add(RoundStep.of(1, queries, slugsPerRound.get(0), null, new ArrayList<>(),
                stepLatency(startMs)));

        KbAskServiceImpl.MergedPool pool = kbAskServiceImpl.mergeQueryRecalls(accumulatedRecalls);
        AskResponse generated = kbAskServiceImpl.generateFromPool(
                askTemplate, question, pool, displayScope, snippetTerms,
                citationTopK, llmContextTopK, llmContextMaxChars, scopeSpaces, true);
        generated.setCitations(
                KbAskServiceImpl.filterCitationsToPool(generated.getCitations(), pool.poolSlugs));
        Long qaLogId = kbAskServiceImpl.saveQaLog(askTemplate, generated, scopeSpaces);
        generated.setQaLogId(qaLogId);

        long latencyMs = System.currentTimeMillis() - startMs;
        AgenticAskVo vo = toAgenticVo(generated);
        vo.setAgentic(true);
        vo.setRounds(1);
        vo.setRewrittenQuery(rewrite.rewritten);
        vo.setSubQuestions(rewrite.subQuestions);
        vo.setCoverage(null);
        vo.setUnsupportedStatements(new ArrayList<>());
        vo.setRetrievedSlugsPerRound(slugsPerRound);
        vo.setDegraded(true);
        vo.setGuard(outputGroundingService.mergeAgenticGuard(guardOutcome, false, null, null));
        saveTrace(request, rewrite, vo, steps, latencyMs, scopeSpaces);
        return vo;
    }

    private AskResponse emptyGenerated(String question, KbAskServiceImpl.Scope displayScope) {
        AskResponse resp = new AskResponse();
        resp.setAnswer("知识库暂无相关内容。");
        resp.setMode("retrieval");
        resp.setScope(displayScope.include.isEmpty() ? "全部类型" : displayScope.include.toString());
        resp.setScopeReason(displayScope.reason);
        resp.setProvider(kbLlmClient.getProvider());
        resp.setModel(kbLlmClient.getModel());
        resp.setCitations(new ArrayList<>());
        return resp;
    }

    private void saveTrace(AgenticAskRequest request, RewriteDecomposeResult rewrite, AgenticAskVo vo,
                           List<RoundStep> steps, long latencyMs, List<Long> scopeSpaces) {
        try {
            KbAgenticTrace trace = new KbAgenticTrace();
            trace.setId(com.moli.common.core.IdGenerator.getId());
            trace.setQaLogId(vo.getQaLogId());
            trace.setSpaceId(scopeSpaces.size() == 1 ? scopeSpaces.get(0) : request.getSpaceId());
            trace.setUserId(ShiroUtils.getUserId());
            trace.setQuestion(request.getQuestion());
            trace.setRewritten(rewrite.rewritten);
            trace.setSubQuestionsJson(JSON.toJSONString(rewrite.subQuestions));
            trace.setRounds(vo.getRounds());
            JSONObject stepsWrapper = new JSONObject();
            stepsWrapper.put("steps", steps);
            AskGuardVo guard = vo.getGuard();
            if (guard != null) {
                stepsWrapper.put("guard", guard);
            }
            trace.setStepsJson(JSON.toJSONString(stepsWrapper));
            trace.setCoverage(vo.getCoverage());
            trace.setDegraded(vo.isDegraded());
            trace.setLatencyMs(latencyMs);
            trace.setCreateTime(new Date());
            kbAgenticTraceMapper.insert(trace);
        } catch (Exception e) {
            log.warn("写 kb_agentic_trace 失败: {}", e.getMessage());
        }
    }

    private SelfCheckResult selfCheck(AskResponse generated, Long askSpaceId) {
        KbGroundingSelfCheckSupport.GroundingCheckResult check = groundingSelfCheckSupport.check(
                KbLlmCallScenes.AGENTIC_SELF_CHECK, askSpaceId, generated);
        SelfCheckResult result = new SelfCheckResult();
        result.supported = check.supported;
        result.unsupported = check.unsupported;
        result.missingInfo = check.missingInfo;
        result.coverage = check.coverage != null ? check.coverage : 0.0;
        result.parseFailed = check.parseFailed;
        return result;
    }

    static SelfCheckResult parseSelfCheckJson(String raw) {
        KbGroundingSelfCheckSupport.GroundingCheckResult check =
                KbGroundingSelfCheckSupport.parseSelfCheckJson(raw);
        SelfCheckResult result = new SelfCheckResult();
        result.supported = check.supported;
        result.unsupported = check.unsupported;
        result.missingInfo = check.missingInfo;
        result.coverage = check.coverage != null ? check.coverage : 0.0;
        result.parseFailed = check.parseFailed;
        return result;
    }

    static double computeCoverage(List<String> supported, List<String> unsupported) {
        return KbGroundingSelfCheckSupport.computeCoverage(supported, unsupported);
    }

    private static List<String> readStringArray(JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) {
            return out;
        }
        for (int i = 0; i < arr.size(); i++) {
            String s = arr.getString(i);
            if (StringUtils.isNotBlank(s)) {
                out.add(s.trim());
            }
        }
        return out;
    }

    private List<String> buildBackfillQueries(SelfCheckResult check) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();
        if (check.missingInfo != null) {
            for (String m : check.missingInfo) {
                if (StringUtils.isNotBlank(m)) {
                    queries.add(m.trim());
                }
            }
        }
        if (check.unsupported != null) {
            for (String u : check.unsupported) {
                if (StringUtils.isNotBlank(u)) {
                    String kw = u.length() > 120 ? u.substring(0, 120) : u;
                    queries.add(kw.trim());
                }
            }
        }
        return new ArrayList<>(queries);
    }

    private boolean budgetExceeded(long startMs) {
        long budget = kbAgenticProperties.getLatencyBudgetMs();
        if (budget <= 0) {
            budget = 20_000L;
        }
        return System.currentTimeMillis() - startMs >= budget;
    }

    private long stepLatency(long startMs) {
        return System.currentTimeMillis() - startMs;
    }

    private Long resolveAskSpaceId(Long spaceId, List<Long> scopeSpaces) {
        if (spaceId != null) {
            return spaceId;
        }
        return scopeSpaces.size() == 1 ? scopeSpaces.get(0) : null;
    }

    private AgenticAskVo blockedAgenticVo(AgenticAskRequest request, InputGuardOutcome guardOutcome) {
        AskRequest askReq = request.toAskRequest(false);
        askReq.setQuestion(guardOutcome.getQuestionForProcessing());
        AskResponse resp = kbAskServiceImpl.executeAsk(askReq, guardOutcome);
        AgenticAskVo vo = toAgenticVo(resp);
        vo.setAgentic(false);
        vo.setRounds(1);
        vo.setDegraded(true);
        vo.setRewrittenQuery(null);
        vo.setSubQuestions(new ArrayList<>());
        vo.setCoverage(null);
        vo.setUnsupportedStatements(new ArrayList<>());
        vo.setRetrievedSlugsPerRound(new ArrayList<>());
        if (resp.getCitations() != null && !resp.getCitations().isEmpty()) {
            List<String> slugs = new ArrayList<>();
            for (AskResponse.Citation c : resp.getCitations()) {
                if (c.getSlug() != null) {
                    slugs.add(c.getSlug());
                }
            }
            vo.getRetrievedSlugsPerRound().add(slugs);
        }
        return vo;
    }

    private AgenticAskVo degradeSingleTurn(AgenticAskRequest request, boolean ranAgentic,
                                           boolean degraded, boolean forceRetrieval) {
        AskRequest askReq = request.toAskRequest(request.getUseLlm() != null ? request.getUseLlm() : true);
        if (forceRetrieval) {
            askReq.setUseLlm(false);
        }
        AskResponse single = kbAskService.ask(askReq);
        AgenticAskVo vo = toAgenticVo(single);
        vo.setAgentic(ranAgentic);
        vo.setRounds(1);
        vo.setRewrittenQuery(null);
        vo.setSubQuestions(new ArrayList<>());
        vo.setCoverage(null);
        vo.setUnsupportedStatements(new ArrayList<>());
        vo.setRetrievedSlugsPerRound(new ArrayList<>());
        if (single.getCitations() != null && !single.getCitations().isEmpty()) {
            List<String> slugs = new ArrayList<>();
            for (AskResponse.Citation c : single.getCitations()) {
                if (c.getSlug() != null) {
                    slugs.add(c.getSlug());
                }
            }
            vo.getRetrievedSlugsPerRound().add(slugs);
        }
        vo.setDegraded(degraded);
        return vo;
    }

    private AgenticAskVo emptyScopeVo() {
        AgenticAskVo vo = new AgenticAskVo();
        vo.setAnswer("无可访问的知识空间。");
        vo.setMode("retrieval");
        vo.setScope("全部类型");
        vo.setProvider(kbLlmClient.getProvider());
        vo.setModel(kbLlmClient.getModel());
        vo.setAgentic(false);
        vo.setRounds(1);
        vo.setDegraded(true);
        return vo;
    }

    private RewriteDecomposeResult rewriteDecompose(String question, Long askSpaceId) {
        int maxSub = kbAgenticProperties.normalizedMaxSubQuestions();
        if (!kbAgenticProperties.isDecompose()) {
            return new RewriteDecomposeResult(question, new ArrayList<>());
        }
        String system = REWRITE_DECOMPOSE_SYSTEM.replace("{maxSub}", String.valueOf(maxSub));
        try {
            String raw = kbLlmClient.chat(
                    KbLlmCallScenes.AGENTIC_REWRITE,
                    askSpaceId,
                    system,
                    "用户问题：" + question);
            RewriteDecomposeResult parsed = parseRewriteJson(raw, question);
            if (parsed.subQuestions.size() > maxSub) {
                parsed.subQuestions = new ArrayList<>(parsed.subQuestions.subList(0, maxSub));
            }
            return parsed;
        } catch (Exception e) {
            log.warn("Agentic rewrite/decompose 失败，使用原问: {}", e.getMessage());
            return new RewriteDecomposeResult(question, new ArrayList<>());
        }
    }

    static RewriteDecomposeResult parseRewriteJson(String raw, String fallbackQuestion) {
        if (StringUtils.isBlank(raw)) {
            return new RewriteDecomposeResult(fallbackQuestion, new ArrayList<>());
        }
        String json = raw.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }
        try {
            JSONObject obj = JSON.parseObject(json);
            String rewritten = obj.getString("rewritten");
            if (StringUtils.isBlank(rewritten)) {
                rewritten = fallbackQuestion;
            }
            List<String> subs = new ArrayList<>();
            JSONArray arr = obj.getJSONArray("subQuestions");
            if (arr != null) {
                for (int i = 0; i < arr.size(); i++) {
                    String s = arr.getString(i);
                    if (StringUtils.isNotBlank(s)) {
                        subs.add(s.trim());
                    }
                }
            }
            Boolean mh = obj.getBoolean("multiHop");
            boolean multiHop = mh != null ? mh : !subs.isEmpty();
            if (!subs.isEmpty()) {
                multiHop = true;
            }
            return new RewriteDecomposeResult(rewritten.trim(), subs, multiHop);
        } catch (Exception e) {
            return new RewriteDecomposeResult(fallbackQuestion, new ArrayList<>(), false);
        }
    }

    private List<String> buildQueries(RewriteDecomposeResult rewrite) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(rewrite.rewritten)) {
            queries.add(rewrite.rewritten.trim());
        }
        for (String sub : rewrite.subQuestions) {
            if (StringUtils.isNotBlank(sub)) {
                queries.add(sub.trim());
            }
        }
        if (queries.isEmpty() && StringUtils.isNotBlank(rewrite.rewritten)) {
            queries.add(rewrite.rewritten.trim());
        }
        return new ArrayList<>(queries);
    }

    private int normalizedCitationTopK(AgenticAskRequest request) {
        if (request.getTopK() != null && request.getTopK() > 0) {
            return request.getTopK();
        }
        return kbAskProperties.normalizedCitationTopK();
    }

    private int normalizedLlmContextTopK(AgenticAskRequest request) {
        if (request.getLlmContextTopK() != null && request.getLlmContextTopK() > 0) {
            return request.getLlmContextTopK();
        }
        return kbAgenticProperties.resolveContextTopK(kbAskProperties);
    }

    private AgenticAskVo toAgenticVo(AskResponse resp) {
        AgenticAskVo vo = new AgenticAskVo();
        vo.setAnswer(resp.getAnswer());
        vo.setMode(resp.getMode());
        vo.setScope(resp.getScope());
        vo.setScopeReason(resp.getScopeReason());
        vo.setProvider(resp.getProvider());
        vo.setModel(resp.getModel());
        vo.setCitations(resp.getCitations());
        vo.setQaLogId(resp.getQaLogId());
        vo.setGuard(resp.getGuard());
        return vo;
    }

    static final class RewriteDecomposeResult {
        String rewritten;
        List<String> subQuestions;
        boolean multiHop;

        RewriteDecomposeResult(String rewritten, List<String> subQuestions) {
            this(rewritten, subQuestions, subQuestions != null && !subQuestions.isEmpty());
        }

        RewriteDecomposeResult(String rewritten, List<String> subQuestions, boolean multiHop) {
            this.rewritten = rewritten;
            this.subQuestions = subQuestions;
            this.multiHop = multiHop || (subQuestions != null && !subQuestions.isEmpty());
        }
    }

    static final class SelfCheckResult {
        List<String> supported = new ArrayList<>();
        List<String> unsupported = new ArrayList<>();
        List<String> missingInfo = new ArrayList<>();
        double coverage = 1.0;
        boolean parseFailed;
    }

    static final class RoundStep {
        public int round;
        public List<String> queries;
        public List<String> slugs;
        public Double coverage;
        public List<String> unsupported;
        public long latencyMs;

        static RoundStep of(int round, List<String> queries, List<String> slugs,
                            Double coverage, List<String> unsupported, long latencyMs) {
            RoundStep s = new RoundStep();
            s.round = round;
            s.queries = queries;
            s.slugs = slugs;
            s.coverage = coverage;
            s.unsupported = unsupported;
            s.latencyMs = latencyMs;
            return s;
        }
    }
}
