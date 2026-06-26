package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiEnrichRequest;
import com.moli.knowledge.server.dto.WikiEnrichResultVo;

/**
 * Wiki enrich 治理（T14 扩展）：已有页追加 patch + log/index/edges。
 * 与 {@code kb/tools/enrich.py}、Ingest EnrichWriter 对齐。
 */
public interface KbWikiEnrichService {

    WikiEnrichResultVo enrich(WikiEnrichRequest request);
}
