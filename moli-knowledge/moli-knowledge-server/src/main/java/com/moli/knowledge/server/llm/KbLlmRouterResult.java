package com.moli.knowledge.server.llm;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KbLlmRouterResult {

    String answer;
    String provider;
    String model;
    boolean failover;
}
