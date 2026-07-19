package com.moli.ai.server.bi.dto;

import lombok.Data;

@Data
public class BiTraceStep {

    private String node;
    private String outcome;
    private String detail;
    private Long costMs;
}
