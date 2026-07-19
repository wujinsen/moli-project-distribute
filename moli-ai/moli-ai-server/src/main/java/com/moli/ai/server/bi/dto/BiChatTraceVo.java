package com.moli.ai.server.bi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BiChatTraceVo {

    private String traceId;
    private String sessionId;
    private Long userId;
    private String question;
    private String finalSql;
    private String status;
    private String rejectCode;
    private String rejectReason;
    private Integer rowCount;
    private Long latencyMs;
    private Integer retry;
    private List<BiTraceStep> steps = new ArrayList<>();
    private String createdAt;
}
