package com.moli.ai.server.bi.dto.agent;

import lombok.Data;

@Data
public class BiAgentGenerateRequest {

    private String sessionId;
    private String question;
    private int retry;
    private String priorSql;
    private String priorError;
}
