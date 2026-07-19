package com.moli.ai.server.bi.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.ai.server.bi.dto.BiChartVo;
import com.moli.ai.server.bi.dto.BiChatAskVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 契约 §1.1 SSE：stage / sql / chart / token / done / error。
 */
@Slf4j
public class BiChatSseProgressSink implements BiChatProgressSink {

    private final SseEmitter emitter;
    private final ObjectMapper objectMapper;

    public BiChatSseProgressSink(SseEmitter emitter, ObjectMapper objectMapper) {
        this.emitter = emitter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void stage(String stage, String traceId) {
        Map<String, Object> data = new HashMap<>(2);
        data.put("stage", stage);
        data.put("traceId", traceId);
        send("stage", data);
    }

    @Override
    public void sql(String sql) {
        Map<String, Object> data = new HashMap<>(1);
        data.put("sql", sql);
        send("sql", data);
    }

    @Override
    public void chart(BiChartVo chart) {
        send("chart", chart == null ? defaultChart() : chart);
    }

    @Override
    public void token(String delta) {
        Map<String, Object> data = new HashMap<>(1);
        data.put("delta", delta);
        send("token", data);
    }

    @Override
    public void done(BiChatAskVo vo) {
        send("done", vo);
    }

    @Override
    public void error(int code, String message) {
        Map<String, Object> data = new HashMap<>(2);
        data.put("code", code);
        data.put("message", message);
        send("error", data);
    }

    private void send(String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(objectMapper.writeValueAsString(data)));
        } catch (IOException ex) {
            log.debug("SSE send {} failed: {}", event, ex.getMessage());
        }
    }

    private static BiChartVo defaultChart() {
        BiChartVo chart = new BiChartVo();
        chart.setType("table");
        return chart;
    }
}
