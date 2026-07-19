package com.moli.knowledge.server.dto.retrieval;

import lombok.Data;

@Data
public class RerankCandidateDto {
    private Long chunkId;
    private String text;
}
