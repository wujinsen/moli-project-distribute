-- AI-10 DeepResearch trace（kb_research_run）
CREATE TABLE IF NOT EXISTS `kb_research_run` (
  `id` bigint NOT NULL COMMENT '主键',
  `run_id` varchar(64) NOT NULL COMMENT '对外 runId',
  `user_id` bigint DEFAULT NULL COMMENT '调用用户',
  `space_ids_json` text COMMENT 'ACL 空间 JSON',
  `topic` varchar(500) NOT NULL COMMENT '脱敏主题',
  `status` varchar(32) NOT NULL COMMENT 'PENDING|RUNNING|SUCCEEDED|FAILED|DEGRADED',
  `degraded` tinyint(1) DEFAULT '0',
  `degrade_reason` varchar(64) DEFAULT NULL,
  `outline_json` mediumtext COMMENT 'Planner 产出',
  `sections_json` mediumtext COMMENT '每节检索 evidence',
  `citations_json` mediumtext COMMENT '全局 citations',
  `coverage` double DEFAULT NULL COMMENT 'Reviewer coverage（Phase B）',
  `report_md` mediumtext COMMENT '报告正文（Phase B）',
  `ingest_job_id` bigint DEFAULT NULL COMMENT '回写 job（Phase B）',
  `latency_ms` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_id` (`run_id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DeepResearch 运行 trace';
