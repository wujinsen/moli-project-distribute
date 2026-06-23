package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbLlmConfigVo;

public interface KbLlmConfigService {

    /** 后端 LLM 能力探测（不含 api-key；是否调用由 /kb/ask 的 useLlm 决定）。 */
    KbLlmConfigVo getConfig();
}
