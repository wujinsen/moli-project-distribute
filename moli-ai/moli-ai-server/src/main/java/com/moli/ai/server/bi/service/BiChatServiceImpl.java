package com.moli.ai.server.bi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.ai.server.bi.config.BiChatProperties;
import com.moli.ai.server.bi.constant.BiChatPermissionConstants;
import com.moli.ai.server.bi.dto.BiChartVo;
import com.moli.ai.server.bi.dto.BiChatAskRequest;
import com.moli.ai.server.bi.dto.BiChatAskVo;
import com.moli.ai.server.bi.dto.BiChatTraceVo;
import com.moli.ai.server.bi.dto.BiColumnVo;
import com.moli.ai.server.bi.dto.BiTraceStep;
import com.moli.ai.server.bi.dto.agent.BiAgentExplainRequest;
import com.moli.ai.server.bi.dto.agent.BiAgentExplainResponse;
import com.moli.ai.server.bi.dto.agent.BiAgentGenerateRequest;
import com.moli.ai.server.bi.dto.agent.BiAgentGenerateResponse;
import com.moli.ai.server.bi.enums.BiChatResponseCode;
import com.moli.ai.server.bi.security.BiSqlRejectCode;
import com.moli.ai.server.bi.security.BiSqlSecurityValidator;
import com.moli.ai.server.bi.security.BiSqlValidationResult;
import com.moli.ai.server.bi.support.BiAgentClient;
import com.moli.ai.server.bi.support.BiAgentUnavailableException;
import com.moli.ai.server.bi.support.BiChatProgressSink;
import com.moli.ai.server.bi.support.BiChatReadonlyQueryExecutor;
import com.moli.ai.server.bi.support.BiChatSseProgressSink;
import com.moli.ai.server.bi.support.BiChatStreamHelper;
import com.moli.ai.server.bi.support.BiQueryExecutionException;
import com.moli.ai.server.bi.support.BiQueryResult;
import com.moli.ai.server.bi.service.AiChatTraceServiceImpl.BiChatTraceForbiddenException;
import com.moli.ai.server.bi.service.AiChatTraceServiceImpl.BiChatTraceNotFoundException;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.utils.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * §2.1 Java conductor：agent 生成 → AST 校验 → 只读执行 → explain → trace。
 */
