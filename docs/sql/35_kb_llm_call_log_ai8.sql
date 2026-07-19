-- =============================================================
-- AI-8 · kb_llm_call_log 扩展（语义缓存 / failover / 成本估算）
-- 运行顺序：在 18_kb_llm_call_log.sql 之后执行
-- 设计：docs/design/contracts/AI-8-contract.md §1.4
-- =============================================================
SET NAMES utf8mb4;

ALTER TABLE `kb_llm_call_log`
  ADD COLUMN `cache_hit` tinyint(1) NOT NULL DEFAULT 0 COMMENT '语义缓存命中' AFTER `latency_ms`,
  ADD COLUMN `failover` tinyint(1) NOT NULL DEFAULT 0 COMMENT '经 fallback 成功' AFTER `cache_hit`,
  ADD COLUMN `prompt_tokens_est` int DEFAULT NULL COMMENT '估算 prompt tokens' AFTER `failover`,
  ADD COLUMN `completion_tokens_est` int DEFAULT NULL COMMENT '估算 completion tokens' AFTER `prompt_tokens_est`,
  ADD COLUMN `estimated_cost_usd` decimal(12,6) DEFAULT NULL COMMENT '估算成本 USD；缓存命中为 0' AFTER `completion_tokens_est`;
