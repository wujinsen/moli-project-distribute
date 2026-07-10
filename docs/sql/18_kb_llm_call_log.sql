-- =============================================================
-- KBOPS-9 · LLM 调用审计（Dashboard 调用率 / 失败率）
-- 运行顺序：在 03_knowledge_schema.sql、17_kb_lint_ops_enhance.sql 之后
-- =============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `kb_llm_call_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `space_id` bigint DEFAULT NULL COMMENT '空间ID（Ask/Wiki/Ingest 等；连通性测试可为空）',
  `user_id` bigint DEFAULT NULL COMMENT '触发用户ID',
  `scene` varchar(32) NOT NULL COMMENT '场景 ask/ingest_plan/ingest_generate/ingest_enrich/wiki_revise/wiki_enrich/llm_test',
  `provider` varchar(32) DEFAULT NULL COMMENT 'LLM 提供方',
  `model` varchar(64) DEFAULT NULL COMMENT '模型名',
  `status` varchar(16) NOT NULL DEFAULT 'success' COMMENT 'success/fail',
  `latency_ms` int DEFAULT NULL COMMENT '耗时毫秒',
  `error_message` varchar(512) DEFAULT NULL COMMENT '失败摘要',
  `create_time` datetime DEFAULT NULL COMMENT '调用时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_llm_call_time` (`create_time`),
  KEY `idx_kb_llm_call_space` (`space_id`),
  KEY `idx_kb_llm_call_scene` (`scene`),
  KEY `idx_kb_llm_call_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库 LLM 调用审计';
