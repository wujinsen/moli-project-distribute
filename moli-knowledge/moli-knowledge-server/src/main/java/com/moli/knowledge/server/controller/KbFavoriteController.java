package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.service.KbFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/favorite")
@Api(tags = "文档收藏")
public class KbFavoriteController {

    @Resource
    private KbFavoriteService kbFavoriteService;

    @PostMapping("/{documentId}")
    @ApiOperation("收藏文档")
    public MoliResult<Boolean> add(@PathVariable Long documentId) {
        kbFavoriteService.add(documentId);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{documentId}")
    @ApiOperation("取消收藏")
    public MoliResult<Boolean> remove(@PathVariable Long documentId) {
        kbFavoriteService.remove(documentId);
        return MoliResult.success(Boolean.TRUE);
    }

    @GetMapping("/my")
    @ApiOperation("我的收藏")
    public MoliResult<Page<KbDocument>> myFavorites(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return MoliResult.success(kbFavoriteService.myFavorites(pageNum, pageSize));
    }
}
