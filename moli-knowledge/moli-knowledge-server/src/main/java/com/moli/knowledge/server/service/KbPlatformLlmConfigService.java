package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbPlatformLlmConfigTestRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigTestResultVo;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigUpdateRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigVo;
import com.moli.knowledge.server.entity.KbPlatformLlmConfig;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;

/**
 * 平台 LLM 配置持久化与生效解析（T19）。
 */
public interface KbPlatformLlmConfigService {

    /** 确保 id=1 占位行存在（无 api-key）。 */
    void ensureSingletonRow();

    /** 解析当前应生效的配置：DB 优先，yaml 兜底。 */
    KbLlmEffectiveConfig resolveEffective();

    /** 读取单例行（可能为 null）。 */
    KbPlatformLlmConfig getSingletonRow();

    /** 管理视图（不含明文 key）。 */
    KbPlatformLlmConfigVo getAdminView();

    /** 保存平台 LLM 配置并刷新 Runtime。 */
    KbPlatformLlmConfigVo save(KbPlatformLlmConfigUpdateRequest request);

    /** 测试 LLM 连通性（可用未保存的表单值）。 */
    KbPlatformLlmConfigTestResultVo testConnection(KbPlatformLlmConfigTestRequest request);

    /** DB 优先、yaml 兜底的调用日志开关（运维看板 D6 / kb_llm_call_log 写入）。 */
    boolean isCallLogEnabled();
}
