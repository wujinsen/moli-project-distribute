package com.moli.knowledge.server.exception;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.IngestRawConflictVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 知识库模块专用异常：在通用 {@link com.moli.common.exception.GlobalExceptionHandler} 之前
 * 为特定业务异常附带结构化 {@code data}。
 */
@RestControllerAdvice
@Slf4j
public class KbKnowledgeExceptionHandler {

    @ExceptionHandler(IngestRawConflictException.class)
    public MoliResult<IngestRawConflictVo> ingestRawConflictHandler(
            HttpServletRequest req, IngestRawConflictException e) {
        log.warn("Ingest raw coverage conflict: {} {}", req.getRequestURI(), e.getErrorMsg());
        return MoliResult.error(e.getErrorCode(), e.getDetail(), e.getErrorMsg());
    }
}
