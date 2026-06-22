package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.entity.KbComment;
import com.moli.knowledge.server.service.KbCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/comment")
@Api(tags = "文档评论")
public class KbCommentController {

    @Resource
    private KbCommentService kbCommentService;

    @GetMapping("/page")
    @ApiOperation("评论分页")
    public MoliResult<Page<KbComment>> page(@RequestParam Long documentId,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return MoliResult.success(kbCommentService.page(documentId, pageNum, pageSize));
    }

    @PostMapping
    @ApiOperation("发表评论")
    public MoliResult<Long> create(@RequestBody KbComment comment) {
        return MoliResult.success(kbCommentService.create(comment));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除评论")
    public MoliResult<Boolean> delete(@PathVariable Long id) {
        kbCommentService.delete(id);
        return MoliResult.success(Boolean.TRUE);
    }
}
