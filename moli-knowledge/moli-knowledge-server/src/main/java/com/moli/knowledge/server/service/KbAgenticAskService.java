package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.AgenticAskRequest;
import com.moli.knowledge.server.dto.AgenticAskVo;

public interface KbAgenticAskService {

    AgenticAskVo agenticAsk(AgenticAskRequest request);
}