@Slf4j
@Service
public class BiChatServiceImpl implements BiChatService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_ERROR = "ERROR";

    private final BiChatProperties chatProperties;
    private final BiAgentClient agentClient;
    private final BiSqlSecurityValidator sqlValidator;
    private final BiChatReadonlyQueryExecutor queryExecutor;
    private final AiChatTraceService traceService;
    private final ObjectMapper objectMapper;

    public BiChatServiceImpl(BiChatProperties chatProperties,
                             BiAgentClient agentClient,
                             BiSqlSecurityValidator sqlValidator,
                             BiChatReadonlyQueryExecutor queryExecutor,
                             AiChatTraceService traceService,
                             ObjectMapper objectMapper) {
        this.chatProperties = chatProperties;
        this.agentClient = agentClient;
        this.sqlValidator = sqlValidator;
        this.queryExecutor = queryExecutor;
        this.traceService = traceService;
        this.objectMapper = objectMapper;
    }

    @Override
    public MoliResult<BiChatAskVo> ask(BiChatAskRequest request) {
        AskExecution execution = executeAsk(request, null, resolveUserId());
        if (execution.errorCode != null) {
            return MoliResult.errorMsg(execution.errorCode.getCode(), execution.errorCode.getMessage());
        }
        return MoliResult.success(execution.vo);
    }

    @Override
    public SseEmitter askStream(BiChatAskRequest request) {
        long timeout = chatProperties.getSseTimeoutMs() <= 0 ? 120000L : chatProperties.getSseTimeoutMs();
        SseEmitter emitter = new SseEmitter(timeout);
        BiChatProgressSink sink = new BiChatSseProgressSink(emitter, objectMapper);
        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> log.debug("SSE client disconnected: {}", ex.getMessage()));
        final Long resolvedUserId = resolveUserId();
        Thread worker = new Thread(() -> {
            try {
                executeAsk(request, sink, resolvedUserId);
                emitter.complete();
            } catch (Exception ex) {
                log.warn("SSE ask failed: {}", ex.getMessage());
                emitter.completeWithError(ex);
            }
        }, "bi-chat-sse");
        worker.setDaemon(true);
        worker.start();
        return emitter;
    }

    @Override
    public MoliResult<BiChatTraceVo> getTrace(String traceId) {
        try {
            BiChatTraceVo vo = traceService.getTrace(traceId, resolveUserId(), hasTraceAllPermission());
            return MoliResult.success(vo);
        } catch (BiChatTraceNotFoundException ex) {
            return MoliResult.errorMsg(BiChatResponseCode.BI_CHAT_TRACE_NOT_FOUND.getCode(),
                    BiChatResponseCode.BI_CHAT_TRACE_NOT_FOUND.getMessage());
        } catch (BiChatTraceForbiddenException ex) {
            return MoliResult.errorMsg(BiChatResponseCode.BI_CHAT_TRACE_FORBIDDEN.getCode(),
                    BiChatResponseCode.BI_CHAT_TRACE_FORBIDDEN.getMessage());
        }
    }

    private AskExecution executeAsk(BiChatAskRequest request, BiChatProgressSink sink, Long userId) {
        long startMs = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId().trim() : traceId;
        Long effectiveUserId = userId != null ? userId : 0L;
        List<BiTraceStep> steps = new ArrayList<>();

        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
        if (!StringUtils.hasText(question) || question.length() > 500) {
            BiChatAskVo vo = baseVo(traceId, sessionId, STATUS_ERROR, 0, startMs);
            vo.setExplanation("问题无效或超过 500 字符");
            traceService.saveTrace(traceId, sessionId, effectiveUserId, question, null, STATUS_ERROR,
                    null, "question invalid", null, elapsed(startMs), 0, steps);
            BiChatResponseCode code = BiChatResponseCode.BI_CHAT_QUESTION_INVALID;
            if (sink != null) {
                sink.error(code.getCode(), code.getMessage());
            }
            return AskExecution.error(code, vo);
        }

        int maxRetry = Math.max(0, chatProperties.getMaxRetry());
        int retry = 0;
        String priorSql = null;
        String priorError = null;
        String finalSql = null;
        String status = STATUS_ERROR;
        String rejectCode = null;
        String rejectReason = null;
        Integer rowCount = null;
        List<BiColumnVo> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        BiChartVo chart = defaultChart();
        String explanation = null;
        BiChatResponseCode errorCode = null;

        if (sink != null) {
            sink.stage("schema", traceId);
        }

        while (true) {
            long genStart = System.currentTimeMillis();
            BiAgentGenerateResponse gen;
            try {
                BiAgentGenerateRequest genReq = new BiAgentGenerateRequest();
                genReq.setSessionId(sessionId);
                genReq.setQuestion(question);
                genReq.setRetry(retry);
                genReq.setPriorSql(priorSql);
                genReq.setPriorError(priorError);
                gen = agentClient.generate(genReq);
            } catch (BiAgentUnavailableException ex) {
                addStep(steps, "generate_sql", "error", "agent unavailable",
                        System.currentTimeMillis() - genStart);
                errorCode = BiChatResponseCode.BI_CHAT_AGENT_UNAVAILABLE;
                status = STATUS_ERROR;
                break;
            }
            addStep(steps, "retrieve_schema", "ok", abbreviate(gen.getSchemaDigest(), 200),
                    System.currentTimeMillis() - genStart);

            if (StringUtils.hasText(gen.getRefusal())) {
                addStep(steps, "generate_sql", "rejected", gen.getRefusal(), 0L);
                status = STATUS_REJECTED;
                rejectCode = BiSqlRejectCode.REJECT_SEMANTIC.getCode();
                rejectReason = sanitizeRejectReason(gen.getRefusal());
                explanation = rejectReason;
                break;
            }

            if (!StringUtils.hasText(gen.getDraftSql())) {
                addStep(steps, "generate_sql", "retry", "draftSql empty", 0L);
                if (retry < maxRetry) {
                    retry++;
                    priorError = "未能生成有效 SELECT，请修正";
                    continue;
                }
                errorCode = BiChatResponseCode.BI_SQL_GENERATION_FAILED;
                status = STATUS_ERROR;
                break;
            }

            addStep(steps, "generate_sql", "ok", abbreviate(gen.getDraftSql(), 200), 0L);
            if (sink != null) {
                sink.stage("sql", traceId);
            }

            long validateStart = System.currentTimeMillis();
            if (sink != null) {
                sink.stage("validate", traceId);
            }
            BiSqlValidationResult validation = sqlValidator.validate(gen.getDraftSql());
            if (!validation.isPassed()) {
                BiSqlRejectCode code = validation.getRejectCode();
                addStep(steps, "validate", "rejected", code.getCode(), System.currentTimeMillis() - validateStart);
                if (isRetryableReject(code) && retry < maxRetry) {
                    retry++;
                    priorSql = gen.getDraftSql();
                    priorError = validation.getRejectReason();
                    addStep(steps, "validate", "retry", priorError, 0L);
                    continue;
                }
                status = STATUS_REJECTED;
                rejectCode = code.getCode();
                rejectReason = sanitizeRejectReason(validation.getRejectReason());
                explanation = rejectReason;
                break;
            }
            addStep(steps, "validate", "ok", "passed", System.currentTimeMillis() - validateStart);
            finalSql = validation.getSanitizedSql();
            if (sink != null) {
                sink.sql(finalSql);
            }

            if (sink != null) {
                sink.stage("execute", traceId);
            }
            long execStart = System.currentTimeMillis();
            try {
                BiQueryResult queryResult = queryExecutor.execute(finalSql, request.getMaxRows());
                columns = queryResult.getColumns();
                rows = queryResult.getRows();
                rowCount = queryResult.getRowCount();
                addStep(steps, "execute", "ok", "rows=" + rowCount, System.currentTimeMillis() - execStart);
                status = STATUS_SUCCESS;
                break;
            } catch (BiQueryExecutionException ex) {
                addStep(steps, "execute", "error", ex.getCode().name(), System.currentTimeMillis() - execStart);
                if (ex.getCode() == BiChatResponseCode.BI_SQL_EXEC_ERROR && retry < maxRetry) {
                    retry++;
                    priorSql = gen.getDraftSql();
                    priorError = ex.getMessage();
                    addStep(steps, "execute", "retry", priorError, 0L);
                    continue;
                }
                errorCode = ex.getCode();
                status = STATUS_ERROR;
                break;
            }
        }

        if (STATUS_SUCCESS.equals(status)) {
            if (sink != null) {
                sink.stage("summarize", traceId);
            }
            long sumStart = System.currentTimeMillis();
            try {
                BiAgentExplainRequest explainReq = new BiAgentExplainRequest();
                explainReq.setSessionId(sessionId);
                explainReq.setQuestion(question);
                explainReq.setSql(finalSql);
                explainReq.setColumns(columns);
                explainReq.setRowsSample(sampleRows(rows, 5));
                explainReq.setRowCount(rowCount == null ? 0 : rowCount);
                BiAgentExplainResponse explainResp = agentClient.explain(explainReq);
                explanation = explainResp.getExplanation();
                if (explainResp.getChart() != null) {
                    chart = explainResp.getChart();
                }
                addStep(steps, "summarize", "ok", "explained", System.currentTimeMillis() - sumStart);
            } catch (BiAgentUnavailableException ex) {
                explanation = buildFallbackExplanation(question, rowCount, columns);
                chart = buildFallbackChart(question, columns, rowCount);
                addStep(steps, "summarize", "error", "agent unavailable", System.currentTimeMillis() - sumStart);
            }
            if (sink != null) {
                sink.chart(chart);
                for (String token : BiChatStreamHelper.splitExplanationTokens(explanation)) {
                    sink.token(token);
                }
            }
        }

        BiChatAskVo vo = baseVo(traceId, sessionId, status, retry, startMs);
        vo.setSql(finalSql);
        vo.setColumns(columns);
        vo.setRows(rows);
        vo.setChart(chart);
        vo.setExplanation(explanation);
        vo.setRejectCode(rejectCode);
        vo.setRejectReason(rejectReason);
        vo.setRowCount(rowCount);

        traceService.saveTrace(traceId, sessionId, effectiveUserId, question, finalSql, status,
                rejectCode, rejectReason, rowCount, elapsed(startMs), retry, steps);

        if (sink != null) {
            if (errorCode != null) {
                sink.error(errorCode.getCode(), errorCode.getMessage());
            } else {
                sink.done(vo);
            }
        }

        if (errorCode != null) {
            return AskExecution.error(errorCode, vo);
        }
        return AskExecution.success(vo);
    }

    private static String buildFallbackExplanation(String question, Integer rowCount, List<BiColumnVo> columns) {
        String cols = columns == null || columns.isEmpty() ? "无" : columns.get(0).getName();
        return String.format("针对「%s」，共返回 %d 行，主要列 %s。",
                abbreviate(question, 40), rowCount == null ? 0 : rowCount, cols);
    }

    private static BiChartVo buildFallbackChart(String question, List<BiColumnVo> columns, Integer rowCount) {
        BiChartVo chart = defaultChart();
        chart.setTitle(abbreviate(question, 40));
        if (columns != null && columns.size() >= 2 && rowCount != null && rowCount > 0) {
            chart.setType("bar");
            chart.setX(columns.get(0).getName());
            List<String> y = new ArrayList<>();
            y.add(columns.get(1).getName());
            chart.setY(y);
        }
        return chart;
    }

    private static BiChatAskVo baseVo(String traceId, String sessionId, String status, int retry, long startMs) {
        BiChatAskVo vo = new BiChatAskVo();
        vo.setTraceId(traceId);
        vo.setSessionId(sessionId);
        vo.setStatus(status);
        vo.setRetry(retry);
        vo.setLatencyMs(elapsed(startMs));
        vo.setChart(defaultChart());
        return vo;
    }

    private static BiChartVo defaultChart() {
        BiChartVo chart = new BiChartVo();
        chart.setType("table");
        return chart;
    }

    private static long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }

    private static void addStep(List<BiTraceStep> steps, String node, String outcome, String detail, long costMs) {
        BiTraceStep step = new BiTraceStep();
        step.setNode(node);
        step.setOutcome(outcome);
        step.setDetail(detail);
        step.setCostMs(costMs);
        steps.add(step);
    }

    private static boolean isRetryableReject(BiSqlRejectCode code) {
        if (code == null) {
            return false;
        }
        switch (code) {
            case REJECT_TABLE_NOT_ALLOWED:
            case REJECT_COLUMN_BLOCKED:
            case REJECT_STAR_SELECT:
                return true;
            default:
                return false;
        }
    }

    private static String sanitizeRejectReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return "无法安全回答该问题";
        }
        return reason.length() > 200 ? reason.substring(0, 200) : reason;
    }

    private static String abbreviate(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private static List<Map<String, Object>> sampleRows(List<Map<String, Object>> rows, int limit) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.size() <= limit ? rows : new ArrayList<>(rows.subList(0, limit));
    }

    private Long resolveUserId() {
        try {
            SysUser user = ShiroUtils.getUserInfo();
            return user != null ? user.getId() : 0L;
        } catch (Exception ex) {
            return 0L;
        }
    }

    private boolean hasTraceAllPermission() {
        try {
            return SecurityUtils.getSubject().isPermitted(BiChatPermissionConstants.TRACE_ALL);
        } catch (Exception ex) {
            return false;
        }
    }

    private static final class AskExecution {
        private final BiChatAskVo vo;
        private final BiChatResponseCode errorCode;

        private AskExecution(BiChatAskVo vo, BiChatResponseCode errorCode) {
            this.vo = vo;
            this.errorCode = errorCode;
        }

        static AskExecution success(BiChatAskVo vo) {
            return new AskExecution(vo, null);
        }

        static AskExecution error(BiChatResponseCode code, BiChatAskVo vo) {
            return new AskExecution(vo, code);
        }
    }
}
