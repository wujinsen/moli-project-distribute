package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.IndexItemsPageVo;
import com.moli.knowledge.server.dto.IndexLocateVo;
import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.PageDetailVo;

public interface KbBrowseService {

    /** 目录 meta：按 kb_type 分组计数，不含 items（轻量首屏）。 */
    IndexTreeVo index(Long spaceId);

    /** 分组内条目分页（轻量：id/slug/title/spaceId）。 */
    IndexItemsPageVo indexItems(Long spaceId, String type, int pageNum, int pageSize);

    /** 目录搜索：服务端过滤，按类型分组返回（limit 条）。 */
    IndexTreeVo indexSearch(Long spaceId, String q, int limit);

    /** 按 slug 定位所属分组与条目（深链展开用）。 */
    IndexLocateVo locate(Long spaceId, String slug);

    /** 按 slug 取单页 + 出链/入链。 */
    PageDetailVo page(String slug, Long spaceId);
}
