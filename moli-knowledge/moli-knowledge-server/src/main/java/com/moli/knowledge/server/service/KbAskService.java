package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;

public interface KbAskService {

    /**
     * 知识库问答：定作用域 → 选页 → （有 key）调 LLM 带引用作答 / （无 key）检索式答案，并记 kb_qa_log。
     */
    AskResponse ask(AskRequest request);
}
