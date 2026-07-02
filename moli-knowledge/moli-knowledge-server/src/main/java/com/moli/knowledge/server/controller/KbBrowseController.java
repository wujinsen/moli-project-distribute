package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.KbTypeFacetVo;
import com.moli.knowledge.server.dto.KbTypeOptionVo;
import com.moli.knowledge.server.dto.PageDetailVo;
import com.moli.knowledge.server.service.KbBrowseService;
import com.moli.knowledge.server.support.KbTypeConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kb")
@Api(tags = "知识库浏览")
public class KbBrowseController {

    @Resource
    private KbBrowseService kbBrowseService;

    private void assertCategoryGroupBy(String groupBy) {
        if (StringUtils.isNotBlank(groupBy) && !"category".equalsIgnoreCase(groupBy.trim())) {
            throw new BaseException("groupBy=" + groupBy + " 已废弃，仅支持 category（分类=目录）");
        }
    }

    @GetMapping("/index")
    @ApiOperation("目录 meta（按分类=目录分组计数；展开分组调 /index/items）")
    public MoliResult<IndexTreeVo> index(@RequestParam(required = false) Long spaceId,
                                         @RequestParam(required = false) List<Long> spaceIds,
                                         @RequestParam(required = false) String kbType,
                                         @RequestParam(required = false) String groupBy) {
        assertCategoryGroupBy(groupBy);
        return MoliResult.success(kbBrowseService.index(spaceId, spaceIds, normalizeKbTypeOrNull(kbType)));
    }

    private String normalizeKbTypeOrNull(String kbType) {
        if (StringUtils.isBlank(kbType)) {
            return null;
        }
        String normalized = KbTypeConstants.normalize(kbType.trim());
        if (normalized == null) {
            throw new BaseException("非法体裁 kbType=" + kbType
                    + "，可选：" + String.join("|", KbTypeConstants.ALL));
        }
        return normalized;
    }

    @GetMapping("/index/types")
    @ApiOperation("体裁 facet：当前空间(可叠加分类)下各 kb_type 已发布计数，供体裁 chip")
    public MoliResult<KbTypeFacetVo> indexTypes(@RequestParam(required = false) Long spaceId,
                                                @RequestParam(required = false) List<Long> spaceIds,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) Boolean uncategorizedOnly) {
        return MoliResult.success(kbBrowseService.types(spaceId, spaceIds, categoryId, uncategorizedOnly));
    }

    @GetMapping("/meta/kb-types")
    @ApiOperation("体裁白名单选项（前端下拉/筛选数据源；单一来源 KbTypeConstants）")
    public MoliResult<List<KbTypeOptionVo>> kbTypes() {
        List<KbTypeOptionVo> options = KbTypeConstants.ALL.stream()
                .map(t -> new KbTypeOptionVo(t, KbTypeConstants.label(t)))
                .collect(Collectors.toList());
        return MoliResult.success(options);
    }

    @GetMapping("/index/items")
    @ApiOperation("目录分组条目分页（key=categoryId 或 uncategorized）")
    public MoliResult<IndexItemsPageVo> indexItems(@RequestParam(required = false) Long spaceId,
                                                   @RequestParam(required = false) List<Long> spaceIds,
                                                   @RequestParam(required = false) String groupBy,
                                                   @RequestParam(required = false) String key,
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "50") int pageSize) {
        assertCategoryGroupBy(groupBy);
        String groupKey = key != null ? key : type;
        return MoliResult.success(kbBrowseService.indexItems(spaceId, spaceIds, groupKey, pageNum, pageSize));
    }

    @GetMapping("/index/search")
    @ApiOperation("目录搜索（按分类分组）")
    public MoliResult<IndexTreeVo> indexSearch(@RequestParam(required = false) Long spaceId,
                                             @RequestParam(required = false) List<Long> spaceIds,
                                             @RequestParam String q,
                                             @RequestParam(defaultValue = "200") int limit,
                                             @RequestParam(required = false) String groupBy) {
        assertCategoryGroupBy(groupBy);
        return MoliResult.success(kbBrowseService.indexSearch(spaceId, spaceIds, q, limit));
    }

    @GetMapping("/index/locate")
    @ApiOperation("按 slug 定位所属分类（深链展开用）")
    public MoliResult<IndexLocateVo> locate(@RequestParam(required = false) Long spaceId,
                                            @RequestParam(required = false) List<Long> spaceIds,
                                            @RequestParam String slug,
                                            @RequestParam(required = false) String groupBy) {
        assertCategoryGroupBy(groupBy);
        return MoliResult.success(kbBrowseService.locate(spaceId, spaceIds, slug));
    }

    @GetMapping("/page")
    @ApiOperation("按 slug 取单页（含出链/入链）。slug 形如 services/用户中心，含斜杠故用查询参数")
    public MoliResult<PageDetailVo> page(@RequestParam String slug,
                                         @RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbBrowseService.page(slug, spaceId));
    }
}
