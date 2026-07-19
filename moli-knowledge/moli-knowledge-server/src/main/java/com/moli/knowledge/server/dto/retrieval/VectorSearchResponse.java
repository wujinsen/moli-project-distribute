package com.moli.knowledge.server.dto.retrieval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VectorSearchResponse {
    private String model;
    private List<VectorSearchHit> results = new ArrayList<>();
}
