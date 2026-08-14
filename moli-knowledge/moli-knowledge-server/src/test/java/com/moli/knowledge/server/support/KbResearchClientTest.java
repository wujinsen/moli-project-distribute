package com.moli.knowledge.server.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moli.knowledge.server.config.KbResearchProperties;
import com.moli.knowledge.server.dto.ResearchRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KbResearchClientTest {

    private HttpServer server;
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private String baseUrl;

    @Before
    public void setUp() throws IOException {
        lastBody.set(null);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/research", new CaptureHandler());
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void runResearch_forwardsAuthAndSpaceIds() {
        KbResearchProperties props = new KbResearchProperties();
        props.setSidecarBaseUrl(baseUrl);
        props.setSidecarTimeoutMs(5000);
        KbResearchClient client = new KbResearchClient(props);

        ResearchRequest req = new ResearchRequest();
        req.setTopic("茉莉微服务架构");
        req.setRetrievalStrategy("hybrid");
        List<Long> spaces = Collections.singletonList(900000000000000003L);

        JSONObject resp = client.runResearch("run-1", req, "Bearer session-token", spaces);
        assertNotNull(resp);
        assertEquals("SUCCEEDED", resp.getString("status"));

        JSONObject body = JSON.parseObject(lastBody.get());
        assertEquals("run-1", body.getString("runId"));
        assertEquals("Bearer session-token", body.getString("authToken"));
        assertEquals(Long.valueOf(900000000000000003L), body.getLong("spaceId"));
        JSONObject options = body.getJSONObject("options");
        assertNotNull(options);
        assertEquals("hybrid", options.getString("retrievalStrategy"));
        assertTrue(options.getIntValue("maxSections") <= 10);
    }

    private class CaptureHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = "{\"runId\":\"run-1\",\"status\":\"SUCCEEDED\",\"topic\":\"t\",\"outline\":{\"sections\":[]},\"sectionEvidence\":[],\"citations\":[],\"progress\":[],\"latencyMs\":1}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
