package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.GraphVo;

/**
 * Wiki 文件直读图谱（wikilink + related + graph/edges.jsonl），对齐 kb/tools/serve.py /api/graph。
 */
public interface KbWikiGraphService {

    GraphVo graph(Long spaceId, String mode, Integer maxNodes, Integer minDeg);
}
