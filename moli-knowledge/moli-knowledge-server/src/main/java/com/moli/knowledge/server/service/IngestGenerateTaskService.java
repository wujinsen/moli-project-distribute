package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.IngestGenerateStartVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * T15f · Ingest 多页生成异步任务与 SSE 进度流。
 */
public interface IngestGenerateTaskService {

    IngestGenerateStartVo start(Long jobId, boolean resume, boolean useLlmGenerate);

    SseEmitter stream(Long jobId, String taskId);
}
