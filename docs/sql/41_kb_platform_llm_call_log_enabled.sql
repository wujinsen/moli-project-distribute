-- =============================================================
-- T19+ · 平台 LLM 配置：调用日志开关（Web 可配置，运维看板 D6）
-- 运行顺序：在 11_kb_platform_llm_config.sql 之后
-- =============================================================
SET NAMES utf8mb4;

ALTER TABLE `kb_platform_llm_config`
  ADD COLUMN `call_log_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '1记录LLM调用日志(成本统计) 0关闭' AFTER `enabled`;
