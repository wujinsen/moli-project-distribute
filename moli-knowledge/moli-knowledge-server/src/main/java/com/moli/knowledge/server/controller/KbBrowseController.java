package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.config.KbBrowseProperties;
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

    @Resource
    private KbBrowseProperties browseProperties;

    private String resolveGroupBy(String groupBy) {
        return groupBy != null ? groupBy : browseProperties.getDefaultGroupBy();
    }

    @GetMapping("/index")
    @ApiOperation("目录 meta（groupBy=category|type 分组计数，默认 category；展开分组调 /index/items）")
    public MoliResult<IndexTreeVo> index(@RequestParam(required = false) Long spaceId,
                                         @RequestParam(required = false) String groupBy) {
        return MoliResult.success(kbBrowseService.index(spaceId, resolveGroupBy(groupBy)));
    }

    @GetMapping("/index/items")
    @ApiOperation("目录分组条目分页（轻量：id/slug/title/spaceId）。key 为 categoryId 或 kb_type")
    public MoliResult<IndexItemsPageVo> indexItems(@RequestParam(required = false) Long spaceId,
                                                   @RequestParam(required = false) String groupBy,
                                                   @RequestParam(required = false) String key,
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "50") int pageSize) {
        String groupKey = key != null ? key : type;
        return MoliResult.success(kbBrowseService.indexItems(spaceId, resolveGroupBy(groupBy), groupKey, pageNum, pageSize));
    }

    @GetMapping("/index/search")
    @ApiOperation("目录搜索（服务端过滤，按 groupBy 分组，默认 category）")
    public MoliResult<IndexTreeVo> indexSearch(@RequestParam(required = false) Long spaceId,
                                             @RequestParam String q,
                                             @RequestParam(defaultValue = "200") int limit,
                                             @RequestParam(required = false) String groupBy) {
        return MoliResult.success(kbBrowseService.indexSearch(spaceId, q, limit, resolveGroupBy(groupBy)));
    }

    @GetMapping("/index/locate")
    @ApiOperation("按 slug 定位所属分组（深链展开用，默认按 category）")
    public MoliResult<IndexLocateVo> locate(@RequestParam(required = false) Long spaceId,
                                            @RequestParam String slug,
                                            @RequestParam(required = false) String groupBy) {
        return MoliResult.success(kbBrowseService.locate(spaceId, slug, resolveGroupBy(groupBy)));
    }

    @GetMapping("/page")
    @ApiOperation("按 slug 取单页（含出链/入链）。slug 形如 services/用户中心，含斜杠故用查询参数")
    public MoliResult<PageDetailVo> page(@RequestParam String slug,
                                         @RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbBrowseService.page(slug, spaceId));
    }
}
