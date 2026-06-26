package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.WikiAiReviseRequest;
import com.moli.knowledge.server.dto.WikiAiReviseResultVo;
import com.moli.knowledge.server.dto.WikiLintPreviewRequest;
import com.moli.knowledge.server.dto.WikiLintPreviewVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.dto.WikiSaveResultVo;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/wiki")
@Api(tags = "知识库 Wiki 在线编辑")
public class KbWikiController {

    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbWikiAiReviseService kbWikiAiReviseService;

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

    @PostMapping("/ai-revise")
    @ApiOperation("AI 改稿建议（不写盘）；需空间 editor + kb.llm 可用")
    public MoliResult<WikiAiReviseResultVo> aiRevise(@RequestBody WikiAiReviseRequest request) {
        return MoliResult.success(kbWikiAiReviseService.aiRevise(request));
    }

    @PostMapping("/page/lint-preview")
    @ApiOperation("保存前 lint 预检（断链/frontmatter）；需空间 editor")
    public MoliResult<WikiLintPreviewVo> lintPreview(@RequestBody WikiLintPreviewRequest request) {
        return MoliResult.success(kbWikiAiReviseService.previewLint(request));
    }
}
