package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbGuardrailsProperties;
import com.moli.knowledge.server.config.KbResearchProperties;
import com.moli.knowledge.server.dto.ResearchRequest;
import com.moli.knowledge.server.dto.ResearchStartVo;
import com.moli.knowledge.server.dto.ResearchVo;
import com.moli.knowledge.server.entity.KbResearchRun;
import com.moli.knowledge.server.guard.InputGuardOutcome;
import com.moli.knowledge.server.guard.KbInputGuardService;
import com.moli.knowledge.server.mapper.KbResearchRunMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbResearchService;
import com.moli.knowledge.server.service.KbResearchWritebackService;
import com.moli.knowledge.server.service.ingest.IngestPlanPathResolver;
import com.moli.knowledge.server.support.KbResearchClient;
import com.moli.knowledge.server.support.KbResearchSidecarException;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KbResearchServiceImpl implements KbResearchService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_DEGRADED = "DEGRADED";

    private final ConcurrentHashMap<String, TaskHolder> tasks = new ConcurrentHashMap<>();

    @Resource
    private KbResearchProperties researchProperties;
    @Resource
    private KbResearchClient researchClient;
    @Resource
    private KbResearchRunMapper researchRunMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbInputGuardService inputGuardService;
    @Resource
    private KbGuardrailsProperties guardrailsProperties;
    @Resource
    private KbResearchWritebackService researchWritebackService;

    private ScheduledExecutorService sweeper;
    private Executor researchExecutor;

    @PostConstruct
    public void initSweeper() {
        researchExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "kb-research-worker");
            t.setDaemon(true);
            return t;
        });
        sweeper = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "kb-research-sweeper");
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
    public ResearchStartVo start(ResearchRequest request, String authToken) {
        ensureEnabled();
        if (StringUtils.isBlank(request.getTopic())) {
            throw new BaseException("topic 不能为空");
        }
        List<Long> scopeSpaces = kbAclService.resolveReadableSpaceIds(
                request.getSpaceId(), request.getSpaceIds());
        if (scopeSpaces.isEmpty()) {
            throw new BaseException("无可访问的知识空间");
        }

        String runId = UUID.randomUUID().toString();
        KbResearchRun row = new KbResearchRun();
        row.setId(IdGenerator.getId());
        row.setRunId(runId);
        row.setUserId(ShiroUtils.getUserId());
        row.setSpaceIdsJson(JSON.toJSONString(scopeSpaces));
        row.setTopic(request.getTopic().trim());
        row.setStatus(STATUS_PENDING);
        row.setDegraded(false);
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());
        researchRunMapper.insert(row);

        TaskHolder holder = new TaskHolder(runId, row.getId(), request, authToken, scopeSpaces);
        tasks.put(runId, holder);

        Subject subject = null;
        try {
            subject = SecurityUtils.getSubject();
        } catch (Exception ignore) {
            // tests
        }
        Runnable task = () -> runTask(holder);
        if (subject != null) {
            researchExecutor.execute(subject.associateWith(task));
        } else {
            researchExecutor.execute(task);
        }

        ResearchStartVo vo = new ResearchStartVo();
        vo.setRunId(runId);
        vo.setStatus(STATUS_RUNNING);
        return vo;
    }

    @Override
    public ResearchVo getRun(String runId) {
        TaskHolder holder = tasks.get(runId);
        if (holder != null && holder.resultVo != null) {
            return holder.resultVo;
        }
        KbResearchRun row = loadRun(runId);
        if (row == null) {
            throw new BaseException("调研任务不存在: " + runId);
        }
        return toVo(row);
    }

    @Override
    public SseEmitter stream(String runId) {
        TaskHolder holder = tasks.get(runId);
        if (holder == null) {
            throw new BaseException("调研任务不存在或已过期: " + runId);
        }
        long timeout = researchProperties.getSseTimeoutMs() > 0
                ? researchProperties.getSseTimeoutMs() : 120_000L;
        SseEmitter emitter = new SseEmitter(timeout);
        holder.emitters.add(emitter);
        emitter.onCompletion(() -> holder.emitters.remove(emitter));
        emitter.onTimeout(() -> holder.emitters.remove(emitter));
        emitter.onError(e -> holder.emitters.remove(emitter));

        try {
            for (Map<String, Object> evt : holder.progressEvents) {
                emitter.send(SseEmitter.event().name("progress").data(JSON.toJSONString(evt)));
            }
            if (holder.resultVo != null) {
                emitter.send(SseEmitter.event().name("complete").data(JSON.toJSONString(holder.resultVo)));
                emitter.complete();
            } else if (holder.errorMessage != null) {
                emitter.send(SseEmitter.event().name("error").data(errorPayload(holder.errorMessage)));
                emitter.complete();
            }
        } catch (IOException e) {
            holder.emitters.remove(emitter);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private void runTask(TaskHolder holder) {
        holder.status = STATUS_RUNNING;
        updateRunStatus(holder.dbId, STATUS_RUNNING, null, null);

        if (researchProperties.isGuardrails() && guardrailsProperties.isEnabled()) {
            InputGuardOutcome guard = inputGuardService.process(holder.request.getTopic());
            if (guard.isBlocked() || guard.isPiiOnlyReject()) {
                finalizeGuardBlocked(holder, guard);
                return;
            }
            if (guard.isPiiRedacted() || guard.isFlagged()) {
                holder.request.setTopic(guard.getQuestionForProcessing());
            }
        }

        emitProgress(holder, "planner", null, "启动 Planner", 5);

        try {
            JSONObject sidecar = researchClient.runResearch(
                    holder.runId, holder.request, holder.authToken, holder.scopeSpaces);
            replaySidecarProgress(holder, sidecar);
            ResearchVo vo = mapSidecarResult(holder, sidecar);

            if (Boolean.TRUE.equals(holder.request.getWriteback()) && StringUtils.isNotBlank(vo.getReportMd())) {
                emitProgress(holder, "writeback", null, "Ingest 回写 outputs/", 96);
                try {
                    KbResearchWritebackService.WritebackResult wb = researchWritebackService.writeback(
                            vo, holder.request.getTopic(), researchProperties.getWritebackSpaceId());
                    vo.setIngestJobId(wb.ingestJobId);
                    vo.setOutputPath(wb.outputPath);
                } catch (Exception wbEx) {
                    log.warn("[research] writeback failed run={}: {}", holder.runId, wbEx.getMessage());
                    vo.setDegraded(true);
                    vo.setDegradeReason("WRITEBACK");
                }
            }

            holder.resultVo = vo;
            holder.status = vo.getStatus();
            persistResult(holder.dbId, sidecar, vo);
            broadcastComplete(holder, vo);
        } catch (Exception e) {
            holder.status = STATUS_FAILED;
            holder.errorMessage = e.getMessage() == null ? "research failed" : e.getMessage();
            updateRunStatus(holder.dbId, STATUS_FAILED, false, null);
            log.warn("[research] run {} failed: {}", holder.runId, holder.errorMessage);
            broadcastError(holder, holder.errorMessage);
        } finally {
            holder.finishedAtMs = System.currentTimeMillis();
        }
    }

    private void finalizeGuardBlocked(TaskHolder holder, InputGuardOutcome guard) {
        ResearchVo vo = new ResearchVo();
        vo.setRunId(holder.runId);
        vo.setStatus(STATUS_FAILED);
        vo.setTopic(holder.request.getTopic());
        vo.setDegraded(true);
        vo.setDegradeReason("GUARD_BLOCK");
        vo.setGuard(guard.toVo());
        holder.resultVo = vo;
        holder.status = STATUS_FAILED;
        holder.errorMessage = "输入 Guard 拦截";
        updateRunStatus(holder.dbId, STATUS_FAILED, true, "GUARD_BLOCK");
        broadcastError(holder, holder.errorMessage);
        holder.finishedAtMs = System.currentTimeMillis();
    }

    private void replaySidecarProgress(TaskHolder holder, JSONObject sidecar) {
        JSONArray progress = sidecar.getJSONArray("progress");
        if (progress == null) {
            return;
        }
        for (int i = 0; i < progress.size(); i++) {
            JSONObject evt = progress.getJSONObject(i);
            if (evt == null) {
                continue;
            }
            emitProgress(holder,
                    evt.getString("phase"),
                    evt.getString("sectionId"),
                    evt.getString("message"),
                    evt.getIntValue("pct"));
        }
    }

    private ResearchVo mapSidecarResult(TaskHolder holder, JSONObject sidecar) {
        ResearchVo vo = new ResearchVo();
        vo.setRunId(holder.runId);
        vo.setStatus(sidecar.getString("status"));
        vo.setTopic(holder.request.getTopic());
        vo.setTitle(sidecar.getString("title"));
        vo.setSlug(StringUtils.isNotBlank(holder.request.getSlug())
                ? holder.request.getSlug()
                : sidecar.getString("slug"));
        JSONObject outline = sidecar.getJSONObject("outline");
        if (outline != null) {
            vo.setOutline(outline.getInnerMap());
        }
        vo.setDegraded(sidecar.getBooleanValue("degraded"));
        vo.setDegradeReason(sidecar.getString("degradeReason"));
        vo.setLatencyMs(sidecar.getLongValue("latencyMs"));
        vo.setReportMd(sidecar.getString("reportMd"));
        if (sidecar.containsKey("coverage") && sidecar.get("coverage") != null) {
            vo.setCoverage(sidecar.getDouble("coverage"));
        }
        JSONArray unsupported = sidecar.getJSONArray("unsupportedStatements");
        if (unsupported != null) {
            vo.setUnsupportedStatements(unsupported.toJavaList(String.class));
        }

        JSONArray sectionEvidence = sidecar.getJSONArray("sectionEvidence");
        if (sectionEvidence != null) {
            vo.setSectionEvidence(JSON.parseObject(sectionEvidence.toJSONString(), List.class));
        }
        JSONArray citations = sidecar.getJSONArray("citations");
        if (citations != null) {
            List<ResearchVo.ResearchCitationVo> list = new ArrayList<>();
            for (int i = 0; i < citations.size(); i++) {
                JSONObject c = citations.getJSONObject(i);
                if (c == null) {
                    continue;
                }
                ResearchVo.ResearchCitationVo item = new ResearchVo.ResearchCitationVo();
                item.setSlug(c.getString("slug"));
                item.setTitle(c.getString("title"));
                JSONArray secIds = c.getJSONArray("sectionIds");
                if (secIds != null) {
                    item.setSectionIds(secIds.toJavaList(String.class));
                }
                list.add(item);
            }
            vo.setCitations(list);
        }
        if (StringUtils.isBlank(vo.getStatus())) {
            vo.setStatus(STATUS_SUCCEEDED);
        }
        return vo;
    }

    private void persistResult(Long dbId, JSONObject sidecar, ResearchVo vo) {
        KbResearchRun row = researchRunMapper.selectById(dbId);
        if (row == null) {
            return;
        }
        row.setStatus(vo.getStatus());
        row.setDegraded(vo.isDegraded());
        row.setDegradeReason(vo.getDegradeReason());
        row.setLatencyMs(vo.getLatencyMs());
        row.setCoverage(vo.getCoverage());
        row.setReportMd(vo.getReportMd());
        row.setIngestJobId(vo.getIngestJobId());
        JSONObject outline = sidecar.getJSONObject("outline");
        if (outline != null) {
            row.setOutlineJson(outline.toJSONString());
        }
        JSONArray sectionEvidence = sidecar.getJSONArray("sectionEvidence");
        if (sectionEvidence != null) {
            row.setSectionsJson(sectionEvidence.toJSONString());
        }
        JSONArray citations = sidecar.getJSONArray("citations");
        if (citations != null) {
            row.setCitationsJson(citations.toJSONString());
        }
        row.setUpdateTime(new Date());
        researchRunMapper.updateById(row);
    }

    private void updateRunStatus(Long dbId, String status, Boolean degraded, String reason) {
        KbResearchRun row = researchRunMapper.selectById(dbId);
        if (row == null) {
            return;
        }
        row.setStatus(status);
        if (degraded != null) {
            row.setDegraded(degraded);
        }
        if (reason != null) {
            row.setDegradeReason(reason);
        }
        row.setUpdateTime(new Date());
        researchRunMapper.updateById(row);
    }

    private KbResearchRun loadRun(String runId) {
        return researchRunMapper.selectOne(new LambdaQueryWrapper<KbResearchRun>()
                .eq(KbResearchRun::getRunId, runId)
                .last("limit 1"));
    }

    private ResearchVo toVo(KbResearchRun row) {
        ResearchVo vo = new ResearchVo();
        vo.setRunId(row.getRunId());
        vo.setStatus(row.getStatus());
        vo.setTopic(row.getTopic());
        vo.setDegraded(Boolean.TRUE.equals(row.getDegraded()));
        vo.setDegradeReason(row.getDegradeReason());
        vo.setLatencyMs(row.getLatencyMs() == null ? 0L : row.getLatencyMs());
        vo.setCoverage(row.getCoverage());
        vo.setReportMd(row.getReportMd());
        vo.setIngestJobId(row.getIngestJobId());
        if (row.getIngestJobId() != null && StringUtils.isNotBlank(row.getReportMd())) {
            String bareSlug = extractSlugFromReportMd(row.getReportMd());
            if (StringUtils.isNotBlank(bareSlug)) {
                vo.setSlug(bareSlug);
                vo.setOutputPath("wiki-moli/develop/outputs/" + bareSlug + ".md");
            }
        }
        if (StringUtils.isNotBlank(row.getOutlineJson())) {
            vo.setOutline(JSON.parseObject(row.getOutlineJson()));
        }
        if (StringUtils.isNotBlank(row.getSectionsJson())) {
            vo.setSectionEvidence(JSON.parseObject(row.getSectionsJson(), List.class));
        }
        if (StringUtils.isNotBlank(row.getCitationsJson())) {
            JSONArray arr = JSON.parseArray(row.getCitationsJson());
            List<ResearchVo.ResearchCitationVo> citations = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                JSONObject c = arr.getJSONObject(i);
                ResearchVo.ResearchCitationVo item = new ResearchVo.ResearchCitationVo();
                item.setSlug(c.getString("slug"));
                item.setTitle(c.getString("title"));
                JSONArray secIds = c.getJSONArray("sectionIds");
                if (secIds != null) {
                    item.setSectionIds(secIds.toJavaList(String.class));
                }
                citations.add(item);
            }
            vo.setCitations(citations);
        }
        return vo;
    }

    private void emitProgress(TaskHolder holder, String phase, String sectionId, String message, int pct) {
        Map<String, Object> evt = new LinkedHashMap<>();
        evt.put("phase", phase);
        if (sectionId != null) {
            evt.put("sectionId", sectionId);
        }
        evt.put("message", message);
        evt.put("pct", pct);
        holder.progressEvents.add(evt);
        broadcast(holder, "progress", JSON.toJSONString(evt));
    }

    private void broadcastComplete(TaskHolder holder, ResearchVo vo) {
        broadcast(holder, "complete", JSON.toJSONString(vo));
        completeEmitters(holder);
    }

    private void broadcastError(TaskHolder holder, String message) {
        broadcast(holder, "error", errorPayload(message));
        completeEmitters(holder);
    }

    private static String errorPayload(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", "RESEARCH_FAILED");
        map.put("message", message);
        return JSON.toJSONString(map);
    }

    private void broadcast(TaskHolder holder, String eventName, String data) {
        holder.lastEventName = eventName;
        holder.lastEventData = data;
        for (SseEmitter emitter : holder.emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                holder.emitters.remove(emitter);
            }
        }
    }

    private void completeEmitters(TaskHolder holder) {
        for (SseEmitter emitter : holder.emitters) {
            try {
                emitter.complete();
            } catch (Exception ignore) {
                // ignore
            }
        }
        holder.emitters.clear();
    }

    private void sweepExpiredTasks() {
        long ttlMs = researchProperties.getSseTimeoutMs() > 0
                ? researchProperties.getSseTimeoutMs() * 2 : 240_000L;
        long now = System.currentTimeMillis();
        tasks.entrySet().removeIf(entry -> {
            TaskHolder h = entry.getValue();
            return h.finishedAtMs > 0 && now - h.finishedAtMs > ttlMs;
        });
    }

    private void ensureEnabled() {
        if (!researchProperties.isEnabled()) {
            throw new BaseException("DeepResearch 未启用（kb.research.enabled=false）");
        }
        if (!researchProperties.configured()) {
            throw new KbResearchSidecarException("kb.research.sidecar-base-url 未配置");
        }
    }

    private static String extractSlugFromReportMd(String reportMd) {
        if (StringUtils.isBlank(reportMd)) {
            return null;
        }
        for (String line : reportMd.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("slug:")) {
                return IngestPlanPathResolver.sanitizeBareSlug(trimmed.substring(5).trim());
            }
        }
        return null;
    }

    static final class TaskHolder {
        final String runId;
        final Long dbId;
        final ResearchRequest request;
        final String authToken;
        final List<Long> scopeSpaces;
        final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
        final List<Map<String, Object>> progressEvents = new ArrayList<>();
        String status = STATUS_PENDING;
        String lastEventName;
        String lastEventData;
        ResearchVo resultVo;
        String errorMessage;
        long finishedAtMs;

        TaskHolder(String runId, Long dbId, ResearchRequest request, String authToken, List<Long> scopeSpaces) {
            this.runId = runId;
            this.dbId = dbId;
            this.request = request;
            this.authToken = authToken;
            this.scopeSpaces = scopeSpaces;
        }
    }
}
