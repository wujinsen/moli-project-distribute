package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbAccessibleSpaceVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.service.KbSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kb/space")
@Api(tags = "知识空间")
public class KbSpaceController {

    @Resource
    private KbSpaceService kbSpaceService;

    @GetMapping("/mine")
    @ApiOperation("当前用户可读空间（含权限摘要，供前端选择器）")
    public MoliResult<List<KbAccessibleSpaceVo>> listMine() {
        return MoliResult.success(kbSpaceService.listAccessible());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询空间")
    public MoliResult<Page<KbSpace>> page(KbSpace query,
                                          @RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return MoliResult.success(kbSpaceService.page(query, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @ApiOperation("空间详情")
    public MoliResult<KbSpace> detail(@PathVariable Long id) {
        return MoliResult.success(kbSpaceService.getById(id));
    }

    @PostMapping
    @ApiOperation("创建空间")
    public MoliResult<Long> create(@RequestBody KbSpace space) {
        return MoliResult.success(kbSpaceService.create(space));
    }

    @PutMapping
    @ApiOperation("更新空间")
    public MoliResult<Boolean> update(@RequestBody KbSpace space) {
        kbSpaceService.update(space);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除空间")
    public MoliResult<Boolean> delete(@PathVariable Long id) {
        kbSpaceService.delete(id);
        return MoliResult.success(Boolean.TRUE);
    }
}
