package com.moli.knowledge.server.dto.retrieval;

import lombok.Data;

/** sidecar POST /search 响应条目（AI-2 §1.2②）。 */
@Data
public class VectorSearchHit {
    private Long chunkId;
    private Long docId;
    private Long spaceId;
    private String slug;
    private String kbType;
    private double score;
    private int rank;
}
