package com.moli.ai.server.bi.dto.agent;

import com.moli.ai.server.bi.dto.BiChartVo;
import lombok.Data;

@Data
public class BiAgentExplainResponse {

    private String explanation;
    private BiChartVo chart;
}
