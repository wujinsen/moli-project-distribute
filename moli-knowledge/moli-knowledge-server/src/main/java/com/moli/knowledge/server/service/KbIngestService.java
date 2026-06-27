package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.dto.IngestCommitResultVo;
import com.moli.knowledge.server.dto.IngestDraftUpdateRequest;
import com.moli.knowledge.server.dto.IngestDraftVo;
import com.moli.knowledge.server.dto.IngestGenerateResultVo;
import com.moli.knowledge.server.dto.IngestJobCreateRequest;
import com.moli.knowledge.server.dto.IngestJobFromTemplateRequest;
import com.moli.knowledge.server.dto.IngestJobVo;
import com.moli.knowledge.server.dto.IngestLintVo;
import com.moli.knowledge.server.dto.IngestPlanUpdateRequest;
import com.moli.knowledge.server.dto.IngestSaveAsTemplateRequest;
import com.moli.knowledge.server.dto.IngestTemplateCreateRequest;
import com.moli.knowledge.server.dto.IngestTemplateVo;
import com.moli.knowledge.server.dto.RawTreeNodeVo;

import java.util.List;

/**
 * Ingest 工作台：raw 树 + 批次 job/plan + 草稿生成/审阅 + lint/commit + 模板（T15a–e）。
 */
public interface KbIngestService {

    List<RawTreeNodeVo> rawTree(String prefix);

    IngestJobVo createJob(IngestJobCreateRequest request);

    Page<IngestJobVo> pageJobs(Long spaceId, String status, int pageNum, int pageSize);

    IngestJobVo getJob(Long id);

    /** 软删历史批次（仅从列表隐藏；不回滚已 commit 的 wiki 文件）。 */
    void deleteJob(Long id);

    IngestJobVo generatePlan(Long id);

    IngestJobVo updatePlan(Long id, IngestPlanUpdateRequest request);

    String exportAgentPrompt(Long id);

    /** 按 plan 生成草稿；resume=true 时跳过已有草稿（断点续跑）。 */
    IngestGenerateResultVo generate(Long jobId, boolean resume);

    List<IngestDraftVo> listDrafts(Long jobId);

    IngestDraftVo getDraft(Long jobId, String slug);

    IngestDraftVo updateDraft(Long jobId, String slug, IngestDraftUpdateRequest request);

    IngestDraftVo regenerateDraft(Long jobId, String slug);

    IngestDraftVo setApproval(Long jobId, String slug, String approval);

    IngestLintVo lint(Long jobId);

    IngestCommitResultVo commit(Long jobId, boolean sync);

    // ---------------------------------------------------------------- T15e 模板

    List<IngestTemplateVo> listTemplates(Long spaceId);

    IngestTemplateVo createTemplate(IngestTemplateCreateRequest request);

    IngestTemplateVo saveJobAsTemplate(Long jobId, IngestSaveAsTemplateRequest request);

    void deleteTemplate(Long id);

    IngestJobVo createJobFromTemplate(Long templateId, IngestJobFromTemplateRequest request);
}
