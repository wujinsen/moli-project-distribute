package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.PageDetailVo;

public interface KbBrowseService {

    /** 目录 meta：按分类（=目录）分组计数，不含 items。 */
    IndexTreeVo index(Long spaceId);

    /** 某分类下条目分页；key 为 categoryId 或 uncategorized。 */
    IndexItemsPageVo indexItems(Long spaceId, String key, int pageNum, int pageSize);

    /** 目录搜索：服务端过滤，按分类分组返回。 */
    IndexTreeVo indexSearch(Long spaceId, String q, int limit);

    /** 按 slug 定位所属分类（深链展开）。 */
    IndexLocateVo locate(Long spaceId, String slug);

    PageDetailVo page(String slug, Long spaceId);
}
