package com.moli.knowledge.server.dto.retrieval;

import lombok.Data;

@Data
public class RerankHitDto {
    private Long chunkId;
    private double score;
    private int rank;
}
