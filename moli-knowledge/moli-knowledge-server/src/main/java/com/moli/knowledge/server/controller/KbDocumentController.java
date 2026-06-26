package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.DocumentDetailVo;
import com.moli.knowledge.server.dto.DocumentMoveResultVo;
import com.moli.knowledge.server.dto.DocumentSaveRequest;
import com.moli.knowledge.server.dto.DocumentSearchRequest;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbDocumentVersion;
import com.moli.knowledge.server.service.KbDocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/document")
@Api(tags = "知识文档")
public class KbDocumentController {

    @Resource
    private KbDocumentService kbDocumentService;

    @GetMapping("/search")
    @ApiOperation("搜索文档")
    public MoliResult<Page<KbDocument>> search(DocumentSearchRequest request) {
        return MoliResult.success(kbDocumentService.search(request));
    }

    @GetMapping("/{id}")
    @ApiOperation("文档详情")
    public MoliResult<DocumentDetailVo> detail(@PathVariable Long id) {
        return MoliResult.success(kbDocumentService.detail(id));
    }

    @PostMapping
    @ApiOperation("保存文档（已停用，请用 Wiki 编辑 + Sync）")
    public MoliResult<Long> save(@Validated @RequestBody DocumentSaveRequest request) {
        return MoliResult.success(kbDocumentService.save(request));
    }

    @PutMapping("/{id}/publish")
    @ApiOperation("发布文档（已停用，status 由 wiki frontmatter + Sync 决定）")
    public MoliResult<Boolean> publish(@PathVariable Long id) {
        kbDocumentService.publish(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping("/{id}/archive")
    @ApiOperation("归档文档（已停用，请改 wiki frontmatter.status 后 Sync）")
    public MoliResult<Boolean> archive(@PathVariable Long id) {
        kbDocumentService.archive(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除文档（已停用，请删 wiki 文件后 Sync 软删）")
    public MoliResult<Boolean> delete(@PathVariable Long id) {
        kbDocumentService.delete(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping("/{id}/move")
    @ApiOperation("移动文档到另一分类(=目录)：移 wiki 文件 + 改引用 + 触发 Sync")
    public MoliResult<DocumentMoveResultVo> move(@PathVariable Long id,
                                                 @RequestParam Long toCategoryId) {
        return MoliResult.success(kbDocumentService.move(id, toCategoryId));
    }

    @GetMapping("/{id}/versions")
    @ApiOperation("版本历史")
    public MoliResult<Page<KbDocumentVersion>> versions(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return MoliResult.success(kbDocumentService.versions(id, pageNum, pageSize));
    }
}
