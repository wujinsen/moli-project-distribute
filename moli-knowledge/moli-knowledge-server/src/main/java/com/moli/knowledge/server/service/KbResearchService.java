package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.ResearchRequest;
import com.moli.knowledge.server.dto.ResearchStartVo;
import com.moli.knowledge.server.dto.ResearchVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface KbResearchService {

    ResearchStartVo start(ResearchRequest request, String authToken);

    ResearchVo getRun(String runId);

    SseEmitter stream(String runId);
}
