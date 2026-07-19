package com.moli.ai.server.bi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BiChatAskVo {

    private String traceId;
    private String sessionId;
    private String status;
    private String sql;
    private List<BiColumnVo> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();
    private BiChartVo chart;
    private String explanation;
    private String rejectCode;
    private String rejectReason;
    private Integer rowCount;
    private Long latencyMs;
    private Integer retry;
}
