package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbResearchProperties;
import com.moli.knowledge.server.dto.IngestCommitResultVo;
import com.moli.knowledge.server.dto.IngestDraftUpdateRequest;
import com.moli.knowledge.server.dto.IngestJobCreateRequest;
import com.moli.knowledge.server.dto.IngestJobVo;
import com.moli.knowledge.server.dto.IngestLintVo;
import com.moli.knowledge.server.dto.IngestPlanUpdateRequest;
import com.moli.knowledge.server.dto.ResearchVo;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.service.KbResearchWritebackService;
import com.moli.knowledge.server.service.ingest.IngestPlanPathResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class KbResearchWritebackServiceImpl implements KbResearchWritebackService {

    @Resource
    private KbIngestService kbIngestService;
    @Resource
    private KbResearchProperties researchProperties;
    @Resource
    private KbIngestProperties ingestProperties;
    @Resource
    private KbAclService kbAclService;

    @Override
    public WritebackResult writeback(ResearchVo result, String topic, Long spaceIdOverride) {
        if (result == null || StringUtils.isBlank(result.getReportMd())) {
            throw new BaseException("writeback 需要 reportMd");
        }
        if (!ingestProperties.isEnabled()) {
            throw new BaseException("Ingest 工作台未启用，无法回写");
        }

        Long spaceId = spaceIdOverride != null ? spaceIdOverride : researchProperties.getWritebackSpaceId();
        kbAclService.assertCanEdit(spaceId);

        String bareSlug = resolveBareSlug(result);
        String relPath = "develop/outputs/" + bareSlug;
        String fullSlug = IngestPlanPathResolver.normalizeFullRelPath(relPath);

        IngestJobCreateRequest jobReq = new IngestJobCreateRequest();
        jobReq.setSpaceId(spaceId);
        jobReq.setTopic(StringUtils.defaultIfBlank(topic, result.getTopic()));
        jobReq.setRawPaths(Collections.singletonList(researchProperties.getWritebackRawPath()));
        jobReq.setRemark("AI-10 DeepResearch writeback runId=" + result.getRunId());
        IngestJobVo job = kbIngestService.createJob(jobReq);

        JSONObject plan = buildPlan(fullSlug, result, bareSlug);
        IngestPlanUpdateRequest planReq = new IngestPlanUpdateRequest();
        planReq.setPlanJson(plan.toJSONString());
        kbIngestService.updatePlan(job.getId(), planReq);

        kbIngestService.generate(job.getId(), false, false);

        IngestDraftUpdateRequest draftReq = new IngestDraftUpdateRequest();
        draftReq.setContent(result.getReportMd());
        kbIngestService.updateDraft(job.getId(), fullSlug, draftReq);
        kbIngestService.setApproval(job.getId(), fullSlug, "approved");

        IngestLintVo lint = kbIngestService.lint(job.getId());
        if (lint != null && lint.getBlockingCount() > 0) {
            throw new BaseException("writeback lint 未通过: blocking=" + lint.getBlockingCount());
        }

        boolean sync = researchProperties.isWritebackAutoSync();
        IngestCommitResultVo commit = kbIngestService.commit(job.getId(), sync);
        log.info("[research] writeback job={} slug={} created={}", job.getId(), fullSlug,
                commit != null ? commit.getCreated() : 0);

        return new WritebackResult(job.getId(), "wiki-moli/" + fullSlug + ".md");
    }

    private String resolveBareSlug(ResearchVo result) {
        if (StringUtils.isNotBlank(result.getSlug())) {
            return IngestPlanPathResolver.sanitizeBareSlug(result.getSlug());
        }
        if (result.getTitle() != null) {
            return IngestPlanPathResolver.sanitizeBareSlug(result.getTitle());
        }
        return IngestPlanPathResolver.sanitizeBareSlug("deep-research-" + result.getRunId().substring(0, 8));
    }

    private JSONObject buildPlan(String fullSlug, ResearchVo result, String bareSlug) {
        JSONObject plan = new JSONObject();
        JSONArray create = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("type", "output");
        item.put("slug", fullSlug);
        item.put("title", StringUtils.defaultIfBlank(result.getTitle(), bareSlug));
        item.put("sources", JSON.parseArray("[\"" + researchProperties.getWritebackRawPath() + "\"]"));
        item.put("related", new JSONArray());
        item.put("reason", "DeepResearch writeback（AI-10）");
        create.add(item);
        plan.put("create", create);
        plan.put("enrich", new JSONArray());
        plan.put("skip", new JSONArray());
        return plan;
    }
}
