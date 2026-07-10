package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.dto.KbOpsLlmCallTrendPointVo;
import com.moli.knowledge.server.entity.KbLlmCallLog;
import com.moli.knowledge.server.mapper.KbLlmCallLogMapper;
import com.moli.knowledge.server.service.KbLlmCallLogService;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class KbLlmCallLogServiceImpl implements KbLlmCallLogService {

    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAIL = "fail";

    @Resource
    private KbLlmCallLogMapper kbLlmCallLogMapper;
    @Resource
    private KbLlmProperties kbLlmProperties;

    @Override
    public void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs) {
        insert(scene, spaceId, provider, model, STATUS_SUCCESS, latencyMs, null);
    }

    @Override
    public void recordFail(String scene, Long spaceId, String provider, String model, long latencyMs,
                           String errorMessage) {
        insert(scene, spaceId, provider, model, STATUS_FAIL, latencyMs, errorMessage);
    }

    @Override
    public LlmCallStats aggregate(List<Long> scopeSpaceIds, boolean includeGlobal, int days) {
        LlmCallStats stats = new LlmCallStats();
        int normalizedDays = days <= 0 ? 7 : Math.min(days, 30);
        LocalDate start = LocalDate.now().minusDays(normalizedDays - 1L);
        Date from = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<KbLlmCallLog> wrapper = new LambdaQueryWrapper<KbLlmCallLog>()
                .ge(KbLlmCallLog::getCreateTime, from);
        if (scopeSpaceIds != null && !scopeSpaceIds.isEmpty()) {
            if (includeGlobal) {
                wrapper.and(w -> w.in(KbLlmCallLog::getSpaceId, scopeSpaceIds)
                        .or().isNull(KbLlmCallLog::getSpaceId));
            } else {
                wrapper.in(KbLlmCallLog::getSpaceId, scopeSpaceIds);
            }
        }
        List<KbLlmCallLog> rows = kbLlmCallLogMapper.selectList(wrapper);

        Map<String, int[]> dayStats = new TreeMap<>();
        Map<String, Long> byScene = new LinkedHashMap<>();
        long success = 0;
        long fail = 0;
        for (KbLlmCallLog row : rows) {
            if (STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                success++;
            } else {
                fail++;
            }
            String scene = row.getScene() == null ? "unknown" : row.getScene();
            byScene.merge(scene, 1L, Long::sum);
            if (row.getCreateTime() != null) {
                String dayKey = row.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                int[] counts = dayStats.computeIfAbsent(dayKey, k -> new int[2]);
                if (STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                    counts[0]++;
                } else {
                    counts[1]++;
                }
            }
        }

        stats.setTotalCalls(success + fail);
        stats.setSuccessCalls(success);
        stats.setFailCalls(fail);
        stats.setCallsByScene(byScene);

        List<KbOpsLlmCallTrendPointVo> trend = new ArrayList<>();
        LocalDate cursor = start;
        LocalDate end = LocalDate.now();
        while (!cursor.isAfter(end)) {
            String key = cursor.toString();
            int[] counts = dayStats.getOrDefault(key, new int[2]);
            KbOpsLlmCallTrendPointVo point = new KbOpsLlmCallTrendPointVo();
            point.setDate(key);
            point.setSuccessCalls(counts[0]);
            point.setFailCalls(counts[1]);
            trend.add(point);
            cursor = cursor.plusDays(1);
        }
        stats.setCallTrend(trend);
        return stats;
    }

    private void insert(String scene, Long spaceId, String provider, String model, String status,
                        long latencyMs, String errorMessage) {
        if (!kbLlmProperties.isCallLogEnabled()) {
            return;
        }
        try {
            Date now = new Date();
            KbLlmCallLog row = new KbLlmCallLog();
            row.setId(IdGenerator.getId());
            row.setSpaceId(spaceId);
            row.setUserId(ShiroUtils.getUserId());
            row.setScene(StringUtils.defaultIfBlank(scene, "unknown"));
            row.setProvider(provider);
            row.setModel(model);
            row.setStatus(status);
            row.setLatencyMs((int) Math.min(Integer.MAX_VALUE, latencyMs));
            if (StringUtils.isNotBlank(errorMessage)) {
                row.setErrorMessage(errorMessage.substring(0, Math.min(512, errorMessage.length())));
            }
            row.setCreateTime(now);
            kbLlmCallLogMapper.insert(row);
        } catch (Exception e) {
            log.warn("kb_llm_call_log 写入失败 scene={}: {}", scene, e.getMessage());
        }
    }
}
