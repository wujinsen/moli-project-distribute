package com.moli.ai.server.bi.service;

import com.moli.ai.server.bi.dto.BiChatTraceVo;
import com.moli.ai.server.bi.dto.BiTraceStep;

import java.util.List;

public interface AiChatTraceService {

    void saveTrace(String traceId, String sessionId, Long userId, String question,
                   String finalSql, String status, String rejectCode, String rejectReason,
                   Integer rowCount, Long latencyMs, Integer retry, List<BiTraceStep> steps);

    BiChatTraceVo getTrace(String traceId, Long currentUserId, boolean traceAllPermitted);
}
