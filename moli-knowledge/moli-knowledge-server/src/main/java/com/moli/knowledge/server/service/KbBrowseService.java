package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.IndexTreeVo;
import com.moli.knowledge.server.dto.PageDetailVo;

public interface KbBrowseService {

    /** 目录树：已发布文档按 kb_type 分组。 */
    IndexTreeVo index(Long spaceId);

    /** 按 slug 取单页 + 出链/入链。 */
    PageDetailVo page(String slug, Long spaceId);
}
