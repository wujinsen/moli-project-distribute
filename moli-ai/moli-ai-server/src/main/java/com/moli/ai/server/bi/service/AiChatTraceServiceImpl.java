package com.moli.ai.server.bi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.ai.server.bi.dto.BiChatTraceVo;
import com.moli.ai.server.bi.dto.BiTraceStep;
import com.moli.ai.server.bi.enums.BiChatResponseCode;
import com.moli.ai.server.entity.AiChatTrace;
import com.moli.ai.server.mapper.AiChatTraceMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class AiChatTraceServiceImpl implements AiChatTraceService {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final AiChatTraceMapper traceMapper;
    private final ObjectMapper objectMapper;

    public AiChatTraceServiceImpl(AiChatTraceMapper traceMapper, ObjectMapper objectMapper) {
        this.traceMapper = traceMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveTrace(String traceId, String sessionId, Long userId, String question,
                          String finalSql, String status, String rejectCode, String rejectReason,
                          Integer rowCount, Long latencyMs, Integer retry, List<BiTraceStep> steps) {
        AiChatTrace entity = new AiChatTrace();
        entity.setTraceId(traceId);
        entity.setSessionId(sessionId);
        entity.setUserId(userId);
        entity.setQuestion(question);
        entity.setFinalSql(finalSql);
        entity.setStatus(status);
        entity.setRejectCode(rejectCode);
        entity.setRejectReason(rejectReason);
        entity.setRowCount(rowCount);
        entity.setLatencyMs(latencyMs);
        entity.setRetry(retry == null ? 0 : retry);
        entity.setStepsJson(toJson(steps));
        entity.setCreatedAt(new Date());
        traceMapper.insert(entity);
    }

    @Override
    public BiChatTraceVo getTrace(String traceId, Long currentUserId, boolean traceAllPermitted) {
        AiChatTrace entity = traceMapper.selectOne(new LambdaQueryWrapper<AiChatTrace>()
                .eq(AiChatTrace::getTraceId, traceId));
        if (entity == null) {
            throw new BiChatTraceNotFoundException();
        }
        if (!traceAllPermitted && currentUserId != null && entity.getUserId() != null
                && !currentUserId.equals(entity.getUserId())) {
            throw new BiChatTraceForbiddenException();
        }
        BiChatTraceVo vo = new BiChatTraceVo();
        vo.setTraceId(entity.getTraceId());
        vo.setSessionId(entity.getSessionId());
        vo.setUserId(entity.getUserId());
        vo.setQuestion(entity.getQuestion());
        vo.setFinalSql(entity.getFinalSql());
        vo.setStatus(entity.getStatus());
        vo.setRejectCode(entity.getRejectCode());
        vo.setRejectReason(entity.getRejectReason());
        vo.setRowCount(entity.getRowCount());
        vo.setLatencyMs(entity.getLatencyMs());
        vo.setRetry(entity.getRetry());
        vo.setSteps(fromJson(entity.getStepsJson()));
        if (entity.getCreatedAt() != null) {
            vo.setCreatedAt(SDF.format(entity.getCreatedAt()));
        }
        return vo;
    }

    private String toJson(List<BiTraceStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(steps);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private List<BiTraceStep> fromJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, BiTraceStep.class));
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public static class BiChatTraceNotFoundException extends RuntimeException {
        public BiChatResponseCode getCode() {
            return BiChatResponseCode.BI_CHAT_TRACE_NOT_FOUND;
        }
    }

    public static class BiChatTraceForbiddenException extends RuntimeException {
        public BiChatResponseCode getCode() {
            return BiChatResponseCode.BI_CHAT_TRACE_FORBIDDEN;
        }
    }
}
