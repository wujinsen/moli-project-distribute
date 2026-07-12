package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.dto.IngestGenerateResultVo;
import com.moli.knowledge.server.dto.IngestGenerateStartVo;
import com.moli.knowledge.server.service.IngestGenerateTaskService;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.support.IngestGenerateProgressSink;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class IngestGenerateTaskServiceImpl implements IngestGenerateTaskService {

    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";

    private final ConcurrentHashMap<String, TaskHolder> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> runningByJobId = new ConcurrentHashMap<>();

    @Resource
    private KbIngestService kbIngestService;

    @Resource
    private KbIngestProperties ingestProperties;

    @Resource(name = "ingestGenerateExecutor")
    private Executor ingestGenerateExecutor;

    private ScheduledExecutorService sweeper;

    @PostConstruct
    public void initSweeper() {
        sweeper = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ingest-generate-sweeper");
            t.setDaemon(true);
            return t;
        });
        sweeper.scheduleWithFixedDelay(this::sweepExpiredTasks, 60, 60, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdownSweeper() {
        if (sweeper != null) {
            sweeper.shutdownNow();
        }
    }

    @Override
    public IngestGenerateStartVo start(Long jobId, boolean resume, boolean useLlmGenerate) {
        if (!ingestProperties.getGenerate().isAsyncEnabled()) {
            throw new BaseException("异步 generate 未启用，请使用 POST .../generate");
        }
        String existing = runningByJobId.get(jobId);
        if (existing != null) {
            TaskHolder running = tasks.get(existing);
            if (running != null && STATUS_RUNNING.equals(running.status)) {
                throw new BaseException("该批次已有进行中的生成任务，taskId=" + existing);
            }
            runningByJobId.remove(jobId, existing);
        }

        int total = kbIngestService.countGeneratePages(jobId);
        String taskId = UUID.randomUUID().toString();
        TaskHolder holder = new TaskHolder(taskId, jobId, resume, total);
        tasks.put(taskId, holder);
        runningByJobId.put(jobId, taskId);

        Subject subject = null;
        try {
            subject = SecurityUtils.getSubject();
        } catch (Exception ignore) {
            // 单测或未绑定 SecurityManager 时直接异步执行
        }
        Runnable task = () -> runTask(holder, resume, useLlmGenerate);
        if (subject != null) {
            ingestGenerateExecutor.execute(subject.associateWith(task));
        } else {
            ingestGenerateExecutor.execute(task);
        }

        IngestGenerateStartVo vo = new IngestGenerateStartVo();
        vo.setTaskId(taskId);
        vo.setJobId(jobId);
        vo.setTotal(total);
        vo.setResume(resume);
        vo.setStatus(STATUS_RUNNING);
        return vo;
    }

    @Override
    public SseEmitter stream(Long jobId, String taskId) {
        if (StringUtils.isBlank(taskId)) {
            throw new BaseException("taskId 不能为空");
        }
        TaskHolder holder = tasks.get(taskId);
        if (holder == null) {
            throw new BaseException("生成任务不存在或已过期: " + taskId);
        }
        if (!holder.jobId.equals(jobId)) {
            throw new BaseException("taskId 与 jobId 不匹配");
        }

        long timeout = ingestProperties.getGenerate().getSseTimeoutMs();
        SseEmitter emitter = new SseEmitter(timeout);
        holder.emitters.add(emitter);

        emitter.onCompletion(() -> holder.emitters.remove(emitter));
        emitter.onTimeout(() -> holder.emitters.remove(emitter));
        emitter.onError(e -> holder.emitters.remove(emitter));

        try {
            if (holder.lastEventName != null && holder.lastEventData != null) {
                emitter.send(SseEmitter.event().name(holder.lastEventName).data(holder.lastEventData));
            }
            if (holder.progressSnapshot != null) {
                emitter.send(SseEmitter.event().name("progress").data(holder.progressSnapshot));
            }
            if (STATUS_COMPLETED.equals(holder.status) && holder.completePayload != null) {
                emitter.send(SseEmitter.event().name("complete").data(holder.completePayload));
                emitter.complete();
            } else if (STATUS_FAILED.equals(holder.status) && holder.errorMessage != null) {
                emitter.send(SseEmitter.event().name("error").data(errorPayload(holder.errorMessage)));
                emitter.complete();
            }
        } catch (IOException e) {
            holder.emitters.remove(emitter);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private void runTask(TaskHolder holder, boolean resume, boolean useLlmGenerate) {
        IngestGenerateProgressSink sink = new TaskProgressSink(holder);
        try {
            IngestGenerateResultVo result = kbIngestService.generateWithProgress(
                    holder.jobId, resume, useLlmGenerate, sink);
            holder.status = STATUS_COMPLETED;
            holder.completePayload = JSON.toJSONString(stripDraftsForSse(result));
            holder.finishedAtMs = System.currentTimeMillis();
            broadcast(holder, "complete", holder.completePayload);
            completeEmitters(holder);
        } catch (Exception e) {
            holder.status = STATUS_FAILED;
            holder.errorMessage = e.getMessage() == null ? "generate failed" : e.getMessage();
            holder.finishedAtMs = System.currentTimeMillis();
            log.warn("[ingest] async generate failed job={} task={}: {}", holder.jobId, holder.taskId, holder.errorMessage);
            broadcast(holder, "error", errorPayload(holder.errorMessage));
            completeEmitters(holder);
        } finally {
            runningByJobId.remove(holder.jobId, holder.taskId);
        }
    }

    private static Map<String, Object> stripDraftsForSse(IngestGenerateResultVo result) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", result.getTotal());
        map.put("generated", result.getGenerated());
        map.put("skipped", result.getSkipped());
        map.put("failed", result.getFailed());
        map.put("resume", result.isResume());
        map.put("templateMode", result.isTemplateMode());
        map.put("llmFallback", result.isLlmFallback());
        map.put("llmFallbackReason", result.getLlmFallbackReason());
        return map;
    }

    private static String errorPayload(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("message", message);
        return JSON.toJSONString(map);
    }

    private void broadcast(TaskHolder holder, String event, String dataJson) {
        holder.lastEventName = event;
        holder.lastEventData = dataJson;
        for (SseEmitter emitter : holder.emitters) {
            try {
                emitter.send(SseEmitter.event().name(event).data(dataJson));
            } catch (IOException e) {
                holder.emitters.remove(emitter);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignore) {
                    // ignore
                }
            }
        }
    }

    private void completeEmitters(TaskHolder holder) {
        for (SseEmitter emitter : new CopyOnWriteArrayList<>(holder.emitters)) {
            try {
                emitter.complete();
            } catch (Exception ignore) {
                // ignore
            }
            holder.emitters.remove(emitter);
        }
    }

    private void sweepExpiredTasks() {
        long ttl = ingestProperties.getGenerate().getTaskTtlMs();
        long now = System.currentTimeMillis();
        tasks.forEach((taskId, holder) -> {
            if (STATUS_RUNNING.equals(holder.status)) {
                return;
            }
            if (holder.finishedAtMs > 0 && now - holder.finishedAtMs > ttl) {
                tasks.remove(taskId);
            }
        });
    }

    private final class TaskProgressSink implements IngestGenerateProgressSink {

        private final TaskHolder holder;

        private TaskProgressSink(TaskHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onStarted(int total, boolean resume, boolean templateMode,
                              boolean llmFallback, String llmFallbackReason) {
            holder.templateMode = templateMode;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("taskId", holder.taskId);
            map.put("jobId", holder.jobId);
            map.put("total", total);
            map.put("resume", resume);
            map.put("templateMode", templateMode);
            map.put("llmFallback", llmFallback);
            map.put("llmFallbackReason", llmFallbackReason);
            String json = JSON.toJSONString(map);
            broadcast(holder, "started", json);
        }

        @Override
        public void onPageStart(int index, String slug, String action) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("index", index);
            map.put("slug", slug);
            map.put("action", action);
            broadcast(holder, "page_start", JSON.toJSONString(map));
        }

        @Override
        public void onPageDone(String slug, String outcome, String message) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("slug", slug);
            map.put("outcome", outcome);
            if (StringUtils.isNotBlank(message)) {
                map.put("message", message);
            }
            broadcast(holder, "page_done", JSON.toJSONString(map));
        }

        @Override
        public void onProgress(int generated, int skipped, int failed, int total) {
            int done = generated + skipped + failed;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("generated", generated);
            map.put("skipped", skipped);
            map.put("failed", failed);
            map.put("done", done);
            map.put("total", total);
            holder.progressSnapshot = JSON.toJSONString(map);
            broadcast(holder, "progress", holder.progressSnapshot);
        }

        @Override
        public void onComplete(IngestGenerateResultVo result) {
            // final complete sent by runTask after persist
        }

        @Override
        public void onError(String message) {
            broadcast(holder, "error", errorPayload(message));
        }
    }

    private static final class TaskHolder {
        private final String taskId;
        private final Long jobId;
        private final boolean resume;
        private final int total;
        private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

        private volatile String status = STATUS_RUNNING;
        private volatile boolean templateMode;
        private volatile String lastEventName;
        private volatile String lastEventData;
        private volatile String progressSnapshot;
        private volatile String completePayload;
        private volatile String errorMessage;
        private volatile long finishedAtMs;

        private TaskHolder(String taskId, Long jobId, boolean resume, int total) {
            this.taskId = taskId;
            this.jobId = jobId;
            this.resume = resume;
            this.total = total;
        }
    }
}
