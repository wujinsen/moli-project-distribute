package com.moli.knowledge.server.llm;

/**
 * 当前生效 LLM 配置来源（T19）。
 */
public enum KbLlmConfigSource {
    /** MySQL kb_platform_llm_config 解密成功。 */
    DATABASE,
    /** DB 无 key 或解密失败时回退 application/Nacos kb.llm.*。 */
    YAML_FALLBACK
}
