package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.CategoryTreeVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.service.KbCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kb/category")
@Api(tags = "知识分类")
public class KbCategoryController {

    @Resource
    private KbCategoryService kbCategoryService;

    @GetMapping("/tree")
    @ApiOperation("分类树")
    public MoliResult<List<CategoryTreeVo>> tree(@RequestParam Long spaceId) {
        return MoliResult.success(kbCategoryService.tree(spaceId));
    }

    @PostMapping
    @ApiOperation("创建分类")
    public MoliResult<Long> create(@RequestBody KbCategory category) {
        return MoliResult.success(kbCategoryService.create(category));
    }

    @PutMapping
    @ApiOperation("更新分类")
    public MoliResult<Boolean> update(@RequestBody KbCategory category) {
        kbCategoryService.update(category);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public MoliResult<Boolean> delete(@PathVariable Long id) {
        kbCategoryService.delete(id);
        return MoliResult.success(Boolean.TRUE);
    }
}
