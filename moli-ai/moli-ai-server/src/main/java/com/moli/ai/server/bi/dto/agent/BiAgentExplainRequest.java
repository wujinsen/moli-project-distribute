package com.moli.ai.server.bi.dto.agent;

import com.moli.ai.server.bi.dto.BiColumnVo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BiAgentExplainRequest {

    private String sessionId;
    private String question;
    private String sql;
    private List<BiColumnVo> columns = new ArrayList<>();
    private List<Map<String, Object>> rowsSample = new ArrayList<>();
    private int rowCount;
}
