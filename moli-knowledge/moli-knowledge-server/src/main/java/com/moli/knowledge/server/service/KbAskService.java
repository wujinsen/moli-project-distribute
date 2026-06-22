package com.moli.knowledge.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.dto.QaHistoryVo;

public interface KbAskService {

    /**
     * 知识库问答：定作用域 → 选页 → （有 key）调 LLM 带引用作答 / （无 key）检索式答案，并记 kb_qa_log。
     */
    AskResponse ask(AskRequest request);

    /** 当前用户的问答历史（按可读空间过滤）。 */
    Page<QaHistoryVo> history(Long spaceId, int pageNum, int pageSize);

    /** 对某次问答提交反馈（1有用/0无用）。 */
    void feedback(Long id, Integer useful);
}
