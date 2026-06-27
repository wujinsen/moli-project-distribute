package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
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
import com.moli.knowledge.server.dto.RawCoverageVo;
import com.moli.knowledge.server.dto.RawTreeNodeVo;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.service.KbRawCoverageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Ingest 工作台：T15a–e（含 enrich patch、断点续跑、批次模板）。
 */
@RestController
@RequestMapping("/kb/ingest")
@Api(tags = "知识库 Ingest 工作台")
public class KbIngestController {

    @Resource
    private KbIngestService kbIngestService;

    @Resource
    private KbRawCoverageService kbRawCoverageService;

    @GetMapping("/raw-tree")
    @ApiOperation("raw 只读目录树")
    public MoliResult<List<RawTreeNodeVo>> rawTree(@RequestParam(required = false) String prefix) {
        return MoliResult.success(kbIngestService.rawTree(prefix));
    }

    @GetMapping("/raw-coverage")
    @ApiOperation("raw 覆盖索引（wiki sources 反向映射，筛未 ingest）")
    public MoliResult<RawCoverageVo> rawCoverage(@RequestParam(required = false) Long spaceId,
                                                 @RequestParam(required = false) String prefix,
                                                 @RequestParam(required = false, defaultValue = "all") String filter,
                                                 @RequestParam(required = false, defaultValue = "false") boolean refresh) {
        return MoliResult.success(kbRawCoverageService.coverage(spaceId, prefix, filter, refresh));
    }

    @PostMapping("/jobs")
    @ApiOperation("创建 Ingest 批次")
    public MoliResult<IngestJobVo> createJob(@Validated @RequestBody IngestJobCreateRequest request) {
        return MoliResult.success(kbIngestService.createJob(request));
    }

    @GetMapping("/jobs")
    @ApiOperation("批次任务分页")
    public MoliResult<Page<IngestJobVo>> pageJobs(@RequestParam(required = false) Long spaceId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return MoliResult.success(kbIngestService.pageJobs(spaceId, status, pageNum, pageSize));
    }

    @GetMapping("/jobs/{id}")
    @ApiOperation("批次详情")
    public MoliResult<IngestJobVo> getJob(@PathVariable Long id) {
        return MoliResult.success(kbIngestService.getJob(id));
    }

    @DeleteMapping("/jobs/{id}")
    @ApiOperation("删除历史批次（软删）")
    public MoliResult<Boolean> deleteJob(@PathVariable Long id) {
        kbIngestService.deleteJob(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @PostMapping("/jobs/{id}/plan")
    @ApiOperation("生成/刷新 Plan")
    public MoliResult<IngestJobVo> generatePlan(@PathVariable Long id) {
        return MoliResult.success(kbIngestService.generatePlan(id));
    }

    @PutMapping("/jobs/{id}/plan")
    @ApiOperation("人工编辑 Plan")
    public MoliResult<IngestJobVo> updatePlan(@PathVariable Long id,
                                              @Validated @RequestBody IngestPlanUpdateRequest request) {
        return MoliResult.success(kbIngestService.updatePlan(id, request));
    }

    @GetMapping("/jobs/{id}/export-agent-prompt")
    @ApiOperation("导出 Cursor Agent 提示词")
    public MoliResult<String> exportAgentPrompt(@PathVariable Long id) {
        return MoliResult.success(kbIngestService.exportAgentPrompt(id));
    }

    @PostMapping("/jobs/{id}/generate")
    @ApiOperation("按 plan 生成草稿；resume=true 断点续跑（跳过已有草稿）")
    public MoliResult<IngestGenerateResultVo> generate(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "false") boolean resume) {
        return MoliResult.success(kbIngestService.generate(id, resume));
    }

    @GetMapping("/jobs/{id}/drafts")
    @ApiOperation("草稿列表")
    public MoliResult<List<IngestDraftVo>> drafts(@PathVariable Long id) {
        return MoliResult.success(kbIngestService.listDrafts(id));
    }

    @GetMapping("/jobs/{id}/draft")
    @ApiOperation("单页草稿")
    public MoliResult<IngestDraftVo> draft(@PathVariable Long id, @RequestParam String slug) {
        return MoliResult.success(kbIngestService.getDraft(id, slug));
    }

    @PutMapping("/jobs/{id}/draft")
    @ApiOperation("人工改草稿（enrich 可传 patch）")
    public MoliResult<IngestDraftVo> updateDraft(@PathVariable Long id, @RequestParam String slug,
                                                 @RequestBody IngestDraftUpdateRequest request) {
        return MoliResult.success(kbIngestService.updateDraft(id, slug, request));
    }

    @PostMapping("/jobs/{id}/draft/regenerate")
    @ApiOperation("单页重生成")
    public MoliResult<IngestDraftVo> regenerate(@PathVariable Long id, @RequestParam String slug) {
        return MoliResult.success(kbIngestService.regenerateDraft(id, slug));
    }

    @PutMapping("/jobs/{id}/draft/approval")
    @ApiOperation("设置审批状态")
    public MoliResult<IngestDraftVo> approval(@PathVariable Long id, @RequestParam String slug,
                                              @RequestParam String approval) {
        return MoliResult.success(kbIngestService.setApproval(id, slug, approval));
    }

    @PostMapping("/jobs/{id}/lint")
    @ApiOperation("lint 预检")
    public MoliResult<IngestLintVo> lint(@PathVariable Long id) {
        return MoliResult.success(kbIngestService.lint(id));
    }

    @PostMapping("/jobs/{id}/commit")
    @ApiOperation("原子落盘；sync=true 时一键 Sync")
    public MoliResult<IngestCommitResultVo> commit(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "false") boolean sync) {
        return MoliResult.success(kbIngestService.commit(id, sync));
    }

    // ------------------------------------------------------------ T15e 模板

    @GetMapping("/templates")
    @ApiOperation("批次模板列表")
    public MoliResult<List<IngestTemplateVo>> templates(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbIngestService.listTemplates(spaceId));
    }

    @PostMapping("/templates")
    @ApiOperation("创建批次模板")
    public MoliResult<IngestTemplateVo> createTemplate(@Validated @RequestBody IngestTemplateCreateRequest request) {
        return MoliResult.success(kbIngestService.createTemplate(request));
    }

    @DeleteMapping("/templates/{id}")
    @ApiOperation("删除批次模板")
    public MoliResult<Boolean> deleteTemplate(@PathVariable Long id) {
        kbIngestService.deleteTemplate(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @PostMapping("/jobs/from-template/{templateId}")
    @ApiOperation("从模板创建批次（可选附带 Plan 快照）")
    public MoliResult<IngestJobVo> createFromTemplate(@PathVariable Long templateId,
                                                      @RequestBody(required = false) IngestJobFromTemplateRequest request) {
        return MoliResult.success(kbIngestService.createJobFromTemplate(templateId, request));
    }

    @PostMapping("/jobs/{id}/save-as-template")
    @ApiOperation("将当前批次另存为模板")
    public MoliResult<IngestTemplateVo> saveAsTemplate(@PathVariable Long id,
                                                       @RequestBody IngestSaveAsTemplateRequest request) {
        return MoliResult.success(kbIngestService.saveJobAsTemplate(id, request));
    }
}
