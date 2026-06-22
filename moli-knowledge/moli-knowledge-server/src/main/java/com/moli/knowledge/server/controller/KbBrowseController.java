package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.PageDetailVo;
import com.moli.knowledge.server.service.KbBrowseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb")
@Api(tags = "知识库浏览")
public class KbBrowseController {

    @Resource
    private KbBrowseService kbBrowseService;

    @GetMapping("/index")
    @ApiOperation("目录树（已发布文档按知识类型分组）")
    public MoliResult<IndexTreeVo> index(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbBrowseService.index(spaceId));
    }

    @GetMapping("/page")
    @ApiOperation("按 slug 取单页（含出链/入链）。slug 形如 services/用户中心，含斜杠故用查询参数")
    public MoliResult<PageDetailVo> page(@RequestParam String slug,
                                         @RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbBrowseService.page(slug, spaceId));
    }
}
