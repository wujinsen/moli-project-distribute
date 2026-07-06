package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.WikiEnrichRequest;
import com.moli.knowledge.server.dto.WikiEnrichResultVo;
import com.moli.knowledge.server.dto.WikiAiReviseRequest;
import com.moli.knowledge.server.dto.WikiAiReviseResultVo;
import com.moli.knowledge.server.dto.WikiImportResultVo;
import com.moli.knowledge.server.dto.WikiLintPreviewRequest;
import com.moli.knowledge.server.dto.WikiLintPreviewVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.dto.WikiSaveResultVo;
import com.moli.knowledge.server.dto.WikiSpaceLintRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintVo;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiEnrichService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.moli.knowledge.server.dto.WikiGovernAiBatchFixRequest;
import com.moli.knowledge.server.dto.WikiGovernAiBatchFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernAutoFixRequest;
import com.moli.knowledge.server.dto.WikiGovernAutoFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernMergeHintRequest;
import com.moli.knowledge.server.dto.WikiGovernMergeHintResultVo;
import com.moli.knowledge.server.dto.WikiGovernOptionsVo;
import com.moli.knowledge.server.dto.WikiGovernScriptFixRequest;
import com.moli.knowledge.server.dto.WikiGovernScriptFixResultVo;
import com.moli.knowledge.server.service.KbWikiGovernService;
import com.moli.knowledge.server.service.KbWikiImportService;
import com.moli.knowledge.server.service.KbWikiLintService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/wiki")
@Api(tags = "知识库 Wiki 在线编辑")
public class KbWikiController {

    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbWikiAiReviseService kbWikiAiReviseService;
    @Resource
    private KbWikiEnrichService kbWikiEnrichService;
    @Resource
    private KbWikiLintService kbWikiLintService;
    @Resource
    private KbWikiGovernService kbWikiGovernService;
    @Resource
    private KbWikiImportService kbWikiImportService;

    @GetMapping("/page")
    @ApiOperation("读 wiki 文件全文（frontmatter+正文）；需空间 editor。文件不存在返回 exists=false")
    public MoliResult<WikiPageVo> read(@RequestParam String slug,
                                       @RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbWikiFileService.readPage(slug, spaceId));
    }

    @PutMapping("/page")
    @ApiOperation("写 wiki 文件；需空间 editor。保存后需 Sync 才进库")
    public MoliResult<WikiSaveResultVo> save(@RequestBody WikiSaveRequest request) {
        return MoliResult.success(kbWikiFileService.writePage(request));
    }

    @PostMapping("/page/import")
    @ApiOperation("T20b/e · 浏览器导入成品 wiki md（可选 assetsZip、lint、Sync）")
    public MoliResult<WikiImportResultVo> importPage(@RequestParam Long spaceId,
                                                     @RequestParam Long categoryId,
                                                     @RequestParam MultipartFile file,
                                                     @RequestParam(required = false) String slug,
                                                     @RequestParam(required = false) String title,
                                                     @RequestParam(required = false, defaultValue = "FAIL") String onConflict,
                                                     @RequestParam(required = false, defaultValue = "false") boolean lintPreview,
                                                     @RequestParam(required = false, defaultValue = "true") boolean sync,
                                                     @RequestParam(required = false) MultipartFile assetsZip) {
        return MoliResult.success(kbWikiImportService.importPage(
                spaceId, categoryId, file, slug, title, onConflict, lintPreview, sync, assetsZip));
    }

    @PostMapping("/ai-revise")
    @ApiOperation("AI 改稿建议（不写盘）；走 kb.llm OpenAI 兼容接口，可选 model")
    public MoliResult<WikiAiReviseResultVo> aiRevise(@RequestBody WikiAiReviseRequest request) {
        return MoliResult.success(kbWikiAiReviseService.aiRevise(request));
    }

    @GetMapping("/govern/options")
    @ApiOperation("Wiki 治理 LLM 选项（kb.llm 模型列表）")
    public MoliResult<WikiGovernOptionsVo> governOptions() {
        return MoliResult.success(kbWikiGovernService.getOptions());
    }

    @PostMapping("/page/lint-preview")
    @ApiOperation("保存前 lint 预检（断链/frontmatter）；需空间 editor")
    public MoliResult<WikiLintPreviewVo> lintPreview(@RequestBody WikiLintPreviewRequest request) {
        return MoliResult.success(kbWikiAiReviseService.previewLint(request));
    }

    @PostMapping("/enrich")
    @ApiOperation("Wiki enrich：已有页追加 patch + 可选 log/index/edges/Sync；需空间 editor")
    public MoliResult<WikiEnrichResultVo> enrich(@RequestBody WikiEnrichRequest request) {
        return MoliResult.success(kbWikiEnrichService.enrich(request));
    }

    @PostMapping("/lint-space")
    @ApiOperation("空间级文件 Lint（文件真值，调 lint.py）；需空间 editor。issue.page 即 slug")
    public MoliResult<WikiSpaceLintVo> lintSpace(@RequestBody WikiSpaceLintRequest request) {
        return MoliResult.success(kbWikiLintService.lintSpace(request));
    }

    @PostMapping("/govern/script-fix")
    @ApiOperation("Wiki 治理 · 脚本批量修复（missing_dates/slug_mismatch/missing_source）；需空间 editor")
    public MoliResult<WikiGovernScriptFixResultVo> governScriptFix(@RequestBody WikiGovernScriptFixRequest request) {
        return MoliResult.success(kbWikiGovernService.scriptFix(request));
    }

    @PostMapping("/govern/ai-batch-fix")
    @ApiOperation("Wiki 治理 · AI 批量修复（写盘）；需空间 editor + kb.llm")
    public MoliResult<WikiGovernAiBatchFixResultVo> governAiBatchFix(@RequestBody WikiGovernAiBatchFixRequest request) {
        return MoliResult.success(kbWikiGovernService.aiBatchFix(request));
    }

    @PostMapping("/govern/auto-fix")
    @ApiOperation("Wiki 治理 · 一键修复：脚本 → AI → 复检 → 可选 Sync")
    public MoliResult<WikiGovernAutoFixResultVo> governAutoFix(@RequestBody WikiGovernAutoFixRequest request) {
        return MoliResult.success(kbWikiGovernService.autoFix(request));
    }

    @PostMapping("/govern/merge-hint")
    @ApiOperation("Wiki 治理 · 重复页合并提示（dup_slug/dup_content；Cursor 指令，不调 LLM）")
    public MoliResult<WikiGovernMergeHintResultVo> governMergeHint(@RequestBody WikiGovernMergeHintRequest request) {
        return MoliResult.success(kbWikiGovernService.mergeHint(request));
    }
}
