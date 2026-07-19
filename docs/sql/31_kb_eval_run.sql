-- =============================================================
-- AI-3 · 知识库评测回归记录 kb_eval_run
-- 运行顺序：在 03_knowledge_schema.sql、18_kb_llm_call_log.sql 之后
-- 设计：docs/design/contracts/AI-3-contract.md §2.1
-- =============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `kb_eval_run` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `run_at` datetime NOT NULL COMMENT '报告 time',
  `strategy` varchar(16) DEFAULT NULL COMMENT 'ngram/hybrid/hybrid-rerank',
  `use_llm` tinyint(1) NOT NULL DEFAULT 0 COMMENT '检索式0/生成式1',
  `golden_total` int NOT NULL DEFAULT 0 COMMENT '报告 total',
  `answerable_total` int NOT NULL DEFAULT 0 COMMENT '可答题数',
  `negative_total` int NOT NULL DEFAULT 0 COMMENT 'negative 题数',
  `errors` int NOT NULL DEFAULT 0 COMMENT 'HTTP/请求错误数',
  `hit1` decimal(5,4) DEFAULT NULL COMMENT 'hit@1',
  `hit3` decimal(5,4) DEFAULT NULL COMMENT 'hit@3',
  `hit5` decimal(5,4) DEFAULT NULL COMMENT 'hit@5',
  `hit8` decimal(5,4) DEFAULT NULL COMMENT 'hit@8',
  `mrr` decimal(5,4) DEFAULT NULL COMMENT 'MRR',
  `coverage` decimal(5,4) DEFAULT NULL COMMENT 'coverage',
  `refusal_accuracy` decimal(5,4) DEFAULT NULL COMMENT '拒答准确率',
  `p95_ms` int DEFAULT NULL COMMENT 'P95 延迟毫秒',
  `by_difficulty_json` json DEFAULT NULL COMMENT 'by_difficulty 原样',
  `report_path` varchar(255) DEFAULT NULL COMMENT 'kb/eval/reports 相对路径',
  `git_sha` varchar(64) DEFAULT NULL COMMENT '关联 git 提交',
  `gate_pass` tinyint(1) DEFAULT NULL COMMENT '§1.2 门禁是否通过',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '落库时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_eval_run_at` (`run_at`),
  KEY `idx_kb_eval_strategy_run` (`strategy`, `run_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库评测回归记录';
