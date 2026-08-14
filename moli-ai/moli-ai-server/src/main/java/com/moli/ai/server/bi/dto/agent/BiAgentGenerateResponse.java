package com.moli.ai.server.bi.dto.agent;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BiAgentGenerateResponse {

    private String draftSql;
    private List<String> usedTables = new ArrayList<>();
    private String schemaDigest;
    private String refusal;
}
