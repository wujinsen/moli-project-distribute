package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.entity.KbTag;
import com.moli.knowledge.server.service.KbTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kb/tag")
@Api(tags = "知识标签")
public class KbTagController {

    @Resource
    private KbTagService kbTagService;

    @GetMapping("/list")
    @ApiOperation("空间标签列表")
    public MoliResult<List<KbTag>> list(@RequestParam Long spaceId) {
        return MoliResult.success(kbTagService.listBySpace(spaceId));
    }

    @PostMapping
    @ApiOperation("创建标签")
    public MoliResult<Long> create(@RequestBody KbTag tag) {
        return MoliResult.success(kbTagService.create(tag));
    }

    @PutMapping
    @ApiOperation("更新标签")
    public MoliResult<Boolean> update(@RequestBody KbTag tag) {
        kbTagService.update(tag);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除标签")
    public MoliResult<Boolean> delete(@PathVariable Long id) {
        kbTagService.delete(id);
        return MoliResult.success(Boolean.TRUE);
    }
}
