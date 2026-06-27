package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.RawCoverageVo;

/**
 * raw 覆盖索引：从 wiki frontmatter {@code sources} 反向映射 raw 文件 ingest 状态。
 */
public interface KbRawCoverageService {

    /**
     * @param spaceId 目标空间（默认 enterprise-kb）
     * @param prefix  相对 rawRoot 子目录，空=全库
     * @param filter  all | open | covered | cluster
     * @param refresh true 时跳过缓存
     */
    RawCoverageVo coverage(Long spaceId, String prefix, String filter, boolean refresh);

    /** commit / 手工刷新后丢弃该空间 wiki 索引缓存。 */
    void invalidateCache(Long spaceId);
}
