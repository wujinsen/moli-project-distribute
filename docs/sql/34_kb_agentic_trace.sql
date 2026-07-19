-- =============================================================
-- AI-7 W12 · Agentic RAG 编排 trace kb_agentic_trace
-- 运行顺序：在 03_knowledge_schema.sql（kb_qa_log）之后
-- 设计：docs/design/contracts/AI-7-contract.md §4.3
-- =============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `kb_agentic_trace` (
  `id` bigint NOT NULL COMMENT '主键',
  `qa_log_id` bigint DEFAULT NULL COMMENT '关联 kb_qa_log.id',
  `space_id` bigint DEFAULT NULL COMMENT '空间ID（单空间时）',
  `user_id` bigint DEFAULT NULL COMMENT '提问用户ID',
  `question` varchar(1024) NOT NULL COMMENT '原始问题',
  `rewritten` varchar(1024) DEFAULT NULL COMMENT '改写后主问',
  `sub_questions_json` json DEFAULT NULL COMMENT '拆解子问题 JSON 数组',
  `rounds` int NOT NULL DEFAULT 1 COMMENT '实际编排轮次',
  `steps_json` json DEFAULT NULL COMMENT '每轮 queries/slugs/coverage/unsupported/latencyMs',
  `coverage` decimal(5,4) DEFAULT NULL COMMENT '末轮自检 coverage',
  `degraded` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否降级',
  `latency_ms` bigint DEFAULT NULL COMMENT '总耗时毫秒',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_agentic_trace_qa` (`qa_log_id`),
  KEY `idx_kb_agentic_trace_user` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agentic RAG 编排 trace';
