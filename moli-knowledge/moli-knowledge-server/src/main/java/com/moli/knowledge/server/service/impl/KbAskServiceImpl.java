package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.QaHistoryVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbQaLog;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbQaLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbAskService;
import com.moli.knowledge.server.service.KbLlmClient;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识库问答（Query）。流程对齐 kb/AGENTS.md §5 与 kb/tools/serve.py：
 * 定作用域 → 选 ≤topK 页 → 有 key 调 LLM 带引用作答 / 无 key 检索式 → 记 kb_qa_log。
 */
@Service
public class KbAskServiceImpl implements KbAskService {

    private static final Logger log = LoggerFactory.getLogger(KbAskServiceImpl.class);

    private static final String SYSTEM_PROMPT =
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
    private KbQaLogMapper kbQaLogMapper;
    @Resource
    private KbLlmProperties llm;
    @Resource
    private KbLlmClient kbLlmClient;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private com.moli.knowledge.server.config.KbSearchProperties kbSearchProperties;

    @Override
    public AskResponse ask(AskRequest request) {
        String question = request.getQuestion().trim();
        int topK = request.getTopK() == null || request.getTopK() <= 0 ? 8 : request.getTopK();

        Scope scope = detectScope(question);
        List<String> terms = buildTerms(question);

        // 候选页：已发布 + 空间过滤 + 作用域类型过滤（叠加 ACL 可读空间）
        List<Long> scopeSpaces = kbAclService.resolveReadableSpaceIds(
                request.getSpaceId(), request.getSpaceIds());
        if (scopeSpaces.isEmpty()) {
            AskResponse empty = new AskResponse();
            empty.setAnswer("无可访问的知识空间。");
            empty.setMode("retrieval");
            empty.setScope(scope.include.isEmpty() ? "全部类型" : scope.include.toString());
            empty.setScopeReason(scope.reason);
            empty.setProvider(llm.getProvider());
            empty.setModel(llm.getModel());
            return empty;
        }

        // 候选召回：优先 ngram 全文按相关度取 top-N（避免全量载入内存）；
        // 全文未启用 / 异常 / 0 命中时，回退到原「全量扫描」保证召回。
        int candidateLimit = kbSearchProperties.normalizedAskCandidateLimit();
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

        // 内存 bigram 重排精排。全文召回集已按相关度排序，命中保留（score==0 用全文序兜底）；
        // 全量扫描兜底则仍按 score>0 过滤，避免把全库零分页塞进结果。
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
        // 稳定排序：分数降序，等分时保留全文相关度顺序
        scored.sort((a, b) -> Integer.compare(b.score, a.score));

        AskResponse resp = new AskResponse();
        resp.setScope(scope.include.isEmpty() ? "全部类型" : scope.include.toString());
        resp.setScopeReason(scope.reason);
        resp.setProvider(llm.getProvider());
        resp.setModel(llm.getModel());

        List<AskResponse.Citation> citations = new ArrayList<>();
        for (int i = 0; i < scored.size() && i < topK; i++) {
            KbDocument d = scored.get(i).doc;
            citations.add(new AskResponse.Citation(d.getId(), d.getSpaceId(), d.getSlug(), d.getTitle(),
                    d.getKbType(), snippet(d, terms)));
        }
        resp.setCitations(citations);

        if (shouldUseLlm(request) && !citations.isEmpty()) {
            try {
                String ctx = buildContext(scored, topK);
                String answer = kbLlmClient.chat(SYSTEM_PROMPT,
                        "问题：" + question + "\n\n可用知识库页（只能依据这些作答）：\n\n" + ctx);
                resp.setAnswer(answer);
                resp.setMode("generative");
            } catch (Exception e) {                       // noqa
                log.warn("LLM 调用失败，降级检索式: {}", e.getMessage());
                resp.setAnswer("> 调用 " + llm.getProvider() + " 失败（" + e.getMessage()
                        + "），已回退检索式。\n\n" + retrievalAnswer(question, citations));
                resp.setMode("retrieval");
            }
        } else {
            String note = "";
            if (!Boolean.TRUE.equals(request.getUseLlm())) {
                note = "> 本次未启用 LLM 生成式，当前为检索式。\n\n";
            } else if (!llm.usable()) {
                note = "> 后端未配置 LLM（kb.llm.enabled/api-key），当前为检索式。\n\n";
            }
            resp.setAnswer(note + retrievalAnswer(question, citations));
            resp.setMode("retrieval");
        }

        Long qaLogId = saveLog(request, resp, scopeSpaces);
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

    private static class Scope {
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
        if (find(q, "方案|解决|最佳实践|优化|调优|排查")) {
            s.include.add("article");
            s.include.add("concept");
            s.reason = "命中『方案/最佳实践』意图 → 限 article + concept";
            return s;
        }
        if (find(q, "怎么|如何|启动|部署|配置|登录|操作|步骤|开通")) {
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

    private int score(KbDocument d, List<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        String title = lower(d.getTitle());
        String summary = lower(d.getSummary());
        String body = lower(d.getContent());
        int score = 0;
        for (String t : terms) {
            score += count(title, t) * 5;
            score += count(summary, t) * 3;
            score += count(body, t) * 1;
        }
        return score;
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
    // 检索式答案 / LLM 上下文 / LLM 调用
    // ------------------------------------------------------------------

    /** 前端显式 useLlm=true 且后端 kb.llm 已就绪时才调 LLM。 */
    private boolean shouldUseLlm(AskRequest request) {
        return Boolean.TRUE.equals(request.getUseLlm()) && llm.usable();
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

    private String buildContext(List<Scored> scored, int topK) {
        int budget = 12000;
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

    private Long saveLog(AskRequest request, AskResponse resp, List<Long> scopeSpaces) {
        try {
            KbQaLog qa = new KbQaLog();
            Long id = com.moli.common.core.IdGenerator.getId();
            qa.setId(id);
            qa.setSpaceId(scopeSpaces.size() == 1 ? scopeSpaces.get(0) : null);
            qa.setUserId(ShiroUtils.getUserId());
            qa.setQuestion(request.getQuestion());
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

    private static class Scored {
        final KbDocument doc;
        final int score;

        Scored(KbDocument doc, int score) {
            this.doc = doc;
            this.score = score;
        }
    }
}
