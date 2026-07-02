package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.KbTypeFacetVo;
import com.moli.knowledge.server.dto.PageDetailVo;

import java.util.List;

public interface KbBrowseService {

    /**
     * 目录 meta：按分类（=目录）分组计数，不含 items。
     * {@code kbTypes} 非空优先于 {@code kbType}；叠加体裁 OR 过滤后再统计（facet 联动）。
     */
    IndexTreeVo index(Long spaceId, List<Long> spaceIds, List<String> kbTypes, String kbType);

    /**
     * 体裁 facet：当前作用域（空间 + 可选分类 OR）下各 kb_type 已发布计数。
     * {@code categoryIds} 非空优先于 {@code categoryId}；可与 {@code uncategorizedOnly} 组合。
     */
    KbTypeFacetVo types(Long spaceId, List<Long> spaceIds, List<Long> categoryIds, Long categoryId,
                        Boolean uncategorizedOnly);

    /** 某分类下条目分页；key 为 categoryId 或 uncategorized。 */
    IndexItemsPageVo indexItems(Long spaceId, List<Long> spaceIds, String key, int pageNum, int pageSize);

    /** 目录搜索：服务端过滤，按分类分组返回。 */
    IndexTreeVo indexSearch(Long spaceId, List<Long> spaceIds, String q, int limit);

    /** 按 slug 定位所属分类（深链展开）。 */
    IndexLocateVo locate(Long spaceId, List<Long> spaceIds, String slug);

    PageDetailVo page(String slug, Long spaceId);
}
