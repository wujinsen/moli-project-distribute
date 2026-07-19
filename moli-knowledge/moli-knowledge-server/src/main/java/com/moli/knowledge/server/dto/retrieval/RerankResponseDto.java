package com.moli.knowledge.server.dto.retrieval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RerankResponseDto {
    private String model;
    private List<RerankHitDto> results = new ArrayList<>();
}
