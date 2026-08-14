package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.IngestGenerateResultVo;

/**
 * T15f · Ingest 多页生成进度回调（同步 generate 传 null；SSE 任务注册实现）。
 */
public interface IngestGenerateProgressSink {

    void onStarted(int total, boolean resume, boolean templateMode, boolean llmFallback, String llmFallbackReason);

    void onPageStart(int index, String slug, String action);

    void onPageDone(String slug, String outcome, String message);

    void onProgress(int generated, int skipped, int failed, int total);

    void onComplete(IngestGenerateResultVo result);

    void onError(String message);
}
