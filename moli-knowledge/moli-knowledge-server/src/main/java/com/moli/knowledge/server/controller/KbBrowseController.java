package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
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
    @ApiOperation("目录 meta（按类型分组计数，不含 items；展开分组调 /index/items）")
    public MoliResult<IndexTreeVo> index(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbBrowseService.index(spaceId));
    }

    @GetMapping("/index/items")
    @ApiOperation("目录分组条目分页（轻量：id/slug/title/spaceId）")
    public MoliResult<IndexItemsPageVo> indexItems(@RequestParam(required = false) Long spaceId,
                                                   @RequestParam String type,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "50") int pageSize) {
        return MoliResult.success(kbBrowseService.indexItems(spaceId, type, pageNum, pageSize));
    }

    @GetMapping("/index/search")
    @ApiOperation("目录搜索（服务端过滤，按类型分组）")
    public MoliResult<IndexTreeVo> indexSearch(@RequestParam(required = false) Long spaceId,
                                             @RequestParam String q,
                                             @RequestParam(defaultValue = "200") int limit) {
        return MoliResult.success(kbBrowseService.indexSearch(spaceId, q, limit));
    }

    @GetMapping("/index/locate")
    @ApiOperation("按 slug 定位所属分组（深链展开用）")
    public MoliResult<IndexLocateVo> locate(@RequestParam(required = false) Long spaceId,
                                            @RequestParam String slug) {
        return MoliResult.success(kbBrowseService.locate(spaceId, slug));
    }

    @GetMapping("/page")
    @ApiOperation("按 slug 取单页（含出链/入链）。slug 形如 services/用户中心，含斜杠故用查询参数")
    public MoliResult<PageDetailVo> page(@RequestParam String slug,
                                         @RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbBrowseService.page(slug, spaceId));
    }
}
