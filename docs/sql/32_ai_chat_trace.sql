-- =============================================================
-- AI-4 W5 · ChatBI 审计表 ai_chat_trace + 只读查询账号
-- 运行顺序：在 03_knowledge_schema.sql、31_kb_eval_run.sql 之后
-- 设计：docs/design/bi-chatbi-nl2sql-contract.md §3 · §5.1 bi-chatbi-nl2sql.md
-- =============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_chat_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_id` varchar(64) NOT NULL COMMENT '对外 traceId',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话',
  `user_id` bigint DEFAULT NULL COMMENT '提问者',
  `question` varchar(512) NOT NULL COMMENT '自然语言',
  `final_sql` text DEFAULT NULL COMMENT '最终执行 SQL（拒答/异常为空）',
  `status` varchar(16) NOT NULL COMMENT 'SUCCESS|REJECTED|ERROR',
  `reject_code` varchar(32) DEFAULT NULL COMMENT '§1.3 拒答码',
  `reject_reason` varchar(512) DEFAULT NULL COMMENT '人类可读拒答原因',
  `row_count` int DEFAULT NULL COMMENT '结果行数',
  `latency_ms` bigint DEFAULT NULL COMMENT '耗时毫秒',
  `retry` int NOT NULL DEFAULT 0 COMMENT '自纠错次数',
  `steps_json` json DEFAULT NULL COMMENT 'BiTraceStep 决策链路 JSON',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_trace_id` (`trace_id`),
  KEY `idx_ai_chat_trace_user` (`user_id`, `created_at`),
  KEY `idx_ai_chat_trace_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ChatBI 问答审计';

-- 只读查询账号（INV-1 · 与业务写账号物理隔离；表范围需与 bi.chat.allow-tables 对齐）
-- 生产环境请改密、收窄 host，并按白名单表增补 GRANT
CREATE USER IF NOT EXISTS 'moli_bi_ro'@'localhost' IDENTIFIED BY 'moli_bi_ro_dev';
CREATE USER IF NOT EXISTS 'moli_bi_ro'@'%' IDENTIFIED BY 'moli_bi_ro_dev';
GRANT SELECT ON `moli`.`seckill_order` TO 'moli_bi_ro'@'localhost', 'moli_bi_ro'@'%';
GRANT SELECT ON `moli`.`seckill_activity` TO 'moli_bi_ro'@'localhost', 'moli_bi_ro'@'%';
FLUSH PRIVILEGES;
