package com.moli.ai.server.bi.service;

import com.moli.ai.server.bi.dto.BiChatAskRequest;
import com.moli.ai.server.bi.dto.BiChatAskVo;
import com.moli.ai.server.bi.dto.BiChatTraceVo;
import com.moli.common.core.MoliResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BiChatService {

    MoliResult<BiChatAskVo> ask(BiChatAskRequest request);

    SseEmitter askStream(BiChatAskRequest request);

    MoliResult<BiChatTraceVo> getTrace(String traceId);
}
