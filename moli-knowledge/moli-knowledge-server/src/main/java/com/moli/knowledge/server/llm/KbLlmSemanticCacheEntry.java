package com.moli.knowledge.server.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KbLlmSemanticCacheEntry {

    private String answer;
    private String provider;
    private String model;
    private long createdAt;
}
